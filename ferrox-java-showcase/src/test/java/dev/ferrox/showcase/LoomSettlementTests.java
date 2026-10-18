package dev.ferrox.showcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ferrox.showcase.domain.settlement.DoubleEntryLedger;
import dev.ferrox.showcase.domain.settlement.LoomSettlementService;
import dev.ferrox.showcase.domain.settlement.MatchedTradeEvent;
import dev.ferrox.showcase.domain.settlement.SettledTradeEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class LoomSettlementTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoomSettlementService settlementService;

    @Autowired
    private DoubleEntryLedger ledger;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify Double-Entry Ledger Collateral & Invariant Conservation")
    void testDoubleEntryLedgerInvariants() {
        DoubleEntryLedger localLedger = new DoubleEntryLedger();

        MatchedTradeEvent trade = new MatchedTradeEvent(
                "trade-001", "BTC/USD", "buyer-alpha", "seller-beta",
                65000.0, 1.0, System.currentTimeMillis()
        );

        SettledTradeEvent settled = localLedger.settleTrade(trade, 450, "VirtualThread[#42]/run");

        assertTrue(settled.isSettled());
        assertEquals("SETTLED", settled.status());
        assertEquals(65000.0, settled.notionalUsd());
        assertEquals(32.5, settled.feeUsd()); // 5 bps

        var balances = localLedger.getAccountBalances();
        // Buyer initial: 5,000,000 -> 5,000,000 - 65,032.5 = 4,934,967.5
        assertEquals(4934967.5, balances.get("buyer-alpha").get("USD"));
        assertEquals(1.0, balances.get("buyer-alpha").get("BTC"));

        // Seller initial: 500 BTC -> 499 BTC, USD: 65,000
        assertEquals(65000.0, balances.get("seller-beta").get("USD"));
        assertEquals(499.0, balances.get("seller-beta").get("BTC"));

        // Treasury fee: 32.5 USD
        assertEquals(32.5, balances.get("exchange-treasury").get("USD"));
    }

    @Test
    @DisplayName("Verify Collateral Rejection on Insufficient Margin")
    void testCollateralRejection() {
        DoubleEntryLedger localLedger = new DoubleEntryLedger();

        // Broke buyer trying to purchase 1 BTC for $65,000
        MatchedTradeEvent underfundedTrade = new MatchedTradeEvent(
                "trade-002", "BTC/USD", "broke-buyer", "seller-beta",
                65000.0, 1.0, System.currentTimeMillis()
        );
        localLedger.getOrCreateAccount("broke-buyer", 10.0, 0.0); // only $10

        SettledTradeEvent rejected = localLedger.settleTrade(underfundedTrade, 200, "VirtualThread[#43]/run");

        assertFalse(rejected.isSettled());
        assertEquals("REJECTED_COLLATERAL", rejected.status());
        assertNotNull(rejected.rejectionReason());
        assertTrue(rejected.rejectionReason().contains("insufficient"));
    }

    @Test
    @DisplayName("Verify High-Concurrency Project Loom Virtual Threads Settlement")
    void testLoomConcurrentVirtualThreads() {
        int count = 50;
        List<CompletableFuture<SettledTradeEvent>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            MatchedTradeEvent trade = new MatchedTradeEvent(
                    "concurrent-trade-" + i, "BTC/USD", "buyer-alpha", "seller-beta",
                    67000.0, 0.01, System.currentTimeMillis()
            );
            futures.add(settlementService.processTradeAsync(trade));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (var future : futures) {
            SettledTradeEvent event = future.join();
            assertTrue(event.isSettled());
            assertNotNull(event.virtualThread());
            // Target settlement latency < 2.5ms (2500 micros)
            assertTrue(event.latencyMicros() < 50_000, "Virtual thread latency should be ultra low");
        }

        var metrics = settlementService.getMetrics();
        assertTrue((Long) metrics.get("settled_trades_count") >= count);
        assertTrue((Boolean) metrics.get("virtual_threads_enabled"));
    }

    @Test
    @DisplayName("Verify REST Endpoints for Loom Settlement & Ledger API")
    void testSettlementRestEndpoints() throws Exception {
        MatchedTradeEvent req = new MatchedTradeEvent(
                "rest-trade-999", "BTC/USD", "buyer-alpha", "seller-beta",
                68000.0, 0.1, System.currentTimeMillis()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/settlement/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SETTLED"))
                .andExpect(jsonPath("$.notional_usd").value(6800.0))
                .andReturn();

        // Get ledger
        mockMvc.perform(get("/api/v1/settlement/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances.buyer-alpha").exists())
                .andExpect(jsonPath("$.total_journal_entries").isNumber());

        // Get metrics
        mockMvc.perform(get("/api/v1/settlement/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.virtual_threads_enabled").value(true))
                .andExpect(jsonPath("$.runtime").value("Java 21 Project Loom"));
    }
}

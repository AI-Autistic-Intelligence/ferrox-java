package dev.ferrox.showcase.presentation;

import dev.ferrox.showcase.domain.settlement.DoubleEntryLedger;
import dev.ferrox.showcase.domain.settlement.LoomSettlementService;
import dev.ferrox.showcase.domain.settlement.MatchedTradeEvent;
import dev.ferrox.showcase.domain.settlement.SettledTradeEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/settlement")
@Tag(name = "Loom Settlement Ledger", description = "Java 21 Project Loom Double-Entry Ledger & Redpanda CloudEvents Stream")
public class SettlementController {

    private final LoomSettlementService settlementService;
    private final DoubleEntryLedger ledger;

    public SettlementController(LoomSettlementService settlementService, DoubleEntryLedger ledger) {
        this.settlementService = settlementService;
        this.ledger = ledger;
    }

    @PostMapping("/trades")
    @Operation(summary = "Settle Matched Trade via Loom Virtual Thread", description = "Executes collateral check and double-entry book balancing with sub-2.5ms latency.")
    public ResponseEntity<SettledTradeEvent> settleTrade(@RequestBody MatchedTradeEvent trade) {
        SettledTradeEvent settled = settlementService.processTradeAsync(trade).join();
        return ResponseEntity.ok(settled);
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get Double-Entry Ledger Balances & Journal History")
    public ResponseEntity<Map<String, Object>> getLedger() {
        return ResponseEntity.ok(Map.of(
                "balances", ledger.getAccountBalances(),
                "recent_journal", ledger.getRecentJournal(20),
                "total_journal_entries", ledger.getJournalSize()
        ));
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get Real-Time Loom Settlement Performance Metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(settlementService.getMetrics());
    }
}

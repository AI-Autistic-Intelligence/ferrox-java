package dev.ferrox.showcase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ShowcaseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Verify Health Probes and Prometheus Metrics")
    void testHealthAndMetrics() throws Exception {
        // Liveness probe
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // Readiness probe
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // Prometheus scrape endpoint
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_threads_live_threads")));
    }

    @Test
    @DisplayName("Verify PASETO v4 Token Generation and Verification")
    void testPasetoLifecycle() throws Exception {
        String reqJson = "{\"subject\":\"alice@ferrox.dev\",\"role\":\"ADMIN\"}";

        MvcResult result = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = root.get("token").asText();

        // Verify token
        mockMvc.perform(get("/api/v1/auth/verify")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.subject").value("alice@ferrox.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Verify Singleflight Concurrency Cache Stampede Protection")
    void testSingleflightMarketQuote() throws Exception {
        mockMvc.perform(get("/api/v1/market/quote/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.singleflight_protected").value(true));
    }

    @Test
    @DisplayName("Verify Off-Heap Panama MemorySegment Storage")
    void testOffHeapAuctionStorage() throws Exception {
        String bidPayload = "{\"amount\": 15400.50}";

        mockMvc.perform(post("/api/v1/auctions/rare-item-777/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auction_id").value("rare-item-777"))
                .andExpect(jsonPath("$.bid_accepted").value(true))
                .andExpect(jsonPath("$.memory_tier").value("OFF_HEAP_PANAMA_SEGMENT"));

        mockMvc.perform(get("/api/v1/auctions/rare-item-777/highest-bid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highest_bid").value(15400.50))
                .andExpect(jsonPath("$.memory_tier").value("OFF_HEAP_PANAMA_SEGMENT"));
    }

    @Test
    @DisplayName("Verify Virtual Thread Circuit Breaker State Transition")
    void testCircuitBreaker() throws Exception {
        // Normal state
        mockMvc.perform(get("/api/v1/resilience/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CLOSED"));

        // Normal execution
        mockMvc.perform(get("/api/v1/resilience/unstable?fail=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("Verify Sentinel Threat Engine Intercepts Exploits")
    void testSentinelThreatInterception() throws Exception {
        // Clean request should pass
        mockMvc.perform(post("/api/v1/secure/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"clean business data\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentinel_verified").value(true));

        // SQL Injection attempt in query string must be blocked with HTTP 403
        mockMvc.perform(post("/api/v1/secure/echo?queryParam=SELECT * FROM users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"innocent payload\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("https://ferrox.dev/errors/sentinel-threat-blocked"));
    }
}

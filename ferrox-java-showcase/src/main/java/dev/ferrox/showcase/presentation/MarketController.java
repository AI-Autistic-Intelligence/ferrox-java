package dev.ferrox.showcase.presentation;

import dev.ferrox.data.SingleflightGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/market")
@Tag(name = "Singleflight Concurrency", description = "Deduplication of Concurrent High-Traffic Requests to Prevent Cache Stampedes")
public class MarketController {

    private final SingleflightGroup singleflight;
    private final AtomicInteger databaseQueryCounter = new AtomicInteger(0);

    public MarketController(SingleflightGroup singleflight) {
        this.singleflight = singleflight;
    }

    @GetMapping("/quote/{symbol}")
    @Operation(summary = "Get Real-Time Market Quote", description = "Protected by Singleflight. 10,000 concurrent requests for the same symbol will trigger only 1 backend fetch.")
    public ResponseEntity<Map<String, Object>> getMarketQuote(@PathVariable("symbol") String symbol) {
        String key = "quote_" + symbol.toUpperCase();

        Map<String, Object> quote = singleflight.work(key, () -> {
            int queryCount = databaseQueryCounter.incrementAndGet();
            // Simulate 100ms expensive external exchange API call or DB query
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            double basePrice = symbol.equalsIgnoreCase("BTC") ? 68500.0 : (symbol.equalsIgnoreCase("ETH") ? 3500.0 : 100.0);
            return Map.of(
                    "symbol", symbol.toUpperCase(),
                    "price", basePrice,
                    "bid", basePrice - 0.5,
                    "ask", basePrice + 0.5,
                    "volume_24h", 1250000.0,
                    "db_query_sequence", queryCount,
                    "singleflight_protected", true
            );
        });

        return ResponseEntity.ok(quote);
    }
}

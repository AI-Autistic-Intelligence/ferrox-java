package dev.ferrox.showcase.domain.settlement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable value object representing a trade matched by the Ferrox Rust matching engine.
 */
public record MatchedTradeEvent(
        @JsonProperty("trade_id") String tradeId,
        @JsonProperty("symbol") String symbol,
        @JsonProperty("buyer_id") String buyerId,
        @JsonProperty("seller_id") String sellerId,
        @JsonProperty("price") double price,
        @JsonProperty("quantity") double quantity,
        @JsonProperty("timestamp") long timestamp
) {
    public MatchedTradeEvent {
        if (price <= 0.0) {
            throw new IllegalArgumentException("Trade price must be strictly positive");
        }
        if (quantity <= 0.0) {
            throw new IllegalArgumentException("Trade quantity must be strictly positive");
        }
    }
}

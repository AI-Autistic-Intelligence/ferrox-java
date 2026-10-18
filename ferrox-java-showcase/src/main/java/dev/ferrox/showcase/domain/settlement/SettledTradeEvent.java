package dev.ferrox.showcase.domain.settlement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable domain event emitted after double-entry accounting settlement on Loom Virtual Threads.
 */
public record SettledTradeEvent(
        @JsonProperty("settlement_id") String settlementId,
        @JsonProperty("trade_id") String tradeId,
        @JsonProperty("symbol") String symbol,
        @JsonProperty("buyer_id") String buyerId,
        @JsonProperty("seller_id") String sellerId,
        @JsonProperty("price") double price,
        @JsonProperty("quantity") double quantity,
        @JsonProperty("notional_usd") double notionalUsd,
        @JsonProperty("fee_usd") double feeUsd,
        @JsonProperty("status") String status,
        @JsonProperty("rejection_reason") String rejectionReason,
        @JsonProperty("latency_micros") long latencyMicros,
        @JsonProperty("virtual_thread") String virtualThread,
        @JsonProperty("timestamp") long timestamp
) {
    public boolean isSettled() {
        return "SETTLED".equalsIgnoreCase(status);
    }
}

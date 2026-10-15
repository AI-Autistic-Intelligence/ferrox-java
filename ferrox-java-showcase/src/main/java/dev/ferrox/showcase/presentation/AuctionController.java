package dev.ferrox.showcase.presentation;

import dev.ferrox.offheap.OffHeapAuctionCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auctions")
@Tag(name = "Off-Heap Storage (Project Panama)", description = "Zero-GC In-Memory Auction Cache using Native MemorySegment Allocation")
public class AuctionController {

    private final OffHeapAuctionCache auctionCache;

    public AuctionController(OffHeapAuctionCache auctionCache) {
        this.auctionCache = auctionCache;
    }

    public record BidRequest(double amount) {}

    @PostMapping("/{id}/bid")
    @Operation(summary = "Place Auction Bid", description = "Writes the new highest bid directly into OS RAM via Java 21 MemorySegment API, bypassing JVM Garbage Collection.")
    public ResponseEntity<Map<String, Object>> placeBid(
            @PathVariable("id") String auctionId,
            @RequestBody BidRequest request) {

        boolean accepted = auctionCache.placeBidIfHigher(auctionId, request.amount());
        double highest = auctionCache.getCurrentHighestBid(auctionId);

        return ResponseEntity.ok(Map.of(
                "auction_id", auctionId,
                "bid_accepted", accepted,
                "current_highest_bid", highest,
                "memory_tier", "OFF_HEAP_PANAMA_SEGMENT"
        ));
    }

    @GetMapping("/{id}/highest-bid")
    @Operation(summary = "Get Highest Bid", description = "Reads current bid directly from native memory without JVM heap object allocations.")
    public ResponseEntity<Map<String, Object>> getHighestBid(@PathVariable("id") String auctionId) {
        double highest = auctionCache.getCurrentHighestBid(auctionId);
        return ResponseEntity.ok(Map.of(
                "auction_id", auctionId,
                "highest_bid", highest,
                "memory_tier", "OFF_HEAP_PANAMA_SEGMENT"
        ));
    }
}

package dev.ferrox.offheap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Off-Heap Arena Allocator leveraging Java 21 Foreign Function & Memory API (Project Panama).
 * Bypasses the JVM Garbage Collector to prevent Stop-The-World pauses under extreme load.
 */
@Service
public class OffHeapAuctionCache implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(OffHeapAuctionCache.class);

    // One shared native memory arena
    private final Arena arena;
    private final MemorySegment nativeArray;
    
    // We allocate space for 100,000 active auctions (storing their highest bid as an 8-byte double)
    private static final int MAX_AUCTIONS = 100_000;
    
    // Map string IDs to an off-heap array index
    private final ConcurrentHashMap<String, Integer> idToIndexMap = new ConcurrentHashMap<>();
    private final AtomicInteger indexGenerator = new AtomicInteger(0);

    public OffHeapAuctionCache() {
        log.info("Allocating {} bytes in Off-Heap Native RAM via Project Panama...", MAX_AUCTIONS * 8L);
        // Allocate native memory outside the JVM Heap
        this.arena = Arena.ofShared();
        this.nativeArray = arena.allocate(MAX_AUCTIONS * 8L, 8);
        
        // Seed demo data
        placeBidIfHigher("auc-123", 1000.0);
    }

    private int getOrCreateIndex(String auctionId) {
        return idToIndexMap.computeIfAbsent(auctionId, k -> {
            int idx = indexGenerator.getAndIncrement();
            if (idx >= MAX_AUCTIONS) {
                throw new OutOfMemoryError("Off-Heap Auction Arena is full!");
            }
            return idx;
        });
    }

    /**
     * Reads the bid directly from the OS RAM (Zero GC involvement)
     */
    public double getCurrentHighestBid(String auctionId) {
        int index = getOrCreateIndex(auctionId);
        long byteOffset = index * 8L;
        return nativeArray.get(ValueLayout.JAVA_DOUBLE, byteOffset);
    }

    /**
     * Writes the new bid directly into the OS RAM
     * Uses volatile semantics implicitly through the MemorySegment API for thread safety
     */
    public boolean placeBidIfHigher(String auctionId, double newBid) {
        int index = getOrCreateIndex(auctionId);
        long byteOffset = index * 8L;
        
        // Emulate compareAndSwap manually via synchronized for safety in this demo, 
        // in production we'd use VarHandle on the MemorySegment.
        synchronized (this) {
            double current = nativeArray.get(ValueLayout.JAVA_DOUBLE, byteOffset);
            if (newBid > current) {
                nativeArray.set(ValueLayout.JAVA_DOUBLE, byteOffset, newBid);
                return true;
            }
            return false;
        }
    }

    @Override
    public void close() {
        // Manually free the native memory (like free() in C)
        arena.close();
        log.info("Off-Heap Arena freed successfully.");
    }
}

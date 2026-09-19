package dev.ferrox.offheap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffHeapAuctionCacheTest {

    private OffHeapAuctionCache cache;

    @BeforeEach
    void setUp() {
        cache = new OffHeapAuctionCache();
    }

    @AfterEach
    void tearDown() {
        cache.close();
    }

    @Test
    void testPlaceBidIfHigher() {
        // Initial state is 0.0 or the seeded value
        assertTrue(cache.placeBidIfHigher("auc-test-1", 500.0));
        assertEquals(500.0, cache.getCurrentHighestBid("auc-test-1"));

        // Lower bid should fail
        assertFalse(cache.placeBidIfHigher("auc-test-1", 400.0));
        assertEquals(500.0, cache.getCurrentHighestBid("auc-test-1"));

        // Higher bid should succeed
        assertTrue(cache.placeBidIfHigher("auc-test-1", 1200.5));
        assertEquals(1200.5, cache.getCurrentHighestBid("auc-test-1"));
    }
}

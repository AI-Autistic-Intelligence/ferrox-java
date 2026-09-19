package dev.ferrox.data;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SingleflightTests {

    @Test
    void testCacheStampedePrevention() throws Exception {
        SingleflightGroup sf = new SingleflightGroup();
        AtomicInteger executionCount = new AtomicInteger(0);
        
        Callable<String> slowWork = () -> {
            executionCount.incrementAndGet();
            Thread.sleep(200); // Simulate slow DB
            return "SUCCESS";
        };

        int threads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        
        Future<String>[] futures = new Future[threads];

        for (int i = 0; i < threads; i++) {
            futures[i] = executor.submit(() -> {
                String res = sf.work("same_key", slowWork);
                latch.countDown();
                return res;
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        
        // All threads should receive the same success result
        for (Future<String> f : futures) {
            assertEquals("SUCCESS", f.get());
        }

        // The critical assertion: the actual work was only executed ONCE
        assertEquals(1, executionCount.get());
        
        executor.shutdown();
    }
}

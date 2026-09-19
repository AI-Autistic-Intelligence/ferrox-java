package dev.ferrox.data;

import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Singleflight prevents Cache Stampede (Dogpile effect) by ensuring that
 * concurrent requests for the same key result in only ONE execution of the
 * underlying data fetch. Other requests wait for the result.
 */
@Component
public class SingleflightGroup {

    private final ConcurrentHashMap<String, CompletableFuture<Object>> calls = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T work(String key, Callable<T> fn) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        CompletableFuture<Object> inFlight = calls.putIfAbsent(key, future);

        if (inFlight != null) {
            // Someone else is already fetching this key, wait for their result.
            try {
                return (T) inFlight.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Singleflight execution failed", e);
            }
        }

        // We are the first one here, execute the work.
        try {
            T result = fn.call();
            future.complete(result);
            return result;
        } catch (Exception e) {
            future.completeExceptionally(e);
            throw new RuntimeException("Singleflight execution failed", e);
        } finally {
            calls.remove(key); // Cleanup
        }
    }
}

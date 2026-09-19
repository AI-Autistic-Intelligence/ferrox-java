package dev.ferrox.core;

import java.util.List;

public record Pagination<T>(
    List<T> items,
    long total,
    int page,
    int size
) {
    public boolean hasNext() {
        return (page * size) < total;
    }
}

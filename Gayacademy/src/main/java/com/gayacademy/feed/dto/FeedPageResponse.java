package com.gayacademy.feed.dto;

import java.util.List;

public record FeedPageResponse(
        List<FeedItemResponse> items,
        int page,
        int size,
        boolean hasNext,
        boolean fromCache
) {
}

package com.gayacademy.feed.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FeedItemResponse(
        UUID postId,

        UUID authorId,

        String authorName,
        
        String text,
        OffsetDateTime createdAt,
        long likes,
        boolean likedByCurrentUser
) {
}

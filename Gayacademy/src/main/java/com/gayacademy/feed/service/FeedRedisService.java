package com.gayacademy.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedRedisService {

    private static final String FEED_ACTIVE_USERS_KEY = "feed:active-users";
    private static final String FEED_TIMELINE_KEY = "feed:timeline:";
    private static final String FEED_POSTS_KEY = "feed:posts:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void registerActiveUser(UUID userId, double score) {
        redisTemplate.opsForZSet().add(FEED_ACTIVE_USERS_KEY, userId.toString(), score);
    }

    public void addPostToUserTimeline(UUID userId, UUID postId, double score) {
        redisTemplate.opsForZSet().add(FEED_TIMELINE_KEY + userId, postId.toString(), score);
    }

    public void addPostToFeedCache(UUID postId, double score) {
        redisTemplate.opsForZSet().add(FEED_POSTS_KEY, postId.toString(), score);
    }

    public void removePostFromFeedCache(UUID postId) {
        redisTemplate.opsForZSet().remove(FEED_POSTS_KEY, postId.toString());
    }

    public Set<ZSetOperations.TypedTuple<Object>> getActiveUserPosts(UUID userId, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(FEED_TIMELINE_KEY + userId, start, end);
    }

    public Set<Object> getFeedTopPosts(long start, long end) {
        return redisTemplate.opsForZSet().range(FEED_POSTS_KEY, start, end);
    }

    public void setLikeCount(UUID postId, long value) {
        redisTemplate.opsForValue().set("feed:likes:" + postId, value, Duration.ofHours(24));
    }

    public long incrementLikeCount(UUID postId) {
        Object value = redisTemplate.opsForValue().increment("feed:likes:" + postId);
        return value == null ? 0 : Long.parseLong(value.toString());
    }

    public long decrementLikeCount(UUID postId) {
        Object value = redisTemplate.opsForValue().decrement("feed:likes:" + postId);
        return value == null ? 0 : Long.parseLong(value.toString());
    }

    public boolean isPostLikedByUser(UUID userId, UUID postId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("feed:liked:post:" + postId, userId.toString()));
    }

    public void likePost(UUID userId, UUID postId) {
        redisTemplate.opsForSet().add("feed:liked:post:" + postId, userId.toString());
    }

    public void unlikePost(UUID userId, UUID postId) {
        redisTemplate.opsForSet().remove("feed:liked:post:" + postId, userId.toString());
    }
}

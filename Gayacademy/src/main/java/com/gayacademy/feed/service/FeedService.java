package com.gayacademy.feed.service;

import com.gayacademy.common.exception.NotFoundException;
import com.gayacademy.feed.dto.FeedItemResponse;
import com.gayacademy.feed.dto.FeedPageResponse;
import com.gayacademy.user.domain.PostActions.Like;
import com.gayacademy.user.domain.PostActions.Post;
import com.gayacademy.user.domain.User;
import com.gayacademy.user.domain.enums.LikeTargetType;
import com.gayacademy.user.repository.LikeRepository;
import com.gayacademy.user.repository.PostRepository;
import com.gayacademy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final FeedRedisService feedRedisService;
    private final FeedWriteBehindService feedWriteBehindService;

    @Transactional(readOnly = true)
    public FeedPageResponse getTimeline(UUID currentUserId, int page, int size) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        List<FeedItemResponse> items = new ArrayList<>();
        var posts = postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        for (Post post : posts.getContent()) {
            boolean likedByCurrentUser = likeRepository.existsByUserAndPostAndTargetType(user, post, LikeTargetType.POST);
            long totalLikes = likeRepository.countByPostAndTargetType(post, LikeTargetType.POST);
            String authorName = post.getUser() == null ? "" : post.getUser().getNomeExibicao();

            items.add(new FeedItemResponse(
                    post.getIdPost(),
                    post.getUser() == null ? null : post.getUser().getId(),
                    authorName,
                    post.getText(),
                    post.getCreatedAt(),
                    totalLikes,
                    likedByCurrentUser
            ));
        }

        return new FeedPageResponse(items, page, size, posts.hasNext(), false);
    }

    @Transactional
    public FeedItemResponse createPost(UUID currentUserId, String text) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Post post = Post.builder()
                .user(user)
                .text(text)
                .createdAt(OffsetDateTime.now())
                .build();

        Post saved = postRepository.save(post);

        feedRedisService.registerActiveUser(currentUserId, System.currentTimeMillis());
        feedRedisService.addPostToUserTimeline(currentUserId, saved.getIdPost(), saved.getCreatedAt().toEpochSecond());
        feedRedisService.addPostToFeedCache(saved.getIdPost(), saved.getCreatedAt().toEpochSecond());

        return new FeedItemResponse(
                saved.getIdPost(),
                user.getId(),
                user.getNomeExibicao(),
                saved.getText(),
                saved.getCreatedAt(),
                0,
                false
        );
    }

    @Transactional
    public FeedItemResponse toggleLike(UUID currentUserId, UUID postId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post não encontrado"));

        boolean alreadyLiked = likeRepository.existsByUserAndPostAndTargetType(user, post, LikeTargetType.POST);

        if (alreadyLiked) {
            likeRepository.deleteByUserAndPostAndTargetType(user, post, LikeTargetType.POST);
            feedRedisService.unlikePost(currentUserId, postId);
            feedRedisService.decrementLikeCount(postId);
            return new FeedItemResponse(postId, post.getUser().getId(), post.getUser().getNomeExibicao(), post.getText(), post.getCreatedAt(),
                    likeRepository.countByPostAndTargetType(post, LikeTargetType.POST), false);
        }

        likeRepository.save(Like.builder().user(user).post(post).targetType(LikeTargetType.POST).build());
        feedRedisService.likePost(currentUserId, postId);
        feedRedisService.incrementLikeCount(postId);
        feedWriteBehindService.syncLikeToDatabase(currentUserId, postId, true);

        return new FeedItemResponse(postId, post.getUser().getId(), post.getUser().getNomeExibicao(), post.getText(), post.getCreatedAt(),
                likeRepository.countByPostAndTargetType(post, LikeTargetType.POST), true);
    }
}

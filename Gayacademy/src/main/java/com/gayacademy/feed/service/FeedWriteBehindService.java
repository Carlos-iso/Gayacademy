package com.gayacademy.feed.service;

import com.gayacademy.user.domain.PostActions.Like;
import com.gayacademy.user.domain.PostActions.Post;
import com.gayacademy.user.domain.User;
import com.gayacademy.user.domain.enums.LikeTargetType;
import com.gayacademy.user.repository.LikeRepository;
import com.gayacademy.user.repository.PostRepository;
import com.gayacademy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedWriteBehindService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Async("feedTaskExecutor")
    @Transactional
    public void syncLikeToDatabase(UUID userId, UUID postId, boolean liked) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            Post post = postRepository.findById(postId).orElse(null);

            if (user == null || post == null) {
                log.warn("Ignorando sincronização de like do feed: userId={}, postId={}", userId, postId);
                return;
            }

            boolean alreadyLiked = likeRepository.existsByUserAndPostAndTargetType(user, post, LikeTargetType.POST);
            if (liked && !alreadyLiked) {
                likeRepository.save(Like.builder().user(user).post(post).targetType(LikeTargetType.POST).build());
            }

            if (!liked && alreadyLiked) {
                likeRepository.deleteByUserAndPostAndTargetType(user, post, LikeTargetType.POST);
            }
        } catch (Exception e) {
            log.error("Erro ao sincronizar like do feed para o banco: userId={}, postId={}", userId, postId, e);
        }
    }
}

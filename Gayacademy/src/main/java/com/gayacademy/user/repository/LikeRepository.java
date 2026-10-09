package com.gayacademy.user.repository;

import com.gayacademy.user.domain.PostActions.Comentario;
import com.gayacademy.user.domain.PostActions.Like;
import com.gayacademy.user.domain.PostActions.Post;
import com.gayacademy.user.domain.User;
import com.gayacademy.user.domain.enums.LikeTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByUserAndPostAndTargetType(User user, Post post, LikeTargetType targetType);

    boolean existsByUserAndCommentAndTargetType(User user, Comentario comment, LikeTargetType targetType);

    void deleteByUserAndPostAndTargetType(User user, Post post, LikeTargetType targetType);

    void deleteByUserAndCommentAndTargetType(User user, Comentario comment, LikeTargetType targetType);

    long countByPostAndTargetType(Post post, LikeTargetType targetType);

    long countByCommentAndTargetType(Comentario comment, LikeTargetType targetType);

    @Query("SELECT  l.post.id, COUNT(l) FROM Like l WHERE l.post IN :posts AND l.targetType = com.gayacademy.user.domain.enums.LikeTargetType.POST GROUP BY l.post.id")
    List<Object[]> countLikesByPostsRaw(@Param("posts") List<Post> posts);

    default Map<UUID, Long> countLikesPerPost(List<Post> posts) {
        return countLikesByPostsRaw(posts).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
}


package com.gayacademy.feed.controller;

import com.gayacademy.common.util.SecurityUtils;
import com.gayacademy.feed.dto.FeedItemResponse;
import com.gayacademy.feed.dto.FeedPageResponse;
import com.gayacademy.feed.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Timeline e interações do feed com cache Redis")
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/timeline")
    @Operation(summary = "Retorna a timeline do usuário autenticado usando cache e paginação")
    public ResponseEntity<FeedPageResponse> getTimeline(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(feedService.getTimeline(userId, page, size));
    }

    @PostMapping("/posts")
    @Operation(summary = "Cria um novo post e registra o item na timeline em Redis")
    public ResponseEntity<FeedItemResponse> createPost(@RequestParam String text) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(feedService.createPost(userId, text));
    }

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "Alterna o like do usuário no post e usa Redis como primeiro ponto de escrita")
    public ResponseEntity<FeedItemResponse> toggleLike(@PathVariable UUID postId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        return ResponseEntity.ok(feedService.toggleLike(userId, postId));
    }
}

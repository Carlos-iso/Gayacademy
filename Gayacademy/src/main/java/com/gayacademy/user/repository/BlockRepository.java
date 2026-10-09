package com.gayacademy.user.repository;

import com.gayacademy.user.domain.Block;
import com.gayacademy.user.domain.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlockRepository extends JpaRepository<Block, BlockId> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    /**
     * Verifica se há bloqueio em qualquer direção entre dois usuários.
     */
    @org.springframework.data.jpa.repository.Query("""
           SELECT COUNT(bl) > 0 FROM Block bl
           WHERE (bl.blockerId = :a AND bl.blockedId = :b)
              OR (bl.blockerId = :b AND bl.blockedId = :a)
           """)
    boolean existsBlockBetween(
            @org.springframework.data.repository.query.Param("a") UUID a,
            @org.springframework.data.repository.query.Param("b") UUID b);
}
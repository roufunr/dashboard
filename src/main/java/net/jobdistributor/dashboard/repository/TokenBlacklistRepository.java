package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Query("SELECT COUNT(tb) FROM TokenBlacklist tb WHERE tb.userId = :userId")
    long countByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.userId = :userId")
    void deleteByUserId(Long userId);
}

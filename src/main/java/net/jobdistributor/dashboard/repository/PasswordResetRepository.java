
package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByResetTokenAndUsedAtIsNull(String resetToken);

    Optional<PasswordReset> findByResetTokenAndTokenExpiresAtAfterAndUsedAtIsNull(
            String resetToken, LocalDateTime now);

    void deleteByUserIdAndUsedAtIsNull(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordReset pr WHERE pr.userId = :userId")
    void deleteByUserId(Long userId);
}

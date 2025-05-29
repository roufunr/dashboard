package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByResetTokenAndUsedAtIsNull(String resetToken);

    Optional<PasswordReset> findByResetTokenAndTokenExpiresAtAfterAndUsedAtIsNull(
            String resetToken, LocalDateTime now);

    void deleteByUserIdAndUsedAtIsNull(Long userId);
}

package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.createdAt >= :since ORDER BY la.createdAt DESC")
    List<LoginAttempt> findByEmailAndCreatedAtAfter(String email, LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.createdAt >= :since")
    long countFailedAttemptsByEmailSince(String email, LocalDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.createdAt >= :since")
    List<LoginAttempt> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);
}

package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {

    @Query("SELECT p FROM Password p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Password> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM Password p WHERE p.user.id = :userId ORDER BY p.createdAt DESC LIMIT 1")
    Optional<Password> findLatestByUserId(Long userId);

    @Query("SELECT p FROM Password p WHERE p.user.id = :userId ORDER BY p.createdAt DESC LIMIT :limit")
    List<Password> findRecentByUserId(Long userId, int limit);
}

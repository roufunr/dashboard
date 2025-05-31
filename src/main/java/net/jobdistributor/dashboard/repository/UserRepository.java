package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.User;
import net.jobdistributor.dashboard.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.emailAddress = :email AND e.isPrimary = true")
    Optional<User> findByPrimaryEmail(String email);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.emailAddress = :email")
    Optional<User> findByAnyEmail(String email);

    // ADD THIS METHOD FOR LOGOUT-ALL
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.tokenGeneration = u.tokenGeneration + 1 WHERE u.id = :userId")
    void incrementTokenGeneration(Long userId);

    // HELPER METHOD TO GET CURRENT GENERATION
    @Query("SELECT u.tokenGeneration FROM User u WHERE u.id = :userId")
    Optional<Long> getTokenGeneration(Long userId);
}
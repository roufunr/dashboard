package net.jobdistributor.dashboard.repository;

import net.jobdistributor.dashboard.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    Optional<Email> findByEmailAddress(String emailAddress);

    Optional<Email> findByVerificationToken(String token);

    List<Email> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<Email> findByUserIdAndIsPrimaryTrueAndIsDeletedFalse(Long userId);
}

package edu.rpi.giveaway.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "false", matchIfMissing = true)
public interface SpringDataUserAccountRepository extends JpaRepository<UserAccountEntity, String> {
    Optional<UserAccountEntity> findByEmailIgnoreCase(String email);
    Optional<UserAccountEntity> findByPhoneNumber(String phoneNumber);
}

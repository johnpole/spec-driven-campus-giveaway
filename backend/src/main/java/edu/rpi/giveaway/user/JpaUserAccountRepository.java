package edu.rpi.giveaway.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "false", matchIfMissing = true)
public class JpaUserAccountRepository implements UserAccountRepository {

    private final SpringDataUserAccountRepository repository;

    public JpaUserAccountRepository(SpringDataUserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserAccountDocument save(UserAccountDocument user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }
        UserAccountEntity entity = UserAccountEntity.fromDocument(user);
        UserAccountEntity saved = repository.saveAndFlush(entity);
        return saved.toDocument();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountDocument> findById(String id) {
        return repository.findById(id).map(UserAccountEntity::toDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountDocument> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return repository.findByEmailIgnoreCase(email.trim()).map(UserAccountEntity::toDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccountDocument> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return Optional.empty();
        return repository.findByPhoneNumber(phoneNumber.trim()).map(UserAccountEntity::toDocument);
    }
}

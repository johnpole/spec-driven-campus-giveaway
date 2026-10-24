package edu.rpi.giveaway.interest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "false", matchIfMissing = true)
public class JpaInterestRepository implements InterestRepository {

    private final SpringDataInterestRepository repository;

    public JpaInterestRepository(SpringDataInterestRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public InterestDocument save(InterestDocument interest) {
        if (interest.getId() == null || interest.getId().isBlank()) {
            interest.setId(UUID.randomUUID().toString());
        }
        InterestEntity entity = InterestEntity.fromDocument(interest);
        InterestEntity saved = repository.saveAndFlush(entity);
        return saved.toDocument();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestDocument> findByPostId(String postId) {
        return repository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .map(InterestEntity::toDocument)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestDocument> findByInquirerUserId(String inquirerUserId) {
        return repository.findByInquirerUserIdOrderByCreatedAtDesc(inquirerUserId).stream()
                .map(InterestEntity::toDocument)
                .toList();
    }

    @Override
    public java.util.Optional<InterestDocument> findById(String id) {
        return repository.findById(id).map(InterestEntity::toDocument);
    }
}

package edu.rpi.giveaway.post;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "false", matchIfMissing = true)
public class JpaPostRepository implements PostRepository {

    private final SpringDataPostRepository repository;

    public JpaPostRepository(SpringDataPostRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PostDocument save(PostDocument document) {
        if (document.getId() == null || document.getId().isBlank()) {
            document.setId(UUID.randomUUID().toString());
        }
        PostEntity entity = PostEntity.fromDocument(document);
        PostEntity saved = repository.save(entity);
        return saved.toDocument();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostDocument> findById(String id) {
        return repository.findById(id).map(PostEntity::toDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDocument> findAllActive(Instant now) {
        Date nowDate = Date.from(now);
        return repository.findAllActive(nowDate).stream()
                .map(PostEntity::toDocument)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDocument> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostEntity::toDocument)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDocument> findByAuthorUserId(String authorUserId) {
        return repository.findByAuthorUserIdOrderByCreatedAtDesc(authorUserId).stream()
                .map(PostEntity::toDocument)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}

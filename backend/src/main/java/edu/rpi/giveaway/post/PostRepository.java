package edu.rpi.giveaway.post;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    PostDocument save(PostDocument document);
    Optional<PostDocument> findById(String id);
    List<PostDocument> findAllActive(Instant now);
    List<PostDocument> findAll();
    List<PostDocument> findByAuthorUserId(String authorUserId);
    void deleteById(String id);
}

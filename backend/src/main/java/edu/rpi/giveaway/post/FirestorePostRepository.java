package edu.rpi.giveaway.post;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "true")
public class FirestorePostRepository implements PostRepository {

    private static final Logger log = LoggerFactory.getLogger(FirestorePostRepository.class);
    private static final String COLLECTION_NAME = "posts";

    private final Firestore firestore;
    // In-memory fallback if Firestore bean is not active in simple test setups
    private final Map<String, PostDocument> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public FirestorePostRepository(@Autowired(required = false) Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public PostDocument save(PostDocument document) {
        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
        }

        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(document.getId());
                ApiFuture<WriteResult> future = docRef.set(document);
                future.get();
                return document;
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to save post to Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error saving post", e);
            }
        } else {
            fallbackStore.put(document.getId(), document);
            return document;
        }
    }

    @Override
    public Optional<PostDocument> findById(String id) {
        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
                DocumentSnapshot snapshot = docRef.get().get();
                if (snapshot.exists()) {
                    PostDocument post = snapshot.toObject(PostDocument.class);
                    if (post != null) {
                        post.setId(snapshot.getId());
                    }
                    return Optional.ofNullable(post);
                }
                return Optional.empty();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to read post from Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error reading post", e);
            }
        } else {
            return Optional.ofNullable(fallbackStore.get(id));
        }
    }

    @Override
    public List<PostDocument> findAllActive(Instant now) {
        Date cutoff = Date.from(now);
        if (firestore != null) {
            try {
                Query query = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("status", "AVAILABLE")
                        .whereGreaterThanOrEqualTo("availableUntil", cutoff)
                        .orderBy("availableUntil")
                        .orderBy("createdAt", Query.Direction.DESCENDING);

                QuerySnapshot snapshots = query.get().get();
                List<PostDocument> results = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    PostDocument post = doc.toObject(PostDocument.class);
                    if (post != null) {
                        post.setId(doc.getId());
                        results.add(post);
                    }
                }
                // Sort by createdAt descending in memory if needed
                results.sort((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                });
                return results;
            } catch (Exception e) {
                log.warn("Composite Firestore query failed (may need index), falling back to query with status filter: {}", e.getMessage());
                try {
                    Query fallbackQuery = firestore.collection(COLLECTION_NAME)
                            .whereEqualTo("status", "AVAILABLE");
                    QuerySnapshot snapshots = fallbackQuery.get().get();
                    List<PostDocument> results = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        PostDocument post = doc.toObject(PostDocument.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            if (post.getAvailableUntil() != null && !post.getAvailableUntil().before(cutoff)) {
                                results.add(post);
                            }
                        }
                    }
                    results.sort((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });
                    return results;
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to query active posts", ex);
                }
            }
        } else {
            return fallbackStore.values().stream()
                    .filter(p -> "AVAILABLE".equals(p.getStatus()))
                    .filter(p -> p.getAvailableUntil() != null && !p.getAvailableUntil().before(cutoff))
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .toList();
        }
    }

    @Override
    public List<PostDocument> findAll() {
        if (firestore != null) {
            try {
                QuerySnapshot snapshots = firestore.collection(COLLECTION_NAME).get().get();
                List<PostDocument> results = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    PostDocument post = doc.toObject(PostDocument.class);
                    if (post != null) {
                        post.setId(doc.getId());
                        results.add(post);
                    }
                }
                return results;
            } catch (Exception e) {
                throw new RuntimeException("Failed to query all posts", e);
            }
        } else {
            return new ArrayList<>(fallbackStore.values());
        }
    }

    @Override
    public List<PostDocument> findByAuthorUserId(String authorUserId) {
        if (authorUserId == null) return List.of();
        return findAll().stream()
                .filter(doc -> authorUserId.equals(doc.getAuthorUserId()))
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (firestore != null) {
            try {
                firestore.collection(COLLECTION_NAME).document(id).delete().get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to delete post from Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error deleting post", e);
            }
        } else {
            fallbackStore.remove(id);
        }
    }
}

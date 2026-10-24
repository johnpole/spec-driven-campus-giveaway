package edu.rpi.giveaway.interest;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "true")
public class FirestoreInterestRepository implements InterestRepository {

    private static final Logger log = LoggerFactory.getLogger(FirestoreInterestRepository.class);

    private final Firestore firestore;
    private final Map<String, List<InterestDocument>> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public FirestoreInterestRepository(@Autowired(required = false) Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public InterestDocument save(InterestDocument interest) {
        if (interest.getId() == null) {
            interest.setId(UUID.randomUUID().toString());
        }

        if (firestore != null) {
            try {
                DocumentReference docRef = firestore.collection("posts")
                        .document(interest.getPostId())
                        .collection("interests")
                        .document(interest.getId());
                docRef.set(interest).get();
                return interest;
            } catch (Exception e) {
                log.error("Failed to save interest to Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error saving interest", e);
            }
        } else {
            fallbackStore.computeIfAbsent(interest.getPostId(), k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(interest);
            return interest;
        }
    }

    @Override
    public List<InterestDocument> findByPostId(String postId) {
        if (firestore != null) {
            try {
                QuerySnapshot snapshot = firestore.collection("posts")
                        .document(postId)
                        .collection("interests")
                        .orderBy("createdAt")
                        .get().get();

                List<InterestDocument> results = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    InterestDocument interest = doc.toObject(InterestDocument.class);
                    if (interest != null) {
                        interest.setId(doc.getId());
                        results.add(interest);
                    }
                }
                return results;
            } catch (Exception e) {
                log.error("Failed to query interests from Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error querying interests", e);
            }
        } else {
            return new ArrayList<>(fallbackStore.getOrDefault(postId, Collections.emptyList()));
        }
    }

    @Override
    public List<InterestDocument> findByInquirerUserId(String inquirerUserId) {
        if (inquirerUserId == null) return List.of();
        List<InterestDocument> results = new ArrayList<>();
        for (List<InterestDocument> list : fallbackStore.values()) {
            for (InterestDocument doc : list) {
                if (inquirerUserId.equals(doc.getInquirerUserId())) {
                    results.add(doc);
                }
            }
        }
        return results;
    }

    @Override
    public java.util.Optional<InterestDocument> findById(String id) {
        if (firestore == null) {
            return fallbackStore.values().stream().flatMap(List::stream)
                    .filter(i -> id.equals(i.getId())).findFirst();
        }
        try {
            return firestore.collectionGroup("interests").whereEqualTo("id", id).get().get()
                    .getDocuments().stream().findFirst()
                    .map(doc -> doc.toObject(InterestDocument.class));
        } catch (Exception e) {
            throw new RuntimeException("Database error reading interest", e);
        }
    }
}

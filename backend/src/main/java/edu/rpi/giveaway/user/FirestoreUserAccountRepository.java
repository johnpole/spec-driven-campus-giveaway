package edu.rpi.giveaway.user;

import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "spring.cloud.gcp.firestore.enabled", havingValue = "true")
public class FirestoreUserAccountRepository implements UserAccountRepository {

    private static final Logger log = LoggerFactory.getLogger(FirestoreUserAccountRepository.class);

    private final Firestore firestore;
    private final Map<String, UserAccountDocument> fallbackStore = new ConcurrentHashMap<>();

    @Autowired
    public FirestoreUserAccountRepository(@Autowired(required = false) Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public UserAccountDocument save(UserAccountDocument user) {
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }

        if (firestore != null) {
            try {
                firestore.collection("users").document(user.getId()).set(user).get();
                return user;
            } catch (Exception e) {
                log.error("Failed to save user account to Firestore: {}", e.getMessage());
                throw new RuntimeException("Database error saving user account", e);
            }
        } else {
            fallbackStore.put(user.getId(), user);
            return user;
        }
    }

    @Override
    public Optional<UserAccountDocument> findById(String id) {
        if (id == null) return Optional.empty();
        if (firestore != null) {
            try {
                var snap = firestore.collection("users").document(id).get().get();
                if (snap.exists()) {
                    return Optional.ofNullable(snap.toObject(UserAccountDocument.class));
                }
                return Optional.empty();
            } catch (Exception e) {
                log.error("Failed to load user from Firestore: {}", e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.ofNullable(fallbackStore.get(id));
    }

    @Override
    public Optional<UserAccountDocument> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String cleanEmail = email.trim().toLowerCase();
        if (firestore != null) {
            try {
                var query = firestore.collection("users").whereEqualTo("email", cleanEmail).get().get();
                if (!query.isEmpty()) {
                    return Optional.ofNullable(query.getDocuments().get(0).toObject(UserAccountDocument.class));
                }
                return Optional.empty();
            } catch (Exception e) {
                log.error("Failed to query user by email from Firestore: {}", e.getMessage());
                return Optional.empty();
            }
        }
        return fallbackStore.values().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(cleanEmail))
                .findFirst();
    }

    @Override
    public Optional<UserAccountDocument> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return Optional.empty();
        String cleanPhone = phoneNumber.trim();
        if (firestore != null) {
            try {
                var query = firestore.collection("users").whereEqualTo("phoneNumber", cleanPhone).get().get();
                if (!query.isEmpty()) {
                    return Optional.ofNullable(query.getDocuments().get(0).toObject(UserAccountDocument.class));
                }
                return Optional.empty();
            } catch (Exception e) {
                log.error("Failed to query user by phone from Firestore: {}", e.getMessage());
                return Optional.empty();
            }
        }
        return fallbackStore.values().stream()
                .filter(u -> u.getPhoneNumber() != null && u.getPhoneNumber().equals(cleanPhone))
                .findFirst();
    }
}

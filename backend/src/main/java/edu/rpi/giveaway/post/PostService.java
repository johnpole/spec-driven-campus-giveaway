package edu.rpi.giveaway.post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final String photoBucket;

    public PostService(PostRepository postRepository,
                       @Value("${app.storage.photo-bucket:campus-giveaway-photos}") String photoBucket) {
        this.postRepository = postRepository;
        this.photoBucket = photoBucket;
    }

    public CreatePostResponse createPost(CreatePostRequest request) {
        edu.rpi.giveaway.auth.UserPrincipal currentUser = edu.rpi.giveaway.auth.UserContext.requireCurrentUser();
        validatePhotoUrl(request.photoUrl());

        String id = UUID.randomUUID().toString();
        String rawEditToken = UUID.randomUUID().toString();
        String editTokenHash = TokenUtils.hashToken(rawEditToken);

        PostDocument doc = new PostDocument();
        doc.setId(id);
        doc.setTitle(request.title().trim());
        doc.setDescription(request.description().trim());
        doc.setCategory(request.category().getDisplayName());
        doc.setCondition(request.condition().name());
        doc.setPrice(request.price().doubleValue());
        doc.setIsFree(request.isFree());
        doc.setPickupLocation(request.pickupLocation().trim());
        doc.setAvailableUntil(Date.from(request.availableUntil()));
        doc.setPhotoUrl(request.photoUrl().trim());
        doc.setLegalConfirmed(request.legalConfirmed());
        doc.setStatus(PostStatus.AVAILABLE.name());
        doc.setAuthorUserId(currentUser.userId());
        doc.setAuthorEmail(currentUser.email());
        doc.setPosterName(currentUser.name() != null && !currentUser.name().isBlank() ? currentUser.name().trim() : request.posterName().trim());
        doc.setPosterContact(currentUser.email() != null && !currentUser.email().isBlank() ? currentUser.email().trim() : request.posterContact().trim());
        doc.setEditTokenHash(editTokenHash);
        doc.setInterestCount(0);
        doc.setCreatedAt(new Date());
        doc.setKeywords(generateKeywords(request.title(), request.description()));

        postRepository.save(doc);
        log.info("Created post with ID: {} and category: {}", id, doc.getCategory());

        return new CreatePostResponse(id, rawEditToken);
    }

    public List<PostSummaryResponse> getActiveFeed(String categoryFilter, Boolean freeOnly, String searchQuery) {
        List<PostDocument> activePosts = postRepository.findAllActive(Instant.now());

        return activePosts.stream()
                .filter(post -> {
                    if (categoryFilter == null || categoryFilter.isBlank()) return true;
                    return post.getCategory() != null &&
                            (post.getCategory().equalsIgnoreCase(categoryFilter) ||
                                    Category.fromString(categoryFilter).getDisplayName().equalsIgnoreCase(post.getCategory()));
                })
                .filter(post -> {
                    if (freeOnly == null || !freeOnly) return true;
                    return Boolean.TRUE.equals(post.getIsFree());
                })
                .filter(post -> {
                    if (searchQuery == null || searchQuery.isBlank()) return true;
                    String query = searchQuery.trim().toLowerCase();
                    boolean inTitle = post.getTitle() != null && post.getTitle().toLowerCase().contains(query);
                    boolean inDesc = post.getDescription() != null && post.getDescription().toLowerCase().contains(query);
                    return inTitle || inDesc;
                })
                .map(PostSummaryResponse::fromDocument)
                .collect(Collectors.toList());
    }

    public PostDetailResponse getPostDetail(String id) {
        PostDocument doc = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + id));
        return PostDetailResponse.fromDocument(doc);
    }

    public PostDetailResponse updateStatus(String id, String rawEditToken, PostStatus newStatus) {
        PostDocument doc = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + id));

        checkAuthorOrToken(doc, rawEditToken);

        doc.setStatus(newStatus.name());
        postRepository.save(doc);
        log.info("Updated status of post {} to {}", id, newStatus);

        return PostDetailResponse.fromDocument(doc);
    }

    public void deletePost(String id, String rawEditToken) {
        PostDocument doc = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + id));

        checkAuthorOrToken(doc, rawEditToken);

        postRepository.deleteById(id);
        log.info("Deleted post with ID: {}", id);
    }

    private void checkAuthorOrToken(PostDocument doc, String rawEditToken) {
        edu.rpi.giveaway.auth.UserPrincipal currentUser = edu.rpi.giveaway.auth.UserContext.getCurrentUser();
        if (currentUser != null) {
            // ADMIN OVERRIDE: administrators have full moderation access
            if (currentUser.isAdmin()) {
                return;
            }
            if (doc.getAuthorUserId() != null && !doc.getAuthorUserId().equals(currentUser.userId())) {
                throw new UnauthorizedEditException("You are not the author of this post");
            }
            return;
        }

        if (rawEditToken != null && !rawEditToken.isBlank()) {
            if (TokenUtils.verifyToken(rawEditToken, doc.getEditTokenHash())) {
                return;
            }
            throw new UnauthorizedEditException("Invalid or missing edit token for item: " + doc.getId());
        }

        throw new edu.rpi.giveaway.auth.UnauthorizedException("Authentication or edit token is required");
    }

    public void incrementInterestCount(String id) {
        postRepository.findById(id).ifPresent(doc -> {
            int current = doc.getInterestCount() != null ? doc.getInterestCount() : 0;
            doc.setInterestCount(current + 1);
            postRepository.save(doc);
        });
    }

    public PostDocument getRawPostForEdit(String id, String rawEditToken) {
        PostDocument doc = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + id));
        if (!TokenUtils.verifyToken(rawEditToken, doc.getEditTokenHash())) {
            throw new UnauthorizedEditException("Invalid or missing edit token for item: " + id);
        }
        return doc;
    }

    public Optional<PostDocument> findRawById(String id) {
        return postRepository.findById(id);
    }

    private void validatePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            throw new InvalidMediaUrlException("Photo URL is required");
        }
        String trimmed = photoUrl.trim();
        boolean isLocalMedia = trimmed.startsWith("/api/media/") || trimmed.startsWith("http://localhost") || trimmed.startsWith("https://localhost");
        boolean isBucket = trimmed.contains("storage.googleapis.com/" + photoBucket) || trimmed.startsWith("https://storage.googleapis.com/");
        if (!isLocalMedia && !isBucket) {
            throw new InvalidMediaUrlException("Photo URL must reference the trusted media endpoint or photo bucket");
        }
    }

    private List<String> generateKeywords(String title, String description) {
        Set<String> tokens = new HashSet<>();
        if (title != null) {
            for (String part : title.toLowerCase().split("\\W+")) {
                if (part.length() > 1) tokens.add(part);
            }
        }
        if (description != null) {
            for (String part : description.toLowerCase().split("\\W+")) {
                if (part.length() > 2) tokens.add(part);
            }
        }
        return new ArrayList<>(tokens);
    }
}

package edu.rpi.giveaway.interest;

import edu.rpi.giveaway.post.PostDocument;
import edu.rpi.giveaway.post.PostNotFoundException;
import edu.rpi.giveaway.post.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InterestService {

    private static final Logger log = LoggerFactory.getLogger(InterestService.class);

    private final InterestRepository interestRepository;
    private final PostService postService;

    public InterestService(InterestRepository interestRepository, PostService postService) {
        this.interestRepository = interestRepository;
        this.postService = postService;
    }

    public void submitInterest(String postId, CreateInterestRequest request) {
        PostDocument post = postService.findRawById(postId)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + postId));

        if ("CLAIMED".equalsIgnoreCase(post.getStatus()) || "TAKEN".equalsIgnoreCase(post.getStatus())) {
            throw new ItemNotAvailableException("Item is already claimed and no longer available");
        }

        if (post.getAvailableUntil() != null && post.getAvailableUntil().before(new Date())) {
            throw new ItemNotAvailableException("Item has expired and is no longer available");
        }

        edu.rpi.giveaway.auth.UserPrincipal currentUser = edu.rpi.giveaway.auth.UserContext.requireCurrentUser();
        if (post.getAuthorUserId() != null && post.getAuthorUserId().equals(currentUser.userId())) {
            throw new IllegalArgumentException("You cannot express interest in an item you posted");
        }
        if (hasRequested(postId, currentUser.userId())) {
            throw new ItemNotAvailableException("You already sent a request for this item");
        }
        validatePickupTime(request.pickupAt(), post);

        // Signed-in students: name defaults to the account; contact may be a phone they prefer.
        String name = (request.visitorName() != null && !request.visitorName().isBlank())
                ? request.visitorName().trim()
                : (currentUser.name() != null ? currentUser.name() : currentUser.email());
        String contact = (request.visitorContact() != null && !request.visitorContact().isBlank())
                ? request.visitorContact().trim()
                : currentUser.email();

        InterestDocument interest = new InterestDocument();
        interest.setId(UUID.randomUUID().toString());
        interest.setPostId(postId);
        interest.setVisitorName(name);
        interest.setVisitorContact(contact);
        interest.setInquirerUserId(currentUser.userId());
        interest.setInquirerEmail(currentUser.email());
        interest.setMessage(request.message() != null ? request.message().trim() : null);
        interest.setPickupAt(request.pickupAt() != null ? Date.from(request.pickupAt()) : null);
        interest.setPickupPlace(request.pickupPlace() != null && !request.pickupPlace().isBlank()
                ? request.pickupPlace().trim() : post.getPickupLocation());
        interest.setCreatedAt(new Date());

        interestRepository.save(interest);
        postService.incrementInterestCount(postId);

        log.info("Registered interest from {} for item {}", name, postId);
    }

    /** True when this user already sent a request for this item. */
    public boolean hasRequested(String postId, String userId) {
        return userId != null && interestRepository.findByInquirerUserId(userId).stream()
                .anyMatch(i -> postId.equals(i.getPostId()));
    }

    private static void validatePickupTime(java.time.Instant pickupAt, PostDocument post) {
        if (pickupAt == null) {
            return;
        }
        if (pickupAt.isBefore(java.time.Instant.now())) {
            throw new IllegalArgumentException("Pickup time must be in the future");
        }
        if (post.getAvailableUntil() != null && pickupAt.isAfter(post.getAvailableUntil().toInstant())) {
            throw new IllegalArgumentException("Pickup time must be before the item's available-until date");
        }
    }

    public List<InterestDocument> getInterestsByInquirer(String userId) {
        return interestRepository.findByInquirerUserId(userId);
    }

    public InterestSummaryResponse getInterestSummary(String postId) {
        postService.findRawById(postId)
                .orElseThrow(() -> new PostNotFoundException("Item not found with id: " + postId));

        List<InterestDocument> interests = interestRepository.findByPostId(postId);
        List<String> firstNames = interests.stream()
                .map(i -> extractFirstName(i.getVisitorName()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());

        return new InterestSummaryResponse(interests.size(), firstNames);
    }

    public List<InterestDetailResponse> getPrivateInterests(String postId, String rawEditToken) {
        // This will authenticate the token or throw UnauthorizedEditException (403)
        postService.getRawPostForEdit(postId, rawEditToken);

        List<InterestDocument> interests = interestRepository.findByPostId(postId);
        return interests.stream()
                .map(InterestDetailResponse::fromDocument)
                .collect(Collectors.toList());
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.trim().split("\\s+")[0];
    }
}

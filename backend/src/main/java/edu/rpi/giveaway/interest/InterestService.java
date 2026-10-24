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

        edu.rpi.giveaway.auth.UserPrincipal currentUser = edu.rpi.giveaway.auth.UserContext.getCurrentUser();
        if (currentUser != null && post.getAuthorUserId() != null && post.getAuthorUserId().equals(currentUser.userId())) {
            throw new IllegalArgumentException("You cannot express interest in an item you posted");
        }

        String name;
        String contact;
        if (currentUser != null) {
            name = (request.visitorName() != null && !request.visitorName().isBlank())
                    ? request.visitorName().trim()
                    : (currentUser.name() != null ? currentUser.name() : "Google User");
            contact = (request.visitorContact() != null && !request.visitorContact().isBlank())
                    ? request.visitorContact().trim()
                    : currentUser.email();
        } else {
            if (request.visitorName() == null || request.visitorName().isBlank()
                    || request.visitorContact() == null || request.visitorContact().isBlank()) {
                throw new edu.rpi.giveaway.auth.UnauthorizedException("Authentication is required to express interest");
            }
            name = request.visitorName().trim();
            contact = request.visitorContact().trim();
        }

        InterestDocument interest = new InterestDocument();
        interest.setId(UUID.randomUUID().toString());
        interest.setPostId(postId);
        interest.setVisitorName(name);
        interest.setVisitorContact(contact);
        interest.setInquirerUserId(currentUser != null ? currentUser.userId() : null);
        interest.setInquirerEmail(currentUser != null ? currentUser.email() : null);
        interest.setMessage(request.message() != null ? request.message().trim() : null);
        interest.setCreatedAt(new Date());

        interestRepository.save(interest);
        postService.incrementInterestCount(postId);

        log.info("Registered interest from {} for item {}", name, postId);
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

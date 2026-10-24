package edu.rpi.giveaway.user;

import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.auth.UserPrincipal;
import edu.rpi.giveaway.interest.InterestDetailResponse;
import edu.rpi.giveaway.interest.InterestDocument;
import edu.rpi.giveaway.interest.InterestRepository;
import edu.rpi.giveaway.post.PostDocument;
import edu.rpi.giveaway.post.PostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final PostRepository postRepository;
    private final InterestRepository interestRepository;

    public UserController(PostRepository postRepository, InterestRepository interestRepository) {
        this.postRepository = postRepository;
        this.interestRepository = interestRepository;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<UserPostItemResponse>> getMyPosts() {
        UserPrincipal currentUser = UserContext.requireCurrentUser();
        List<PostDocument> posts = postRepository.findByAuthorUserId(currentUser.userId());

        List<UserPostItemResponse> responses = posts.stream().map(post -> {
            List<InterestDocument> inquiries = interestRepository.findByPostId(post.getId());
            List<InterestDetailResponse> inquiryResponses = inquiries.stream()
                    .map(InterestDetailResponse::fromDocument)
                    .toList();

            return new UserPostItemResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getDescription(),
                    post.getCategory(),
                    post.getCondition(),
                    post.getPrice(),
                    post.getIsFree(),
                    post.getPickupLocation(),
                    post.getAvailableUntil() != null ? post.getAvailableUntil().toInstant() : null,
                    post.getPhotoUrl(),
                    post.getStatus(),
                    post.getInterestCount() != null ? post.getInterestCount() : 0,
                    post.getCreatedAt() != null ? post.getCreatedAt().toInstant() : null,
                    inquiryResponses
            );
        }).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/interests")
    public ResponseEntity<List<UserInquiryItemResponse>> getMyInterests() {
        UserPrincipal currentUser = UserContext.requireCurrentUser();
        List<InterestDocument> interests = interestRepository.findByInquirerUserId(currentUser.userId());

        List<UserInquiryItemResponse> responses = new ArrayList<>();
        for (InterestDocument interest : interests) {
            String title = "Item";
            String photoUrl = "";
            String status = "UNKNOWN";

            if (interest.getPostId() != null) {
                var postOpt = postRepository.findById(interest.getPostId());
                if (postOpt.isPresent()) {
                    var post = postOpt.get();
                    title = post.getTitle();
                    photoUrl = post.getPhotoUrl();
                    status = post.getStatus();
                }
            }

            responses.add(new UserInquiryItemResponse(
                    interest.getId(),
                    interest.getPostId(),
                    title,
                    photoUrl,
                    status,
                    interest.getMessage(),
                    interest.getCreatedAt() != null ? interest.getCreatedAt().toInstant() : null
            ));
        }

        return ResponseEntity.ok(responses);
    }
}

package edu.rpi.giveaway.interest;

import edu.rpi.giveaway.auth.UserContext;
import edu.rpi.giveaway.auth.UserPrincipal;
import edu.rpi.giveaway.post.PostDocument;
import edu.rpi.giveaway.post.PostNotFoundException;
import edu.rpi.giveaway.post.PostService;
import edu.rpi.giveaway.post.UnauthorizedEditException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/** Lets the poster and the interested student talk about a pickup on a single interest request. */
@Service
public class InterestReplyService {

    private final InterestRepository interestRepository;
    private final InterestReplyRepository replyRepository;
    private final PostService postService;

    public InterestReplyService(InterestRepository interestRepository,
                                InterestReplyRepository replyRepository,
                                PostService postService) {
        this.interestRepository = interestRepository;
        this.replyRepository = replyRepository;
        this.postService = postService;
    }

    public InterestReplyResponse addReply(String interestId, String message) {
        UserPrincipal user = UserContext.requireCurrentUser();
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Reply message cannot be empty");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Reply message cannot exceed 500 characters");
        }
        InterestDocument interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new PostNotFoundException("Request not found: " + interestId));
        String role = roleOf(user, interest);

        InterestReplyEntity reply = new InterestReplyEntity(UUID.randomUUID().toString(), interestId,
                user.userId(), user.name() != null ? user.name() : user.email(), role, message.trim(), new Date());
        return InterestReplyResponse.fromEntity(replyRepository.save(reply));
    }

    public List<InterestReplyResponse> repliesFor(String interestId) {
        return replyRepository.findByInterestIdOrderByCreatedAtAsc(interestId).stream()
                .map(InterestReplyResponse::fromEntity)
                .toList();
    }

    /** Only the two people in the conversation may reply: the poster (SELLER) or the requester (BUYER). */
    private String roleOf(UserPrincipal user, InterestDocument interest) {
        if (user.userId().equals(interest.getInquirerUserId())) {
            return "BUYER";
        }
        PostDocument post = postService.findRawById(interest.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Item not found: " + interest.getPostId()));
        // Admins may answer for any listing (e.g. the seeded sample items).
        if (user.userId().equals(post.getAuthorUserId()) || user.isAdmin()) {
            return "SELLER";
        }
        throw new UnauthorizedEditException("Only the poster and the requester can reply to this request");
    }
}

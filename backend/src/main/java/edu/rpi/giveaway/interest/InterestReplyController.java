package edu.rpi.giveaway.interest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests/{interestId}/replies")
public class InterestReplyController {

    private final InterestReplyService replyService;

    public InterestReplyController(InterestReplyService replyService) {
        this.replyService = replyService;
    }

    @PostMapping
    public ResponseEntity<InterestReplyResponse> reply(@PathVariable String interestId, @RequestBody ReplyRequest request) {
        InterestReplyResponse reply = replyService.addReply(interestId, request == null ? null : request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    public record ReplyRequest(String message) {
    }
}

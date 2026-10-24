package edu.rpi.giveaway.interest;

import edu.rpi.giveaway.post.UnauthorizedEditException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{id}/interests")
public class InterestController {

    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @PostMapping
    public ResponseEntity<InterestSummaryResponse> submitInterest(
            @PathVariable String id,
            @Valid @RequestBody CreateInterestRequest request
    ) {
        interestService.submitInterest(id, request);
        InterestSummaryResponse summary = interestService.getInterestSummary(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @GetMapping("/summary")
    public ResponseEntity<InterestSummaryResponse> getInterestSummary(@PathVariable String id) {
        InterestSummaryResponse summary = interestService.getInterestSummary(id);
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    public ResponseEntity<List<InterestDetailResponse>> getPrivateInterests(
            @PathVariable String id,
            @RequestHeader(value = "X-Edit-Token", required = false) String editToken
    ) {
        if (editToken == null || editToken.isBlank()) {
            throw new UnauthorizedEditException("X-Edit-Token header is required to retrieve private inquiry details");
        }
        List<InterestDetailResponse> details = interestService.getPrivateInterests(id, editToken);
        return ResponseEntity.ok(details);
    }
}

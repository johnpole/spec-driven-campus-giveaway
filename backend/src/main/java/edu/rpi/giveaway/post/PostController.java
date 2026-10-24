package edu.rpi.giveaway.post;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        CreatePostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PostSummaryResponse>> getActiveFeed(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean freeOnly,
            @RequestParam(name = "q", required = false) String query
    ) {
        List<PostSummaryResponse> feed = postService.getActiveFeed(category, freeOnly, query);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable String id) {
        PostDetailResponse detail = postService.getPostDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PostDetailResponse> updateStatus(
            @PathVariable String id,
            @RequestHeader(value = "X-Edit-Token", required = false) String editToken,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        if ((editToken == null || editToken.isBlank()) && edu.rpi.giveaway.auth.UserContext.getCurrentUser() == null) {
            throw new edu.rpi.giveaway.auth.UnauthorizedException("Authentication or X-Edit-Token header is required for status updates");
        }
        PostDetailResponse updated = postService.updateStatus(id, editToken, request.status());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            @RequestHeader(value = "X-Edit-Token", required = false) String editToken
    ) {
        if ((editToken == null || editToken.isBlank()) && edu.rpi.giveaway.auth.UserContext.getCurrentUser() == null) {
            throw new edu.rpi.giveaway.auth.UnauthorizedException("Authentication or X-Edit-Token header is required for post deletion");
        }
        postService.deletePost(id, editToken);
        return ResponseEntity.noContent().build();
    }
}

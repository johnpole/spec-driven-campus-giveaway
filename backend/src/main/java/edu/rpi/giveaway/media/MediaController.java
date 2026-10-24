package edu.rpi.giveaway.media;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {
        edu.rpi.giveaway.auth.UserContext.requireCurrentUser(); // sign in before uploading a photo
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Image file is required and cannot be empty"));
        }

        if (file.getSize() > MediaService.MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File exceeds maximum allowed size of 5 MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !MediaService.ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(Map.of("error", "Unsupported media type. Allowed types are image/jpeg, image/png, and image/webp"));
        }

        try {
            String photoUrl = mediaService.storePhoto(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MediaUploadResponse(photoUrl));
        } catch (StorageServiceException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Storage service failure: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "I/O error storing media: " + e.getMessage()));
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) throws IOException {
        Resource resource = mediaService.loadPhotoAsResource(filename);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = "image/jpeg";
        if (filename.endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.endsWith(".webp")) {
            contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/autofill")
    public ResponseEntity<AutoFillSuggestionResponse> autoFill(@RequestBody AutoFillRequest request) {
        edu.rpi.giveaway.auth.UserContext.requireCurrentUser(); // sign-in required: protects the Gemini quota
        AutoFillSuggestionResponse response = mediaService.autoFillFromPhoto(request.photoUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/autofill/status")
    public ResponseEntity<Map<String, Boolean>> getAutofillStatus() {
        return ResponseEntity.ok(Map.of("enabled", mediaService.isAutofillEnabled()));
    }

    @PostMapping("/autofill/toggle")
    public ResponseEntity<Map<String, Boolean>> toggleAutofill(@RequestParam(required = false) Boolean enabled) {
        edu.rpi.giveaway.auth.UserPrincipal user = edu.rpi.giveaway.auth.UserContext.requireCurrentUser();
        if (!user.isAdmin()) {
            throw new edu.rpi.giveaway.post.UnauthorizedEditException("Only administrators can toggle Gemini AI auto-fill");
        }
        boolean newState = enabled != null ? enabled : !mediaService.isAutofillEnabled();
        mediaService.setAutofillEnabled(newState);
        return ResponseEntity.ok(Map.of("enabled", newState));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "File exceeds maximum allowed size of 5 MB"));
    }

    @ExceptionHandler(StorageServiceException.class)
    public ResponseEntity<?> handleStorageException(StorageServiceException exc) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Storage service failure: " + exc.getMessage()));
    }
}

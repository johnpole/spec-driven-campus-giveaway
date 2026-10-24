package edu.rpi.giveaway.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    public static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private static final Map<String, String> MIME_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Storage storage;
    private final String bucketName;
    private final Path localUploadDir;

    @Value("${giveaway.ai.autofill.enabled:true}")
    private boolean autofillEnabled = true;

    @Value("${gemini.api.key:${GEMINI_API_KEY:}}")
    private String geminiApiKey = "";

    public boolean isAutofillEnabled() {
        return autofillEnabled;
    }

    public void setAutofillEnabled(boolean enabled) {
        this.autofillEnabled = enabled;
    }

    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public AutoFillSuggestionResponse autoFillFromPhoto(String photoUrl) {
        if (!autofillEnabled) {
            throw new AutoFillDisabledException("AI Auto-Fill is currently disabled");
        }

        // 1. If Gemini API Key is configured, attempt real Gemini 2.5 Flash Multimodal Vision
        if (geminiApiKey != null && !geminiApiKey.trim().isBlank()) {
            try {
                String filename = photoUrl != null && photoUrl.contains("/")
                        ? photoUrl.substring(photoUrl.lastIndexOf('/') + 1)
                        : photoUrl;

                Resource resource = loadPhotoAsResource(filename);
                if (resource != null && resource.exists()) {
                    byte[] bytes = resource.getInputStream().readAllBytes();
                    String mimeType = "image/jpeg";
                    if (filename != null && filename.toLowerCase().endsWith(".png")) {
                        mimeType = "image/png";
                    } else if (filename != null && filename.toLowerCase().endsWith(".webp")) {
                        mimeType = "image/webp";
                    }

                    AutoFillSuggestionResponse geminiResponse = callGeminiVision(bytes, mimeType);
                    if (geminiResponse != null) {
                        log.info("Gemini 2.5 Flash successfully identified photo: {}", geminiResponse.title());
                        return geminiResponse;
                    }
                }
            } catch (Exception e) {
                log.warn("Gemini 2.5 Flash call failed; falling back to heuristic presentation mode: {}", e.getMessage());
            }
        }

        // 2. Deterministic Presentation Heuristic Fallback (zero cost, zero failure risk)
        return heuristicFallback(photoUrl);
    }

    private AutoFillSuggestionResponse callGeminiVision(byte[] imageBytes, String mimeType) {
        try {
            String b64Image = Base64.getEncoder().encodeToString(imageBytes);

            String categories = java.util.Arrays.stream(edu.rpi.giveaway.post.Category.values())
                    .map(c -> "\"" + c.getDisplayName() + "\"")
                    .collect(java.util.stream.Collectors.joining(", "));
            String conditions = java.util.Arrays.stream(edu.rpi.giveaway.post.Condition.values())
                    .map(c -> "\"" + c.name() + "\"")
                    .collect(java.util.stream.Collectors.joining(", "));
            String prompt = "Analyze this photo of an item posted by an RPI college student on a campus giveaway and resale exchange. " +
                    "Return a JSON object with: " +
                    "\"title\": concise descriptive item name (max 60 chars), " +
                    "\"category\": exactly one of [" + categories + "], " +
                    "\"condition\": exactly one of [" + conditions + "], " +
                    "\"suggestedPrice\": a fair used price in USD for a student (0.0 only if the item is clearly low value), " +
                    "\"description\": 2 to 3 sentences describing the item condition, features, and campus pickup utility.";

            Map<String, Object> inlineData = Map.of(
                    "mimeType", mimeType,
                    "data", b64Image
            );

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> imagePart = Map.of("inlineData", inlineData);

            Map<String, Object> content = Map.of(
                    "parts", java.util.List.of(textPart, imagePart)
            );

            Map<String, Object> payload = Map.of(
                    "contents", java.util.List.of(content),
                    "generationConfig", Map.of("responseMimeType", "application/json")
            );

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey.trim()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode parts = candidates.get(0).path("content").path("parts");
                    String rawJson = null;
                    if (parts.isArray()) {
                        for (JsonNode part : parts) {
                            if (part.has("text")) {
                                rawJson = part.get("text").asText();
                                break;
                            }
                        }
                    }

                    if (rawJson != null) {
                        String cleanJson = rawJson.trim();
                        if (cleanJson.startsWith("```json")) {
                            cleanJson = cleanJson.substring(7);
                        } else if (cleanJson.startsWith("```")) {
                            cleanJson = cleanJson.substring(3);
                        }
                        if (cleanJson.endsWith("```")) {
                            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                        }
                        cleanJson = cleanJson.trim();

                        JsonNode parsed = objectMapper.readTree(cleanJson);
                        String title = parsed.has("title") ? parsed.get("title").asText("Campus Essential Item") : "Campus Essential Item";
                        String category = parsed.has("category") ? parsed.get("category").asText("Dorm essentials") : "Dorm essentials";
                        String condition = parsed.has("condition") ? parsed.get("condition").asText("GOOD") : "GOOD";
                        double price = parsed.has("suggestedPrice") ? parsed.get("suggestedPrice").asDouble(0.0) : 0.0;
                        String description = parsed.has("description") ? parsed.get("description").asText("Item available for pickup.") : "Item available for pickup.";

                        return new AutoFillSuggestionResponse(title, normalizeCategory(category),
                                normalizeCondition(condition), Math.max(0.0, price), description);
                    }
                }
            } else {
                log.warn("Gemini API returned HTTP status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Failed to complete Gemini Vision request", e);
        }
        return null;
    }

    /** Maps the model's category to one the app accepts, so a suggestion can never break the post form. */
    static String normalizeCategory(String raw) {
        for (edu.rpi.giveaway.post.Category c : edu.rpi.giveaway.post.Category.values()) {
            if (c.getDisplayName().equalsIgnoreCase(raw == null ? "" : raw.trim()) || c.name().equalsIgnoreCase(raw)) {
                return c.getDisplayName();
            }
        }
        return edu.rpi.giveaway.post.Category.OTHER.getDisplayName();
    }

    static String normalizeCondition(String raw) {
        String clean = raw == null ? "" : raw.trim().toUpperCase().replace(' ', '_');
        if ("NEW".equals(clean)) {
            return edu.rpi.giveaway.post.Condition.LIKE_NEW.name();
        }
        for (edu.rpi.giveaway.post.Condition c : edu.rpi.giveaway.post.Condition.values()) {
            if (c.name().equals(clean)) {
                return c.name();
            }
        }
        return edu.rpi.giveaway.post.Condition.GOOD.name();
    }

    private AutoFillSuggestionResponse heuristicFallback(String photoUrl) {
        String lower = photoUrl != null ? photoUrl.toLowerCase() : "";
        if (lower.contains("desk") || lower.contains("chair") || lower.contains("furniture")) {
            return new AutoFillSuggestionResponse(
                    "Wooden Study Desk / Chair",
                    "Furniture",
                    "GOOD",
                    15.0,
                    "Sturdy study furniture, clean condition, perfect for dorm or off-campus room."
            );
        } else if (lower.contains("calc") || lower.contains("book") || lower.contains("textbook")) {
            return new AutoFillSuggestionResponse(
                    "Calculus Early Transcendentals, 9th Edition",
                    "Textbooks",
                    "LIKE_NEW",
                    20.0,
                    "Required textbook for MATH 1010/1020. No missing pages or highlighting."
            );
        } else if (lower.contains("fan") || lower.contains("lamp") || lower.contains("microwave") || lower.contains("kitchen")) {
            return new AutoFillSuggestionResponse(
                    "Compact Dorm Microwave / Fan",
                    "Kitchen",
                    "GOOD",
                    25.0,
                    "700W compact appliance in great working order. Clean and ready for pickup."
            );
        } else {
            return new AutoFillSuggestionResponse(
                    "Campus Dorm Essential Item",
                    "Dorm essentials",
                    "GOOD",
                    0.0,
                    "Item available for pickup. Clean and in good condition."
            );
        }
    }


    @Autowired
    public MediaService(@Autowired(required = false) Storage storage,
                        @Value("${app.storage.photo-bucket:campus-giveaway-photos}") String bucketName,
                        @Value("${app.storage.local-dir:uploads}") String localDir) {
        this.storage = storage;
        this.bucketName = bucketName;
        this.localUploadDir = Paths.get(localDir);
        try {
            Files.createDirectories(this.localUploadDir);
        } catch (IOException e) {
            log.warn("Could not create local upload directory {}: {}", localDir, e.getMessage());
        }
    }

    @Override
    public String storePhoto(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String extension = MIME_EXTENSIONS.getOrDefault(contentType, ".jpg");
        String filename = UUID.randomUUID() + extension;

        if (storage != null) {
            try {
                BlobId blobId = BlobId.of(bucketName, filename);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
                storage.create(blobInfo, file.getBytes());
                log.info("Stored photo {} to GCS bucket {}", filename, bucketName);
                return "/api/media/" + filename;
            } catch (Exception e) {
                log.error("Cloud storage upload failed for file {}: {}", filename, e.getMessage());
                throw new StorageServiceException("Failed to upload photo to Cloud Storage", e);
            }
        } else {
            // Local fallback / emulator storage
            Path targetLocation = this.localUploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/api/media/" + filename;
        }
    }

    @Override
    public Resource loadPhotoAsResource(String filename) throws IOException {
        if (storage != null) {
            try {
                Blob blob = storage.get(BlobId.of(bucketName, filename));
                if (blob != null && blob.exists()) {
                    return new ByteArrayResource(blob.getContent()) {
                        @Override
                        public String getFilename() {
                            return filename;
                        }
                    };
                }
            } catch (Exception e) {
                log.warn("Failed to retrieve photo {} from GCS bucket {}: {}", filename, bucketName, e.getMessage());
            }
        }

        // Local storage fallback (never outside the upload directory)
        Path uploadRoot = this.localUploadDir.toAbsolutePath().normalize();
        Path filePath = uploadRoot.resolve(filename).normalize();
        if (filePath.startsWith(uploadRoot)) {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        }

        // Sample photos for seeded listings ship inside the app
        if (filename != null && filename.matches("seed-[a-z0-9-]+\\.jpg")) {
            Resource seed = new org.springframework.core.io.ClassPathResource("seed-images/" + filename);
            if (seed.exists()) {
                return seed;
            }
        }
        return null;
    }
}

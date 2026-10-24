# Tasks: Phase 1 Open Board

## 1. Backend Foundation & Media Service

- [x] 1.1 Scaffold Spring Boot 3.5 Maven backend in `/backend` (Java 21, Spring Web, Bean Validation, Spring Cloud GCP Firestore & Cloud Storage dependencies) and verify with `mvn clean test` passing a context load test
- [x] 1.2 Implement `MediaController` and `MediaService` for single photo upload (`POST /api/media/upload`) with 5 MB size limit, MIME validation (JPEG, PNG, WebP), and Cloud Storage/local emulator integration; verify with MockMvc tests for valid upload (201), oversized file (413), and unsupported type (415)

## 2. Backend Posts & Interests Capabilities

- [x] 2.1 Create Post domain models, Firestore document mapping, and public DTOs enforcing validation rules (title ≤ 100, description ≤ 1000, category/condition enums, email/E.164 contact validation, `legalConfirmed=true`) and stripping private fields; verify with unit tests asserting validation constraints and DTO field stripping
- [x] 2.2 Implement post creation in `PostService` and `PostRepository` generating a random UUID edit token and persisting only its SHA-256 hash in Firestore; verify with repository integration tests executing against the Firestore emulator
- [x] 2.3 Implement feed query and in-memory keyword search in `PostService` and `PostRepository` supporting category filtering, freeOnly filtering, and active item constraint (`status == AVAILABLE` and `availableUntil >= now`); verify with tests executing against the Firestore emulator
- [x] 2.4 Implement item detail retrieval, claim status update (`PATCH /api/posts/{id}/status`), and post deletion (`DELETE /api/posts/{id}`) with constant-time token hash verification (`MessageDigest.isEqual`); verify with tests executing against the Firestore emulator
- [x] 2.5 Implement `PostController` endpoints (`POST /api/posts`, `GET /api/posts`, `GET /api/posts/{id}`, `PATCH /api/posts/{id}/status`, `DELETE /api/posts/{id}`); verify with MockMvc tests covering creation (201), detail (200), delete (204), validation errors (400), and invalid token rejections (403)
- [x] 2.6 Implement Interest inquiry model, service, and `InterestController` endpoints (`POST /api/posts/{id}/interests`, `GET /api/posts/{id}/interests/summary`, `GET /api/posts/{id}/interests`); verify with MockMvc tests asserting public summary masking, conflict on claimed/expired posts (409), and token-authenticated inquiry retrieval (200/403)
- [x] 2.7 Implement development seed data initializer (`CommandLineRunner` / profile-gated bean) seeding 12 realistic RPI campus items across dorm furniture, electronics, and textbooks; verify with an integration test asserting all 12 items load in the Firestore emulator

## 3. Frontend Foundation & Mobile Design System

- [x] 3.1 Initialize Angular 20 standalone application in `/frontend` with SCSS design tokens, typography (`Plus Jakarta Sans`, `Inter`), responsive viewport setup (360px–430px base), and verify with `npm test` passing initial smoke tests
- [x] 3.2 Implement typed Angular services (`PostService`, `InterestService`, `MediaService`) with Signals-based state management; verify with service unit tests using `HttpTestingController`
- [x] 3.3 Implement client-side camera capture directive and image resizer utility downscaling files to max dimension ≤ 1600px with canvas; verify with unit tests asserting dimension reduction and output MIME type

## 4. Frontend Mobile Views (S1–S3 & My Post)

- [x] 4.1 Implement S1 Home / Browse Feed component featuring sticky top search bar, horizontal category filter chips, "Free only" switch, 2-column card grid, and sticky bottom navigation; verify with component rendering tests
- [x] 4.2 Implement S2 Item Detail component with photo display, price pill, metadata chips, interest counter, sticky bottom action bar, "I'm interested" bottom sheet modal, and success toast; verify with component interaction tests
- [x] 4.3 Implement S3 Post Item component featuring native camera file picker, reactive form validations, "It's free" toggle, poster contact inputs, "legal, safe, and mine to give" confirmation checkbox, and success screen showing private edit link; verify with form submission tests
- [x] 4.4 Implement "My Post" private management component allowing posters to access their item via private link/token, inspect interested inquiries with contact details, mark the item as claimed, or permanently delete the post; verify with component interaction and action trigger tests

## 5. Unified Packaging & Cloud Run Deployment

- [x] 5.1 Create multi-stage Dockerfile (Stage 1: Node 22 build Angular; Stage 2: Maven build Spring Boot copying static assets; Stage 3: JRE 21 distroless runtime) and `deploy.sh` script for Cloud Run (`us-east1`); verify with a local container build check

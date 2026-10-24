# Design: Phase 1 Open Board

## Context
See [proposal.md](proposal.md) for motivation and capability scope.
The platform is built as a unified single-container deployable: an Angular 20 mobile-first client and a Java 21 Spring Boot 3.5 backend targeting Google Cloud Run (`us-east1`). The system persists data in Google Cloud Firestore (Native mode) and media in Google Cloud Storage.

## Goals / Non-Goals

**Goals:**
- Provide zero-authentication item posting with client-side image compression and validation.
- Implement Firestore schema separating public post fields from private contact data and cryptographic edit token hashes.
- Implement visitor interest capture with aggregate count tracking and poster-only contact retrieval.
- Build responsive mobile UI adhering strictly to the design tokens in `design/DESIGN.md` and Stitch screens S1–S3, plus a dedicated "My Post" management view.
- Containerize Angular build into Spring Boot static resources for Cloud Run deployment.

**Non-Goals:**
- User account creation, SSO/LDAP integration, or persistent user sessions.
- Pre-moderation or content scanning queues.
- Real-time WebSockets or server-sent events for live chat.

## Data Model (Firestore Collections & Documents)

### Collection: `posts`
Document ID: Auto-generated UUID string (e.g., `item_7f2b9a10...`)

| Field | Type | Description | Visibility |
|---|---|---|---|
| `id` | String | Unique item identifier | Public |
| `title` | String | Item title (max 100 chars) | Public |
| `description` | String | Detailed item description (max 1000 chars) | Public |
| `category` | String | Enum (`Furniture`, `Electronics`, `Textbooks`, `Kitchen`, `Dorm essentials`, `Clothing`, `Bikes`, `Other`) | Public |
| `condition` | String | Enum (`LIKE_NEW`, `GOOD`, `FAIR`) | Public |
| `price` | Number | Price in USD (0.00 denotes free) | Public |
| `isFree` | Boolean | True if price == 0.00 | Public |
| `pickupLocation` | String | Campus pickup landmark/building | Public |
| `availableUntil` | Timestamp | ISO-8601 UTC timestamp | Public |
| `photoUrl` | String | Trusted media URL referencing application bucket/endpoint | Public |
| `legalConfirmed` | Boolean | True confirming "legal, safe, and mine to give" | Internal |
| `status` | String | Enum (`AVAILABLE`, `CLAIMED`) | Public |
| `posterName` | String | First name or display name of the poster | Public |
| `posterContact` | String | Required validated email or E.164 phone | **Strictly Private** (never returned by any endpoint) |
| `editTokenHash` | String | Hex-encoded SHA-256 hash of the edit token | **Strictly Private** (never returned by any endpoint) |
| `interestCount` | Number | Atomic integer counter of submitted interests | Public |
| `createdAt` | Timestamp | Server timestamp | Public |
| `keywords` | Array<String> | Lowercased tokens for prefix/text search | Internal |

### Subcollection: `posts/{id}/interests`
Document ID: Auto-generated UUID string

| Field | Type | Description | Visibility |
|---|---|---|---|
| `id` | String | Unique interest inquiry identifier | Private |
| `visitorName` | String | Full name of interested visitor | Private (only first name exposed in summary) |
| `visitorContact` | String | Email or phone number | **Private** (only visible to poster with valid editToken) |
| `message` | String | Optional note to poster | **Private** (only visible to poster with valid editToken) |
| `createdAt` | Timestamp | Server timestamp | Private |

## Decisions

### Decision 1: Cryptographic Token-Based Post Management
- **Choice**: Generate a secure random 128-bit UUID `editToken` on item creation. Return the raw token **only once** in the creation response (`POST /api/posts`). The database stores **only the SHA-256 hash** (`editTokenHash`). When the poster sends `X-Edit-Token` on status changes (`PATCH /api/posts/{id}/status`), inquiries (`GET /api/posts/{id}/interests`), or deletion (`DELETE /api/posts/{id}`), the backend hashes the incoming token and compares it with `MessageDigest.isEqual` in constant time to prevent timing side-channel attacks.
- **Rationale**: Meets Phase 1 zero-login requirement while preventing unauthorized state tampering or contact snooping, with zero risk if Firestore data is inspected.
- **Alternatives Considered**: Storing plain text tokens in Firestore — rejected due to credential leak risks.

### Decision 2: In-Memory / Keyword Token Search in Firestore
- **Choice**: Store lowercased keywords array on post documents and perform combined field filtering in Spring Boot service. An item is defined as "active" when `status == AVAILABLE` and `availableUntil >= now`.
- **Rationale**: Native Firestore lacks substring full-text search. For campus-scale volume in Phase 1 (hundreds to low thousands of concurrent items), tokenized matching + in-memory filtering avoids third-party search dependencies (Algolia/Elasticsearch).
- **Alternatives Considered**: Full Algolia integration — excessive operational complexity for Phase 1.

### Decision 3: Streaming Multipart Upload via Backend
- **Choice**: Angular submits multipart form to `POST /api/media/upload`, which streams to GCS (or local mock emulator).
- **Rationale**: Simplifies local development with Firestore/GCS emulators without managing signed URL CORS and credential handoffs.
- **Alternatives Considered**: Signed Cloud Storage direct uploads — introduces additional CORS and IAM setup for local testing.

### Decision 4: Single Deployable Container Architecture
- **Choice**: Multi-stage Dockerfile builds Angular via Node 22, copies `dist/` into Spring Boot `src/main/resources/static/`, packages a non-root JRE 21 container, and serves both SPA and REST APIs on port 8080.
- **Rationale**: Eliminates cross-origin CORS overhead in production, simplifies Cloud Run setup, and reduces deployment footprint to one URL.

## Risks / Trade-offs

- **[Risk] Edit Token Loss**: If a student closes their browser without copying the private link or bookmarking it, they cannot mark the post as claimed.
  → *Mitigation*: The UI automatically prompts copying the private link upon posting and stores the `editToken` in client `localStorage` under `my_posts`.
- **[Risk] Unmoderated Inappropriate Content**: Without moderation in Phase 1, spam or inappropriate photos could be posted.
  → *Mitigation*: Explicitly acknowledged by stakeholder; poster contact is retained internally for moderation accountability; Phase 2 introduces Gemini Flash pre-moderation pipeline (`S5 Post Status`).
- **[Risk] Image Payload & Mobile Bandwidth**: Users uploading raw 12MP smartphone photos would exhaust mobile data and memory.
  → *Mitigation*: Enforce client-side canvas downscaling (≤ 1600px max edge, JPEG quality 0.8) prior to multipart dispatch, backed by a hard 5 MB server limit.

## Migration & Test Execution Plan
1. Repository integration tests execute against the Google Cloud Firestore emulator (`gcloud beta emulators firestore start` or Maven test-managed emulator runner).
2. Deploy script `deploy.sh` executing `gcloud builds submit` and `gcloud run deploy campus-giveaway --region=us-east1`.

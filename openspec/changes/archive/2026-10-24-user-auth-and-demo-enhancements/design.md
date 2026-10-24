# Design

## Context

See `proposal.md` for motivation. Currently, the application runs on Spring Boot 3.5 with PostgreSQL / H2 persistence (and Firestore models) and Angular 20. Post creation is unauthenticated and uses an `X-Edit-Token`. This design introduces Google OAuth2/OIDC authentication, user post management, frictionless inquiries, scannable QR fliers, and a feature-flagged Gemini 2.5 Flash vision integration.

## Goals / Non-Goals

**Goals:**
- Provide seamless Google Sign-In with zero session state on Cloud Run (stateless JWT verification).
- Eliminate repetitive contact form entry when logged-in users express interest.
- Provide a unified "My Activity" view for managing authored posts and tracking requested items.
- Support instant soft-archival (`status = TAKEN`) removing items from public feeds.
- Generate client-side printable campus fliers with scannable QR codes for dorm bulletin boards.
- Guard Gemini multimodal photo auto-fill behind an administrative feature flag for budget safety.
- Provide a clear test harness for Antigravity's self-correcting agent loop demo.

**Non-Goals:**
- Restricting accounts strictly to `@rpi.edu` (kept for a future change).
- In-app chat or messaging system (uses buyer email/phone exchange).
- Payment processing.

## Data Model Changes

### 1. `posts` Entity / Collection
- `author_user_id` (`VARCHAR(255)`, indexed): Google user ID (`sub`).
- `author_email` (`VARCHAR(255)`): Verified Google email address.
- `status` (`VARCHAR(50)`): Transitions: `ACTIVE` -> `PENDING` -> `CLAIMED` -> `TAKEN` (archived). Public feed filters strictly for `ACTIVE`.

### 2. `interests` Entity / Collection
- `inquirer_user_id` (`VARCHAR(255)`, indexed): Google user ID of buyer.
- `inquirer_name` (`VARCHAR(255)`): User display name from Google.
- `inquirer_email` (`VARCHAR(255)`): Verified Google email.
- `message` (`TEXT`): Buyer's message / meeting note.

## Decisions

### 1. Stateless Google ID Token (JWT) Verification
- **Choice**: Frontend uses Google Identity Services to obtain a Google ID token (`credential`). The client transmits this in the `Authorization: Bearer <id_token>` header. Spring Boot validates the token against Google's public JWKS endpoints (`https://www.googleapis.com/oauth2/v3/certs`) via standard Spring Security / Nimbus JOSE or Google ID Token Verifier.
- **Why**: Zero session memory footprint, perfect for Cloud Run scale-to-zero, requires no extra user database table.
- **Alternatives**: Session cookies with Spring Session Redis (adds infra complexity and cost) or custom email/password (friction for students).

### 2. Client-Side Printable QR Flier
- **Choice**: Angular renders QR codes directly in the DOM using a lightweight SVG/Canvas generator (`qrcode` library) and a CSS `@media print` card stylesheet.
- **Why**: Instant zero-latency rendering, no backend image processing overhead, works offline or online.

### 3. Feature Flag for Multimodal Gemini Vision
- **Choice**: Gated by `@Value("${giveaway.ai.autofill.enabled:false}")` with an admin runtime toggle endpoint.
- **Why**: Ensures public users scanning the QR code cannot spam Gemini vision APIs, keeping Cloud Run and Vertex AI costs at ~$0 outside of the 5–10 minute live talk demo.

### 4. Self-Correcting Agentic Test Verification
- **Choice**: Integration tests (`PostAuthIntegrationTest`, `InterestAuthIntegrationTest`) assert `401 Unauthorized` for missing tokens and `403 Forbidden` for non-author edits.
- **Why**: Provides the exact "test catch and self-correct" moment for John Pole's live demonstration in Antigravity.

## Risks / Trade-offs

- **[Risk] Google Token Expiration during Post Drafting**:
  - *Mitigation*: Angular `AuthService` checks token expiry before submission and prompts silent refresh; form state is stored in reactive signals so drafts are preserved.
- **[Risk] Legacy Posts Lacking `authorUserId`**:
  - *Mitigation*: Seed posts and pre-existing demo posts default to `authorUserId = "system-seed"`. Public read endpoints continue serving them smoothly.
- **[Risk] Audience Spam on Auto-Fill**:
  - *Mitigation*: Feature flag default is `false`. When enabled for the talk, it enforces rate-limiting and requires authentication.

## Migration Plan

1. Flyway migration / JPA schema update to add `author_user_id` and `author_email` to `posts`, and `inquirer_user_id` to `interests`.
2. Seed data runner updated to assign demo user identity to pre-seeded items.
3. Deploy to Cloud Run with `AI_AUTOFILL_ENABLED=false`.

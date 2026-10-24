# Proposal: Phase 1 Open Board

## Why
Every May, graduating and moving-out RPI students discard usable furniture, appliances, and textbooks, while incoming and junior students repurchase those same items at retail cost. Phase 1 delivers an open, accountless mobile web board enabling departing students to post items immediately and peers to browse, filter, and express interest friction-free.

## What Changes
- Introduce public item posting without user accounts, requiring poster name, validated contact info (email or E.164 phone), "legal, safe, and mine to give" confirmation, and a single photo.
- Support items with title (≤ 100 chars), description (≤ 1000 chars), category, condition, price (USD or free), pickup location, and availability deadline.
- Add immediate public publishing of posted items without pre-moderation gates.
- Deliver mobile-first feed with full-text search, category chip filters, and "Free only" toggle for active items.
- Enable visitors to express interest (name, contact, optional message) on items.
- Protect poster privacy: poster contact info is retained only for accountability/moderation and is never returned by any endpoint.
- Support single-photo upload with client-side downscaling (≤ 1600px) and validation (JPEG/PNG/WebP, ≤ 5 MB).
- Provide private management token/link per item so posters can track incoming inquiries, mark items as claimed, or delete the post.

## Capabilities

### New Capabilities
- `posts`: Create, list, search, filter, retrieve details, mark claimed, and delete via private edit token.
- `interests`: Express interest in an item, calculate interest counts, and present interest summaries to the poster.
- `media`: Validate, store, and serve item photos using Cloud Storage.

### Modified Capabilities
*(None: initial baseline greenfield capabilities; no existing requirements are modified.)*

## Out of Scope
- User accounts, passwords, SSO (CAS/Shibboleth), and persistent authentication profiles.
- Automated AI or human pre-publication content moderation (planned for Phase 2).
- Multi-photo galleries (strictly one photo per item in Phase 1).
- In-app live chat and financial transaction processing (pickups and payments happen off-platform).

## Impact
- **Backend**: New Spring Boot 3.5 package `edu.rpi.giveaway` with REST controllers, Firestore repositories, and GCS integrations.
- **Frontend**: New Angular 20 mobile-first application (`/frontend`) following `design/DESIGN.md` tokens.
- **Deployment**: Unified container build packaging Angular static assets inside Spring Boot for Google Cloud Run (`us-east1`).

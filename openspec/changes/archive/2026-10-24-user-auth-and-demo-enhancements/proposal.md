# Proposal

## Why

Currently, posts and inquiries are anonymous and post management relies on an `X-Edit-Token` stored in browser `localStorage`. To transition from an open demo board to an authenticated user experience while demonstrating enterprise AI agent capabilities, we must introduce Google Sign-In, a user dashboard for tracking personal posts and requested items, streamlined 1-field inquiries, an in-browser printable dorm QR flier, and a feature-flagged Gemini multimodal photo auto-fill.

## What Changes

- Introduce universal **Google Sign-In (OAuth2/OIDC)** and profile navigation pill/avatar.
- **BREAKING**: Require Google authentication for `POST /api/posts` and `POST /api/posts/{id}/interests`.
- Bind created posts and inquiries to the authenticated user's ID (`sub`) and verified email.
- Introduce a **"My Activity"** dashboard displaying "My Posts" (with inquiries received and one-tap archive/delete) and "Items I Requested".
- Add a `TAKEN` / `ARCHIVED` status transition that immediately hides items from public feeds while preserving user history.
- Streamline the "I'm Interested" modal to a single comment/pickup note field for logged-in users.
- Add an in-browser **Printable Campus Flier & QR Code** generator for physical dorm boards.
- Add a feature-flagged **"✨ Auto-Fill with AI"** button powered by Gemini 2.5 Flash vision.
- Introduce integration test suites to showcase Antigravity's self-correcting agentic loop.

## Out of Scope

- Restricting authentication strictly to `@rpi.edu` domains (kept in backlog for a future phase).
- In-app real-time chat between buyer and seller (relies on email/phone contact exchange).
- Payment processing or in-app escrow.

## Capabilities

### New Capabilities
- `auth-and-profile`: Google Sign-In authentication, reactive Angular user signals, top/bottom profile badge, and backend Bearer token verification.
- `interests`: Authenticated interest submission auto-filling verified buyer identity with a single message field, bound to `inquirerUserId`.
- `user-dashboard`: "My Activity" screen for managing personal posts, viewing inquiries, and tracking claimed/requested items.
- `ai-autofill`: Multimodal photo analysis via Gemini 2.5 Flash to suggest title, category, condition, and price, protected by a demo feature flag.

### Modified Capabilities
- `posts`:
  - **MODIFIED**: `Relational Post Persistence Lifecycle` now binds posts to `authorUserId` and requires Google authentication for creation.
  - **MODIFIED**: `Cascading Deletion of Post Inquiries` now authorizes post deletion and status changes via authenticated user ownership instead of anonymous `X-Edit-Token`.
  - **ADDED**: Printable Campus Flier and scannable item QR code generation.

## Impact

- **Backend**: Spring Security OAuth2 resource server / Google token verification filter; updated `PostEntity`, `InterestEntity`, and corresponding DTOs.
- **Frontend**: Google Identity Services integration, `AuthService` signals, updated navbar/bottom-nav, new `/my-activity` route, updated interest modal, printable QR modal.
- **Infrastructure**: Secret / environment variable for `GOOGLE_CLIENT_ID` and feature flag `AI_AUTOFILL_ENABLED`.

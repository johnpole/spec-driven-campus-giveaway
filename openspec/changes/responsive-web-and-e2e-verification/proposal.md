# Proposal: Responsive Web Layout, Media Contract Fix & Playwright E2E Verification

## Why
When testing locally, two major issues surfaced: the photo upload returned a contract field mismatch (`photoUrl` vs `url`) causing a false "photo required" error, and the desktop view was rigidly locked into a 480px mobile column where overlapping fixed bars blocked users from reaching lower form fields. We need progressive responsive design across desktop and mobile, full form accessibility, local sample media assets, and an automated Playwright E2E verification harness with visual screenshot proof.

## What Changes
- **Media Upload Contract**: Update `MediaUploadResponse` to return both `photoUrl` and `url` to ensure seamless interop with frontend consumers.
- **Responsive Layout**: Enable progressive desktop/tablet enhancement (`min-width: 768px`) expanding `--app-max-width` to a comfortable container (up to 1140px on feed, 680px on forms/details) with multi-column grids, while preserving the 360px–430px mobile ergonomics.
- **Post Item Form Layout**: Remove `<app-bottom-nav>` from `/post-item` and cleanly anchor the sticky submit button to `bottom: 0`, ensuring all form fields (description, location, contact, legal checkbox) remain fully visible and scrollable.
- **Seed Image Assets**: Provide realistic campus giveaway placeholder images in `backend/uploads/` so all 12 pre-seeded items display genuine photos without 404s.
- **Playwright E2E Test Suite & Skill**: Create `.agents/skills/playwright-e2e-verifier/` with a comprehensive Playwright automation script verifying all 6 user flows and exporting screenshots to `artifacts/screenshots/`.

## Capabilities

### New Capabilities
- `e2e-verification`: Automated Playwright test suite verifying home feed, search/filter, item detail, interest modal, post creation, and post management with screenshot artifacts.

### Modified Capabilities
- `media`: MODIFIED requirement to return dual-property JSON (`photoUrl` and `url`) on successful photo upload.
- `posts`: MODIFIED requirement for responsive desktop layout alongside mobile viewports and unblocked form inputs on `/post-item`.

## Out of Scope
- User authentication or persistent login sessions.
- Multi-image gallery uploads (retaining single photo limit for Phase 1).
- Backend database migration away from Cloud Firestore.

## Impact
- Frontend: `styles.scss`, `post-item.component.html`, `post-item.component.scss`, `post-item.component.ts`, `media.model.ts`.
- Backend: `MediaUploadResponse.java`.
- Assets & Tooling: `backend/uploads/`, Playwright configuration, custom verification skill.

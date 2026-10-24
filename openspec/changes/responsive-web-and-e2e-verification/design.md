# Design: Responsive Web Adaptation, Media Contract Fix & Playwright E2E Verification

## Context
See `proposal.md` for background and motivation. The system currently forces all viewports into a narrow 480px width, introduces a double-fixed nav on the post creation screen that hides form inputs, and has a field name mismatch (`photoUrl` vs `url`) between backend and frontend.

## Goals / Non-Goals

**Goals:**
- Fix `MediaUploadResponse` so both `photoUrl` and `url` are returned, eliminating the false "photo required" error.
- Remove `<app-bottom-nav>` from `/post-item`, ensuring all form fields (description, location, contact, legal checkbox) are completely reachable and scrollable above the bottom submit bar.
- Implement progressive responsive enhancement (`min-width: 768px` and `min-width: 1024px`) allowing the web application to scale cleanly up to 1080px on desktop while maintaining mobile-first touch ergonomics on mobile.
- Provide local mock image assets in `backend/uploads/` so all 12 pre-seeded items display genuine photos without 404s.
- Create an automated Playwright E2E verification test suite and custom agent skill (`.agents/skills/playwright-e2e-verifier/`) capturing visual screenshot artifacts.

**Non-Goals:**
- Implementing desktop-only standalone multi-panel sidebars.
- Migrating persistence away from Cloud Firestore.

## Data Model Changes
No Firestore collection or schema alterations are required. The Java backend `MediaUploadResponse` record is updated to provide both `photoUrl` and `url`:
```java
public record MediaUploadResponse(String photoUrl, String url) {
    public MediaUploadResponse(String photoUrl) {
        this(photoUrl, photoUrl);
    }
}
```

## Decisions

### Decision 1: Dual-Property Media Upload Response
- **Choice**: Return `{ "photoUrl": "...", "url": "..." }` from `POST /api/media/upload`.
- **Rationale**: Guarantees backwards and forwards compatibility without risking any consumers breaking.
- **Alternatives Considered**: Changing either frontend or backend only; dual-property is strictly safer and more resilient.

### Decision 2: Stepped Post Creation Screen Isolation
- **Choice**: Remove `<app-bottom-nav>` from `post-item.component.html` and anchor `.sticky-submit-bar` directly to `bottom: 0` with `padding-bottom: 120px` on `.post-page`.
- **Rationale**: Directly matches Stitch design S3 where the creation flow is an isolated task with a top `✕` close button. Eliminates the 140px overlapping footer that covered form inputs.

### Decision 3: Progressive Breakpoint Architecture
- **Choice**: Maintain `--app-max-width: 480px` on mobile (< 768px), and scale up dynamically at `@media (min-width: 768px)` to `--app-max-width: 1080px` with 3 to 4 column card grids for the feed, and centered 640px max-width for post forms and item details.
- **Rationale**: Fulfills both mobile-first QR scanner ergonomics and clean, expansive desktop web viewing without awkward letterboxing.

### Decision 4: Playwright E2E Verification Suite and Agent Skill
- **Choice**: Build a dedicated Playwright script in `.agents/skills/playwright-e2e-verifier/` that runs tests across all core scenarios and saves visual screenshot artifacts into `artifacts/screenshots/`.
- **Rationale**: Gives instantaneous, repeatable visual proof of complete system functionality before any cloud deployment.

## Risks / Trade-offs
- [Risk] Larger desktop viewports might expose stretched card layouts.
  → Mitigation: Card image aspect ratio is locked at 1:1, card containers have fixed maximum widths, and CSS grid uses `repeat(auto-fill, minmax(220px, 1fr))`.
- [Risk] Playwright requires local browser binaries.
  → Mitigation: Use Playwright with Chromium (or pre-installed Chrome) and provide clear execution script with automated pre-flight checks.

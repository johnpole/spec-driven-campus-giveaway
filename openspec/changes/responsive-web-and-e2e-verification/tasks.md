# Tasks

## 1. Backend Contract and Seed Asset Setup

- [x] 1.1 Update `MediaUploadResponse` in backend to return both `photoUrl` and `url` and verify with `./mvnw test -Dtest=MediaControllerTest`
- [x] 1.2 Generate 12 clean campus item placeholder image files in `backend/uploads/` matching seed filenames and verify HTTP 200 response via `curl -s -I http://localhost:8080/api/media/seed-desk.jpg`

## 2. Frontend Contract and Post Item Layout Fix

- [x] 2.1 Update `MediaUploadResponse` model and `PostItemComponent` in frontend to support `photoUrl` and `url` interchangeably and verify with `npm test -- --watch=false --include=src/app/features/post-item/post-item.component.spec.ts`
- [x] 2.2 Remove `<app-bottom-nav>` from `post-item.component.html`, position `.sticky-submit-bar` at `bottom: 0`, add `padding-bottom: 120px` to `.post-page`, and verify form field visibility and scrollability

## 3. Progressive Responsive Web Layout

- [x] 3.1 Update layout tokens in `styles.scss` with `@media (min-width: 768px)` and `@media (min-width: 1024px)` to scale `--app-max-width` up to 1080px on desktop while maintaining 360-430px mobile base
- [x] 3.2 Update `feed.component.scss` and `item-detail.component.scss` for desktop multi-column card grids and centered detail layouts, and verify with `npm test -- --watch=false`

## 4. Playwright E2E Suite and Agent Skill

- [x] 4.1 Install and configure Playwright in the project and write E2E test scenarios covering Feed, Filter, Detail, Interest Modal, and Post Creation
- [x] 4.2 Create `.agents/skills/playwright-e2e-verifier/SKILL.md` with automated test runner, execute full suite, and verify screenshots saved to `artifacts/screenshots/`


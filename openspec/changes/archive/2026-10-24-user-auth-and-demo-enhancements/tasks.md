# Tasks

## 1. Backend Security & Data Model

- [x] 1.1 Implement Google ID token verification security filter and verify with `TokenVerificationTest` proving valid tokens populate authentication context and invalid tokens return 401
- [x] 1.2 Update `PostEntity` and `PostDocument` with `authorUserId` and `authorEmail`, require authentication on `POST /api/posts`, and verify with `PostAuthIntegrationTest`
- [x] 1.3 Implement `TAKEN` / `ARCHIVED` status transition and author-only post deletion in `PostService`, and verify with `PostStatusAuthTest`
- [x] 1.4 Update `InterestEntity` with `inquirerUserId`, `inquirerName`, and `inquirerEmail`, streamline `POST /api/posts/{id}/interests` to accept single message note, and verify with `InterestAuthIntegrationTest`
- [x] 1.5 Implement `UserController` providing `GET /api/users/me/posts` and `GET /api/users/me/interests`, and verify with `UserActivityControllerTest`
- [x] 1.6 Implement feature-flagged Gemini 2.5 Flash multimodal vision auto-fill service (`/api/media/autofill`), and verify with `MediaAutofillTest`

## 2. Frontend Identity, Activity Dashboard & Demo Features

- [x] 2.1 Implement `AuthService` with Google Identity Services SDK, JWT decoding, and reactive Angular signals, and verify with `auth.service.spec.ts`
- [x] 2.2 Add user profile badge and Google Sign-In button to header and sticky bottom nav, and verify with `header.component.spec.ts`
- [x] 2.3 Create `MyActivityComponent` with "My Posts" (inquiries received, mark as taken, delete) and "Items I Requested" tabs, and verify with `my-activity.component.spec.ts`
- [x] 2.4 Update Item Detail "I'm Interested" modal to auto-populate user contact and prompt only for pickup message, and verify with `interest-modal.component.spec.ts`
- [x] 2.5 Create Printable Campus Flier and scannable QR modal on Item Detail, and verify with `flier-modal.component.spec.ts`
- [x] 2.6 Add "✨ Auto-Fill with AI" camera trigger on Post Form gated by feature flag, and verify with `post-create.component.spec.ts`

## 3. Full-Stack Verification & Agentic Loop Validation

- [x] 3.1 Run complete backend JUnit test suite (`mvn clean test`) to confirm zero regressions and verify the self-correcting agent loop
- [x] 3.2 Run frontend Jasmine test suite (`npm test -- --watch=false`) to verify all Angular components and services pass
- [x] 3.3 Verify multi-stage container build (`docker build -t campus-giveaway:demo .`) to ensure production Cloud Run single-container packaging succeeds

## Workflow follow-up

- Archive the change after implementation and test verification.
- Deploy to Google Cloud Run via `./deploy.sh`.

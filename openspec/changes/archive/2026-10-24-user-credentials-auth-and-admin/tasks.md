# Tasks

## 1. Backend Persistence & Password Encoding

- [x] 1.1 Implement BCrypt password encoder bean and verify with `PasswordEncoderTest`
- [x] 1.2 Implement `UserEntity`, `UserDocument`, `UserRepository`, `JpaUserRepository`, and `FirestoreUserRepository` and verify persistence tests pass
- [x] 1.3 Add `isAdmin` check and claims handling in `UserPrincipal` and `GoogleTokenVerifierService` to designate `admin@campus-giveaway.test` as ADMIN, verified with unit tests

## 2. Backend Credential Auth & Admin Endpoints

- [x] 2.1 Implement `AuthService` and `AuthController` with `POST /api/auth/register` (first name, last name, email, phone, simple password) and `POST /api/auth/login` (username/password), verified with MockMvc tests
- [x] 2.2 Update `PostService.deletePost` and `PostService.updatePostStatus` to allow admin bypass for `admin@campus-giveaway.test`, verified with `PostAdminIntegrationTest`
- [x] 2.3 Restrict `MediaController.toggleAutofill` to authenticated administrators, verified with `MediaAutofillAdminTest`

## 3. Frontend Simple Auth Modal & Google Integration

- [x] 3.1 Create `AuthModalComponent` with clean tabbed interface for "Sign In" (username/password, Google button at bottom) and "Register" (first name, last name, email, phone, simple password, Google button at bottom)
- [x] 3.2 Update `HeaderComponent` with prominent top "Sign In" button opening the modal, and render the `ADMIN` badge when logged in as `admin@campus-giveaway.test`
- [x] 3.3 Add admin moderation controls (Delete Item, Mark Taken/Available, Toggle Gemini AI) to `ItemDetailComponent` when user is admin
- [x] 3.4 Verify complete frontend suite with `npm test -- --watch=false` and full Playwright E2E suite

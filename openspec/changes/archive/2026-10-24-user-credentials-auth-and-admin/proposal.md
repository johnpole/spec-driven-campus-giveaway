# Proposal: User Credentials Auth, Simple Sign-In/Register Modal, and Super Admin Access

## Why
Users and conference evaluators need a zero-friction, self-contained way to sign in and register using simple credentials (first name, last name, email/phone, simple password) alongside the existing Google Sign-In. Additionally, platform administrators (specifically `admin@campus-giveaway.test`) require universal administrative privileges to moderate listings, update post statuses, and govern AI feature flags.

## What Changes
- **Simple Sign-In & Registration UI**: A clean, accessible modal triggered by a prominent top "Sign In" button in the header. The modal provides a segmented view: "Sign In" (username/password or Google Sign-In) and "Register" (first name, last name, email, phone number, simple password, or Google Sign-In).
- **Backend Credential Authentication**: New REST endpoints `POST /api/auth/register` and `POST /api/auth/login` supporting email/phone username lookup and BCrypt password hashing with demo-friendly validation (minimum 4 characters).
- **Dual Auth Compatibility**: Existing Google Identity Services (GIS) ID tokens and newly issued credential JWT tokens are both verified seamlessly by the backend filter and frontend `AuthService`.
- **Super Admin Privileges (`admin@campus-giveaway.test`)**:
  - Automatically assigned `ADMIN` role upon sign-in/registration.
  - Universal moderation: Can delete any item post (`DELETE /api/posts/{id}`) and update status (`PATCH /api/posts/{id}/status`) regardless of original author.
  - Exclusive access to toggle the Gemini 2.5 Multimodal AI autofill feature flag (`POST /api/media/autofill/toggle`).
  - Prominent `ADMIN` badge in header and moderation controls on item detail views.

## Capabilities

### Modified Capabilities
- `auth-and-profile`: Adds credential-based registration and sign-in endpoints, password hashing, and user credential persistence alongside Google OAuth; adds admin role identification for `admin@campus-giveaway.test`.
- `posts`: Adds administrative bypass allowing admin users to moderate (delete and update status of) any item post.
- `ai-autofill`: Restricts Gemini multimodal autofill feature flag toggling to authorized administrators.

## Out of Scope
- Complex password complexity rules (e.g. symbol/uppercase mandates) to ensure simple conference live-demo usability.
- Multi-factor authentication (MFA / SMS OTP).
- Self-service password reset emails via SMTP.

## Impact
- **Backend**: New `AuthController`, `UserService`, `UserCredentialsEntity` / `UserDocument`, `PasswordEncoder` (BCrypt), and updated `UserPrincipal` and `GoogleTokenVerifierService`.
- **Frontend**: New `AuthModalComponent` (Sign In / Register tabs), updated `HeaderComponent` (top "Sign In" button and Admin pill), and updated `ItemDetailComponent` (Admin action bar).

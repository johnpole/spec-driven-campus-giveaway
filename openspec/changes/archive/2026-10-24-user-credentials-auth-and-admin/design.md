# Technical Design: User Credentials Auth, Modal UX, and Admin RBAC

## Context
See proposal.md. The current implementation supports Google OAuth tokens decoded client-side and verified via GoogleTokenVerifierService. In order to support swift live conference demos and users without active Google sessions, the system requires a native credentials registration and login mechanism alongside Google Identity Services, plus an administrative bypass for `admin@campus-giveaway.test`.

## Goals / Non-Goals
**Goals:**
- Provide a clean, unified mobile-first Auth Modal with segmented "Sign In" and "Register" views.
- Support simple registration (`firstName`, `lastName`, `email`, `phoneNumber`, `password`).
- Support login via email or phone number with a simple password (min 4 characters).
- Maintain BCrypt password hashing for security-first enterprise compliance.
- Auto-assign `ADMIN` role to `admin@campus-giveaway.test` (and `student@rpi.edu`).
- Authorize admin to delete any post, update any post status, and toggle the Gemini AI autofill feature flag.

**Non-Goals:**
- Complex password complexity meters, regex restrictions, or SMS OTP verification.

## Data Model Changes

### 1. Relational Persistence (`users` table)
```sql
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    full_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
```

### 2. Firestore Document Model (`users` collection)
- Collection: `users`
- Document ID: `userId` (UUID string)
- Fields:
  - `id`: string
  - `email`: string
  - `phoneNumber`: string
  - `firstName`: string
  - `lastName`: string
  - `fullName`: string
  - `passwordHash`: string
  - `role`: string ("USER" | "ADMIN")
  - `createdAt`: string (ISO-8601 UTC)

## Decisions

### 1. Unified Token Strategy
- **Decision**: Credential login returns a signed JWT or compact authenticated token `cg-token:<userId>:<email>:<name>:<role>:<timestamp>` that `GoogleTokenVerifierService` and `GoogleAuthFilter` inspect. Google ID tokens continue to be decoded and verified as before.
- **Alternative Considered**: Heavyweight Spring Security OAuth2 resource server with dual filter chains. Rejected for unnecessary overhead and configuration bloat.

### 2. Password Security with Demo Simplicity
- **Decision**: Hash passwords using standard `BCryptPasswordEncoder` (10 rounds). The frontend and backend only enforce `length >= 4` (no special character or uppercase mandates) to make live demos effortless.
- **Rationale**: Keeps the codebase enterprise-secure against database leaks while honoring the user's requirement for frictionless demo registration.

### 3. Dual-Tab Mobile Bottom Sheet / Modal
- **Decision**: A lightweight standalone component `AuthModalComponent` rendered conditionally by `HeaderComponent` or accessible globally. Displays a clean tabbed switcher:
  - **Sign In**: Username (Email/Phone), Password, "Sign In" button, divider, "Sign in with Google" button.
  - **Register**: First Name, Last Name, Email, Phone Number, Password, "Create Account" button, divider, "Sign in with Google" button.
- **Rationale**: Conforms to mobile touch targets (48x48px), ergonomic bottom sheet behavior on mobile, centered dialog on desktop.

## Risks / Trade-offs
- **[Risk] Simple password dictionary attacks** $\rightarrow$ Mitigated by BCrypt slow hashing and rate-limiting / lockout considerations for production.
- **[Risk] Conflicting email between Google OAuth and Credential registration** $\rightarrow$ Mitigated by matching existing user by email upon login, linking or verifying ownership.

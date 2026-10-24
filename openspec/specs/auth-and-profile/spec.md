# auth-and-profile Specification

## Purpose
Provides secure Google Sign-In authentication, client-side session state management, profile navigation indicators, and cryptographically verified Bearer token propagation.

## Requirements

### Requirement: Google Identity Sign-In and Session State
The system SHALL authenticate users using standard Google Identity Services (OIDC JWT) and maintain the authenticated user profile in reactive client-side signals.

#### Scenario: User successfully signs in with Google
- **WHEN** a user initiates Google Sign-In and completes authentication
- **THEN** the client receives a verified Google ID token, populates user profile signals (name, email, avatar), and updates the UI header and bottom navigation with the profile avatar

#### Scenario: User signs out of application
- **WHEN** an authenticated user clicks the "Sign Out" option in the profile menu
- **THEN** client authentication signals are cleared, cached tokens are removed, and the UI reverts to the unauthenticated visitor state

### Requirement: Backend Cryptographic Token Verification
The backend service SHALL cryptographically verify incoming Google Bearer tokens on protected REST endpoints, rejecting expired or forged tokens.

#### Scenario: Valid Google Bearer token accepted
- **WHEN** a client sends a protected API request with a valid Google ID token in the Authorization header
- **THEN** the backend verifies token signature and expiration against Google public JWKS keys and binds the user identity to the security context

#### Scenario: Expired or forged token rejected
- **WHEN** a client sends an API request with an expired or invalid token
- **THEN** the backend rejects the request with HTTP 401 Unauthorized and structured error details

### Requirement: User Registration and Credential Authentication
The system SHALL provide simple credential-based user registration and sign-in alongside Google Identity Services, securely hashing passwords using BCrypt and supporting email or phone number authentication.

#### Scenario: User registers with credentials
- **WHEN** a user submits POST /api/auth/register with valid first name, last name, email, phone number, and password (minimum 4 characters)
- **THEN** the system hashes the password, creates the user account, and returns HTTP 201 with an authenticated session token and user profile

#### Scenario: Registration rejected on duplicate email
- **WHEN** a user attempts to register with an email address that is already registered
- **THEN** the system rejects registration with HTTP 409 Conflict and an error message indicating the email is taken

#### Scenario: User signs in with username and password
- **WHEN** a user submits POST /api/auth/login with valid username (email or phone) and correct password
- **THEN** the system validates credentials and returns HTTP 200 with an authenticated session token and user profile

#### Scenario: Sign in rejected on invalid password
- **WHEN** a user submits POST /api/auth/login with incorrect credentials
- **THEN** the system rejects the request with HTTP 401 Unauthorized and an invalid credentials message

### Requirement: Admin Role Assignment and Indicators
The system SHALL grant the ADMIN role only to (a) the account seeded at startup from `ADMIN_EMAIL`/`ADMIN_PASSWORD`, or (b) a verified Google account listed in `ADMIN_EMAILS`. Admin credentials SHALL come from the environment (`.env` locally, Secret Manager in the cloud), never from source code. Self-registration SHALL never grant ADMIN. Admins see an ADMIN badge in the header.

#### Scenario: Seeded admin signs in
- **WHEN** the account created from `ADMIN_EMAIL` signs in with `ADMIN_PASSWORD`
- **THEN** the signed session token carries `admin: true` and the header displays an ADMIN badge

#### Scenario: Registering with an admin email does not grant admin
- **WHEN** someone self-registers using an email listed in `ADMIN_EMAILS`
- **THEN** the account is created with `isAdmin: false`

#### Scenario: Forged tokens cannot claim admin
- **WHEN** a request carries an unsigned, tampered, or test token claiming admin
- **THEN** the system rejects it with HTTP 401 Unauthorized

#### Scenario: Non-admin profile reflects standard permissions
- **WHEN** any other student account signs in
- **THEN** the profile reflects `isAdmin: false` with standard student privileges

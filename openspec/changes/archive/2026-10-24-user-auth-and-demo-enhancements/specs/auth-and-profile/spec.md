# Spec Delta

## Purpose

Provides secure Google Sign-In authentication, client-side session state management, profile navigation indicators, and cryptographically verified Bearer token propagation.

## ADDED Requirements

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

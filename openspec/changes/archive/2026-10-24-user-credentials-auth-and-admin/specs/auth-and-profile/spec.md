# Spec Delta: auth-and-profile

## ADDED Requirements

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

### Requirement: Super Admin Role Assignment and Indicators
The system SHALL automatically designate `admin@campus-giveaway.test` as an administrator upon authentication and display an ADMIN status badge in the user interface.

#### Scenario: Super admin profile includes admin flag
- **WHEN** `admin@campus-giveaway.test` signs in via Google or credentials
- **THEN** the security context and client user profile record `isAdmin: true` and the header displays an ADMIN badge

#### Scenario: Non-admin profile reflects standard permissions
- **WHEN** any other student account signs in
- **THEN** the profile reflects `isAdmin: false` with standard student privileges

# Spec Delta

## MODIFIED Requirements

### Requirement: Relational Post Persistence Lifecycle
The system SHALL persist all posted items to a relational SQL database, binding created posts to the authenticated author's Google identity, and guaranteeing that items remain accessible across application restarts until expiration, archival, or deletion.

#### Scenario: Created post persists across service restarts
- **WHEN** an authenticated user creates a post via POST /api/posts and the backend service is restarted
- **THEN** GET /api/posts and GET /api/posts/{id} continue to return the created post with all original attributes preserved

#### Scenario: Unauthenticated post creation rejected
- **WHEN** a client attempts to create a post via POST /api/posts without an Authorization Bearer header or with an invalid token
- **THEN** the system rejects the request with HTTP 401 Unauthorized

#### Scenario: Database connection failure handling
- **WHEN** the backend database connection is interrupted during a query or write operation
- **THEN** the system returns HTTP 500 Internal Server Error with a structured error response and logs the failure without crashing

#### Scenario: Production Cloud SQL connectivity
- **WHEN** the application boots under the `prod` profile in Google Cloud Run
- **THEN** database migrations and queries execute successfully against Google Cloud SQL PostgreSQL 17 using credentials loaded from Secret Manager

### Requirement: Cascading Deletion of Post Inquiries
The system SHALL ensure that when an item post is deleted by its authenticated author, all associated interest inquiries are automatically deleted via relational cascade, and unauthenticated or unauthorized deletion attempts are rejected.

#### Scenario: Deleting post cascades and removes all child inquiries
- **WHEN** the authenticated author sends DELETE /api/posts/{id} with a valid Bearer token
- **THEN** the post record and all associated interest inquiries in the database are permanently removed, and subsequent queries return HTTP 404

#### Scenario: Deletion with invalid token rejected
- **WHEN** a client sends DELETE /api/posts/{id} with an invalid or missing Authorization Bearer token
- **THEN** the system rejects the deletion with HTTP 401 Unauthorized and all records remain untouched in the database

#### Scenario: Non-author deletion rejected
- **WHEN** an authenticated user sends DELETE /api/posts/{id} for a post authored by another user
- **THEN** the system rejects the deletion with HTTP 403 Forbidden and records remain untouched

#### Scenario: Author updates post status to TAKEN archives item
- **WHEN** the authenticated author sends PATCH /api/posts/{id}/status with status "TAKEN"
- **THEN** the item status is updated to TAKEN, the item is immediately excluded from the public feed GET /api/posts, and remains accessible in the author's dashboard

## ADDED Requirements

### Requirement: Printable Campus Flier and QR Code Generation
The system SHALL generate an in-browser printable dorm bulletin flier and scannable QR code for any item post, linking directly to the item's mobile web detail page.

#### Scenario: Author or visitor views printable flier
- **WHEN** a user triggers the "Print Campus Flier" action on an item detail view
- **THEN** the system displays a printable card containing item photo, title, price, location, and a scannable QR code resolving to the item URL

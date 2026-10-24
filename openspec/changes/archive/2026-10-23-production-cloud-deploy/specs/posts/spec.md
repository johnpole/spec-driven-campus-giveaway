# Spec Delta: posts

## MODIFIED Requirements

### Requirement: Relational Post Persistence Lifecycle
The system SHALL persist all posted items to a relational SQL database, guaranteeing that items remain accessible across application restarts and service redeployments until expiration or deletion.

#### Scenario: Created post persists across service restarts
- **WHEN** a client successfully creates a post via POST /api/posts and the backend service is restarted
- **THEN** GET /api/posts and GET /api/posts/{id} continue to return the created post with all original attributes preserved

#### Scenario: Database connection failure handling
- **WHEN** the backend database connection is interrupted during a query or write operation
- **THEN** the system returns HTTP 500 Internal Server Error with a structured error response and logs the failure without crashing

#### Scenario: Production Cloud SQL connectivity
- **WHEN** the application boots under the `prod` profile in Google Cloud Run
- **THEN** database migrations and queries execute successfully against Google Cloud SQL PostgreSQL 17 using credentials loaded from Secret Manager

# Spec Delta: posts

## Purpose

Defines durable relational SQL persistence for campus giveaway posts, data survival across service restarts, and cascading deletion of associated interest inquiries.

## ADDED Requirements

### Requirement: Relational Post Persistence Lifecycle
The system SHALL persist all posted items to a relational SQL database, guaranteeing that items remain accessible across application restarts and service redeployments until expiration or deletion.

#### Scenario: Created post persists across service restarts
- **WHEN** a client successfully creates a post via POST /api/posts and the backend service is restarted
- **THEN** GET /api/posts and GET /api/posts/{id} continue to return the created post with all original attributes preserved

#### Scenario: Database connection failure handling
- **WHEN** the backend database connection is interrupted during a query or write operation
- **THEN** the system returns HTTP 500 Internal Server Error with a structured error response and logs the failure without crashing

### Requirement: Cascading Deletion of Post Inquiries
The system SHALL ensure that when an item post is deleted using a valid private edit token, all associated interest inquiries are automatically deleted via relational cascade.

#### Scenario: Deleting post cascades and removes all child inquiries
- **WHEN** client sends DELETE /api/posts/{id} with a valid X-Edit-Token header
- **THEN** the post record and all associated interest inquiries in the database are permanently removed, and subsequent queries return HTTP 404

#### Scenario: Deletion with invalid token rejected
- **WHEN** client sends DELETE /api/posts/{id} with an invalid or missing X-Edit-Token header
- **THEN** the system rejects the deletion with HTTP 403 Forbidden and all records remain untouched in the database

### Requirement: Environment-Scoped Seed Data Execution
The system SHALL execute demo seed data initialization strictly in development and local environments, and SHALL NOT initialize demo seed records when operating in the production environment.

#### Scenario: Seed data disabled in production
- **WHEN** the backend starts up with the `prod` profile active or `giveaway.seed.enabled=false`
- **THEN** the seed data runner is not executed and no automated mock or demo posts are inserted into the database

#### Scenario: Seed data initialization in local dev
- **WHEN** the backend starts up with the default or local development profile and the post repository contains zero records
- **THEN** 12 starter posts are seeded into the database for immediate development testing

# Proposal

## Why

Currently, local development relies on volatile in-memory storage that wipes all created listings upon application restart. Concurrently, mobile portrait photos uploaded by students are cropped and appear unnaturally zoomed-in due to rigid `object-fit: cover` styling. Transitioning to a relational SQL backend provides durable local persistence, production-ready Cloud SQL support, and uncropped, responsive photo presentation.

## What Changes

- **Relational SQL Persistence**: Transition database layer from NoSQL Firestore/in-memory fallback to Spring Data JPA with relational schemas (`posts` and `interests` tables with foreign keys and cascading delete).
- **Local Docker Database & Persistent Storage**: Provide a `docker-compose.yml` to spin up a local PostgreSQL 17 container with volume persistence (`docker compose up -d`) matching production Cloud SQL, alongside an embedded H2 fallback for zero-dependency unit tests.
- **Production Cloud SQL Support**: Configure production profile with Cloud SQL PostgreSQL socket factory for Cloud Run deployment.
- **Uncropped Media Presentation**: Refactor detail and card image displays from aggressive `object-fit: cover` to `object-fit: contain` with subtle neutral framing so full vertical portrait photos remain 100% visible.

## Capabilities

### Modified Capabilities

- `posts`: MODIFIED requirement for item persistence lifecycle and cascading deletion of associated inquiries across service restarts.
- `media`: MODIFIED requirement for item photo display ensuring portrait and landscape photos render completely without cropped edges.

## Out of Scope

- Multi-image gallery uploads per post (remains 1 primary photo).
- User authentication accounts (remains anonymous posting with private edit token).
- Live provisioning of paid Google Cloud SQL instances prior to local verification.

## Impact

- **Backend**: Adds `spring-boot-starter-data-jpa`, `postgresql`, and `h2` dependencies. Replaces `FirestorePostRepository` with JPA implementations while preserving `PostRepository` interface contracts.
- **Frontend**: Modifies image presentation styles in `item-detail.component.scss` and `item-card.component.ts`.
- **Database**: Adds relational DDL schemas with indexes on status, expiration, and foreign keys.

# Design

## Context

The backend currently isolates persistence behind `PostRepository` and `InterestRepository` interfaces. In local mode without Google Cloud credentials, the implementation falls back to an in-memory `ConcurrentHashMap`, causing data loss whenever the Spring Boot server restarts. Concurrently, mobile cameras capture 3:4/9:16 portrait images that get clipped by CSS `object-fit: cover`.

See `proposal.md` for overall motivation.

## Goals / Non-Goals

**Goals:**
- Implement durable relational SQL persistence using Spring Data JPA (`PostEntity`, `InterestEntity`).
- Configure a local file-persisted database (`./data/campusgiveaway.mv.db`) so created listings, claims, and inquiries survive local restarts without requiring external database servers or Docker.
- Provide production-ready configuration for Google Cloud SQL (PostgreSQL) connected via Cloud Run Unix domain sockets.
- Update frontend image presentation to use `object-fit: contain` on a neutral brand canvas (`#F4F1EA`), ensuring 100% of the item photo is visible.

**Non-Goals:**
- Changing existing REST API request/response formats or Angular services.
- Multi-image photo carousel galleries.
- Rewriting Cloud Storage / local disk photo storage mechanisms.

## Decisions

### Decision 1: Spring Data JPA with Local Docker PostgreSQL and Embedded H2 Fallback
- **Choice**: Add `spring-boot-starter-data-jpa`, `org.postgresql:postgresql`, and `com.h2database:h2`.
- **Local PostgreSQL via Docker**:
  - Provide a dedicated `docker-compose.yml` defining a `postgres:17` container (`campusgiveaway-db`) with a named persistent volume (`giveaway-pgdata`) mapped to port `5433` (or configurable via `POSTGRES_PORT` to avoid conflict with existing local databases on 5432).
  - Running `docker compose up -d` boots a true PostgreSQL 17 instance matching production Cloud SQL.
- **Embedded H2 Mode (`default-local`)**:
  - If Docker is not running, the application can smoothly run against `jdbc:h2:file:./data/campusgiveaway;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`, persisting data to disk without external services.
- **Production Profile (`prod`)**:
  - Uses `org.postgresql.Driver` with Google Cloud SQL socket factory for Cloud Run.
- **Alternatives Considered**:
  - *SQLite*: SQLite JPA support in Spring Boot 3.5 lacks mature auto-DDL and sequence generation.

### Decision 2: Normalized Relational Schema with Foreign Key Cascade
- **Schema Mapping**:
  ```sql
  CREATE TABLE posts (
      id VARCHAR(36) PRIMARY KEY,
      title VARCHAR(100) NOT NULL,
      description VARCHAR(1000) NOT NULL,
      category VARCHAR(50) NOT NULL,
      condition VARCHAR(50) NOT NULL,
      price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
      is_free BOOLEAN NOT NULL DEFAULT TRUE,
      pickup_location VARCHAR(120) NOT NULL,
      available_until TIMESTAMP WITH TIME ZONE NOT NULL,
      photo_url VARCHAR(500) NOT NULL,
      poster_name VARCHAR(100) NOT NULL,
      poster_contact VARCHAR(100) NOT NULL,
      edit_token_hash VARCHAR(64) NOT NULL,
      legal_confirmed BOOLEAN NOT NULL,
      status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
      interest_count INT NOT NULL DEFAULT 0,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
      claimed_at TIMESTAMP WITH TIME ZONE
  );

  CREATE TABLE interests (
      id VARCHAR(36) PRIMARY KEY,
      post_id VARCHAR(36) NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
      visitor_name VARCHAR(100) NOT NULL,
      visitor_contact VARCHAR(100) NOT NULL,
      message VARCHAR(500),
      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
  );

  CREATE INDEX idx_posts_active ON posts(status, available_until, created_at DESC);
  CREATE INDEX idx_interests_post ON interests(post_id);
  ```
- **Rationale**: Clean normalization with `ON DELETE CASCADE` guarantees that deleting a post cleanly removes all private inquiry notes.

### Decision 3: Image Containment with Neutral Letterboxing
- **Choice**: In `item-detail.component.scss`, set `.showcase-img` to `object-fit: contain` with `background-color: #F4F1EA` and max-height constraints. In `item-card.component.ts`, adjust aspect ratio to `4 / 3` with centered framing.
- **Rationale**: Smartphone portrait photos (3:4 / 9:16) no longer lose top or bottom edges to aggressive square cropping.

## Risks / Trade-offs

- **[Risk] H2 file locking during concurrent test runs** → *Mitigation*: Include `AUTO_SERVER=TRUE` in connection string and use isolated in-memory DB (`jdbc:h2:mem:testdb`) for Maven unit test suites.
- **[Risk] Cloud SQL infrastructure cost on Google Cloud** → *Mitigation*: Provide clear documentation on Cloud SQL instance pricing vs Cloud Firestore free tier so developers can choose the profile that matches their budget.

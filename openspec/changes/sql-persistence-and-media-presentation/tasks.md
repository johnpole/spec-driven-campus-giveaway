# Tasks

## 1. Backend Dependencies and JPA Entities

- [x] 1.1 Add `spring-boot-starter-data-jpa`, `org.postgresql:postgresql`, and `com.h2database:h2` dependencies to `backend/pom.xml` and verify `./mvnw clean compile` succeeds
- [x] 1.2 Implement `PostEntity` and `InterestEntity` with Jakarta Persistence annotations, UUID keys, and foreign key cascade delete, and verify with entity unit tests

## 2. Spring Data JPA Repositories and Database Configuration

- [x] 2.1 Implement `JpaPostRepository` and `JpaInterestRepository` implementing the domain repository interfaces `PostRepository` and `InterestRepository`, and verify with `./mvnw test -Dtest=*Repository*`
- [x] 2.2 Configure `application.properties` for local file persistence (`data/campusgiveaway.mv.db`) and `application-prod.properties` for Google Cloud SQL PostgreSQL, and verify with `./mvnw test`
- [x] 2.3 Create `docker-compose.yml` for local PostgreSQL 17 container with volume persistence and verify container startup via `docker compose up -d`

## 3. Frontend Responsive Image Presentation Fix

- [x] 3.1 Update `item-detail.component.scss` showcase container to use `object-fit: contain` with `#F4F1EA` neutral framing, and verify with `npm test -- --watch=false --include=src/app/features/item-detail/item-detail.component.spec.ts`
- [x] 3.2 Update `item-card.component.ts` and `my-post.component.scss` image aspect ratios and background letterboxing to prevent vertical cropping, and verify with `npm test -- --watch=false`

## 4. End-to-End Verification and Persistence Test

- [x] 4.1 Run Playwright E2E verification suite to validate full item creation, restart persistence, and uncropped image capture with screenshot output in `artifacts/screenshots/`

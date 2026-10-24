# Design

## Context

See [proposal.md](proposal.md) for background and motivation. The application currently runs locally using an embedded/Dockerized PostgreSQL 17 instance with local disk media storage. For the GDG demonstration, it must be provisioned and deployed to Google Cloud (`gdg-hudson`, region `us-east1`) with enterprise-grade security, zero secret leakage, reproducible Terraform IaC, and a reliable stop plan to halt database billing after 2 days.

## Goals / Non-Goals

**Goals:**
- Provide Terraform IaC scripts under `infra/terraform/` to declare and provision all necessary GCP resources (Cloud Run, Cloud SQL, Cloud Storage, Secret Manager, IAM).
- Package the Angular 20 SPA and Spring Boot 3.5 backend into a unified multi-stage container deployed to Cloud Run.
- Configure Spring Boot `prod` profile with the Google Cloud SQL Java socket factory to connect securely without public IP exposure or hardcoded passwords.
- Store uploaded item images in a private Google Cloud Storage bucket and stream them via the existing `/api/media/{key}` controller.
- Provide a simple, deterministic post-event shutdown script (`infra/stop.sh`) to patch the Cloud SQL activation policy to `NEVER` 2 days after the conference.

**Non-Goals:**
- Multi-cloud or multi-region deployment.
- High-availability multi-zone database clustering with failover replicas (single `db-f1-micro` instance is sufficient for conference demo).
- Automated CI/CD pipelines via GitHub Actions (deployment is driven on-demand via `deploy.sh` and Google Cloud Build).

## Decisions

### 1. Terraform for Infrastructure as Code (IaC)
- **Decision**: Define cloud resources in `infra/terraform/` using the HashiCorp Google provider (`hashicorp/google`).
- **Rationale**: Terraform provides declarative drift detection, reproducible provisioning across environments, clean state management, and unified destruction (`terraform destroy`).
- **Alternatives Considered**:
  - *Raw `gcloud` CLI commands*: Simple to run imperatively, but lacks state tracking, dependency resolution, and clean teardown guarantees.
  - *Google Cloud Deployment Manager / Config Connector*: Proprietary GCP-only tooling with steeper learning curves than Terraform.

### 2. Single-Container Cloud Run Deployment
- **Decision**: Multi-stage Docker build that builds the Angular 20 production bundle, copies it to Spring Boot's `/backend/src/main/resources/static/`, and packages a lightweight JRE 21 runtime container.
- **Rationale**: Eliminates CORS complexity, avoids managing multiple Cloud Run services or load balancers, and minimizes cold starts and operating costs.
- **Alternatives Considered**:
  - *Separate Cloud Run service for frontend (e.g., Nginx) and backend*: Introduces unnecessary inter-service latency, cross-origin request handling, and double Cloud Run idle costs.
  - *Firebase Hosting for frontend + Cloud Run for backend*: Requires separate routing and domains; single container matches the core architecture defined in `AGENTS.md`.

### 3. Cloud SQL PostgreSQL 17 with Unix Socket Factory
- **Decision**: Provision a Cloud SQL PostgreSQL 17 instance (`db-f1-micro`) attached directly to Cloud Run using the Google Cloud SQL JDBC Socket Factory (`com.google.cloud.sql:postgres-socket-factory`).
- **Rationale**: Eliminates exposure of the database to public IP networks. Cloud Run securely tunnels database traffic over encrypted Unix sockets using IAM service account permissions. Passwords are stored in Secret Manager and injected as environment variables.
- **Alternatives Considered**:
  - *Public IP with authorized networks*: Insecure and susceptible to brute-force attacks if firewalls are misconfigured.
  - *Cloud SQL Auth Proxy container (sidecar)*: Adds operational overhead and memory consumption compared to the native Java socket factory library.

### 4. Media Storage via Private GCS Bucket
- **Decision**: Store uploaded images in a private Cloud Storage bucket (`gdg-hudson-campusgiveaway-photos`) and stream binaries through `/api/media/{key}`.
- **Rationale**: Keeps images private to the campus application, prevents direct bucket scraping, avoids signed URL expiration complexities, and maintains consistent caching and placeholder fallbacks on the backend.
- **Alternatives Considered**:
  - *Public GCS bucket with direct URLs*: Violates data governance best practices and exposes user uploads directly to search indexing.

### 5. 2-Day Post-Event Resource Stop Plan
- **Decision**: Provide `infra/stop.sh` which executes `gcloud sql instances patch giveaway-db --activation-policy=NEVER` and sets Cloud Run max-instances to 0. Additionally support complete teardown via `terraform destroy`.
- **Rationale**: Cloud SQL instances incur hourly compute costs even when idle. Setting activation policy to `NEVER` completely stops the VM compute billing while preserving database storage/disk snapshots. When ready to permanently clean up, `terraform destroy` destroys all created resources cleanly.
- **Alternatives Considered**:
  - *Deleting the project*: Destroys all other unrelated services in the user's GCP account.
  - *Manual Google Cloud Console clicking*: Prone to forgetting specific billable resources.

## Risks / Trade-offs

- **[Risk] Cloud SQL Cold Start / Provisioning Latency** → Provisioning a Cloud SQL instance can take 5–8 minutes during initial terraform apply.
  - *Mitigation*: Run `terraform apply` early before the talk and verify connectivity using `/actuator/health`.
- **[Risk] Cloud Run Cold Start on Free Tier** → The first request after idle can take 2–3 seconds to boot Spring Boot.
  - *Mitigation*: Configure `--min-instances=1` during the demonstration period, and reduce to 0 after the event in `infra/stop.sh`.
- **[Risk] Accidental Post-Event Billing** → If left running, Cloud SQL and Cloud Run could accrue unexpected monthly charges.
  - *Mitigation*: Deliver an explicit `infra/stop.sh` script with step-by-step instructions and a calendar reminder for 2 days post-event.

## Migration Plan

1. Enable necessary GCP APIs: `run.googleapis.com`, `sqladmin.googleapis.com`, `storage.googleapis.com`, `secretmanager.googleapis.com`, `cloudbuild.googleapis.com`, `artifactregistry.googleapis.com`.
2. Apply Terraform configuration:
   ```bash
   cd infra/terraform
   terraform init
   terraform apply -var="project_id=gdg-hudson"
   ```
3. Build and deploy container image via `deploy.sh`:
   ```bash
   ./deploy.sh --project gdg-hudson
   ```
4. Verify deployment health:
   ```bash
   curl -s https://<cloud-run-url>/actuator/health
   ```
5. **Post-Event Teardown (2 days later)**:
   ```bash
   ./infra/stop.sh
   # Or for complete cleanup:
   cd infra/terraform && terraform destroy -var="project_id=gdg-hudson"
   ```

# Tasks

## 1. Backend Production Configuration & Cloud SQL Dependencies

- [x] 1.1 Add Google Cloud SQL PostgreSQL socket factory (`com.google.cloud.sql:postgres-socket-factory`) and Google Cloud Storage (`com.google.cloud:google-cloud-storage`) dependencies to `backend/pom.xml` and verify compilation with `./mvnw test-compile`
- [x] 1.2 Configure `backend/src/main/resources/application-prod.properties` for Cloud SQL socket factory JDBC URL, Secret Manager credential resolution, and GCS bucket configuration, and verify configuration validity with unit tests
- [x] 1.3 Implement `GcsMediaStorageService` implementing `MediaStorageService` for production photo streaming and storage with fallback, and verify with a dedicated unit test

## 2. Infrastructure as Code (Terraform)

- [x] 2.1 Author `infra/terraform/variables.tf` and `infra/terraform/main.tf` declaring GCP APIs, Artifact Registry, Cloud SQL PostgreSQL 17 (`giveaway-db`), Secret Manager secret, and GCS bucket, and verify with `terraform fmt -check` and `terraform validate`
- [x] 2.2 Author `infra/terraform/outputs.tf` and Cloud Run service resource definition in `infra/terraform/main.tf` with IAM bindings (`roles/cloudsql.client`, `roles/storage.objectAdmin`, `roles/secretmanager.secretAccessor`), and verify with `terraform validate`

## 3. Deployment & Teardown Automation

- [x] 3.1 Update `deploy.sh` script to orchestrate the multi-stage Docker build via Google Cloud Build and deploy the unified container to Cloud Run with Cloud SQL attachment, and verify syntax with `bash -n deploy.sh`
- [x] 3.2 Author `infra/stop.sh` post-event shutdown script to patch Cloud SQL activation policy to `NEVER` and set Cloud Run min/max instances to 0 to eliminate post-event compute billing, and verify syntax with `bash -n infra/stop.sh`

## 4. Cloud Verification & Smoke Testing

- [x] 4.1 Deploy infrastructure via Terraform to `gdg-hudson` in region `us-east1` and verify resource creation with `terraform output`
- [x] 4.2 Deploy application container via `deploy.sh` to Cloud Run and verify production health endpoint (`/actuator/health`) returns HTTP 200 with status `UP`

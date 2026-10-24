# Proposal

## Why

To support the live GDG demonstration tomorrow, Campus Giveaway must deploy to Google Cloud (`gdg-hudson`, region `us-east1`) as a highly available, publicly accessible HTTPS web application. Moving from local development to production requires declarative Infrastructure as Code (Terraform) for reproducible provisioning, private media storage in Google Cloud Storage, Cloud SQL PostgreSQL 17 persistence with Secret Manager, and a structured shutdown plan to halt billing 2 days after the talk.

## What Changes

- Add Terraform IaC configuration in `infra/terraform/` defining Google Cloud Run, Cloud SQL PostgreSQL 17, Google Cloud Storage, Secret Manager, and least-privilege IAM service accounts.
- Update `deploy.sh` to package the Angular 20 and Spring Boot single container via Cloud Build and deploy to Cloud Run in `gdg-hudson`.
- Configure `application-prod.properties` to connect to Cloud SQL using the Google Cloud SQL Java socket factory and stream photos from the private Cloud Storage bucket.
- Provide post-event teardown scripts (`infra/stop.sh` and `terraform destroy`) to safely suspend Cloud SQL and prevent unexpected billing 2 days after the conference.

## Capabilities

### New Capabilities

- `cloud-infrastructure`: Defines declarative cloud infrastructure provisioning, container runtime configuration on Cloud Run, and post-event resource deactivation.

### Modified Capabilities

- `posts`: Adds production database connectivity requirements using Google Cloud SQL with IAM and Secret Manager credentials.
- `media`: Modifies photo persistence to stream uploaded images from private Google Cloud Storage bucket rather than local disk.

## Out of Scope

- User authentication / Google Sign-In (handled in subsequent change `google-sign-in`).
- Automated AI moderation workflows (handled in `add-post-moderation`).
- Multi-region disaster recovery replication.

## Impact

- Target GCP Project: `gdg-hudson` (Region `us-east1`).
- Files added: `infra/terraform/main.tf`, `infra/terraform/variables.tf`, `infra/terraform/outputs.tf`, `infra/stop.sh`.
- Deployment automation: `deploy.sh` updated for project `gdg-hudson` and Artifact Registry.

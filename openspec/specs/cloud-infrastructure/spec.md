# cloud-infrastructure Specification

## Purpose

Defines declarative cloud infrastructure provisioning, container runtime configuration on Cloud Run, and post-event resource deactivation.

## Requirements

### Requirement: Cloud Run Managed Container Hosting
The system SHALL execute the combined Angular frontend and Spring Boot backend as a single containerized application deployed to Google Cloud Run in the `us-east1` region, listening on port 8080 and terminating TLS automatically.

#### Scenario: Public HTTPS access to containerized application
- **WHEN** an unauthenticated user navigates to the deployed Cloud Run HTTPS service URL
- **THEN** the server returns HTTP 200 with the compiled Angular single-page application and static resources

#### Scenario: Health actuator check
- **WHEN** an HTTP GET request is issued to `/actuator/health`
- **THEN** the system returns HTTP 200 with JSON status `UP` indicating database connectivity and service readiness

### Requirement: Cloud SQL PostgreSQL 17 Lifecycle and Attachment
The system SHALL connect the Cloud Run application service to a managed Google Cloud SQL PostgreSQL 17 instance via the Cloud SQL Java connector using unix socket factory and Secret Manager credentials.

#### Scenario: Secure database connection without public IP exposure
- **WHEN** Cloud Run service starts up in production mode
- **THEN** the application establishes database connections to Cloud SQL through the internal socket factory using credentials resolved from Secret Manager

### Requirement: Post-Event Resource Deactivation
The system SHALL provide an automated, scriptable mechanism to suspend Cloud SQL instance execution and de-provision non-essential resources 2 days following the conference demonstration to eliminate recurring cloud costs.

#### Scenario: Suspend Cloud SQL execution
- **WHEN** the post-event stop script (`infra/stop.sh`) is executed
- **THEN** the Cloud SQL instance activation policy is set to `NEVER`, halting database compute billing while preserving data on disk

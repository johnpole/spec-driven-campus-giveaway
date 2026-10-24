# Spec Delta: media

## ADDED Requirements

### Requirement: Cloud Storage Media Persistence
The system SHALL store uploaded item photos in a private Google Cloud Storage bucket in production environments and stream them securely to clients via the `/api/media/{key}` endpoint.

#### Scenario: Production photo streaming from Cloud Storage
- **WHEN** a client requests an image via GET `/api/media/{key}` in production
- **THEN** the backend fetches the object from the configured Google Cloud Storage bucket using the Cloud Run service account credentials and streams the binary content with appropriate Cache-Control and Content-Type headers

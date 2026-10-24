# Spec Delta: media

## Purpose
Manages image uploads for item posts, enforcing file type and size constraints, storing image assets in Google Cloud Storage or local storage, and returning dual-property URLs.

## ADDED Requirements

### Requirement: Single Photo Upload and Validation
The system SHALL provide an endpoint to upload a single item photo. The system SHALL accept only image files of MIME types `image/jpeg`, `image/png`, or `image/webp` with a maximum payload size of 5 MB (5,242,880 bytes). Validated images SHALL be saved to storage and a public URL returned in JSON with both `photoUrl` and `url` keys.

#### Scenario: Successful image upload
- **WHEN** client posts a multipart form containing an image file of 2.1 MB with Content-Type "image/jpeg"
- **THEN** system saves the object into the storage bucket/directory with a unique UUID filename and returns HTTP 201 Created with JSON containing `photoUrl` and `url`

#### Scenario: Validation failure on file exceeding 5 MB limit
- **WHEN** client attempts to upload an image file of 5.5 MB (exceeding 5 MB limit)
- **THEN** system rejects the upload with HTTP 413 Payload Too Large indicating maximum file size is 5 MB

#### Scenario: Validation failure on unsupported file type
- **WHEN** client attempts to upload a file with Content-Type "image/gif" or "application/pdf"
- **THEN** system rejects the upload with HTTP 415 Unsupported Media Type indicating only JPEG, PNG, and WebP are allowed

#### Scenario: Storage service failure handling
- **WHEN** Cloud Storage is unavailable or encounters an I/O write error during upload
- **THEN** system logs the failure and returns HTTP 502 Bad Gateway with a structured error response

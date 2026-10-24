# media Specification

## Purpose

Defines responsive, uncropped media presentation for item photos across feed and detail views, preventing vertical cropping on smartphone portrait images.

## Requirements

### Requirement: Uncropped Media Presentation
The system SHALL present item photos in detail and browse views using aspect-ratio containment and neutral framing backdrops, preventing vertical or horizontal clipping of item photos.

#### Scenario: Portrait photo displayed without clipping in detail view
- **WHEN** a user views the item detail page for a listing with a portrait-oriented photo (e.g. 3:4 or 9:16 aspect ratio)
- **THEN** the image is rendered fully visible within the showcase container using object-fit contain without top or bottom edges being cut off

#### Scenario: Landscape photo centered with neutral letterbox framing
- **WHEN** a user views an item with a wide landscape photo
- **THEN** the image is centered within the media viewport and any surrounding canvas is rendered in a soft neutral background tone matching the brand palette

#### Scenario: Missing or broken image displays fallback placeholder
- **WHEN** an item photo URL fails to load or returns an HTTP error
- **THEN** the image component catches the error event and renders a clean SVG campus item placeholder graphic

### Requirement: Cloud Storage Media Persistence
The system SHALL store uploaded item photos in a private Google Cloud Storage bucket in production environments and stream them securely to clients via the `/api/media/{key}` endpoint.

#### Scenario: Production photo streaming from Cloud Storage
- **WHEN** a client requests an image via GET `/api/media/{key}` in production
- **THEN** the backend fetches the object from the configured Google Cloud Storage bucket using the Cloud Run service account credentials and streams the binary content with appropriate Cache-Control and Content-Type headers

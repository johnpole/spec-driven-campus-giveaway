# Spec Delta: interests

## Purpose
Allows campus peers to express interest in posted items by submitting their name, contact information, and an optional message, while providing public interest metrics and restricting visitor contact details to the verified poster.

## ADDED Requirements

### Requirement: Visitor Expresses Interest
The system SHALL allow any visitor to express interest in an active item by submitting their full name, contact information (email or phone number), and an optional message.

#### Scenario: Successful interest expression
- **WHEN** visitor sends POST /api/posts/{id}/interests with visitorName "Taylor Jordan", visitorContact "jordat@rpi.edu", and message "Can pick up today at 4pm"
- **THEN** system saves the interest record, increments the item's interest count, and returns HTTP 201 Created

#### Scenario: Validation failure on missing name
- **WHEN** visitor sends POST /api/posts/{id}/interests with empty or missing visitorName
- **THEN** system rejects the request with HTTP 400 Bad Request indicating name is required

#### Scenario: Validation failure on missing contact
- **WHEN** visitor sends POST /api/posts/{id}/interests with empty or missing visitorContact
- **THEN** system rejects the request with HTTP 400 Bad Request indicating contact info is required

#### Scenario: Express interest rejected for claimed item
- **WHEN** visitor sends POST /api/posts/{id}/interests for an item whose status is "CLAIMED"
- **THEN** system rejects the request with HTTP 409 Conflict indicating the item is no longer available

#### Scenario: Express interest rejected for expired item
- **WHEN** visitor sends POST /api/posts/{id}/interests for an item whose availableUntil date is in the past
- **THEN** system rejects the request with HTTP 409 Conflict indicating the item has expired

### Requirement: Public Interest Metrics and Anonymized Names
The system SHALL provide public interest summaries for an item, including the total count of interested users and an array of their first names only. Full contact information and personal messages SHALL strictly NOT be included in public responses.

#### Scenario: Public interest summary
- **WHEN** a client requests GET /api/posts/{id}/interests/summary
- **THEN** system returns HTTP 200 with totalCount (e.g. 3) and firstNames array (e.g. ["Taylor", "Jordan", "Sam"]), strictly excluding emails, phones, and messages

#### Scenario: Summary for item with no interest
- **WHEN** a client requests GET /api/posts/{id}/interests/summary for an item with 0 inquiries
- **THEN** system returns HTTP 200 with totalCount: 0 and firstNames: []

### Requirement: Poster Private Interest Inquiries Retrieval
The system SHALL allow the original poster to retrieve the complete list of interest inquiries—including full visitor names, contact details, messages, and timestamps—only when providing the item's valid private edit token.

#### Scenario: Poster retrieves full inquiries with valid token
- **WHEN** client sends GET /api/posts/{id}/interests with header X-Edit-Token matching the item's edit token
- **THEN** system returns HTTP 200 with full array of inquiry records including full names, contact info, messages, and timestamps

#### Scenario: Inquiry retrieval forbidden without valid token
- **WHEN** client sends GET /api/posts/{id}/interests with missing or invalid X-Edit-Token
- **THEN** system rejects the request with HTTP 403 Forbidden

# Spec Delta: posts

## Purpose
Enables students to post items for giveaway or sale without user accounts, browse items via a mobile-first feed, search and filter listings, retrieve public item details, and manage posts via private edit tokens while protecting poster contact privacy.

## ADDED Requirements

### Requirement: Item Creation Without Account
The system SHALL allow users to create an item post by providing title, description, category, condition, price or free indicator, pickup location, available-until date, photoUrl, posterName, posterContact, and legalConfirmed=true. posterContact is required, validated as an email or E.164 phone, retained only for internal accountability/moderation, and never returned by any endpoint. A unique ID and private editToken SHALL be generated.

#### Scenario: Successful item creation
- **WHEN** user submits valid item details with posterName "Alex Chen", valid posterContact "chena@rpi.edu", category "Furniture", condition "GOOD", price 0.00, availableUntil in future, valid photoUrl referencing the application media endpoint, and legalConfirmed true
- **THEN** system saves the item with status "AVAILABLE", returns HTTP 201 Created with the public item ID, and returns the private editToken

#### Scenario: Validation failure on missing name or contact
- **WHEN** user submits item creation request missing posterName or posterContact
- **THEN** system rejects the submission with HTTP 400 Bad Request detailing missing required poster identity fields

#### Scenario: Validation failure on malformed contact
- **WHEN** user submits item creation request with posterContact that is neither a valid email address nor a valid E.164 telephone number
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating poster contact must be a valid email or E.164 phone number

#### Scenario: Validation failure on negative price
- **WHEN** user submits item creation request with price less than 0.00 USD
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating price must be zero or positive

#### Scenario: Validation failure on expired or past availability date
- **WHEN** user submits item creation request with availableUntil date in the past
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating available-until date must be in the future

#### Scenario: Validation failure on unknown category or condition
- **WHEN** user submits item creation request with category "Automobiles" or condition "BROKEN"
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating category and condition must match allowed enumerations

#### Scenario: Validation failure on title or description length
- **WHEN** user submits item creation request with title exceeding 100 characters or description exceeding 1000 characters
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating character limit violations

#### Scenario: Validation failure on missing or non-whitelisted photoUrl
- **WHEN** user submits item creation request with missing photoUrl or a photoUrl pointing to an external domain outside the application bucket or media endpoint
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating photo is required and must reference the trusted media endpoint

#### Scenario: Validation failure on unconfirmed legal statement
- **WHEN** user submits item creation request with legalConfirmed false or omitted
- **THEN** system rejects the submission with HTTP 400 Bad Request indicating the user must confirm the item is legal, safe, and theirs to give

### Requirement: Public Feed Listing and Filtering
The system SHALL provide a public feed of active items sorted by creation timestamp descending. An active item MUST have status "AVAILABLE" and availableUntil timestamp greater than or equal to current time. The endpoint SHALL support optional filtering by category and by "free only" items without requiring authentication.

#### Scenario: List active items
- **WHEN** a client requests GET /api/posts with no query parameters
- **THEN** system returns HTTP 200 with an array of active available items where availableUntil >= now sorted newest first, excluding poster contact details

#### Scenario: Filter items by category
- **WHEN** a client requests GET /api/posts with query parameter category="Electronics"
- **THEN** system returns HTTP 200 with only active items belonging to the Electronics category

#### Scenario: Filter items by free only
- **WHEN** a client requests GET /api/posts with query parameter freeOnly=true
- **THEN** system returns HTTP 200 with only active items where price is 0.00 USD

#### Scenario: Exclude expired or claimed items from public feed
- **WHEN** an item has status "CLAIMED" or availableUntil in the past
- **THEN** system excludes that item from GET /api/posts public feed results

#### Scenario: Empty feed
- **WHEN** no items match the requested category or filter
- **THEN** system returns HTTP 200 with an empty list

### Requirement: Keyword Text Search
The system SHALL allow clients to search active items using a text query that matches terms in the item title or description.

#### Scenario: Successful keyword search
- **WHEN** a client requests GET /api/posts?q=desk
- **THEN** system returns HTTP 200 containing active items whose title or description contains "desk", case-insensitively

#### Scenario: Keyword search with no matches
- **WHEN** a client requests GET /api/posts?q=nonexistentwidget
- **THEN** system returns HTTP 200 with an empty list

### Requirement: Public Item Detail Retrieval
The system SHALL return item details for a given item ID regardless of whether the status is "AVAILABLE" or "CLAIMED". The public response SHALL strictly omit poster contact information (email and phone).

#### Scenario: Successful item detail retrieval for available item
- **WHEN** a client requests GET /api/posts/{id} for an active available item
- **THEN** system returns HTTP 200 with title, description, category, condition, price, pickupLocation, availableUntil, photoUrl, status "AVAILABLE", posterName, and interestCount, strictly omitting posterContact

#### Scenario: Successful item detail retrieval for claimed item
- **WHEN** a client requests GET /api/posts/{id} for an item whose status is "CLAIMED"
- **THEN** system returns HTTP 200 with complete item fields and status "CLAIMED", strictly omitting posterContact

#### Scenario: Item detail not found
- **WHEN** a client requests GET /api/posts/{id} for a non-existent item ID
- **THEN** system returns HTTP 404 Not Found

### Requirement: Post Status Management via Edit Token
The system SHALL allow the original poster to mark an item as "CLAIMED" by providing the item's private edit token.

#### Scenario: Mark item as claimed with valid token
- **WHEN** poster sends PATCH /api/posts/{id}/status with status "CLAIMED" and header X-Edit-Token matching the item's edit token
- **THEN** system updates the item status to "CLAIMED" and returns HTTP 200 with the updated item status

#### Scenario: Status update rejected with invalid token
- **WHEN** a client sends PATCH /api/posts/{id}/status with an incorrect or missing edit token
- **THEN** system rejects the request with HTTP 403 Forbidden

### Requirement: Post Deletion via Edit Token
The system SHALL allow the original poster to permanently delete a post by providing the item's private edit token via DELETE /api/posts/{id}.

#### Scenario: Delete post with valid token
- **WHEN** poster sends DELETE /api/posts/{id} with header X-Edit-Token matching the item's edit token
- **THEN** system removes the post and its interests from the database and returns HTTP 204 No Content

#### Scenario: Delete post rejected with invalid token
- **WHEN** a client sends DELETE /api/posts/{id} with an incorrect or missing edit token
- **THEN** system rejects the request with HTTP 403 Forbidden

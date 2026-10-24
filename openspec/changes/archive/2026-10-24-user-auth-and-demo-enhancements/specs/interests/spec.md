# Spec Delta

## Purpose

Enables authenticated buyers to express interest in giveaway items with a single message note while automatically supplying verified buyer contact information to the item poster.

## ADDED Requirements

### Requirement: Authenticated 1-Field Interest Submission
The system SHALL require Google authentication for submitting interest on an item and SHALL auto-populate the inquirer's verified Google name and email, prompting only for a message or pickup note.

#### Scenario: Authenticated buyer submits interest
- **WHEN** an authenticated user submits an inquiry via POST /api/posts/{id}/interests providing a message
- **THEN** the system stores the inquiry bound to the inquirer's Google user ID, name, and email, increments the post's public interest count, and returns HTTP 201 Created

#### Scenario: Unauthenticated buyer prompted to sign in
- **WHEN** an unauthenticated visitor clicks "I'm Interested" on an item detail page
- **THEN** the UI opens the Google Sign-In prompt before allowing inquiry submission

#### Scenario: Author cannot express interest on own post
- **WHEN** the authenticated author of an item attempts to submit an interest inquiry on their own post
- **THEN** the system rejects the submission with HTTP 400 Bad Request and an explanatory error message

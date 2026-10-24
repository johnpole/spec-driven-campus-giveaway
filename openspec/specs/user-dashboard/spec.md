# user-dashboard Specification

## Purpose
Provides authenticated users with a centralized "My Activity" space to manage their authored giveaways, review received inquiries, and track items they have requested.

## Requirements

### Requirement: Authored Giveaways Management
The system SHALL provide an authenticated endpoint and view displaying all items authored by the current user with real-time status and received inquiry details.

#### Scenario: Author views personal post list
- **WHEN** an authenticated user navigates to "My Activity" -> "My Posts" tab (GET /api/users/me/posts)
- **THEN** the system returns all posts created by the user (Active, Taken, Archived) including buyer names, emails, and pickup messages

#### Scenario: Author marks post as taken from dashboard
- **WHEN** the author clicks "Mark as Taken" on an active giveaway in their dashboard
- **THEN** the post status transitions to TAKEN, the item is removed from the public feed, and the dashboard reflects the updated status

### Requirement: Requested Items Tracking
The system SHALL provide an authenticated view displaying all items where the current user submitted an interest inquiry.

#### Scenario: User views requested items
- **WHEN** an authenticated user navigates to "My Activity" -> "Items I Requested" tab (GET /api/users/me/interests)
- **THEN** the system returns all items the user inquired about along with each item's current availability status

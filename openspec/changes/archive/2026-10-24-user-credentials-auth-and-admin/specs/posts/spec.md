# Spec Delta: posts

## ADDED Requirements

### Requirement: Administrative Post Moderation and Status Override
The system SHALL allow authenticated users possessing the ADMIN role to moderate any giveaway post, including deleting listings and updating post statuses without requiring the original author credentials or edit tokens.

#### Scenario: Admin deletes any item post
- **WHEN** an authenticated administrator (`admin@campus-giveaway.test`) submits DELETE /api/posts/{id} for a post created by another user
- **THEN** the system bypasses author checks, deletes the post and cascading inquiries, and returns HTTP 204 No Content

#### Scenario: Admin updates status of any item post
- **WHEN** an authenticated administrator submits PATCH /api/posts/{id}/status for a post created by another user
- **THEN** the system updates the item status (e.g. to TAKEN or AVAILABLE) and returns the updated post details

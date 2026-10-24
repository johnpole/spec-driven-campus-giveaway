# Spec Delta: posts

## Purpose
Enables students to post and discover items for giveaway across mobile and responsive desktop layouts while protecting poster privacy.

## ADDED Requirements

### Requirement: Responsive Desktop Layout and Unblocked Form Navigation
The system SHALL present an unblocked, fully scrollable post creation form with a dedicated bottom submit action bar without overlapping bottom navigation elements. On viewports exceeding 768px width, the system SHALL progressively scale feed cards into a responsive multi-column grid and scale content containers comfortably up to 1080px width while preserving mobile touch ergonomics on viewports under 768px.

#### Scenario: Full form field reachability on Post Item screen
- **WHEN** user navigates to the "/post" or "/post-item" screen on any viewport
- **THEN** all form inputs (photo upload, title, category, condition, price, description, pickup location, poster name, poster contact, legal consent) SHALL be reachable and completely scrollable without obstruction from fixed navigation bars

#### Scenario: Responsive grid layout on desktop viewports
- **WHEN** user opens the application on a viewport of 1024px or wider
- **THEN** the home feed displays item cards in a multi-column responsive grid (3 to 4 columns) with max container width up to 1080px instead of being locked into a single 480px column

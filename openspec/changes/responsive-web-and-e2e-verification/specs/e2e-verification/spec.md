# Spec Delta: e2e-verification

## Purpose
Provides automated end-to-end browser verification for all core student user journeys in the Campus Giveaway application, capturing high-resolution visual evidence across mobile and desktop viewports.

## ADDED Requirements

### Requirement: Automated Playwright Scenario Suite
The system SHALL provide an automated Playwright test suite that exercises all core user capabilities against the local application server and saves PNG screenshots of each state to an artifacts directory.

#### Scenario: Verify Home Feed and Search Filtering
- **WHEN** Playwright navigates to the home feed URL
- **THEN** it verifies the header branding, impact banner, category chips, and search input, filters by category and free-only, and saves a screenshot

#### Scenario: Verify Item Detail and Interest Submission
- **WHEN** Playwright opens an item detail page and submits an "I'm interested" inquiry
- **THEN** it verifies the interest count increments, privacy contact is not exposed, a confirmation toast appears, and saves a screenshot

#### Scenario: Verify Post Creation and Management Lifecycle
- **WHEN** Playwright uploads a photo, fills in all post fields, confirms legal consent, and submits the post
- **THEN** it verifies the private management link is displayed, navigates to "/my-posts", marks the item as claimed, and saves screenshots

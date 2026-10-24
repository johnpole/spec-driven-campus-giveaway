# Spec Delta: ai-autofill

## ADDED Requirements

### Requirement: Administrative Feature Flag Management
The system SHALL restrict toggling the multimodal AI auto-fill operational feature flag to authenticated administrators to prevent unintended API usage and costs.

#### Scenario: Admin toggles feature flag
- **WHEN** an authenticated administrator (`admin@campus-giveaway.test`) submits POST /api/media/autofill/toggle
- **THEN** the system updates the operational flag state and returns the new status with HTTP 200 OK

#### Scenario: Non-admin toggle attempt rejected
- **WHEN** an unauthenticated visitor or non-admin user submits POST /api/media/autofill/toggle
- **THEN** the system rejects the request with HTTP 403 Forbidden

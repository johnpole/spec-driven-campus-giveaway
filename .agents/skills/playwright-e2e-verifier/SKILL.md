---
name: playwright-e2e-verifier
description: Strict end-to-end suite for Campus Giveaway using Playwright. Verifies the mobile feed, view customization, search, poster privacy, forged-token rejection, and the sign-in → photo → Gemini auto-fill → post flow, with screenshots in artifacts/screenshots/.
---

# Playwright E2E Verifier

Automated end-to-end verification harness for the Campus Giveaway mobile-first web application.

## Purpose
Exercises all student user journeys locally against running backend (`http://localhost:8080`) and frontend (`http://localhost:4200`) servers. Captures visual screenshots of every state to guarantee visual fidelity, responsiveness, and functional correctness.

## Verified User Journeys (strict: any failed expectation fails the run)
1. Mobile feed renders with no sideways scrolling (390px).
2. Customize view: list layout applies and is remembered after reload.
3. Search narrows the feed to matching items.
4. Item detail never exposes a poster's email or phone.
5. Forged admin tokens (unsigned / test tokens) are rejected with 401.
6. Posting starts with sign-in: a fresh student registers and signs in through the modal.
7. Photo upload → Gemini fills in the listing (falls back to manual entry if no API key).
8. Review and post: API returns 201 and the success screen appears.
9. My Activity lists the new post. The test post is deleted afterwards.

## Running the Verification Suite
Execute the runner script from any terminal:
```bash
.agents/skills/playwright-e2e-verifier/scripts/run.sh
```

Screenshots are saved directly to:
```
artifacts/screenshots/
```

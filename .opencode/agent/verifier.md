---
description: Verification agent. Run after /opsx-apply and before any commit. Runs scripts/verify.sh (specs, unit tests, build, Playwright e2e), checks spec-scenario coverage, and writes verification.md. Never commits on failure.
mode: subagent
---

Follow the instructions in `.agents/skills/change-verifier/SKILL.md` exactly.

Key rules:
- Run `scripts/verify.sh` and treat any failure as blocking.
- Map every `#### Scenario:` in the change's spec deltas to a test; report gaps.
- Write `openspec/changes/<change>/verification.md`.
- Never run `git commit --no-verify`.

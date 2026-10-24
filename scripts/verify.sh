#!/usr/bin/env bash
# Verification gate: proves nothing broke before code is committed.
# Usage: scripts/verify.sh [full|quick]   (quick skips the Playwright end-to-end run)
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${1:-${VERIFY_MODE:-full}}"
REPORT_DIR="$ROOT/artifacts/verification"
REPORT="$REPORT_DIR/last-run.txt"
mkdir -p "$REPORT_DIR"
: > "$REPORT"

log() { echo "$1" | tee -a "$REPORT"; }

run_step() {
  local name="$1"; shift
  local start=$SECONDS
  log "▶ $name"
  if "$@" > "$REPORT_DIR/$(echo "$name" | tr ' /' '__').log" 2>&1; then
    log "  ✅ passed ($((SECONDS - start))s)"
  else
    log "  ❌ FAILED ($((SECONDS - start))s) — see artifacts/verification/$(echo "$name" | tr ' /' '__').log"
    tail -25 "$REPORT_DIR/$(echo "$name" | tr ' /' '__').log"
    log "VERIFICATION FAILED at: $name"
    exit 1
  fi
}

servers_up() {
  curl -sf http://localhost:8080/api/posts > /dev/null && curl -sf http://localhost:4200 > /dev/null
}

log "Campus Giveaway verification gate — mode: $MODE — $(date '+%Y-%m-%d %H:%M:%S')"

run_step "OpenSpec validate" openspec validate --all
run_step "Backend tests" bash -c "cd '$ROOT/backend' && ./mvnw -q test"
run_step "Frontend unit tests" bash -c "cd '$ROOT/frontend' && npx ng test --watch=false --browsers=ChromeHeadless"
run_step "Frontend production build" bash -c "cd '$ROOT/frontend' && npx ng build"

if [ "$MODE" = "full" ]; then
  if ! servers_up; then
    log "❌ End-to-end needs the app running: backend on :8080 and frontend on :4200."
    log "   Start them, then re-run. (Use 'scripts/verify.sh quick' to skip e2e.)"
    exit 1
  fi
  run_step "Playwright end-to-end" "$ROOT/.agents/skills/playwright-e2e-verifier/scripts/run.sh"
else
  log "⏭  Playwright end-to-end skipped (quick mode)"
fi

log "✅ ALL CHECKS PASSED — safe to commit."

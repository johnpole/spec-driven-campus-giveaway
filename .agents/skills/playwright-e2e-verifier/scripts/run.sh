#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$DIR/../../../../" && pwd)"

echo "🔍 Pre-flight check: Verifying backend and frontend are running..."

# Check backend
if ! curl -s http://localhost:8080/api/posts > /dev/null; then
  echo "❌ Error: Backend is not responding at http://localhost:8080"
  echo "   Please start it with: cd backend && ./mvnw spring-boot:run"
  exit 1
fi

# Check frontend
if ! curl -s http://localhost:4200 > /dev/null; then
  echo "❌ Error: Frontend is not responding at http://localhost:4200"
  echo "   Please start it with: cd frontend && npm start"
  exit 1
fi

echo "✅ Backend (8080) and Frontend (4200) are healthy."
echo "▶ Running Playwright E2E Verification Suite..."

cd "$PROJECT_ROOT/frontend"
node "$DIR/e2e-suite.js"

echo "✅ Playwright verification completed successfully!"

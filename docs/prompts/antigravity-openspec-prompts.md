# Antigravity + OpenSpec Prompts

> These are the prompts used to build this app with OpenSpec in Google Antigravity.
> The app evolved after them (e.g. Firestore → PostgreSQL, passcode → Google/email sign-in) —
> that evolution is captured as archived changes in `openspec/changes/archive/`.

Two parts:
- **Part A (A1–A4): build Phase 1 *before* the talk.** This is your rehearsal, and it produces the baseline "brownfield" app.
- **Part B (A5–A8): the live brownfield change on stage.**

OpenSpec 1.x slash commands in Antigravity: `/opsx-explore`, `/opsx-propose`, `/opsx-apply`, `/opsx-archive`.

---

## A0 — Install (also shown live in the "install" moment)

```bash
npm install -g @fission-ai/openspec@latest
```
```bash
cd campus-giveaway && git init && openspec init --tools antigravity
```

This creates `openspec/` (`specs/`, `changes/`, `config.yaml`) and the Antigravity workflow files.
Then create `AGENTS.md` at the repo root (prompt A1 asks the agent to do this).

---

## Part A — Build the baseline (rehearsal)

### A1 — Project context (`AGENTS.md`)

```
Create AGENTS.md at the repo root. This is the standing context for every agent working in this repo.

Product: "Campus Giveaway". Students who are graduating or moving out of RPI post items (free or cheap)
so other students can claim them. Sustainability + helping juniors.

Stack (do not deviate without proposing a change):
- Frontend: Angular 20, standalone components, signals, new control flow (@if/@for), OnPush,
  Reactive Forms, Angular Material is NOT used; plain SCSS following design/stitch/ exports.
  Located in /frontend.
- Backend: Java 21, Spring Boot 3.5, Maven, Spring Web, Bean Validation, Spring Cloud GCP
  (Firestore + Cloud Storage). Located in /backend. Package: edu.rpi.giveaway.
- Single deployable: Angular build is copied into backend static resources; one container on Cloud Run.
- Data: Firestore (Native mode). Photos: Cloud Storage bucket. Local dev: Firestore emulator.
- Region: us-east1.

Mobile-first (users arrive by scanning a QR code on their phone):
- Design for 360–430px wide first, then scale up with min-width media queries.
- Touch targets ≥ 48px, primary actions in a sticky bottom bar, no hover-only interactions.
- Photo input uses <input type="file" accept="image/*" capture="environment"> so it opens the camera.
- Correct input types (email, tel, numeric) for mobile keyboards.
- Installable PWA (web manifest + icons); Lighthouse mobile performance ≥ 90; images resized
  client-side to ≤ 1600px before upload.

Conventions:
- REST under /api/**, JSON, ISO-8601 timestamps, DTOs separate from Firestore documents.
- Every requirement in openspec/specs must have at least one automated test
  (JUnit 5 + MockMvc on backend; Vitest/Jasmine on frontend).
- No secrets in code. Config via env vars / Secret Manager.
- Contact info of posters is NEVER returned by list endpoints; only revealed to the poster's
  interest notifications.
- Accessibility: WCAG AA, labeled inputs, keyboard navigable.
- Conventional commits.

Workflow: never write code for a new capability without an OpenSpec change
(explore → propose → apply → archive). openspec/specs is the source of truth.
```

### A2 — Explore the baseline

```
/opsx-explore
I want to build Phase 1 of Campus Giveaway (see AGENTS.md). Rules for Phase 1:
- No login/accounts. To post, a user must provide name + contact (email or phone).
- Item: title, description, category (Furniture, Electronics, Textbooks, Kitchen, Dorm essentials,
  Clothing, Bikes, Other), condition (LIKE_NEW, GOOD, FAIR), price in USD or free, pickup location,
  available-until date, one photo (jpeg/png/webp, max 5 MB).
- Anyone can browse, search by text, filter by category and "free only".
- "I'm interested": visitor gives name + contact + message; the item shows interest count
  and first names; poster's contact is not public.
- Posts appear immediately (no moderation yet — that is deliberate).
Designs are in design/stitch/ (S1–S3, mobile-first). Ask me clarifying questions before proposing.
```

### A3 — Propose

```
/opsx-propose phase-1-open-board
Create the proposal, design, tasks and spec deltas. Split specs into capabilities:
`posts` (create/list/search/detail), `interests` (express interest), `media` (photo upload).
Every requirement needs WHEN/THEN scenarios, including validation failures
(missing name, missing contact, price < 0, photo too large, wrong type).
Tasks must be small (≤ 30 min each), ordered backend-first, each ending with a test.
Include a final task: Dockerfile (multi-stage: node build → maven build → distroless/JRE 21)
and deploy.sh for Cloud Run.
```

→ **Review the files by hand.** Tighten anything vague. Then:

### A4 — Apply, verify, archive

```
/opsx-apply phase-1-open-board
Work through tasks.md in order. After each task: run the relevant tests
(`./mvnw -q test` in backend, `npm test -- --watch=false` in frontend), fix failures, tick the box.
Use the Firestore emulator for integration tests. Match the Stitch exports in design/stitch/
for layout and colors. Stop and ask me if a spec is ambiguous instead of guessing.
```
```
/opsx-archive phase-1-open-board
```
Then deploy with `./deploy.sh` (see `04-GCP-DEPLOY.md`), tag it in git (`git tag v1-open-board`), and seed 10–15 realistic items.

---

## Part B — Live on stage: brownfield change

### A5 — Explore (on stage)

```
/opsx-explore
Our Campus Giveaway app is live and anyone can post anything — someone just posted a
"slightly used chemistry set". We need moderation before posts go public.
Read openspec/specs and the current code first, then tell me:
which existing requirements change, what new states a post needs, and the risks.
Idea: an AI moderator agent pre-screens each post, and a human moderator makes the final call.
Locally the agent should use Gemma via Ollama (offline, free); in the cloud, Gemini on Vertex AI.
```

### A6 — Propose (on stage)

```
/opsx-propose add-post-moderation
Requirements:
- New posts get status PENDING and are NOT visible in public list/search/detail.
- On create, a ModerationAgent reviews title, description, category, price and photo
  (image if model supports it) against a campus policy (no weapons, drugs, alcohol,
  prescription meds, hazardous chemicals, counterfeit, live animals, personal data, hate/harassment).
- Agent returns {verdict: APPROVE|REJECT|NEEDS_HUMAN, confidence 0..1, reason, policyTags[]}
  as structured JSON. APPROVE with confidence ≥ 0.85 auto-publishes; REJECT with ≥ 0.85 auto-rejects;
  everything else goes to the human queue as NEEDS_HUMAN.
- If the model call fails or times out (5s), the post becomes NEEDS_HUMAN (fail closed, never auto-publish).
- Human moderator UI at /moderate, protected by a moderator passcode (Secret Manager), with
  Pending/Approved/Rejected tabs, AI verdict + reason, Approve/Reject with reason. Match design/stitch S4.
- Poster can see their post's status via a private link returned on creation.
- Implementation: Java interface ModerationAgent with GemmaOllamaModerationAgent (profile "local")
  and GeminiVertexModerationAgent (profile "cloud"), using Spring AI. Model names via config.
- Every decision (AI or human) is written to an audit log on the post.
Mark which existing requirements in `posts` are MODIFIED.
```

→ **Try it:** open `specs/posts/spec.md` in the change and add one requirement by hand
(for example "Rejected posts are purged after 30 days"). Changing a line here is free; changing it after deploy costs far more.

### A7 — Apply (on stage)

```
/opsx-apply add-post-moderation
Implement tasks in order, running tests after each. For agent tests, mock the ModerationAgent
and add one contract test that runs against Gemma via Ollama only when OLLAMA_URL is set.
Show me a summary of files changed when done.
```

### A8 — Archive + ship

```
/opsx-archive add-post-moderation
```
```
Run ./deploy.sh with the cloud profile, then curl /api/health on the new URL and
create a test post "chemistry set with mystery chemicals" — show me its moderation verdict.
```

---

## Bonus prompts (for Q&A, or if you're ahead of schedule)

- **agy CLI subagent:** `agy run "Read openspec/specs/posts/spec.md and list any requirement without a matching test in backend/src/test"` (check the exact `agy` syntax beforehand with `agy --help`).
- **Phase 3 teaser:** `/opsx-explore Should we require @rpi.edu email verification via magic link? What breaks?`

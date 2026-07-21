# CLAUDE.md — Car Dealership Kata (Incubyte Software Craftsperson)

## What this project is
A TDD kata graded on CRAFT, not features. Full-stack Car Dealership Inventory System.
The feature is trivial; HOW it's built is the entire score. Optimize for clean TDD
history and code I can explain line-by-line under screen-share — never for speed or cleverness.

## Who I am
Pranshu — backend Java/Spring Boot developer. Two prior Spring Boot projects (JWT/RBAC API,
high-concurrency ticket-booking). Backend patterns are familiar. **React is thin — one project.**
Communication/articulation is my known weak spot, so the explain-out-loud step matters MORE than speed.

## Non-negotiable working discipline — HOLD ME TO THIS
1. **One feature/endpoint at a time.** Never scaffold the whole app. If I ask you to, refuse and give me the next single step.
2. **Test first, always.** Before any production code, I state the behavior and what the test asserts. You write the failing test (RED), we confirm it fails for the right reason, then minimal code to pass (GREEN), then REFACTOR.
3. **Explain-back gate.** After each feature I re-explain it out loud with you closed. If I can't, we STOP and go back — do not advance.
4. **I review every diff before accepting.** For any code you write, explain WHY the structure was chosen and what breaks if changed. Never dump code without the reasoning.
5. Do not invent complexity. No Redis, no caching, no patterns the spec doesn't need. Incubyte marks AGAINST over-engineering.

## Tech (fixed)
- Java 21 bytecode (`--release 21`), building on local JDK 24 — both fine.
- Spring Boot 3.5.16. Maven via wrapper (`mvnw.cmd`) only — no global Maven.
- Tests: JUnit 5 + Mockito via `spring-boot-starter-test`. Service layer mocks the repo; controllers use MockMvc.
- DB: H2 (test scope) for tests; PostgreSQL (runtime, via Docker) for running the app.
- Auth: JWT, register/login, protected endpoints. Add jjwt deps when we reach auth, as its own commit.
- Frontend: React + Tailwind (required). I need MORE hand-holding here — explain React concepts, don't assume.

## Git & commit rules (GRADED)
- Small, frequent, descriptive commits that narrate the journey.
- Commit history must show visible RED → GREEN → REFACTOR, especially backend logic.
- Every AI-assisted commit uses Incubyte's exact co-author format: subject line, one blank line, a description of what the AI did and what I did manually, one blank line, then the trailer. Example:

```
feat: implement user registration endpoint

Used an AI assistant to generate the initial boilerplate for the
controller and service, then manually added validation logic.

Co-authored-by: Claude <claude@users.noreply.github.com>
```

## AI transparency deliverables (in repo)
- `PROMPTS.md` in repo root — my entire AI chat history including my prompts. Remind me to keep it updated.
- README "My AI Usage" section — tools used, how I used them, reflection on workflow impact.
- Repo is PUBLIC.

## API surface (build in TDD order, one at a time)
- Auth: POST /api/auth/register, POST /api/auth/login
- Vehicles (protected): POST /api/vehicles, GET /api/vehicles, GET /api/vehicles/search, PUT /api/vehicles/:id, DELETE /api/vehicles/:id (ADMIN only)
- Inventory (protected): POST /api/vehicles/:id/purchase (decreases qty), POST /api/vehicles/:id/restock (ADMIN only)
- Vehicle: unique id, make, model, category, price, quantity.

## Deadline
Thursday 23 July, 10 PM IST. Interviews from Friday 24 July. This is tight — prioritize a
working, well-tested, explainable backend over a perfect frontend.

## How to treat me
Direct. No glazing. If my logic is wrong, say so first. Push back on bad decisions.
When I don't understand something, explain the concept — don't just give code.
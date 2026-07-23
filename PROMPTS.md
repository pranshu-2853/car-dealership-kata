# PROMPTS.md — AI Tooling Chat History

This file documents my AI-assisted development process for the Car Dealership Inventory
System kata, per Incubyte's AI Usage Policy. It includes prompts from two sessions:
(1) planning/setup conversation with Claude (claude.ai), and (2) implementation session
with Claude Code, working directly in this repository.

---

## Session 1: Planning & Setup (Claude, claude.ai)

Used for: project setup, pom.xml configuration, git/GitHub setup, understanding TDD
concepts, and reasoning through design decisions before implementation.

### Setup & environment

- "and what to use in this i m always confused in this i have a folder named car-dealership-kata in directly inside d dreive always using this creates a loop of folders so help me here after that we will create a repo"
- [Shared pom.xml, asked whether Spring Boot version needed changing, whether to use Docker/PostgreSQL given existing Docker images for Postgres and Redis]
- "Spring Boot [4.1.1/4.1.0/4.0.8/4.0.7] there is no 3.5.13 seen in above selection bro what to choose now and check any same type of changes needed or not??"
- [Shared corrected pom.xml, asked about spring-boot-starter-test bundling and H2/Postgres scope reasoning]
- [Asked whether JDK 24 vs 21 matters given java.version=21 in pom]
- "should i use intelliJ ultimate 2025 or like vs code only as it dont consume much power like inteliJ as a point will come where dokcer and other things are going on also" — followed up with "yes it has 16 GB ram so not a issue"

### Git & repo setup

- [Shared mvnw test output, confirmed BUILD SUCCESS, asked next steps for git init vs GitHub repo order]
- "should not we keep claude.md in gitignore as i guess do we need to push it into repo or what should we do"
- [Asked to correct CLAUDE.md commit message format for PowerShell, and Incubyte's required Co-authored-by trailer format]
- "should not we make in format they want we are not using that bro" [re: multi-line commit message syntax in PowerShell]
- "readme is written by claude bro" [clarifying which commits need AI co-author trailers]
- "I am asking that we should now create the Github repository And connect it with the Our git folder And after that we should connect the cloud code with Selected folder on local machine Is this the approach can you give me the steps"

### Understanding the kata & architecture

- "Before doing anything like this Tell me exactly What I should do Keep the things consistent on Jdk and like the poem xml file is generally used for the compile dependency purpose..."
- "First let's understand what exactly is in the TDD Kata First we should understand what exactly the architecture they want like what they want us to do explain that in simple terms..." [requested plain-language explanation of TDD, architecture, and project approach before writing code]
- "which models to use tell me thats also a good question here" [Claude model selection for chat vs Claude Code]

### Claude Code integration & first feature planning

- "i m using claude code desktop version we use this desktop version or which one exactly?? and how to implement it Like I think we should follow the modular monolithic approach And we should create the folders like in the thing we I have created so far I have always created the Authentication folder first Can you tell me like how should I create the things here..."
- [Shared Claude Code's build-order plan (Vehicle-first, not auth-first), asked to explain the reasoning and confirm understanding]
- "why the test file for vehicle service is created not for the vehicle controller I have these questions" [asked why service-layer test comes before controller test]
- "So it is asking that it will first create the vehicle service object... my question is that like for the failing test do we have to commit that this is the failing test for vehicle class or like we have to just commit after the whole complete working phase" [asked whether RED state gets its own commit]
- "should i do this way like first give the green code like the correct code and then in the refactor section I should use this Reflection and no argument constructor thing" [proposed manufacturing a fake refactor step — reconsidered after feedback]

### Design decision: setId and JPA reflection

- "I have a question here and why we you did not use the reflection here like the number argument constructor" [asking about no-arg constructor requirement for JPA]
- "So be aware of the gators and setters you are giving me why are you giving me the gutters and setters of the id... Please be assure of like whatever the code you are giving me don't give me the code that make me shame in the interview"
- "What reason I should give of the using the set id as a setter... If it is a primary key then it should not be changeable I guess Please be correct with the decisions you are making Dont make random hazy decision"
- "I guess the old way is a better way sorry to say that... doing this type of thing will make my decision more harder" [decided to keep public setId for simplicity/familiarity under time pressure, after weighing tradeoffs]

### Vehicle creation — controller layer & explain-back

- "Sorry to interrupt but I guess the controller is pending" [caught that Create was incomplete without a VehicleController before moving to List]
- [Reviewed Claude Code's plan for VehicleControllerTest — @WebMvcTest, @MockitoBean, addFilters=false — asked to explain @WebMvcTest vs @SpringBootTest, why the service is mocked, and the thenAnswer vs thenReturn distinction]
- "yes it goes green what i need to explain here..." [gave explain-back attempt; corrected to include the @AutoConfigureMockMvc(addFilters = false) / security reasoning that was initially missed]
- "No I did not wrote the code By my hand I use the AI so mention that in the commit" [corrected commit attribution — AI wrote VehicleController, not hand-written]

### List vehicles — planning & judgment calls

- [Asked for List-vehicles plan; Claude Code recommended controller-test-only, no service test, reasoning that findAll() is a pass-through with no branching logic to test]
- "Even after reading something Some part of this text I did not understand what exactly it was trying to tell so tell me... And what percent of back end is done... please consider of writing the code in perspective of writing the front end also" [asked for plain-language breakdown of @WebMvcTest internals, the padding-test vs meaningful-test distinction, the empty-list-200-vs-404 design decision, and the flagged @DataJpaTest gap; also asked for backend completion estimate and frontend-integration concerns]
- [Reviewed the two List tests (returnsAllVehicles, returnsEmptyListWhenNoVehicles), the private vehicle() test-data-builder helper, and the String-not-BigDecimal constructor reasoning]
- "So the thing I need to remember here is that then answer method will grab the thing that we have passed..." [confirmed understanding of thenAnswer vs thenReturn distinction]
- "You said that the answer will inspect and react to what was actually passed what does react mean" [asked for precision on what "react" means in thenAnswer — confirmed it means inspect + optionally mutate + return]

### Search feature — repository-layer testing decision

- [Asked whether Search should be tested at service, controller, or repository layer, given real filtering logic exists]
- [Confirmed understanding: repository layer tested directly via @DataJpaTest since filtering logic lives in the JPQL query, not the service; mocked-repository service tests can't prove filtering actually works]
- "why the case sensitivity is there why we not match the capital T with the small T" [asked why case-insensitive matching wasn't implemented; confirmed as a deliberate deferred decision, not an oversight]
- "Today's time is 5:00 PM on 22nd July and before tomorrow 12 AM I have to submit it" [flagged real time pressure; triggered a scope/pace triage — batching remaining search filter tests, full TDD reserved for Purchase/Auth only, no frontend tests]
- [Reviewed batched implementation of model/category/price-range filters plus the "no filters returns everything" regression test — all 4 repository tests green]
- [Asked for detailed explanation of Mockito's all-or-nothing matcher rule (eq/isNull) and ArgumentCaptor usage for BigDecimal in the search controller test]
- [Confirmed full test suite green — 10 tests total across VehicleServiceTest, VehicleControllerTest, VehicleRepositoryTest]

### Purchase — full TDD cycle (the feature with real logic)

- [Reviewed plan for Purchase: service-first, happy path before edge cases, custom exception for insufficient stock (409) vs IllegalArgumentException for non-positive quantity (400), separate test for vehicle-not-found (404)]
- "So the thing I need to remember here is that then answer method will grab the thing that we have passed" → extended to Purchase's ArgumentCaptor usage, confirming why checking the object's untouched state (not just verify(never()).save()) matters — a managed entity could persist via dirty checking even without an explicit save call
- "why we are not using less than equal to" [asked why the stock guard uses < not <=, confirming that purchasing exactly the remaining stock must succeed]
- [Caught and corrected two commit-attribution mistakes — initially said "I wrote this by hand" for AI-generated code, corrected both before pushing]
- [Reviewed VehicleNotFoundException replacing bare orElseThrow(), understanding why a domain-specific exception is needed for @RestControllerAdvice to map cleanly to 404 instead of a generic 500]
- [Reviewed rejectsNonPositivePurchaseQuantity — asked in detail why quantity <= 0 fails with IllegalArgumentException before findById is even called, and why verify(never()).findById() proves validation-before-database-access ordering]
- [Confirmed @Transactional added with no dedicated test — corrected own misunderstanding that this was about H2 vs Postgres; clarified it's about VehicleServiceTest mocking the repository entirely with Mockito, which has no real transaction manager to observe]
- [Reviewed the controller batch: GlobalExceptionHandler (web package, shared across features), ApiError as a record, @Import(GlobalExceptionHandler.class) in @WebMvcTest, and why controller tests assert exact message text while service tests only check message contains key facts]
- "So we say the service test will handle the business logic... and the controller test will take care of the exact response text" [confirmed understanding of the service-test vs controller-test distinction]

### Restock, Update, Delete — batched given time pressure

- "Today's time is 5 PM... I think we should batch things" [triggered explicit pace change: Restock/Update/Delete get tests+implementation together, no separate RED commit, since they mirror Purchase's already-documented cycle]
- [Reviewed Restock's asymmetric default — Purchase defaults quantity to 1, Restock requires it explicitly, since a warehouse delivery always has a known count]
- [Reviewed the findOrThrow/requirePositive refactor extracted from duplication between purchase() and restock() — asked to clarify this was a shared method, not a shared test]
- [Asked whether a request DTO should be added now, given the id-overwrite risk exposed by updatesAllEditableFieldsButKeepsTheOriginalId — decided to defer, since the service layer already defends against it correctly, and Auth is the higher-priority remaining risk]
- [Reviewed why delete() uses findOrThrow before repository.delete() rather than deleteById(), since deleteById silently no-ops on a missing id in newer Spring Data]
- [Confirmed full backend vehicle domain complete — Create, List, Search, Purchase, Restock, Update, Delete all tested and green]

### Authentication — planning and design corrections

- [Reviewed Claude Code's auth build order: User entity → register/hashing → duplicate username → JwtService → login → AuthController → SecurityConfig + JWT filter → SecurityIntegrationTest]
- "I genuinely don't understand why we don't need a user detail service because in my perspective the user detail service is the only format that the spring security will understand" [pushed back on the suggestion to skip UserDetailsService; corrected understanding that Spring Security needs an Authentication in the SecurityContextHolder, and UserDetailsService is one way to produce it — but insisted on including it anyway so a deleted or demoted user loses privileges immediately rather than at token expiry. This decision was adopted.]
- [Also overrode the either/or framing of URL rules vs @PreAuthorize — decided on both, with @PreAuthorize on the **service** methods rather than controllers, so existing @WebMvcTest controller tests keep passing since the service is a Mockito mock there and method security stays inert]
- "Don't plan around cutting RBAC. Auth must ship complete" [rejected the proposed fallback of shipping authenticated-only security if JWT ran long]
- [Asked for a full explanation of how a JWT filter sets the Authentication object, and how that differs from the form-login/DaoAuthenticationProvider path]
- "What is the production default value what exactly does it mean" [asked how Spring's ${VAR:default} environment override pattern works, coming from a .env / .env.example habit in Node projects]
- [Asked for a detailed explanation of CORS: why preflight OPTIONS requests are rejected by the security filter chain before reaching a controller, why @CrossOrigin doesn't fix it, and what a CorsConfigurationSource bean plus .cors(Customizer.withDefaults()) actually does]
- [Reviewed the seeder gating decision — anything running at application startup also runs under @SpringBootTest, so the admin seeder is gated behind a config property switched off in test properties]

### Authentication — implementation review

- [Reviewed the register test: real BCryptPasswordEncoder rather than a mock, since a mocked encoder would let register store plaintext and still pass. Asserting both "not equal to plaintext" and "matches() returns true" proves a real hash was produced]
- [Reviewed @Table(name = "users") — USER is a reserved word in PostgreSQL, so DDL generation would fail against the real database while H2 let it through]
- [Reviewed role defaulting to USER server-side, never read from client input; asked what "admin accounts get created out of band" means]
- [Reviewed rejectsTokenSignedWithADifferentSecret — a second JwtService with a different key forges an ADMIN token and the real service rejects it. JWT claims are readable by anyone, so signature verification is the only real control]
- [Reviewed rejectsExpiredToken using a negative expiry so the token is born expired, avoiding Thread.sleep in tests]
- [Reviewed the username-enumeration decision: unknown username and wrong password throw the same exception with the same message, so an attacker cannot probe which accounts exist]
- [Reviewed why DTOs earn their place for auth but not for Vehicle — User has a field (the BCrypt hash) that must never cross the wire, proven by asserting the password field does not exist in the response]
- "Can you explain me exactly how the things are working in Spring Security" [asked for the full end-to-end flow — register, login, and every subsequent request — and had three misunderstandings corrected: that passwords are stored at login (they are hashed at register and only compared at login), that the JWT filter compares passwords (it verifies a signature and looks up current role), and the precise relationship between SecurityContextHolder, SecurityContext, and the Authentication object]
- [Asked for a detailed explanation of why JwtAuthenticationFilter must NOT be a @Component: @WebMvcTest instantiates Filter beans, and the filter's JwtService and UserDetailsService dependencies do not exist in that slice, so the context fails to load. addFilters = false does not help because it prevents filter execution, not bean construction. Required several passes to fully understand — resolved by tracing what loads in the real app vs @WebMvcTest vs @SpringBootTest]
- [Asked why 401 and 403 must be distinguished, and what practical difference it makes to the frontend — 401 redirects to login, 403 shows a permission message without logging the user out]
- [Reviewed hasRole vs hasAuthority and the ROLE_ prefix convention, and why @EnableMethodSecurity is required or @PreAuthorize is silently inert]
- [Reviewed SecurityIntegrationTest as the only place the filter chain and @PreAuthorize actually execute, and why explicit deleteAll() in @BeforeEach is required since @SpringBootTest does not roll back between tests unlike @DataJpaTest]

### Real database integration

- "I have a question here you are not considering the PostgreSQL on the docker thing we have to first test the authentication with that" [flagged that the assessment requires a real database and the app had never actually been run outside of tests — only test suites had ever executed]
- [Wired Postgres via Docker, ran the application for the first time, and manually smoke-tested register, login, protected endpoints, and RBAC through Postman and PowerShell]
- [Reviewed ddl-auto: update vs create-drop, and why Flyway or Liquibase would be correct in production]

### Bug hunt 1 — 401 returned instead of 403

- [Manual testing found DELETE with a valid USER token returning 401 instead of 403, while SecurityIntegrationTest stayed green]
- [Rejected an initial hypothesis about a missing accessDeniedHandler after Claude Code corrected the mechanism — Spring's default handler returns 403 for an authenticated principal; the entry point only fires when the context is anonymous]
- [Root cause confirmed by reproducing against the running app: sendError triggers a Tomcat ERROR dispatch to /error, the security chain runs a second time, OncePerRequestFilter.shouldNotFilterErrorDispatch defaults to true so the JWT filter is skipped, the context is anonymous, and 401 overwrites the correct 403. MockMvc does not perform ERROR dispatch, so the suite was structurally blind to it]
- [Verified the new RANDOM_PORT regression test actually fails without the fix by stashing the SecurityConfig change and running it — a regression test never seen failing is a test not yet trusted]

### Bug hunt 2 and 3 — search failing on PostgreSQL

- [Search returned 500 from the browser though all repository tests passed on H2. Asked for the real stack trace before any fix rather than guessing]
- [Bug 2 root cause reproduced directly against the live Postgres container: could not determine data type of parameter — blank filters bind as untyped nulls, Postgres types parameters at parse time, and a bare :param IS NULL gives it nothing to infer from. Fixed with explicit CAST on all five parameters]
- [Bug 3 surfaced after that fix: cannot cast type bytea to numeric. Hypothesised that a null parameter arrives as unspecified binary the server treats as bytea, that bytea → varchar is legal while bytea → numeric is not (explaining why only numeric filters failed), and that a double cast through text would resolve it. All three cast cases were then confirmed on the live container before the fix was applied]
- "Why the hell it is going to work in the wrong direction It was working properly why it go into this way" [frustration at a second consecutive Postgres bug — clarified that search had never actually worked against Postgres, only against H2 tests, so nothing regressed; the first fix addressed parse-time and exposed a separate execution-time failure]
- [Both fixes verified against live Postgres for the all-null search and an inclusive price range, plus the full 53-test suite]

### Frontend integration and error handling

- [Wrote the Lovable specification prompt containing the exact verified API contract — endpoints, request and response shapes, status codes, and the Authorization header format — after capturing real responses from the running backend]
- "should we add image of each car" [considered adding vehicle images; decided against it as scope creep not requested by the specification, with copyright and time cost outweighing a purely cosmetic gain]
- [Reviewed the case-insensitive search implementation — LOWER() on both sides, since wrapping only the parameter would still miss a stored "Ford" when searching "ford". Noted the index tradeoff]
- [Found the quantity inputs coerced empty values back to 1 on every keystroke, making them impossible to edit; fixed to hold string state while typing with the fallback applied on blur]
- [Caught that the fix wrongly applied purchase's fallback-to-1 to restock, which requires an explicit positive quantity with no default — corrected so restock blocks submission and shows a validation error instead]
- [Found the global 401 handler was logging users out and showing "Session expired" for a failed login, since /api/auth/login also returns 401 for bad credentials. Fixed so the logout and redirect fire only for a 401 on a non-auth endpoint, and all statuses prefer the backend's message field]
- [Used Antigravity for a later pass on UI design and component layout, and to restructure the repository into backend/ and frontend/ folders]

---

## Session 2: Implementation (Claude Code, in-repo)

> These prompts are reconstructed from what was relayed into the planning chat, since
> Claude (claude.ai) cannot read the Claude Code session directly. The exchanges above in
> Session 1 capture the reasoning behind each one in full.

### Vehicle domain
- "Read CLAUDE.md. Give me the TDD feature sequence — which endpoint first, exact test-first order, one feature at a time. Don't write code yet."
- "Agreed on build order and entity-not-DTO. For now, only write the failing test for VehicleService.create()... Do NOT create the Vehicle entity, VehicleRepository, or VehicleService yet."
- [Requested removal of public setId from Vehicle; test updated to use ReflectionTestUtils]
- [Requested revert of ReflectionTestUtils back to public setId, citing time constraints and interview-round familiarity]
- "Vehicle creation is done and committed. Next: list all vehicles. Give me the plan for VehicleControllerTest only, no code yet."
- "Search is next. Should this be tested at the service, controller, or repository layer? Give me the plan, no code."
- "Time-constrained — combine the remaining search filters into fewer, larger test-then-implement steps."
- "Purchase gets full TDD treatment since it has real logic. Plan only, no code yet."
- "Restock, Update, Delete — batch them, tests and implementation together, no separate RED commits since they mirror Purchase."

### Authentication
- "Backend vehicle domain is complete. Next: Auth — register, login, JWT, then locking Delete/Restock to ADMIN. Full plan before any code."
- "Include UserDetailsService. Without a per-request lookup a deleted or demoted user keeps their token's privileges until expiry."
- "Use both URL rules and @PreAuthorize — @PreAuthorize on the service methods, not the controllers, so my existing @WebMvcTest tests keep passing."
- "Don't make the jjwt dependencies their own commit — batch them with the User entity."
- "Also add CORS config for the frontend origin while you're building the filter chain."
- "Wire the admin seeder — creates one ADMIN if none exists, credentials from config, gated behind a property switched off in test properties."

### Real database and bug hunts
- "Wire the main application.yaml to PostgreSQL with environment variable overrides and local dev defaults. Tests stay on H2."
- "DELETE with a valid USER token returns 401 instead of 403 against the running app, though SecurityIntegrationTest is green. Check SecurityConfig — reproduce before fixing."
- "Search returns 500 against real Postgres though VehicleRepositoryTest passes on H2. Here's the stack trace. Reproduce it against the live container before applying any fix."
- "New error after the CAST fix: cannot cast type bytea to numeric. My hypothesis is a null param arrives as bytea and bytea → numeric is illegal while bytea → varchar is legal. Verify against the live container before changing the Java."
- "Make search case-insensitive — LOWER() on both sides. Write the test first."

### Frontend
- [Full Lovable specification prompt — API contract, auth flow, page structure, admin UI rules, design direction, and a code-style constraint requiring all API calls in a single src/lib/api.ts wrapper with plain React Context for auth state]
- "Search every numeric input for the pattern that forces the value back to a fallback on every keystroke, and fix it everywhere — allow empty while typing, apply the fallback on blur."
- "Restock must not default to 1 — block submission and show a validation error if empty or zero. Purchase's fallback stays."
- "Frontend error handling is swallowing backend messages — a failed login logs the user out and shows 'Session expired'. Fix so 401s from auth endpoints surface the real message without logging out."

---

## Reflection on AI usage

Four tools, in distinct roles: **Claude (claude.ai)** for planning and understanding,
**Claude Code** for in-repo implementation under a committed `CLAUDE.md`, **Lovable** for
generating the React frontend from a verified API specification, and **Antigravity** for a
later UI pass and the repository restructure.

The discipline that mattered most was the explain-back gate — restating each feature with
the file closed before advancing. It repeatedly caught things I had accepted without being
able to justify.

I reversed AI suggestions where I disagreed: keeping the public `setId` over a
reflection-based test (unfamiliar code is a liability in a live pairing round), insisting on
`UserDetailsService` against the recommendation to skip it (a demoted user should lose access
immediately, not at token expiry), and rejecting the proposal to descope RBAC if auth ran
long.

AI was least reliable at anything that only manifests at runtime — all three bugs above passed
a fully green test suite, and two of them were PostgreSQL-specific behaviours H2 structurally
cannot reproduce. The pattern that worked was refusing to let the tool guess: every fix began
by reproducing the failure against the live database or running server, and twice the first
hypothesis was wrong and the reproduction corrected it. The tool was consistently better at
investigating a real error than at predicting one.

Full attribution is per-commit via `Co-authored-by` trailers, with each commit body stating
what the AI generated versus what I decided.
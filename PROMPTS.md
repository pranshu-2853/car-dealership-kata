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

---

## Session 2: Implementation (Claude Code, in-repo)

> NOTE: The prompts below need to be filled in from the actual Claude Code session log,
> as that conversation happened in a separate tool and isn't visible to Claude (claude.ai).
> Append the real prompts here as the build progresses. Rough sequence so far, based on
> what was relayed back to the planning chat:

- "Read CLAUDE.md. Give me the TDD feature sequence — which endpoint first, exact test-first order, one feature at a time. Don't write code yet."
- "Agreed on build order and entity-not-DTO. For now, only write the failing test for VehicleService.create()... Do NOT create the Vehicle entity, VehicleRepository, or VehicleService yet."
- [Requested removal of public setId from Vehicle, and update to test using ReflectionTestUtils]
- [Requested revert of ReflectionTestUtils change back to public setId, citing time constraints and familiarity]
- [Next: requested plan for GET /api/vehicles list endpoint, test-first]

---

## Reflection on AI usage (to expand in README)

AI was used as a planning and pairing partner, not a code generator. Key discipline:
one feature at a time, test-first, minimal implementation, explain-back before advancing.
Notably reversed an AI-suggested design change (reflection-based id assignment) after
weighing the tradeoff against interview-round time pressure — documented in commit history
and above.
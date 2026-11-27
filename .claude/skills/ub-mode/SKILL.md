---
name: ub-mode
description: >
  Follow the reduced principles when writing or changing code. Every change
  follows First Make it Work, then Then Make it Right, then Follow Practice.
  Use when the user runs /ub-mode or asks for the reduced principles.
---

# ub-mode

Every time you are asked to make a change follow:
* First Make it Work.
* Then Make it Right.
* Follow Practice

# First Make it Work

## Laziness Protocol

* One decision lives in one place.
* Ship the smallest edit that solves the problem.

## Foundational Thinking

* Get the data shape right before you write the logic.
* Do first whatever helps every later phase.
* If a concurrent writer would matter, do not share state.

## TDD

* Write the failing test before you change the code.
* See it fail for the reason you expect.
* Make the smallest change that makes it pass.
* If an honest test is impractical, do not write a bad one.
* In SportKid, test first what is pure Kotlin: state holders (`<Screen>State`: `toggleSport`, `matches`, `toDraft`, Saver round-trip), formatters (`DateLabels`, money, swim/race times) and `core:model`. JVM tests go in the module's `src/test` (`./gradlew :feature:<x>:testDebugUnitTest`); add `testImplementation(libs.junit)` to the module if it is missing.
* Interaction goes in a Compose UI test on the Screen with `*Samples` data (`connectedDebugAndroidTest`, emulator `ANDROID_SERIAL=emulator-5554`).
* Pure visual changes have no honest unit test: the check is `@SkPreviews` (light, dark, font 1.3) plus the emulator capture against the Figma node (`scripts/ui-catalogo/capturar.py <id>`).

## Redesign From First Principles

* Do not bolt a new requirement onto the old design.
* Ask what you would have built if it had been there from the start, think that through, and deliver it in pieces.

## Attack the Premise

* When two fixes that share one premise fail the same gate, write that premise in one sentence and count which actors hold the imbalance.
* Remove the assignment.
* Do not start another fix until you have done that.

## Subtract Before You Add

* Take complexity out before you add anything.

## Outcome-Oriented Execution

* Aim at the end state you can check.
* A break in the middle is allowed when you planned it and can undo it.

## Build the Lever

* If the work is not obvious at a glance, build the tool that does it.
* Do one unit by hand, then make the tool match that unit.

## Architecture

### Model the Domain

* Put the domain in a structure that makes the illegal states impossible.
* Maintain a glossary of domain terms: in SportKid it is README §5 (state vocabulary) and the enums of `core:model` (`Sport`, `Belt`, `MedalResult`, `PaymentStatus`, `FamilyRole`).
* If the code is already clear and local, leave it.

### Boundary Discipline

* Check the data where it enters.
* Inside, trust it.
* Keep the business logic in a pure function.

### Type System Discipline

* Make the illegal value impossible to write.
* Parse outside data once, where it comes in.
* Do not silence the compiler.

### Make Operations Idempotent

* Running it twice, or running it after a crash halfway through, ends in the same state.

### Separate Before Serializing Shared State

* If they do not need the same mutable thing, give each its own.
* If they do, allow one writer, and enforce that in the structure.

# Then Make it Right

## Fix Root Causes

* Reproduce it.
* Fix the cause.
* A guard that only hides the crash is not a fix.

## Sequence work into verifiable units

* Finish one small unit and check it.
* Then start the next.

## Test Behavior, Not Implementation

* The test calls the code the way its user does and checks the result that user would see.
* If the test would still pass when every function returned nothing, throw it out.

## Complexity

* There is no CRAP tool here (TD-SK-001: no coverage yet; TD-SK-008: Detekt not wired). Use size as the proxy:
  `find app core feature -name '*.kt' -not -path '*/build/*' | xargs wc -l | sort -rn | head`.
* A composable over 60 lines is split into `private` sections; a file you create stays under 400 lines; a file you touch over 400 lines does not grow (TD-SK-005).
* Logic with branches leaves the composable for a pure function or the state holder, where it can be tested.
* `when` over an enum of `core:model` that maps to text, icon and color lives once in `core:ui` (README §5); do not reduce it further.

## Duplication

* There is no dryer tool. Before writing a piece, run `/catalogo-ui-sportkid consultar <piece>` and
  `grep -rn "fun Sk\|^fun [A-Z]" core/designsystem core/ui --include=*.kt`.
* A piece that appears in two features moves to `core:ui` (or to `core:designsystem` with prefix `Sk` if it has no domain), as `FormCard`, `FeeRow` and `AthleteSportHeader` did.
* A drawable from Figma is compared with `diff` against the ones in `core` and the feature (Assets tab of the catalog) before adding it.
* One state, one name, one presentation: never redefine a label, icon or color of README §5 in a feature.

## Coverage and Dead Code

* There is no mutation tool. For each test you write, break the code it guards on purpose (flip a condition, return the default) and confirm the test fails; then restore it.
* Cover the uncovered: the state holder or formatter you touched gets its test (TD-SK-001 only shrinks).
* Delete dead code: `./gradlew lint` reports `UnusedResources` (strings, drawables); remove unused `*Samples` entries, previews of deleted components and resolved `TODO`s.
* After removing a component or screen, regenerate the catalog (`python3 scripts/ui-catalogo/generar.py`) and update README §1–§4.

## Refactoring

* Pin what the code does.
* Change the structure.
* Prove the behavior did not change.

# Practice

## Keep a Log

* Keep a log of every action you take and the reason you took it.
* Report that log upon completion.

## Guard the Context Window

* Keep the main thread small.
* Send the bulk elsewhere, and keep the summary.

## Worktree Cleanup

* Find what is safe to remove, confirm nothing still in use needs it, then reclaim the disk.
* Uncommitted work waits for a decision.
* Each app of the template lives in its own branch (`sport_kids`, `multi_game_compose`…): `build/` folders left by another branch (`feature/games`, `feature/profile`, `feature/records`) are safe to delete; source files are not.
* Scratch scripts (SVG → VectorDrawable converter, temporary captures) go to the scratchpad, never to the repo.

## Commit and Push

* Commit only when asked; then commit often, in small ordered commits.
* Subject `<type>(<module>): <imperative description>` with type `feat|fix|refactor|test|docs|chore|build|revert` (`feat(home): …`, `docs(catalogo): …`).
* The body says why, what changed, and how you proved it (build, tests, emulator capture).
* A change visible to the user adds its fragment in `changelog.d/`; a change of convention updates `CLAUDE.md` and runs `python3 .github/scripts/sync_claude_to_copilot.py`.
* Push to `sport_kids`, the main line of this app. Never to `main`: it is the shared template.

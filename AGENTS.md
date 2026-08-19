# Workspace Agent Guidelines & Skills

This repository is equipped with **Matt Pocock's Agent Skills** (`mattpocock/skills`) located under `.agents/skills/`.

## 🛠️ Installed Skills Overview

### 1. 🎯 Engineering & Design Workflows
* **`ask-matt`**: Router to choose the right skill or flow for your current situation.
* **`grill-with-docs`**: Socratic interview to stress-test plans/designs while capturing ADRs (Architecture Decision Records) and project glossary.
* **`codebase-design`**: Vocabulary and principles for designing deep modules, seams, and clean architecture.
* **`domain-modeling`**: Build and refine domain models, terminology (`CONTEXT.md`), and ADRs.
* **`prototype`**: Rapid throwaway prototypes to sanity-check logic, UI, or state models.
* **`research`**: Structured investigation against high-trust primary sources with findings written to docs.
* **`to-spec`**: Transform conversations and requirements into a clear technical specification.
* **`to-tickets`**: Break specs and plans into tracer-bullet vertical slice tickets with blocking dependencies.
* **`wayfinder`**: Map massive chunks of work into decision tickets on your tracker.
* **`improve-codebase-architecture`**: Scan codebase for deepening opportunities and present an interactive visual report.

### 2. 🧪 Implementation & Quality
* **`tdd`**: Test-Driven Development following the red-green loop on vertical slices at pre-agreed seams.
* **`implement`**: Step-by-step implementation driven by specs or ticket sequences.
* **`code-review`**: Systematic two-axis code review against fixed points, specifications, and project standards.
* **`diagnosing-bugs`**: Systematic diagnostic loop for difficult bugs and performance regressions.
* **`resolving-merge-conflicts`**: Safely resolving complex git merge/rebase conflicts.
* **`wizard`**: Generate interactive step-by-step human guidance for manual infrastructure or external tool tasks.

### 3. 🚀 Productivity & Collaboration
* **`grill-me` / `grilling`**: Socratic questioning session to sharpen plans, resolve ambiguities, and explore design trees.
* **`handoff`**: Compact conversation and context into a clean handoff document for another agent or session.
* **`teach`**: Interactive teaching workflow for concepts and skills.
* **`to-questionnaire`**: Convert unresolved decisions into structured questionnaires for stakeholders.
* **`writing-for-agents`**: Guidelines for writing agent-friendly documentation, skills, and prompt files.
* **`wait-what`**: Re-pitch or re-frame ideas when instructions don't land effectively.

### 4. 🧰 Utilities & Tooling
* **`setup-matt-pocock-skills`**: Configure issue tracker (GitHub, GitLab, local files) and triage labels.
* **`triage`**: Process issues and PRs through structured triage states.
* **`setup-pre-commit`**: Configure Husky and lint-staged hooks.
* **`setup-ts-deep-modules`**: Enforce deep module boundaries with dependency-cruiser.
* **`git-guardrails-claude-code`**: Guardrails against accidental destructive git commands.

---

## Agent skills

### Issue tracker

Issues live as markdown files under `.scratch/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Canonical triage roles mapped 1:1 to label strings (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context repo (`CONTEXT.md` and `docs/adr/` at the root). See `docs/agents/domain.md`.

---

## 💡 How to Trigger Skills
You can activate any skill by:
1. Mentioning the skill by name (e.g. *"let's do tdd on this feature"*, *"grill me about the architecture"*, *"ask matt how we should proceed"*).
2. Using slash commands or requesting specific workflows (e.g. `/grill-me`, `/tdd`, `/code-review`, `/to-tickets`).


# 01: Room Data Infrastructure and Entities

**What to build:** An offline-first, persistent database layer using Android Jetpack Room to store focus sessions, custom task tags, cat profiles, and badge milestones, pre-populated with initial default data.

**Blocked by:** None (can start immediately)

**Status:** resolved

- [x] Room database dependencies added to `app/build.gradle.kts` (ksp/kapt or Room runtime + compiler + ktx).
- [x] Entities defined: `FocusSessionEntity`, `TaskTagEntity`, `CatProfileEntity`, `BadgeEntity`.
- [x] DAOs implemented with reactive Kotlin `Flow` queries and CRUD functions for sessions, tags, cats, and badges.
- [x] Room database singleton configured with pre-population callback for 6 default TaskTags, 5 CatProfiles (Orange Tabby unlocked by default), and starter Badges.
- [x] `AppDatabase` builds successfully and repository classes provide clean abstractions for downstream features.

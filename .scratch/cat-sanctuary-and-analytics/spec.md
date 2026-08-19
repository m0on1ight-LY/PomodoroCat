# Feature Specification: Cat Sanctuary, Task Tags, and Focus Analytics

**Status:** `ready-for-agent`

## Problem Statement

Users of PomodoroCat currently have a single-session timer and white noise player, but their focus history is not persisted across app launches. Users cannot categorize focus sessions by task or topic, cannot review long-term productivity trends or daily reflections, and lack persistent emotional rewards or progression incentives to motivate continuous daily focus habits.

## Solution

Transform PomodoroCat into a comprehensive focus companion app that merges productivity management with gentle gamification:
1. **TaskTag & Quick Switching**: Provide preset and user-defined task tags with custom icons and colors, selectable directly from the main focus screen.
2. **Local-First History & Diary**: Persist every focus session in a local Room database, capturing start/end timestamps, durations, tags, completion states, user reflections (FocusDiary), and 1-5 star ratings.
3. **Multi-Dimensional Analytics**: Offer daily, weekly, monthly, and all-time analytics dashboards featuring total focus time, dried fish earnings, tag distribution charts, focus streak heatmaps, and a chronological diary stream.
4. **Cat Sanctuary Gamification**: Establish an in-app economy where focus time generates DriedFish currency (1 min = 1 fish), allowing users to unlock 5 distinct cat companion profiles, feed them to increase BondLevels (Lv.1 to Lv.5), unlock procedural animations, accessories, personality dialogues, and earn achievement badges.
5. **Modern Tab Navigation**: Integrate a 3-tab Material 3 bottom navigation bar (`Focus`, `Analytics`, `Sanctuary`) for fluid one-handed navigation.

## User Stories

1. As a user, I want to see preset task tags (Work, Study, Reading, Exercise, Creation, Meditation) on the main screen, so that I can quickly tag my current session.
2. As a user, I want to create, edit, and delete custom task tags with custom names, Material icons, and colors, so that I can tailor the app to my specific workflows.
3. As a user, I want my focus time to automatically earn 1 DriedFish per minute of completed focus, so that I feel rewarded for my hard work.
4. As a user, I want a post-session reflection dialog when a timer finishes, allowing me to record a brief focus note and rate my focus quality (1-5 stars), so that I can review my state of mind later.
5. As a user, I want to view my daily, weekly, and monthly focus statistics, so that I can understand how my time is distributed across different tags.
6. As a user, I want to see a focus streak calendar heatmap, so that I can track my consistency over time.
7. As a user, I want to browse a chronological focus diary timeline, so that I can look back on my past focus notes and achievements.
8. As a user, I want to browse a Cat Sanctuary catalog containing 5 distinct cat breeds (Orange Tabby, Calico, Tuxedo, Siamese, British Shorthair), so that I have long-term unlockable goals.
9. As a user, I want to spend earned DriedFish to unlock new cat companions according to their tier requirements, so that my focus efforts have meaningful in-app value.
10. As a user, I want to feed my active cat companion 10 DriedFish per feeding to increase their Affection EXP and BondLevel (Lv.1 to Lv.5), so that I can deepen our emotional bond.
11. As a user, I want my cat to unlock new procedural animations (such as head tilts, ear twitches, belly rubs, kneading) and accessories (such as graduation caps, bowties) as their BondLevel increases.
12. As a user, I want to earn unique achievement badges (e.g., First Pomodoro, Night Owl, Fish Tycoon, Cat Family Collector), so that I can showcase my milestones on the badge wall.
13. As a user, I want to seamlessly switch between the Focus, Analytics, and Sanctuary tabs via a bottom navigation bar, so that I can access all features effortlessly.
14. As a user, I want all my data stored locally and offline-first without requiring an internet connection or account login, so that my data remains completely private and instantly accessible.

## Implementation Decisions

1. **Local-First Database Layer (Room)**:
   - Entity schemas for `FocusSessionEntity`, `TaskTagEntity`, `CatProfileEntity`, and `BadgeEntity`.
   - DAOs with reactive Kotlin `Flow` queries for live UI updates.
   - Initial database pre-population for default TaskTags (Work, Study, Reading, etc.), the 5 CatProfiles (Orange Tabby default unlocked), and milestone Badges.

2. **Repository Architecture**:
   - `FocusSessionRepository`: Manages recording, querying, and aggregating session metrics.
   - `TaskTagRepository`: Manages tag CRUD and default tag seeding.
   - `CatCompanionRepository`: Manages cat unlocks, feeding, Affection EXP, BondLevel calculations, and active cat selection.
   - `BadgeRepository`: Evaluates milestone criteria after each session/feeding and unlocks achievements.

3. **Navigation & Screen Structure**:
   - Material 3 `NavigationBar` and `Scaffold` host 3 destination composables: `FocusScreen`, `AnalyticsScreen`, and `SanctuaryScreen`.
   - Shared ViewModels / StateFlows injected into screens for clean state separation.

4. **Procedural Vector Multi-Skin Canvas Engine**:
   - Define `CatSkinSpec` data models encapsulating fur color tokens, eye colors, stripe patterns, ear inner colors, and accessory drawables.
   - Enhance `CatCompanion.kt` to dynamically bind the active `CatSkinSpec` and `BondLevel` animation states while preserving zero-allocation Path reuse.

5. **Post-Focus Settlement Flow**:
   - On session completion, trigger an interactive settlement dialog displaying earned DriedFish, fish icon animation, rating selector (1-5 stars), and focus note input.

## Testing Decisions

- **Unit / Repository Tests**: Test Room DAO queries, data aggregation (daily/weekly sums, tag percentages), BondLevel level-up calculations, and Badge unlock trigger rules.
- **Pure Behavior Validation**: Verify that 25-minute sessions accurately credit 25 DriedFish, feeding deducts currency and adds EXP, and reaching thresholds transitions BondLevel from Lv.1 through Lv.5.
- **Seams**:
  - Room DAO / Repository seam for data integrity.
  - TimerService settlement event seam for currency credit.
  - Procedural Canvas spec seam for visual integrity.

## Out of Scope

- Remote cloud sync, user authentication, or social leaderboards (all data is strictly local-first and private).
- In-app purchases with real money (economy uses purely focus-earned DriedFish).
- Audio recording or camera attachments in focus diary.

## Further Notes

- Respect the terminology in [CONTEXT.md](file:///d:/项目/番茄钟/CONTEXT.md) and architectural decisions in [ADR 0001](file:///d:/项目/番茄钟/docs/adr/0001-local-room-database-architecture.md).
- Follow zero-allocation Canvas drawing principles in all new and updated UI components.

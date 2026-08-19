# ADR 0001: Local-First Room Database Architecture for PomodoroCat

## Context
PomodoroCat requires persistent storage for focus session history, custom task tags, cat bonding progress, dried fish currency, and badge achievements. Users expect zero-latency offline operation, privacy, and seamless historical querying across days, weeks, and months.

## Decision
We adopt **Android Jetpack Room Database** as the single source of truth for all structured user data (`FocusSessionEntity`, `TaskTagEntity`, `CatProfileEntity`, `BadgeEntity`). 
Data flows will be exposed via Kotlin `Flow` through dedicated Repositories to ensure reactive UI updates across screens.

## Consequences
- Offline-first: no remote server dependency required for core features.
- High-performance aggregation: Room DAO queries can natively compute daily/weekly statistics, tag distributions, and streaks.
- Schema migrations must be managed through Room migration scripts as new features evolve.

# 02: TaskTag Management and Main Screen Tag Selector

**What to build:** A task tag selection and customization system allowing users to select a preset tag (Work, Study, Reading, etc.) from a horizontal scrollable bar on the main screen, create custom tags with custom names, icons, and colors, and bind the active tag to current focus sessions.

**Blocked by:** 01 (Room Data Infrastructure and Entities)

**Status:** resolved

- [x] Main focus screen displays a horizontally scrollable chip selector showing available task tags with active highlight.
- [x] Clicking a tag selects it as the active focus context for the next timer session.
- [x] An "Add Tag" button opens a dialog allowing users to input a tag name, select a Material icon, and pick an accent color.
- [x] Users can edit or delete existing custom tags.
- [x] Tag selection persists across sessions and passes active tag info to the timer state and database.

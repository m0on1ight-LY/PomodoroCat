# 03: Post-Focus Settlement Dialog and DriedFish Economy Engine

**What to build:** An end-to-end focus completion flow that calculates earned DriedFish currency (1 min = 1 fish), automatically saves the completed `FocusSession` into Room, and presents a celebration dialog where users can review fish earned, rate their focus quality (1-5 stars), and record an optional focus diary note.

**Blocked by:** 01 (Room Data Infrastructure and Entities), 02 (TaskTag Management and Main Screen Tag Selector)

**Status:** resolved

- [x] Focus session completion in `TimerService` triggers a settlement event with calculated DriedFish.
- [x] User's cumulative DriedFish balance in `PreferenceManager` / Room is credited accurately.
- [x] A post-focus settlement dialog appears upon timer finish, showing a cheerful cat animation and total fish earned.
- [x] Dialog includes interactive 1-5 star rating selector and a text field for focus diary notes.
- [x] Saving the dialog updates the `FocusSessionEntity` with the rating and diary reflection.

# Working Doc: Issue 84

<!-- TODO: Delete this file before merge -->
## Impossible to attack a plot that has blocks placed at world build-height limit.

- [GitHub](https://github.com/TownyAdvanced/FlagWar/issues/84)

### Summary

If an attacking player is attempting to flag a plot, and a ceiling exists
above the flag location, the flag will not be placeable. Some defending
players will take advantage of this fact by placing a plot-wide ceiling
at a world's maximum build height.

This issue also extends to worlds with natural ceilings such as worlds
using the default Nether generator settings.

This issue stems from design decisions as old as the general-audience
release of the game. As such, this issue is a natural bug. It is also
classified as a blocker for future official FlagWar releases due to its
exploit-ability.

### Proposed Solution

This issue should be easily addressed by applying a few hard-coded rules
to when and where a war-flag may be placed.

- [x] Allow placing flags under ceilings. (Required)
  - [x] Traditional check: In a world without a natural ceiling, and the highest block is within 5 meters of max height.
  - [x] Add a basic height check:
    - [x] (submersion) checks for if a flag base has a liquid up to 4 meters above it
    - [x] (general space) check same vertical space for any non-empty, non-liquid blocks
  - [x] Add sea-level (minus depth) check
    - Flag base must be at or above a world's sea-level, minus modifier.
    - Default modifier: 12 meters

- [x] Rework Coordinate Broadcasting
  
  - [ ] ~~Specific Audience Options (_Config_)~~
    - This specific plan was scrapped.
  - [x] Location Accuracy (_Config_)
    - towny (_Towny Coord, default_)
    - precise (_Minecraft's X,Y,Z_)

# Working Doc: Issue 84

<!-- TODO: Delete this file before merge -->
## Impossible to attack a plot that has blocks placed at world build-height limit.

- [GitHub](https://github.com/TownyAdvanced/FlagWar/issues/84)

### Summary

If an attacking player is attempting to flag a plot, and a ceiling exists
above the flag location, the flag will not be placable. Some defending
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

- [ ] Allow placing flags under ceilings. (Required)
  - Conform to change in Caves & Cliffs update
    - Valid worlds can now span y:320 &ndash; y:-64. Rules related to
    height should adjust for them.
  - 3-high space required for flag
  - Always broadcast coordinates (see next todo item)
  - Require 1-block space around flag, at timer height.
  - Require above sea level (_Config_)
  - Require above 

- [ ] Coordinate Broadcasting
  - Specific Audience Options (_Config_)
    - everyone
    - direct (_atk + def; default_)
    - defenders
    - allied-def (_def and allies_)
    - direct-plus (_direct, plus allies for both_)
  - Show precise elevation, default false (_Config_) 
  - Location Accuracy (_Config_)
    - General (_Towny Coord, default_)
    - Accurate (_Minecraft's X,Z_)

# Changelog

Latest first.

## STAGING

### Additions
- Add `FlagWarAPI#isUnderAttack(Nation)`
  - Returns true if any town in the supplied nation contains a cell under attack.
- Add `OutlawListener` and listen for the `OutlawTeleportEvent`
  - Outlaws cannot teleport away during an attack while in enemy territory or enemy nation zone during a war.

### Misc. Changes
- Bump Towny API dependency to 0.97.0.17; set as Min_Towny_Version
- Bump Config Version to 1.3 (Post 0.3.0... Woopsie.)
- Removed unnecessary deployment to GitHub Packages

## 0.3.0
General Availability

### Additions
- Added Optional Holograms for War Flags (Courtesy of @gaffy00 with #58)
  - Requires HolographicDisplays to function.

### Fixes
- Restore WarZoneListener Functionality

### Misc. Changes
- API: Removed `CellUnderAttack#isFlag(Block)`, previously deprecated.
  - Use `#isFlagTimer(Block)`, or `#isFlagPart(Block)`
- Removed `alternatives` Maven profile
- Update Build Targets: Java 16, Paper 1.17.1, Towny 0.97.0.12

## 0.2.0
Development Release

### Additions
- Added a Checkstyle configuration, based on traditional Sun style guidelines with some modified rules. (Non-Gameplay)
- Added a Messaging utility class, with debug methods. Config option to enable debug messaging. (PR #40)
- Rolling, viewer-friendly Changelog (this document).
- Add ability to change War Flag timer materials. (PR #49, @gaffy00)
    - Phase timing can be adjusted by increasing the ratio of specific materials.
    - Example: Using `matX, matX, matX, matY, matY, matZ` produces a flag with three phases, at a 3:2:1 timing ratio.

### Fixes
- Fix incorrect formatting of colored messages containing formatting characters. (PR #48, @gaffy00)
- Fix Localization Issues with parsing Regions and Region Variants (PR #43, @Mrredstone5230)
- Fix beacons not rendering (PR #40)
- Change `config:flag.waiting_time` to work as expected. Now reflects total length of a flag, not of the flag phase.
  (PR #38)

### Regressions
- Disabled WarZoneListener, has some issues with how it's functioning that negatively affects gameplay.

### Misc. Changes
- Bumped Dependancies
    - Towny 0.96.7.4 >> 0.97.0.6 (0.97.0.5 Minimum Req.)
    - Spotbugs-Annotations 4.2.1 >> 4.2.3 (Compile Only)
    - Jetbrains' Annotations 20.1.0 >> 21.0.1
    - Checkstyle >> 
    
- Upstream Deprecations
    - Removed EconomyException (deprecated)
    
- Code Cleanup / Refactoring
    - Make code CheckStyle compliant and enforce through Maven.
    - Target Java 11 & MC 1.16+.
    - Switched to PAPER as the Bukkit API provider.
    
### Trivia
- Was initially to be 0.1.2, bumped due to introduction of debug messaging and alternate flag materials.

## 0.1.1
Development Release (Broken)

### Additions
- Add Localization feature for supporting multiple languages. Uses ResourcePacks.
    - Adds `LocaleUtil` and `Translate` classes.
    - English (US) included as baseline translation.
    
### Fixes
- Fix issue with null listeners prefenting proper plugin loading.
- Fix issue where certain settings would be loaded in from Towny's implementation, instead of FlagWar.

### Misc. Changes
- API target set to 1.14, in line with Towny.
- Made Logger calls "lazy"

## 0.1.0
Development Release (Broken)

### Initial Pre-Release
- Split off from TownyAdvanced/Towny, adopting the Apache License (v2).
- Changed package space to `io.github.townyadvanced.flagwar`
- Added FlagWarAPI wrapper class.
- Directly implement WarZoneListener (Broken)
- Implemented standalone plugin configuration (CommentedConfiguration license was unknown.)
- Implemented Dependabot dependency tracking.
- Updated EditorConfig style guidelines for portability, enforced on `mvn verify`
- Added .gitattributes to preserve files @ checkout & push.
- Un-ignored some `.idea/` files for better interoperability among developers using IDEA.

# Changelog

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Updated Changelog, conforming to [Keep a Changelog]

## [0.4.0] - 2021-08-12

### Added
- `FlagWarAPI#isUnderAttack(Nation)`, which returns true if any town in the supplied nation contains a cell under attack.
- New `OutlawListener` to listen for the `OutlawTeleportEvent`
  - Outlaws cannot be teleported away during an attack while in enemy territory (or enemy nation zone) during an active
  war.
  - Add `error.outlaw.cannot-teleport-here` translation key, sent to the outlaw.

### Changed
- Bumped Towny API dependency to 0.97.0.17; set as Min_Towny_Version
- Bumped Config Version to 1.3 (Post 0.3.0... Woopsie.)
- Set Bukkit API version flag to 1.16 (From 1.17)
  - Reflects "Support API-1" policy.

### Removed
- Deployment to GitHub Packages

## [0.3.0] - 2021-07-17

### Added
- Added Optional Holograms for War Flags (Courtesy of @gaffy00 with #58)
  - Requires HolographicDisplays to function.

### Changed
- Build Targets: Java 16 (was 8), Paper 1.17.1, Towny 0.97.0.12

### Removed
- `alternatives` Maven profile from the FlagWar pom file.
- API: `CellUnderAttack#isFlag(Block)`
  - Previously deprecated; use `#isFlagTimer(Block)` or `#isFlagPart(Block)` instead.

### Fixed
- WarZoneListener Functionality

## [0.2.0] - 2021-06-18

### Added
- Checkstyle configuration, based on traditional Sun style guidelines with some modified rules.
- A Messaging utility class, with debug methods. Config option to enable debug messaging.
([PR #40](https://github.com/TownyAdvanced/FlagWar/pull/40))
- A viewer-friendly Changelog (this document).
- The ability to change War Flag timer materials. ([Pull Request #49](https://github.com/TownyAdvanced/FlagWar/pull/49), [@gaffy00](https://github.com/gaffy00/))
    - Phase timing can be adjusted by increasing the ratio of specific materials.
    - Example: Using `matX, matX, matX, matY, matY, matZ` produces a flag with three phases, at a 3:2:1 timing ratio.

### Changed
- Change `config:flag.waiting_time` to work as expected. Now reflects total length of a flag, not of the flag phase.
  ([PR #38](https://github.com/TownyAdvanced/FlagWar/pull/38))
- Bumped Dependencies: Towny 0.97.0.6, Java 11, Paper 1.16, Annotations (various), Checkstyle Configuration

### Removed
- WarZoneListener (Disabled)
  - Had some issues with functionality negatively affecting gameplay.
- Catches for Towny's `EconomyException` (Deprecated)

### Fixed
- Incorrect formatting of colored messages containing formatting characters. ([PR #48](https://github.com/TownyAdvanced/FlagWar/pull/48), [@gaffy00](https://github.com/gaffy00))
- Localization Issues with parsing Regions and Region Variants ([PR #43](https://github.com/TownyAdvanced/FlagWar/pull/43), [@Mrredstone5230](https://github.com/Mrredstone5230))
- Beacons not rendering ([PR #40](https://github.com/TownyAdvanced/FlagWar/pull/40))

## [0.1.1] - 2021-03-03

### Added
- Localization feature for supporting multiple languages, using ResourcePacks.
    - Adds `LocaleUtil` and `Translate` classes.
    - English (US) included as baseline translation.

### Changed
- Target Bukkit API 1.14, matching Towny.
- Made logger calls "lazy"
    
### Fixed
- Null listeners preventing plugin loading.
- Certain setting being loaded from Towny's in-built implementation, instead of self-contained settings.

## [0.1.0] - 2021-02-18

### Added
- FlagWarAPI wrapper class.
- `.gitattributes` to enforce and preserve file formatting on Git operations.
- Dependabot dependency tracking.
- WarZoneListener specific to FlagWar (Broken)
- Standalone Plugin Configuration (Not CommentedConfiguration based)

### Changed
- Split off from [TownyAdvanced/Towny](https://github.com/TownyAdvanced/Towny),
adopting the [Apache License (v2)](https://github.com/TownyAdvanced/FlagWar/blob/main/LICENSE).
- Package space renamed to `io.github.townyadvanced.flagwar`
- Un-ignored some `.idea/` files for better interoperability among developers using IDEA.

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Unreleased]: https://github.com/TownyAdvanced/FlagWar/compare/0.4.0...HEAD
[0.4.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.2.0-devel...0.3.0
[0.2.0]: https://github.com/TownyAdvanced/FlagWar/compare/v0.1.1-devel...0.2.0-devel
[0.1.1]: https://github.com/TownyAdvanced/FlagWar/compare/v0.1.0-devel...v0.1.1-devel
[0.1.0]: https://github.com/TownyAdvanced/FlagWar/releases/tag/v0.1.0-devel

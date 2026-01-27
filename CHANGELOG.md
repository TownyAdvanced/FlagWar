# Changelog

The format is based on [Keep a Changelog][Keep a Changelog], and this project adheres to [Semantic Versioning][semver].

If a change is missing, it is likely simple or was forgotten about when this log was updated. 😓

Feel free to PR corrections to this file. Links are reference-style, except for PR links. In other words,
check the bottom of the markdown document.

<!-- ## [Unreleased][Unreleased]
### Added

### Changed

### Fixed

### Removed
-->

---

## [0.8.0][0.8.0]
_Released 2026-01-24_

### Added
- DecentHolograms Support (_Experimental_) ([FlagCourier][FlagCourier] - [#386](https://github.com/TownyAdvanced/FlagWar/pull/386))

### Removed
- HolographicDisplays Support ([FlagCourier][FlagCourier] - [#386](https://github.com/TownyAdvanced/FlagWar/pull/386))

## [0.7.2][0.7.2]
_Released 2025-12-03_

### Fixed
- `FlagWar.loadConfig()` returning opposite of what was intended. ([LlmDl][LlmDl] - [#412](https://github.com/TownyAdvanced/FlagWar/pull/412))

## [0.7.1][0.7.1]
_Released 2025-11-08_

### Added
- `/ta reload` command to reload FlagWar. ([LlmDl][LlmDl] - [#409](https://github.com/TownyAdvanced/FlagWar/pull/409))

### Fixed
- CellWonEvent being unable to be acted upon. ([LlmDl][LlmDl] - [#406](https://github.com/TownyAdvanced/FlagWar/pull/406))
- Jitpack.yml java version not allowing builds against the repository. ([LlmDl][LlmDl] - [#408](https://github.com/TownyAdvanced/FlagWar/pull/408))
- CellWonEvent being unable to be thrown asynchronously. ([LlmDl][LlmDl] - [#411](https://github.com/TownyAdvanced/FlagWar/pull/411))

## [0.7.0][0.7.0]
_Released 2025-03-09_

### Added
- Ability to protect an area surrounding the flag from building. ([LlmDl][LlmDl] - [#401](https://github.com/TownyAdvanced/FlagWar/pull/401))
  - Made configurable in [#402](https://github.com/TownyAdvanced/FlagWar/pull/402) 

### Changed
- (CI) Update compile-on-push workflow to use Java 21 ([LlmDl][LlmDl] - [#400](https://github.com/TownyAdvanced/FlagWar/pull/400))

## [0.6.4][0.6.4]
_Released 2024-09-27_

### Added
- zh_CN translation ([Kongchenglige][Kongchenglige] - [#371](https://github.com/TownyAdvanced/FlagWar/pull/371))

### Changed
- (Automation) Bumped dependencies:
  - `maven-shade-plugin`, `spotbugs-annotations`, `maven-surefire-plugin`, `maven-checkstyle-plugin`, `maven-failsafe-plugin`

## [0.6.3][0.6.3]
_Released 2024-07-12_

### Changed
- Updated import class paths for Towny's `Transaction` class (was `TransactionType`)
- Bumped minimum required Towny version to 0.100.3.7
- (Automation) Bumped dependencies:
  - `maven-javadoc-plugin`, `maven-compiler-plugin`, `checkstyle`, Jetbrains' `java-annotations`

## [0.6.2][0.6.2]
_Released 2024-01-15_

### Fixed

- Fix attacks tasks not being cancelled correctly.

## [0.6.1][0.6.1]
_Released 2024-01-15_

### Changed

* Bump com.github.spotbugs:spotbugs-annotations from 4.7.3 to 4.8.3
* Bump org.apache.maven.plugins:maven-checkstyle-plugin from 3.3.0 to 3.3.1
* Bump com.puppycrawl.tools:checkstyle from 10.12.4 to 10.12.7
* Bump org.apache.maven.plugins:maven-surefire-plugin from 3.1.2 to 3.2.5
* Bump org.apache.maven.plugins:maven-failsafe-plugin from 3.1.2 to 3.2.5

### Fixed

- Fix scheduler not running properly for flags.

## [0.6.0][0.6.0]
_Released 2023-12-30_

> First release under [LlmDl][LlmDl]

### Added

* Add support for Folia, bump min. Towny version to 0.100.0.0. by [LlmDl][LlmDl] in [#324](https://github.com/TownyAdvanced/FlagWar/pull/324)

### Changed

* Bump com.palmergames.bukkit.towny:towny from 0.99.5.0 to 0.99.6.0 by [dependabot][dependabot] in [#308](https://github.com/TownyAdvanced/FlagWar/pull/308)

## [0.5.4][0.5.4]
_Released 2023-10-02_

> Last release under maintainership of [FlagCourier][FlagCourier].

### Known-Issues
- [Possibly fixed: See #168](https://github.com/TownyAdvanced/FlagWar/pull/168) &mdash; Impossible to attack plots that have
  [blocks placed at world build height](https://github.com/TownyAdvanced/FlagWar/issues/84). Marked as release-blocker. **Testers that want to test that it is fixed would be appreciated.**

### Added

* Make use of new Town#isAllowedToWar Towny feature. by @LlmDl in https://github.com/TownyAdvanced/FlagWar/pull/221

### Changed

* Bump Snapshot and Baseline Targets by [FlagCourier][FlagCourier] in [#196](https://github.com/TownyAdvanced/FlagWar/pull/196)
* Bump Dependencies (via [dependabot][dependabot]):
  * Bump `bstats-bukkit` from `3.0.0` to `3.0.2`
  * Bump `checkstyle` from `10.3.4` to `10.12.4`
  * Bump `java-annotations` from `23.0.0` to `24.0.1`
  * Bump `maven-checkstyle-plugin` from `3.2.0` to `3.3.0`
  * Bump `maven-compiler-plugin` from `3.10.1` to `3.11.0`
  * Bump `maven-failsafe-plugin` from `2.22.2` to `3.1.2`
  * Bump `maven-javadoc-plugin` from `3.4.1` to `3.6.0`
  * Bump `maven-shade-plugin` from `3.4.0` to `3.5.1`
  * Bump `maven-source-plugin` from `3.2.1` to `3.3.0`
  * Bump `maven-surefire-plugin` from `2.22.2` to `3.1.2`
  * Bump `towny` from `0.98.4.0` to `0.99.5.0`

* Transfer of Maintainership by [FlagCourier][FlagCourier] in [#288](https://github.com/TownyAdvanced/FlagWar/pull/288)

## [0.5.3][0.5.3]
_Released 2022-10-20_

### Known-Issues
- [Possibly fixed: See #168](https://github.com/TownyAdvanced/FlagWar/pull/168) &mdash; Impossible to attack plots that have
  [blocks placed at world build height](https://github.com/TownyAdvanced/FlagWar/issues/84). Marked as release-blocker.

### Added

- Russian Translation ([#101](https://github.com/TownyAdvanced/FlagWar/pull/101), [@HighError][HighError])
- Accuracy switch for broadcast locations. ([#168](https://github.com/TownyAdvanced/FlagWar/pull/168))
- Ability to place flags in worlds marked as having ceilings. ([#168](https://github.com/TownyAdvanced/FlagWar/pull/168))
  - Sea-level based restrictions added to compensate. 
- Feature: Neighbouring plots protect from attack. ([#141](https://github.com/TownyAdvanced/FlagWar/pull/141), [@LlmDl][LlmDl])

### Changed

- Update usages of the deprecated `TownyMessaging#sendResidentMessage` calls to use Spigot's `Player#sendMessage`.
  ([#108](https://github.com/TownyAdvanced/FlagWar/pull/108), [@LlmDl][LlmDl])
- Updated Dependencies (via [Dependabot][Dependabot])
- Switch Towny repository from JitPack to Glare's Repository.
- Allow for an empty editable materials list. ([#117](https://github.com/TownyAdvanced/FlagWar/pull/117), [@LlmDl][LlmDl])
- Switch Towny repository from JitPack to Glare's Repository.
- Update the static min towny versions. ([#142](https://github.com/TownyAdvanced/FlagWar/pull/142), [@LlmDl][LlmDl])
- Updated Dependencies ([Dependabot][Dependabot])
  - Towny 0.97.3.0 to 0.98.3.8
  - bstats-bukkit 2.2.1 to 3.0.0
  - maven-checkstyle-plugin 3.1.2 to 3.2.0
  - maven-compiler-plugin 3.8.1 to 3.10.1
  - maven-javadoc-plugin 3.3.1 to 3.4.1
  - maven-shade-plugin 3.3.0-SNAPSHOT to 3.3.0 
  - maven-site-plugin 3.9.1 to 3.12.1
  - spotbugs-annotations 4.5.2 to 4.7.2
  - holographicdisplays-api 2.4.5 to 2.4.9
  - checkstyle 9.1 to 10.3.3

### Fixed

- War Beacon blocks are no longer checked against when War Beacons are disabled.
([#107](https://github.com/TownyAdvanced/FlagWar/pull/107), [@LlmDl][LlmDl])
- Fix FlaggedInteractCooldown not parsing with TimeTools.
([#116](https://github.com/TownyAdvanced/FlagWar/pull/116), [@LlmDl][LlmDl])

### Removed

- Removed deprecated time methods.
  - CellUnderAttack
    - `formatTime(int, String)`
  - CellAttackEvent
    - `getTime()` \[long\]
    - `setTime(long)`
  - FlagWarAPI
    - `getFlaggedTimestamp(Town)`
  - FlagWar
    - `getMaterialShiftTime()` \[long\]
  - FlagWarConfig
    - `getFlagWaitingTime()` \[long\]
    - `getTimeBetweenFlagColorChange()` \[long\]
    - `getTimeToWaitAfterFlagged()` \[long\]

## [0.5.2][0.5.2]
_Released 2021-11-21_

### Known-Issues
- Impossible to attack plots that have
[blocks placed at world build height](https://github.com/TownyAdvanced/FlagWar/issues/84). Marked as release-blocker for 0.5.3.

### Added

- `VALIDATED_TOWNY_VER` version field:
  - Stores a manually-updated version string, containing the highest tested version of Towny that we've validated
  FlagWar for.
  - If a newer version of Towny is run, a startup warning is issued from either of the following translation keys.
    - `startup.check-towny.new.stable-release`
      - "Running Version of Towny released after FlagWar version. Are you up-to-date?"
    - `startup.check-towny.new.pre-release`
      - "This Towny Pre-Release is newer than what we've tested. Please report any issues."
    - _French and Spanish (MX) keys were machine-translated using [Bing Translate](https://translate.bing.com/). If you
    are familiar with either, feel free to drop in and make sure that the translation is appropriate._

- `rules.nations_can_toggle_neutral` config setting. _Default: `FALSE`_
  ([#96](https://github.com/TownyAdvnaced/FlagWar/pull/#96), [@LlmDl][LlmDl])
  - Replaces the now removed option that was in Towny. If False, prevents Towny from
  allowing Nations to become neutral. 
  - ℹ️It is important for users to not run FlagWar 0.5.1, or older, with Towny 0.97.2.15, or newer. The required API
    used for determining player-set neutrality in older releases is no longer present in newer Towny versions.

- WarZone Configuration Section and Enhancements 
  ([#88](https://github.com/TownyAdvanced/FlagWar/pull/88), [@LlmDl][LlmDl])
  - Bumps Config version to 1.5 
  - New Config section 'warzone' containing the following keys:
    - `warzone.editable_materials` (list)
      - List of materials that can be interacted with in war zones; Or, alternatively, cannot be interacted with.
      - Accepts wildcards `*` and negations `-{material}`.
    - `warzone.item_use` (boolean)
      - Enables / Disables item usage in war_zones.
    - `warzone.switch` (boolean)
      - Enables / Disables use of switches in war zones.
    - `warzone.fire` (boolean)
      - Enables / Disables fire in war zones, add `-fire` to `warzone.editable_materials` for complete fire protection
        when this is false.
    - `warzone.explosions` (boolean)
      - Enables / Disables explosions in war zones.
    - `warzone.explosions_break_blocks` (boolean)
      - Enables / Disables explosions effecting Blocks.
  - API: Enhanced FlagWarConfig
    - Add `#setEditableMaterials()` (void)
    - Add `#isAllowingExplosionsInWarZone()` (bool)
    - Add `#isAllowingExplosionsToBreakBlocks()` (bool)
    - Add `#isAllowingFireInWarZone()` (bool)
    - Add `#isAllowingSwitchInWarZone()` (bool)
    - Add `#isAllowingItemUseInWarZone()` (bool)
    - Add `#isEditableMaterialInWarZone(Material)` (bool)

### Changed

- Build System
  - Bumped SpotBugs-Annotations, Checkstyle, and IntelliJ Java Annotations
  - Bumped Minimum Towny Version to 0.97.3.0. Same as validated version.

- Refactored Holograph line population methods to reduce complexity. (commit: b08e1fa41a74019a2559d1dac7c2430f78ef22fc)
  - This is a code-quality change, and should have no bearing on server operations.

- Changed how time is tracked and calculated: ([#89](https://github.com/TownyAdvanced/FlagWar/pull/89))
  - FlagWar will now use Duration and Instant instead of the primitive `long` when storing cool-downs and timestamps.

- Changed how payments / fees are calculated: Using BigDecimal to avoid IEEE754 floating-point inconsistencies.
Scale to 2, use "Bankers' Rounding" (HALF_EVEN)

- Refactored how Messaging.debug(string, args) functions: Use `Object...` over `Object[]`, mimicking String.format().
  - Avoids passing args as `new Object[]{arg1,arg2,etc}`

### Fixed

- Fix `Cell#parse(WorldCoord)` (Also from [#88](https://github.com/TownyAdvanced/FlagWar/pull/88))

### Deprecated API

- `CellAttackEvent#getTime()` (long)
- `CellAttackEvent#setTime(long)`
- `CellUnderAttack#formatTime(int, string)` (String)
- `FlagWarAPI#getFlaggedTimestamp(Town)` (long)
- `FlagWarConfig#getFlagWaitingTime()` (long)
- `FlagWarConfig#getTimeBetweenFlagColorChange()` (long)
- `FlagWarConfig#getTimeToWaitAfterFlagged()` (long)

## [0.5.1][0.5.1]
_Released 2021-10-27_

> Note, two editions of 0.5.1 exist: Git Tag `0.5.1` and Git Tag `0.5.1b`. This changelog represents the latter, which 
> is an amended release.

### Added

- French Translation (`fr_FR`)
  ([#83](https://github.com/TownyAdvanced/FlagWar/pull/83), [@Bibithom][Bibithom])

- Expansion of the WarZoneListener
  ([#87](https://github.com/TownyAdvanced/FlagWar/pull/87), [@LlmDl][LlmDl])
  - Now implements listeners for the following Towny events:
    - `TownyBurnEvent`
    - `TownyExplodingBlocksEvent`
    - `TownyExplosionDamagesEntityEvent`
    - `TownBlockPVPTestEvent`
  - Follows the Towny WarZoneConfig like the Build/Destroy/Switch/ItemUse events do.
  - Also adds `Cell#parse(WorldCoord)` method for convenience.

### Fixed

- An issue with canceling `null` hologramThreads.
  ([#85](https://github.com/TownyAdvanced/FlagWar/pull/85), [@LlmDl][LlmDl])

- Adjust implementation of the WarZoneListener, and how FlagWar utilizes WarZones.
  ([#86](https://github.com/TownyAdvanced/FlagWar/pull/86), [@LlmDl][LlmDl])

## [0.5.0][0.5.0]
_Released 2021-10-13_

### Added

- New Option to require a specific number of attackers to be online in the attacking Town and/or Nation.
  ([#72](https://github.com/TownyAdvanced/FlagWar/pull/72), [@LlmDl][LlmDl])
    - Modifies behavior. Originally, this quota was determined by the same method used for defenders.

- New Option to have conquered Town blocks unclaimed instead of transferred to the attacker.
  ([#73](https://github.com/TownyAdvanced/FlagWar/pull/73), [@LlmDl][LlmDl])

### Changed

- **Towny Target:**
  - Minimum version required is _**0.97.2.0**_. It is recommended to use the latest version available to you.
  
- FlagWar will now respect Towny's per-world settings for allowing war.
  ([#69](https://github.com/TownyAdvanced/FlagWar/pull/69) _nice..._, [@LlmDl][LlmDl])
  - Originally used exclusively for Towny's EventWar.

- GitHub Actions
  - Now tests against Eclipse Temurin, from [Adoptium][adoptium].
  - Tests against both JDK 16 and JDK 17.

- In `CellUnderAttack.class`, replace uses of `scheduleSyncRepeatingTask()` with `runTaskTimer()`.
  - Should be purely aesthetic, but also removes a deprecation warning. SSRT simply calls RTT under the hood.

### Fixed

- Fixed NullPointerException found in OutlawEventListener.
  ([#79](https://github.com/TownyAdvanced/FlagWar/pull/79), [@LlmDl][LlmDl])

- Fixed Jitpack building.
  - Specifically uses Microsoft's build of OpenJDK 17, and Maven 3.8.3 (As opposed to Oracle JDK 8, Maven 3.6.1)
  - [Tested Working](https://jitpack.io/com/github/TownyAdvanced/FlagWar/main-7d7ba1c80e-1/build.log) 

## [0.4.0][0.4.0]
_Released 2021-08-12_

### Added

- `FlagWarAPI#isUnderAttack(Nation)`; returns `TRUE` if any Town in the supplied Nation contains a cell under attack.

- New `OutlawListener` to listen for the `OutlawTeleportEvent`
  - Outlaws cannot be teleported away during an attack while in enemy territory (or enemy Nation Zone) during an active
  war.
  - Add `error.outlaw.cannot-teleport-here` translation key, sent to the outlaw.

### Changed

- `rules.time_to_wait_after_flagged` replaced by `rules.prevented_interaction_cooldown, which uses DHMS formatting.
  - The (long) `FlagWarConfig#getTimeToWaitAfterFlagged()` API endpoint remains, but will default if the node is missing
    or empty. (Default Value: `600000l`, _10 minutes_.)
  - This change should reflect the purpose of the node, which has never prevented follow-up attacks.

- Bumped Towny dependency to 0.97.0.22; set 0.97.0.17 as Min_Towny_Version

- Bumped Config Version to 1.3 (Post 0.3.0...)

- Set Bukkit API version flag to 1.16 (From 1.17)
  - Reflects "Support API-1" policy.

### Removed

- Deployment to GitHub Packages

## [0.3.0][0.3.0]
_Released 2021-07-17_

### Added

- Added Optional Holograms for War Flags ([#58](https://github.com/TownyAdvanced/FlagWar/pull/58), [@gaffy00][gaffy00])
  - Requires HolographicDisplays to function.

### Changed

- Build Targets: Java 16 (was 8), Paper 1.17.1, Towny 0.97.0.12

### Removed

- `alternatives` Maven profile from the FlagWar pom file.

- API: `CellUnderAttack#isFlag(Block)`
  - Previously deprecated; use `#isFlagTimer(Block)` or `#isFlagPart(Block)` instead.

### Fixed

- WarZoneListener Functionality

## [0.2.0][0.2.0]
_Released 2021-06-18_

### Added

- Checkstyle configuration, based on traditional Sun style guidelines with some modified rules.

- A Messaging utility class, with debug methods. Config option to enable debug messaging.
([PR #40](https://github.com/TownyAdvanced/FlagWar/pull/40))

- A viewer-friendly Changelog (this document).

- The ability to change War Flag timer materials.
  ([Pull Request #49](https://github.com/TownyAdvanced/FlagWar/pull/49), [@gaffy00][gaffy00])
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

- Incorrect formatting of colored messages containing formatting characters.
  ([PR #48](https://github.com/TownyAdvanced/FlagWar/pull/48), [@gaffy00][gaffy00])

- Localization Issues with parsing Regions and Region Variants
  ([PR #43](https://github.com/TownyAdvanced/FlagWar/pull/43), [@Mrredstone5230][Mrredstone5230])

- Beacons not rendering ([PR #40](https://github.com/TownyAdvanced/FlagWar/pull/40))

## [0.1.1][0.1.1]
_Released 2021-03-03_

### Added

- Localization feature for supporting multiple languages, using ResourcePacks.
    - Adds `LocaleUtil` and `Translate` classes.
    - English (US) included as baseline translation.

### Changed

- Target Bukkit API 1.14, matching Towny.

- Made logger calls "lazy" (Using lambda expressions)
    
### Fixed

- Null listeners preventing plugin loading.

- Certain setting being loaded from Towny's in-built implementation, instead of self-contained settings.

## [0.1.0][0.1.0]
_Released 2021-02-18_

### Added

- FlagWarAPI wrapper class.

- `.gitattributes` to enforce and preserve file formatting on Git operations.

- Dependabot dependency tracking.

- WarZoneListener specific to FlagWar (Broken)

- Standalone Plugin Configuration (Not CommentedConfiguration based)

### Changed

- Split off from [TownyAdvanced/Towny][towny], adopting the [Apache License (v2)][license].
  - See the [NOTICE file][notice] for 3rd-party licenses.

- Package space renamed to `io.github.townyadvanced.flagwar`

- Un-ignored some `.idea/` files for better interoperability among developers using IDEA.

# Pre-History

- FlagWar created by [@Zren][Zren] as "Cell War", then merged into Towny. Maintained for over ten
  years as part of Towny, before the decoupling effort.

<!---------------------------->
<!--  LINKS and REFERENCES  -->
<!---------------------------->

<!-- Links to Tagged Changes -->
<!-- Unreleased ---------- Change This TAG to CURRENT RELEASE  V V V -->
[Unreleased]: https://github.com/TownyAdvanced/FlagWar/compare/0.8.0...HEAD

<!-- Released -->
[0.8.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.7.2...0.8.0
[0.7.2]: https://github.com/TownyAdvanced/FlagWar/compare/0.7.1...0.7.2
[0.7.1]: https://github.com/TownyAdvanced/FlagWar/compare/0.7.0...0.7.1
[0.7.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.6.4...0.7.0
[0.6.4]: https://github.com/TownyAdvanced/FlagWar/compare/0.6.3...0.6.4
[0.6.3]: https://github.com/TownyAdvanced/FlagWar/compare/0.6.2...0.6.3
[0.6.2]: https://github.com/TownyAdvanced/FlagWar/compare/0.6.1...0.6.3
[0.6.1]: https://github.com/TownyAdvanced/FlagWar/compare/0.6.0...0.6.3
[0.6.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.5.4...0.6.0
[0.5.4]: https://github.com/TownyAdvanced/FlagWar/compare/0.5.3...0.5.4
[0.5.3]: https://github.com/TownyAdvanced/FlagWar/compare/0.5.2...0.5.3
[0.5.2]: https://github.com/TownyAdvanced/FlagWar/compare/0.5.1b...0.5.2
[0.5.1]: https://github.com/TownyAdvanced/FlagWar/compare/0.5.0...0.5.1b
[0.5.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.4.0...0.5.0
[0.4.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/TownyAdvanced/FlagWar/compare/0.2.0-devel...0.3.0
[0.2.0]: https://github.com/TownyAdvanced/FlagWar/compare/v0.1.1-devel...0.2.0-devel
[0.1.1]: https://github.com/TownyAdvanced/FlagWar/compare/v0.1.0-devel...v0.1.1-devel
[0.1.0]: https://github.com/TownyAdvanced/FlagWar/releases/tag/v0.1.0-devel

<!-- Project Links -->
[towny]: https://github.com/TownyAdvanced/Towny
[license]: https://github.com/TownyAdvanced/FlagWar/blob/main/LICENSE
[notice]: https://github.com/TownyAdvnaced/FlagWar/blob/main/NOTICE

<!-- Misc. Links -->
[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[semver]: https://semver.org/spec/v2.0.0.html
[adoptium]: https://adoptium.net

<!-- Recognized Contributors (Non-exhaustive) -->
[Bibithom]: https://github.com/Bibithom/
[Zren]: https://github.com/zren/
[LlmDl]: https://github.com/LlmDl/
[gaffy00]: https://github.com/gaffy00/
[Mrredstone5230]: https://github.com/Mrredstone5230/
[HighError]: https://github.com/HighError/
[Dependabot]: https://github.com/Dependabot/
[Kongchenglige]: https://github.com/Kongchenglige/
[FlagCourier]: https://github.com/TheFlagCourier/
[dependabot]: https://github.com/dependabot/

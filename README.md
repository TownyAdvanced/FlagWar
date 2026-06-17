BannerWar for Towny Advanced
============================

BannerWar is a hard fork of TownyAdvanced's FlagWar, rebuilt around longer nation-scale battles,
battle stages, restoration, TownyCivics, TownyAI, and modern banner/flag support while keeping the
classic FlagWar core: attackers place war flags in enemy townblocks, defenders break them, and a
homeblock capture decides the war.

Important Notices
-----------------

<details><summary><b>Hard fork status</b></summary>

BannerWar began as a hard fork of [FlagWar](https://github.com/TownyAdvanced/FlagWar). Package names,
some commands, and some historical documentation may still reference FlagWar, but the plugin prefix,
configuration additions, and new gameplay systems are BannerWar-specific.
</details>

<details><summary><b>Metrics / Telemetry</b></summary>

BannerWar inherits FlagWar's bStats integration. You can opt out of bStats telemetry by editing the
bStats configuration in `plugins/bStats/`.
</details>

Summary
-------

FlagWar's original idea was simple: give attackers a physical objective in Towny wars. BannerWar keeps
that objective and expands it into a staged battle system:

1. A player from an enemy nation attempts to build in a townblock, starting a battle if all Towny,
   online-player, neutrality, and enemy checks pass.
2. The battle enters `PRE_FLAG`, a preparation period where flags cannot be placed yet.
3. The battle advances to `FLAG`, where attackers place fence-based war flags in enemy claims.
4. Each flag that survives captures its townblock. Capturing the homeblock ends the battle in an
   attacker victory.
5. If time expires first, the defense wins and the town enters a dormant cooldown.
6. If attackers win, the town is ruined for a configured stage duration before also entering dormant
   cooldown.
7. BannerWar stores battles in a database so they can resume after restarts.

Feature Overview
----------------

### Staged battles

BannerWar replaces one-off flag skirmishes with persistent battles against a contested town.

* **Stages:** `PRE_FLAG`, `FLAG`, `RUINED`, `DORMANT`, and `END`.
* **Configurable timing multipliers:** every major stage can be sped up or slowed down in
  `battle.timing_multipliers`.
* **Dynamic battle length:** active flagging time scales from the defending town's starting size, with
  city states receiving a shorter pre-flag period.
* **Dormant cooldowns:** recently attacked towns cannot be attacked again until the dormant stage ends.
* **Admin controls:** `/StageAdvance` (aliases `/PhaseAdvance`, `/pa`, `/sa`) advances a battle stage,
  and `/BannerWarReload` (aliases `/bwreload`, `/bwr`) reloads BannerWar's configuration.
* **Boss bars:** active battles show stage and time information to involved players.
* **Premature ending:** disbanded towns/nations cause active related battles to end cleanly.

### Banner/flag combat

BannerWar keeps FlagWar's block-based warfare and adds new flag-life mechanics.

* Attackers place fence-based war flags only during the `FLAG` stage.
* Defenders break flags to defend townblocks.
* A flag victory captures that townblock; a homeblock capture ends the battle in an attacker victory.
* Attackers and allies can right-click an active flag with a configured payment item to add extra lives.
* Added flag lives increase flag lifetime and use exponential pricing (`base_price * 2^n`).
* Recently damaged flags can become temporarily protected with a configured invincibility material.
* Boutique integration can apply custom flag toppers to newly placed flags.
* DecentHolograms support can display configurable flag holograms and timers.

### Town preservation and restoration

BannerWar is designed for destructive wars without permanently destroying the defending town's original
layout.

* The plugin records the defending town's initial townblocks, homeblock, mayor, spawn, and outpost
  spawns.
* FastAsyncWorldEdit/WorldEdit support snapshots the contested town's initial non-outpost chunks into a
  `.schem` file.
* When a battle ends or a ruined town recovers, the town's blocks are pasted back with configurable
  material blacklists.
* Restoration avoids pasting air, skips blacklisted materials, removes dropped items in the restored
  region, and attempts to move suffocating players out safely.
* Pre-war Towny metadata is restored and verified, including homeblock, spawn, and outpost spawns.

### TownyCivics integration

BannerWar adds war systems that interact with TownyCivics government and CivTech mechanics.

* **War Weariness:** battle actions add or remove upkeep modifiers from towns or nations.
* **Federations vs. nations:** weariness can apply to towns for federations and to nations otherwise.
* **Autocracy-aware outcomes:** configured weariness changes can differ for autocracies.
* **Leaving/kicking restrictions:** residents cannot leave or be kicked when town/nation war weariness
  exceeds the configured threshold.
* **Daily recovery:** Towny new-day events reduce weariness over time and can apply additional recovery
  to towns whose last banner placement has aged out.
* **Infernal War Flags:** at a configured life threshold, eligible flags can gain extra lifetime and use
  a configured infernal material.
* **Attrition Doctrine:** eligible attackers can start flags with additional lifetime.
* **War Economy:** eligible attackers can reduce weariness increases.
* **Caesar Cipher:** provides a configurable delay value used by BannerWar's CivTech configuration.

### TownyAI, city states, and bots

BannerWar depends on `townyAI`.

* Battles query TownyAI to detect city states.
* City-state battles use a shorter pre-flag duration.
* BannerWar can exclude TownyAI bots from involved/non-involved player targeting and waypoint updates.

### Waypoints and player guidance

BannerWar can integrate with WayfinderAPI-style waypoint services.

* Active flags receive red flag waypoints keyed to the flag owner.
* Waypoints are shown to players associated with the battle and hidden from others.
* Waypoints are removed when the relevant flag is defended or won.

### Warzone controls

When a townblock is under attack, BannerWar can selectively loosen or restrict activity in the warzone.

* Configure editable materials with allow/deny syntax.
* Control item use, switches, fire, explosions, and whether explosions break blocks.
* Protect a configurable area around and above active flags.
* Block burning and piston movement are checked against active flag protections.

### Broadcasts and localization

* BannerWar has a configurable broadcast prefix name and colors.
* Repeated player messages are filtered to reduce spam.
* Existing translation files include `en_US`, `fr_FR`, `ru_RU`, `es_MX`, and `zh_CN` resources.

### Developer API and events

BannerWar exposes helpers and Bukkit events for integrations. Full integration documentation is available in [docs/API.md](docs/API.md).

* `BannerWarAPI` exposes battle lookup, town battle state checks, attacker/defender association checks,
  associated-player queries, non-associated-player queries, and TownyAI bot queries.
* Battle events include start, resume, flaggable, end, ruin, and premature end events.
* Cell events include attack, attack cancel, defend, and win events.

Requirements
------------

BannerWar is built as a Bukkit/Paper plugin for modern Towny servers.

| Component | Status | Notes |
|:--|:--|:--|
| Java | Required | The Maven compiler is configured for Java 21 source/target. |
| Paper/Bukkit API | Required | Project API version is 1.19; Paper API dependency is `1.19.4-R0.1-SNAPSHOT`. |
| Towny | Required | Current dependency target is Towny `0.102.0.0`. |
| townyAI | Required | Used for city-state and bot-aware battle behavior. |
| TownyCivics | Provided dependency | Enables CivTech and war-weariness systems. |
| WorldEdit / FAWE | Optional but recommended | Used for town snapshots and restoration. |
| DecentHolograms | Optional | Used for configurable war-flag holograms. |
| Boutique | Optional | Used for custom flag toppers. |
| WayfinderAPI | Optional integration | Used by BannerWar's waypoint manager when available. |

Configuration Highlights
------------------------

BannerWar keeps the original FlagWar configuration and adds BannerWar-specific sections after the
`BANNERWAR CONFIGURATIONS START HERE` marker.

* `universe.current_day` tracks Towny-day progression for weariness/upkeep behavior.
* `universe.blacklisted_materials` controls which materials are not restored from snapshots.
* `universe.bracket_color`, `universe.name_color`, and `universe.prefix_name` configure broadcast
  formatting.
* `battle.timing_multipliers` configures `pre_flag`, `flag`, `ruined`, and `dormant` durations.
* `flag_lives` configures extra lives, payment item, exponential pricing, lifetime increases, payment
  cutoffs, and invincibility behavior.
* `clock.cycle_speed` configures how often BannerWar updates battles, database records, boss bars, and
  waypoints.
* `civics.civtechs` configures Infernal War Flags, Caesar Cipher, Attrition Doctrine, and War Economy.
* `civics.war_weariness` configures all weariness changes, thresholds, and daily recovery.

Building
--------

BannerWar uses Maven.

```bash
mvn clean package
```

The shaded plugin jar is produced in `target/`.

Licensing
---------

BannerWar inherits FlagWar's Apache License 2.0 source licensing. Some shaded or provided libraries may
have their own licenses; see `NOTICE` and dependency metadata for details.

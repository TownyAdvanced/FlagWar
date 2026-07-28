# BannerWar Developer API

BannerWar is a hard fork of TownyAdvanced FlagWar. Public classes still live under the historical `io.github.townyadvanced.flagwar` package, so existing FlagWar integrations can continue to use the legacy API while newer integrations use the BannerWar battle API.

This document covers the API surfaces currently intended for external integrations:

- Legacy FlagWar cell/flag helpers in `FlagWarAPI`.
- New BannerWar battle helpers in `BannerWarAPI`.
- Battle and flag model objects returned by the APIs and events.
- Bukkit events fired by the plugin.

## Dependency setup

Add BannerWar as a plugin dependency or soft dependency in your plugin.yml, depending on whether your plugin can run without it:

```yaml
depend: [BannerWar]
# or
softdepend: [BannerWar]
```

The plugin main and API classes are packaged in `io.github.townyadvanced.flagwar`. Most API methods also use Bukkit/Paper, Towny, and BannerWar model classes, so integrations should compile against the same dependency family used by BannerWar.

## Threading and mutability notes

- Bukkit and Towny objects should be read or mutated on the server thread unless the upstream API explicitly documents async safety.
- `BannerWarAPI#getAssociatedNonBots`, `getNonAssociatedNonBots`, and `getAllBots` return `CompletableFuture` because TownyAI bot lookups are asynchronous.
- `Battle`, `CellUnderAttack`, and Towny objects returned by the API are live runtime objects. Prefer read-only access unless you are intentionally controlling a battle.
- Some legacy APIs may return `null` when no battle, townblock, flag, or Towny object exists. Check return values before dereferencing.

## Quick examples

### Check whether a town is in a BannerWar battle

```java
Town town = TownyAPI.getInstance().getTown("ExampleTown");
if (town != null && BannerWarAPI.isInBattle(town)) {
    Battle battle = BannerWarAPI.getBattle(town);
    plugin.getLogger().info(town.getName() + " is in stage " + battle.getCurrentStage());
}
```

### Listen for a battle becoming flaggable

```java
@EventHandler
public void onBattleFlaggable(BattleFlaggableEvent event) {
    Battle battle = event.getBattle();
    plugin.getLogger().info("Flags are now enabled in " + battle.getContestedTown().getName());
}
```

### Cancel a flag placement with a reason

```java
@EventHandler(ignoreCancelled = true)
public void onCellAttack(CellAttackEvent event) {
    if (shouldBlockFlag(event.getPlayer(), event.getFlagBlock())) {
        event.setCancelled(true);
        event.setReason("Your plugin-specific rule prevented this flag placement.");
    }
}
```

## Legacy FlagWar API

Class: `io.github.townyadvanced.flagwar.FlagWarAPI`

This class exposes the still-used classic FlagWar cell/flag API. A `Cell` represents a Towny-sized world coordinate cell. A `CellUnderAttack` represents an active war flag in one cell.

| Method | Returns | Description |
| --- | --- | --- |
| `isUnderAttack(Cell cell)` | `boolean` | Whether a specific cell currently has an active war flag. |
| `isUnderAttack(Town town)` | `boolean` | Whether any cell related to the town is under attack. |
| `isUnderAttack(Nation nation)` | `boolean` | Whether any town in the nation has a cell under attack. |
| `getCellsUnderAttack()` | `List<CellUnderAttack>` | All active war flags/cells. |
| `getCellsUnderAttack(Town town)` | `List<CellUnderAttack>` | Active war flags/cells related to a town. |
| `getCellsUnderAttack(Player player)` | `List<CellUnderAttack>` | Active war flags placed by the player. |
| `getCellsUnderAttack(String playerName)` | `List<CellUnderAttack>` | Active war flags placed by a player name. |
| `getNumActiveFlags(Player player)` | `int` | Number of active flags placed by the player. |
| `getAttackData(Cell cell)` | `CellUnderAttack` | Active attack data for a cell, or `null` if none exists. |
| `getFlaggedInstant(Town town)` | `Instant` | Time when the town was last flagged. |

### Legacy cell model: `Cell`

Class: `io.github.townyadvanced.flagwar.objects.Cell`

A cell is keyed by world name and Towny cell coordinates.

| Constructor or method | Description |
| --- | --- |
| `Cell(String worldName, int x, int z)` | Creates a cell from a world name and cell coordinates. |
| `Cell(Cell cell)` | Copies another cell. |
| `Cell(Location location)` | Creates a cell by parsing a Bukkit location. |
| `getX()` / `getZ()` | Returns cell coordinates. |
| `getWorldName()` | Returns the Bukkit world name stored for the cell. |
| `parse(String worldName, int x, int z)` | Converts raw block coordinates to a cell using Towny's configured cell size. |
| `parse(WorldCoord worldCoord)` | Converts a Towny `WorldCoord` into a cell. |
| `parse(Location location)` | Converts a Bukkit location into a cell. |
| `isUnderAttack()` | Shortcut for `FlagWarAPI.isUnderAttack(this)`. |
| `getAttackData()` | Shortcut for `FlagWarAPI.getAttackData(this)`. |

`equals` and `hashCode` are based on world name, x, and z.

## BannerWar battle API

Class: `io.github.townyadvanced.flagwar.BannerWarAPI`

This class exposes BannerWar's newer battle-level state and player-association helpers.

| Method | Returns | Description |
| --- | --- | --- |
| `isInBattle(Town town)` | `boolean` | Whether the town has any tracked battle, including dormant cooldown. |
| `isInBattle(String townName)` | `boolean` | Name-based version of `isInBattle`. |
| `isNotDormant(Town town)` | `boolean` | Whether the town has a tracked battle whose stage is not `DORMANT`. Returns `false` if there is no battle. |
| `getBattle(Town town)` | `Battle` | Battle associated with the town, or `null`. |
| `getBattle(String townName)` | `Battle` | Name-based battle lookup, or `null`. |
| `getBattleAt(TownBlock townBlock)` | `Battle` | Battle whose initial townblock set contains the provided townblock, or `null`. |
| `getBattleAt(WorldCoord coord)` | `Battle` | Looks up the Towny townblock at the coordinate and delegates to `getBattleAt(TownBlock)`. |
| `getBattleRegions(Battle battle)` | `Collection<BoundingBox>` | Newly-created, 64-block-expanded region boxes derived from the battle's initial claims. They remain stable when claims change and across server restarts. |
| `isAssociatedWithNation(Resident res, Nation nat)` | `boolean` | Whether a resident belongs to the nation or one of its allies. |
| `isAssociatedWithAttacker(Resident res, Battle battle)` | `boolean` | Whether a resident is associated with the attacking nation. |
| `isAssociatedWithAttacker(String residentName, Battle battle)` | `boolean` | Name-based attacker association lookup. |
| `isAssociatedWithDefender(Resident res, Battle battle)` | `boolean` | Whether a resident is associated with the defending nation. |
| `isAssociatedWithDefender(String residentName, Battle battle)` | `boolean` | Name-based defender association lookup. |
| `isAssociatedWithBattle(Resident res, Battle battle)` | `boolean` | Whether a resident is associated with either side. |
| `getAssociatedPlayers(Battle battle)` | `Collection<Player>` | Online players associated with either side of the battle. |
| `getNonAssociatedPlayers(Battle battle)` | `Collection<Player>` | Online players not associated with either side of the battle. |
| `getAssociatedNonBots(Battle battle)` | `CompletableFuture<Collection<Player>>` | Associated online players with TownyAI city-state bot players removed. |
| `getNonAssociatedNonBots(Battle battle)` | `CompletableFuture<Collection<Player>>` | Non-associated online players with TownyAI city-state bot players removed. |
| `getAllBots()` | `CompletableFuture<Collection<Player>>` | Online `Player` handles for TownyAI city-state residents. Returns an empty collection if TownyAI is not loaded. |

## Battle model

Class: `io.github.townyadvanced.flagwar.objects.Battle`

A battle tracks one attacking nation, one defending nation, and one contested town. Integrations usually receive `Battle` from `BannerWarAPI` or from battle events.

### Battle stages

Enum: `io.github.townyadvanced.flagwar.objects.BattleStage`

| Stage | Meaning |
| --- | --- |
| `PRE_FLAG` | Battle has begun, but flags cannot be placed yet. |
| `FLAG` | Main flagging stage; attackers may place flags and capture townblocks. |
| `RUINED` | Attackers won; the town is ruined for the configured duration. |
| `DORMANT` | Battle combat has ended; the town is in cooldown and cannot be attacked again yet. |
| `END` | BannerWar is no longer tracking the battle and the town can be attacked again. |

### Read APIs

| Method | Returns | Description |
| --- | --- | --- |
| `getAttacker()` | `Nation` | Attacking nation. |
| `getDefender()` | `Nation` | Defending nation. |
| `getContestedTown()` | `Town` | Town being fought over. |
| `getHomeBlock()` | `TownBlock` | Current Towny object for the pre-war homeblock. |
| `getHomeBlockCoords()` | `WorldCoord` | Stored pre-war homeblock coordinate. |
| `getInitialMayor()` | `Resident` | Mayor captured at battle start. |
| `getInitialSpawn()` | `Location` | Clone of the pre-war town spawn, or `null`. |
| `getInitialOutpostSpawns()` | `List<Location>` | Clones of pre-war outpost spawns. |
| `getDuration(BattleStage stage)` | `Duration` | Configured/calculated duration for a stage in this battle. |
| `getInitialTownBlocks()` | `Collection<TownBlock>` | Townblocks belonging to the town when the battle began. |
| `getInitialTownBlocksAsWorldCoords()` | `Collection<WorldCoord>` | Stored coordinates for initial townblocks. |
| `getCapturedTownBlocks()` | `Collection<TownBlock>` | Initial townblocks no longer owned by the contested town. |
| `getTimeRemainingForCurrentStage()` | `Duration` | Remaining time in the current stage, floored at zero. |
| `isPendingStageAdvance()` | `boolean` | Whether current-stage time has elapsed. |
| `getCurrentStage()` | `BattleStage` | Current stage. |
| `getStageStartTime()` | `long` | Unix epoch milliseconds when the current stage began. |
| `isCityState()` | `boolean` | Whether the contested town was classified as a TownyAI city state. |
| `isInactive()` | `boolean` | `true` outside `PRE_FLAG` and `FLAG`. |
| `isActive()` | `boolean` | Opposite of `isInactive()`. |
| `isFlagging()` | `boolean` | Whether the battle is in `FLAG`. |
| `getCellUnderAttack(int x, int z)` | `CellUnderAttack` | Active flag at the given cell coordinates for this battle, or `null`. |
| `getFlagOwners()` | `Collection<String>` | Names of players with flags associated with the battle. |

### Control APIs

These methods mutate battle state and may fire Bukkit events. Use them carefully and normally only from server-thread administrative logic.

| Method | Description |
| --- | --- |
| `setStage(BattleStage stage)` | Sets the stage and resets the stage start time. Does not by itself perform the side effects of normal stage transitions. |
| `addFlag(String playerName)` | Associates a player-owned flag with the battle. |
| `removeFlag(String playerName)` | Removes a player-owned flag association. |
| `advanceStage(boolean winDefense)` | Advances to the next logical stage. In `FLAG`, `winDefense=true` causes defender victory and `false` causes attacker victory/ruin. |
| `makeFlaggable()` | Moves to `FLAG` and fires `BattleFlaggableEvent`. |
| `loseDefense()` | Ends active flag procedures, ruins the town, and fires `BattleEndEvent` with defense-lost state. |
| `winDefense()` | Ends active flag procedures, makes the town dormant, and fires `BattleEndEvent` with defense-won state. |
| `prematurelyEndBattle()` | Deletes boss bar, removes the battle from tracking/database, and fires `BattlePrematureEndEvent`. |
| `updateBossBar()` | Updates associated players' boss bar title/progress. |
| `deleteBossBar()` | Removes and clears the boss bar. |

## Active flag model

Class: `io.github.townyadvanced.flagwar.objects.CellUnderAttack`

`CellUnderAttack` extends `Cell` and represents one active war flag. Integrations commonly receive it from `FlagWarAPI`, `CellAttackEvent#getData()`, `CellWonEvent#getCellUnderAttack()`, or `Battle#getCellUnderAttack(int, int)`.

| Method | Returns | Description |
| --- | --- | --- |
| `getFlagBaseBlock()` | `Block` | Base/pole block for the flag. |
| `getFlagTimerBlockLocation()` | `Location` | Location one block above the base, used for timer material. |
| `getFlagLightBlock()` | `Block` | Light/top block two blocks above the base. |
| `getNameOfFlagOwner()` | `String` | Name of the player who placed the flag. |
| `hasEnded()` | `boolean` | Whether the flag's remaining lifetime is zero or negative. |
| `getCellString()` | `String` | Human-readable `world (x, z)` identifier. |
| `isFlagLight(Block block)` | `boolean` | Whether a block is the light/top block. |
| `isFlagTimer(Block block)` | `boolean` | Whether a block is the timer block. |
| `isFlagBase(Block block)` | `boolean` | Whether a block is the base block. |
| `isFlagPart(Block block)` | `boolean` | Whether a block is any of the three flag blocks. |
| `isPartOfBeacon(Block block)` | `boolean` | Always `false` in BannerWar because beacon support is deprecated in favor of waypoint/player-locator behavior. |
| `isImmutableBlock(Block block)` | `boolean` | Whether a block is protected flag/beacon structure. |
| `tryAddLife(Player adder)` | `boolean` | Attempts to add a configured extra life, messages the player, and respects cutoff/max-life rules. |
| `getLives()` | `int` | Current flag lives. |
| `decrementLife()` | `int` | Removes one life, applies temporary invincibility, updates the flag, and returns remaining lives. |
| `getLifeAdditions()` | `int` | Number of extra lives added after creation. |

### Flag lifecycle/control methods

These methods change world blocks and scheduled tasks. They are primarily plugin-internal but public for compatibility.

| Method | Description |
| --- | --- |
| `loadBeacon()` | Legacy beacon loader. BannerWar's beacon block lists are deprecated/commented, so this is mostly compatibility behavior. |
| `changeFlag()` | Advances the timer material phase and updates the flag. |
| `drawFlag()` | Places the base, timer, and light blocks. |
| `updateFlag()` | Updates timer material unless the flag is ended, infernal, or temporarily invincible. |
| `destroyFlag()` | Removes the base, timer, and light blocks. |
| `taskUpdateHologram()` | Updates DecentHolograms timer text when holograms are enabled. |
| `beginAttack()` | Draws the flag and starts scheduled update, lifetime, and hologram tasks. |
| `cancel()` | Cancels scheduled tasks, removes flag blocks, and destroys the hologram. |
| `makeInvincible()` | Temporarily sets the timer block to the configured invincibility material. |

## Bukkit events

All events are in `io.github.townyadvanced.flagwar.events` and follow Bukkit's standard listener pattern.

### Battle events

| Event | Cancellable | Fired when | Accessors |
| --- | --- | --- | --- |
| `BattleStartEvent` | No | A new battle starts after BannerWar creates and stores the battle. | `getBattle()`, `getBannerPlacerTown()` |
| `BattleResumeEvent` | No | A persisted battle is resumed during plugin/database loading. | `getBattle()` |
| `BattleFlaggableEvent` | No | A battle enters the `FLAG` stage. | `getBattle()` |
| `BattleEndEvent` | No | A battle is won by either the defense or attack. | `getBattle()`, `isDefenseWon()` |
| `BattleRuinEvent` | No | A defense is lost and the town enters ruined state. | `getBattle()`, `getRuinDuration()` |
| `BattlePrematureEndEvent` | No | A battle is removed early, such as when required Towny entities are disbanded. | `getBattle()` |

### Cell/flag events

| Event | Cancellable | Fired when | Accessors and mutators |
| --- | --- | --- | --- |
| `CellAttackEvent` | Yes | A player attempts to place/start a war flag. | `getPlayer()`, `getFlagBlock()`, `getFlagTimerLocation()`, `getFlagLightLocation()`, `getData()`, `getPhaseDuration()`, `setPhaseDuration(Duration)`, `getReason()`, `setReason(String)`, `hasReason()` |
| `CellAttackCanceledEvent` | Yes | BannerWar is processing cancellation/removal of an active attack. | `getCell()` |
| `CellDefendedEvent` | Yes | A flag is broken/defended before it wins its cell. | `getPlayer()`, `getCell()` |
| `CellWonEvent` | Yes | A flag survives and is about to win its cell. | `getCellUnderAttack()` |

Cancellation behavior follows the event's call site. For example, cancelling `CellAttackEvent` prevents flag creation and `setReason` can provide the player-facing failure reason. Cancelling `CellWonEvent`, `CellDefendedEvent`, or `CellAttackCanceledEvent` prevents the corresponding win/defense/cancel procedure from completing at that call site.

## Event timing overview

A typical attacker victory flow is:

1. `BattleStartEvent` fires when a battle starts in `PRE_FLAG`.
2. `BattleFlaggableEvent` fires when the battle enters `FLAG`.
3. `CellAttackEvent` fires for each flag placement attempt.
4. For each active flag, either `CellDefendedEvent` fires when it is broken or `CellWonEvent` fires when it survives.
5. Winning the homeblock triggers `BattleEndEvent` with `isDefenseWon() == false` and `BattleRuinEvent` as the town enters `RUINED`.
6. After the ruined stage, the battle becomes `DORMANT` until cooldown ends.

A typical defender victory flow is:

1. `BattleStartEvent` fires in `PRE_FLAG`.
2. `BattleFlaggableEvent` fires in `FLAG`.
3. Flag events occur as attackers place flags and defenders break them.
4. When the `FLAG` stage expires, `BattleEndEvent` fires with `isDefenseWon() == true` and the battle enters `DORMANT`.

## Versioning guidance

BannerWar preserves the historical package name for compatibility, but BannerWar-specific behavior is represented by `BannerWarAPI`, `Battle`, and battle events. Prefer `BannerWarAPI` for battle-level integrations and keep `FlagWarAPI` usage focused on active flag/cell lookups.

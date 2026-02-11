/*
 * Copyright 2021 TownyAdvanced
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.townyadvanced.flagwar.objects;

import io.github.townyadvanced.flagwar.util.HologramUtil;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.scheduling.ScheduledTask;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;

import io.github.townyadvanced.flagwar.CellAttackThread;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.HologramUpdateThread;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension of a {@link Cell} when under attack.
 **/
public class CellUnderAttack extends Cell {

    /** Holds an instance of FlagWar's logger. */
    private static final Logger LOGGER = FlagWar.getInstance().getLogger();
    /** The TaskScheduler used to schedule attacks and holograms. */
    private final TaskScheduler scheduler = FlagWar.getFlagWar().getScheduler();

    /** Holds the name of the war flag owner. */
    private final String nameOfFlagOwner;
    /** Holds the {@link Block} used as the base of the war flag. */
    private final Block flagBaseBlock;
    /** Holds the {@link Block} representing middle of the traditional war flag. */
    private final Block flagTimerBlock;
    /** Holds the {@link Block} representing the light-emitting top of a war flag. */
    private final Block flagLightBlock;
    /** Holds the value between timer phases for both the war flag and the beacon. */
    private final Duration flagPhaseDuration;
    /** {@link List} of {@link Block}s used in the war beacon's body. */
    private List<Block> beaconFlagBlocks;
    /** {@link List} of {@link Block}s used for the war beacon's wireframe. */
    private List<Block> beaconWireframeBlocks;
    /** Identifies the phase the war flag is in. **/
    private int flagPhaseID;
    /** A thread used to update the state of the {@link CellUnderAttack} using the Scheduler's repeating task.*/
    private final CellAttackThread thread;
    /** A task used to the thread used to cancel the repeating task.*/
    private ScheduledTask threadTask;
    /** A thread used to update a Hologram's timer. */
    private final HologramUpdateThread hologramThread;
    /** A task used by the hologramThread, to cancel the repeating task.*/
    private ScheduledTask hologramTask;
    /** Holds the time, in seconds, assuming 20 ticks is 1 second, of the war flag. */
    private Duration flagLifeTime;

    /**
     * Prepares the CellUnderAttack.
     *
     * @param flagOwner Name of the Resident that placed the flag
     * @param base {@link Block} representing the "flag pole" of the block
     * @param timerPhase Time (as a long) between Material shifting the flag and beacon.
     */
    public CellUnderAttack(final String flagOwner, final Block base, final Duration timerPhase) {

        super(base.getLocation());
        this.nameOfFlagOwner = flagOwner;
        this.flagBaseBlock = base;
        this.flagPhaseID = 0;
        this.flagLifeTime = FlagWarConfig.getFlagLifeTime();

        var world = base.getWorld();
        this.flagTimerBlock = world.getBlockAt(base.getX(), base.getY() + 1, base.getZ());
        this.flagLightBlock = world.getBlockAt(base.getX(), base.getY() + 2, base.getZ());

        this.flagPhaseDuration = timerPhase;
        this.thread = new CellAttackThread(this);
        this.hologramThread = new HologramUpdateThread(this);
    }

    /** @return if {@link CellUnderAttack} equals a given {@link Object}. (Defers to {@link Cell#equals(Object)}.) */
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    /** @return the {@link Cell#hashCode()} for the {@link CellUnderAttack}. */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** Function to load the war beacon. */
    public void loadBeacon() {
        if (!FlagWarConfig.isDrawingBeacon()) {
            Messaging.debug("loadBeacon() returned. Config:beacon.draw read as false");
            return;
        }

        beaconFlagBlocks = new ArrayList<>();
        beaconWireframeBlocks = new ArrayList<>();

        int beaconSize = FlagWarConfig.getBeaconSize();
        if (Coord.getCellSize() < beaconSize) {
            Messaging.debug("loadBeacon() returned. \"Coord#getCellSize()\" smaller than Config:beacon.size");
            return;
        }

        var minBlock = getBeaconMinBlock(getFlagBaseBlock().getWorld());
        var minHeight = (getTopOfFlagBlock().getY() + FlagWarConfig.getBeaconMinHeightAboveFlag());
        if (minHeight <= getTopOfFlagBlock().getY()) {
            Messaging.debug("loadBeacon() returned. Minimum Y-height <= top of flag.");
            Messaging.debug("Top-of-flag: %d, Beacon Min-Height: %d", getTopOfFlagBlock().getY(), minHeight);
            return;
        }

        int outerEdge = beaconSize - 1;
        Messaging.debug("(Beacon) Drawing. Now iterating over blocks.");
        for (var y = 0; y < beaconSize; y++) {
            for (var z = 0; z < beaconSize; z++) {
                for (var x = 0; x < beaconSize; x++) {
                    var block = flagBaseBlock.getWorld().getBlockAt(minBlock.getX() + x,
                        minBlock.getY() + y, minBlock.getZ() + z);
                    if (block.isEmpty()) {
                        Messaging.debug("(Beacon) Spawning %s at %d, %d, %d", block.toString(), x, y, z);
                        drawBeaconOrWireframe(outerEdge, y, z, x, block);
                    }
                }
            }
        }
    }

    private void drawBeaconOrWireframe(final int edge, final int y, final int z, final int x, final Block block) {
        int edgeCount = getEdgeCount(x, y, z, edge);
        if (edgeCount > 1) {
            beaconWireframeBlocks.add(block);
        } else if (edgeCount == 1) {
            beaconFlagBlocks.add(block);
        }
    }

    private Block getTopOfFlagBlock() {
        return flagLightBlock;
    }

    private int getEdgeCount(final int x, final int y, final int z, final int outerEdge) {
        return (zeroOrEq(x, outerEdge) ? 1 : 0) + (zeroOrEq(y, outerEdge) ? 1 : 0) + (zeroOrEq(z, outerEdge) ? 1 : 0);
    }

    /**
     * Simple Boolean to determine if an integer (a) is 0, or matches the secondary integer (b).
     * @param a the number being evaluated.
     * @param b the number being compared against.
     * @return TRUE if n is either 0 or equal to max.
     */
    private boolean zeroOrEq(final int a, final int b) {
        return a == 0 || a == b;
    }

    /**
     * Calculates and returns the {@link Block} at the origin-point of the beacon.
     * @param world the world the beacon should be drawn in. Used for retrieving the maximum world height, and returning
     *              the origin-point.
     * @return the Block at the origin-point of the beacon.
     */
    private Block getBeaconMinBlock(final World world) {
        int fromCorner = ((int) Math.floor(Coord.getCellSize() / 2.0)) - (FlagWarConfig.getBeaconRadius() - 1);
        int x = (getX() * Coord.getCellSize()) + fromCorner;
        int z = (getZ() * Coord.getCellSize()) + fromCorner;
        int maxY = world.getMaxHeight();
        int y = getTopOfFlagBlock().getY() + FlagWarConfig.getBeaconMaxHeightAboveFlag();

        if (y > maxY) {
            y = maxY - FlagWarConfig.getBeaconSize();
        }

        return world.getBlockAt(x, y, z);
    }

    /** @return the value of {@link #flagBaseBlock}. */
    public Block getFlagBaseBlock() {
        return flagBaseBlock;
    }

    /** @return the value of {@link #nameOfFlagOwner}. */
    public String getNameOfFlagOwner() {
        return nameOfFlagOwner;
    }

    /** @return TRUE if the {@link #flagPhaseID} is equal or greater than the length of
     * {@link FlagWarConfig#getTimerBlocks()} */
    public boolean hasEnded() {
        return flagPhaseID >= FlagWarConfig.getTimerBlocks().length;
    }

    /** Function to increment the {@link #flagPhaseID} and then run {@link #updateFlag()}. */
    public void changeFlag() {
        flagPhaseID += 1;
        updateFlag();
    }

    /**
     * Function to draw the war flag, and beacon wire frame (if {@link #beaconWireframeBlocks} is not empty.)
     * First, runs {@link #loadBeacon()}. Then, sets the {@link #flagBaseBlock} to the type defined by
     * {@link FlagWarConfig#getFlagBaseMaterial()}. Runs {@link #updateFlag()}, then proceeds to draw the
     * {@link #flagLightBlock}. Finally, for each {@link Block} in {@link #beaconWireframeBlocks}, draws it.
     */
    public void drawFlag() {
        loadBeacon();
        flagBaseBlock.setType(FlagWarConfig.getFlagBaseMaterial());
        updateFlag();
        flagLightBlock.setType(FlagWarConfig.getFlagLightMaterial());
        for (Block block : beaconWireframeBlocks) {
            block.setType(FlagWarConfig.getBeaconWireFrameMaterial());
        }
    }

    /**
     * If {@link #hasEnded()} returns False, update the {@link #flagTimerBlock} from the timerBlock array, using the
     * {@link #flagPhaseID} for the array ID. Iterate through and update the {@link #beaconFlagBlocks}.
     * Finally, log the update on the INFO channel.
     */
    public void updateFlag() {
        Material[] timer = FlagWarConfig.getTimerBlocks();
        if (!hasEnded()) {
            flagTimerBlock.setType(timer[flagPhaseID]);
            LOGGER.log(Level.INFO, () ->
                Translate.from("log.warflag-updated", getCellString(), timer[flagPhaseID].toString()));
            for (Block block : beaconFlagBlocks) {
                block.setType(timer[flagPhaseID]);
            }
        }
    }

    /** Set all blocks constituting the war flag and beacon as AIR. */
    public void destroyFlag() {
        flagLightBlock.setType(Material.AIR);
        flagTimerBlock.setType(Material.AIR);
        flagBaseBlock.setType(Material.AIR);
        for (Block block : beaconFlagBlocks) {
            block.setType(Material.AIR);
        }
        for (Block block : beaconWireframeBlocks) {
            block.setType(Material.AIR);
        }
    }

    /** Off-loaded to {@link HologramUtil#updateHologramTimer(String, Duration)}. */
    public void taskUpdateHologram() {
        this.flagLifeTime = flagLifeTime.minusSeconds(1);
        HologramUtil.updateHologramTimer(getCellHologramKey(), flagLifeTime);
    }

    /**
     * Draw the initial phase of the flag and jump-start both the {@link #thread} and {@link #hologramThread}.
     * <p>
     *     Uses the {@link #flagPhaseDuration} as both the repeat delay and runtime period for the {@link #thread}.
     *     The delay and period are derived from the phase duration in milliseconds, divided by 50.
     *     This value is floored to the last tick, and is not rounded.
     * </p>
     * <p>
     *     If {@link FlagWarConfig#isHologramEnabled()} returns true, draws a hologram, and if
     *     {@link FlagWarConfig#hasTimerLine()} returns true, also start a {@link #hologramThread}, with
     *     20 ticks as both the repeat delay and runtime timer.
     * </p>
     */
    public void beginAttack() {
        drawFlag();
        final int tps = 20;
        final int milliTicks = 50;
        final long ticksFromMs = this.flagPhaseDuration.toMillis() / milliTicks;
        threadTask = scheduler.runRepeating(thread, ticksFromMs, ticksFromMs);
        if (FlagWarConfig.isHologramEnabled()) {
            HologramUtil.drawHologram(getCellHologramKey(), flagLightBlock.getLocation(), flagLifeTime);

            if (FlagWarConfig.hasTimerLine()) {
                hologramTask = scheduler.runRepeating(hologramThread, tps, tps);
            }
        }
    }

    /**
     * Cancels the {@link #thread} task, started in {@link #beginAttack()}. Then runs {@link #destroyFlag()}.
     * Also cancels the {@link #hologramThread} task, if running, and destroys the Hologram, if it
     * exists, using {@link HologramUtil#destroyHologram(String)}.
     */
    public void cancel() {
        if (threadTask != null) {
            threadTask.cancel();
        }
        if (FlagWarConfig.isHologramEnabled() && hologramTask != null) {
            hologramTask.cancel();
        }
        destroyFlag();
        HologramUtil.destroyHologram(getCellHologramKey());
    }

    // -----------------------------------------------------------------
    // Utility – return a 16‑character hash of any input string.
    // -----------------------------------------------------------------
    private static String getFixedHash(String input) {
        try {
            // SHA‑256 gives us a 32‑byte digest.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the first 8 bytes (16 hex chars) to a string.
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {            // 8 bytes × 2 hex chars = 16
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();                    // e.g., "3f5a9c1b7d4e6f23"
        } catch (NoSuchAlgorithmException e) {
            // This should never happen – SHA‑256 is guaranteed to exist.
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String getCellHologramKey() {
        return getFixedHash(this.getCellString());
    }

    /** @return the string "'WORLD_NAME' ('X', 'Z')". */
    public String getCellString() {
        return String.format("%s (%d, %d)", getWorldName(), getX(), getZ());
    }

    /**
     * @param block the supplied {@link Block}.
     * @return TRUE if the supplied Block equals the {@link #flagLightBlock}.
     * */
    public boolean isFlagLight(final Block block) {
        return this.flagLightBlock.equals(block);
    }

    /**
     * @param block the supplied {@link Block}.
     * @return TRUE if the supplied Block equals the {@link #flagTimerBlock}.
     * */
    public boolean isFlagTimer(final Block block) {
        return this.flagTimerBlock.equals(block);
    }

    /**
     * @param block the supplied {@link Block}.
     * @return TRUE if the supplied Block equals the {@link #flagBaseBlock}.
     * */
    public boolean isFlagBase(final Block block) {
        return this.flagBaseBlock.equals(block);
    }

    /**
     * Check to see if the supplied {@link Block} is part of the war flag's construction.
     * @param block the supplied Block.
     * @return TRUE if the supplied block matches any of the conditions.
     */
    @SuppressWarnings("unused")
    public boolean isFlagPart(final Block block) {
        return isFlagTimer(block) || isFlagLight(block) || isFlagBase(block);
    }

    /**
     * @param block Supplied {@link Block}.
     * @return TRUE if the supplied Block is contained in either the {@link #beaconFlagBlocks} or
     * {@link #beaconWireframeBlocks} lists.
     */
    public boolean isPartOfBeacon(final Block block) {
        return beaconFlagBlocks != null && beaconFlagBlocks.contains(block) || beaconWireframeBlocks.contains(block);
    }

    /**
     * Checks to see if the supplied {@link Block} returns true for any of the following:
     * {@link #isPartOfBeacon(Block)}, {@link #isFlagBase(Block)}, or {@link #isFlagLight(Block)}.
     * @param block the supplied {@link Block}.
     * @return TRUE if any condition is true.
     */
    public boolean isImmutableBlock(final Block block) {
        return isPartOfBeacon(block) || isFlagBase(block) || isFlagLight(block);
    }
}

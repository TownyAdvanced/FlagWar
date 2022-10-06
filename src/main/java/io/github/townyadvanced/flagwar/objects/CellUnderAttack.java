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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.palmergames.bukkit.towny.object.Coord;
import io.github.townyadvanced.flagwar.CellAttackThread;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.HologramUpdateThread;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension of a {@link Cell} when under attack.
 **/
public class CellUnderAttack extends Cell {

    /** Holds an instance of FlagWar's logger. */
    private static final Logger LOGGER = FlagWar.getInstance().getLogger();
    /** Holds an instance of Towny. */
    private final Plugin plugin;

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
    /** A thread used to update the state of the {@link CellUnderAttack} using the scheduleSyncRepeatingTask. */
    private BukkitTask thread;
    /** A thread used to update the {@link #hologram}'s {@link #timerLine}. */
    private BukkitTask hologramThread;
    /** Holds the war flag hologram. */
    private Hologram hologram;
    /** Holds the time, in seconds, assuming 20 ticks is 1 second, of the war flag. */
    private Duration flagLifeTime;
    /** Holds the {@link TextLine} of the hologram timer line. */
    private TextLine timerLine;

    /**
     * Prepares the CellUnderAttack.
     *
     * @param instance A plugin instance. Preferably of Towny or FlagWar.
     * @param flagOwner Name of the Resident that placed the flag
     * @param base {@link Block} representing the "flag pole" of the block
     * @param timerPhase Time (as a long) between Material shifting the flag and beacon.
     */
    public CellUnderAttack(final Plugin instance, final String flagOwner, final Block base, final Duration timerPhase) {

        super(base.getLocation());
        this.plugin = instance;
        this.nameOfFlagOwner = flagOwner;
        this.flagBaseBlock = base;
        this.flagPhaseID = 0;
        this.flagLifeTime = FlagWarConfig.getFlagLifeTime();

        var world = base.getWorld();
        this.flagTimerBlock = world.getBlockAt(base.getX(), base.getY() + 1, base.getZ());
        this.flagLightBlock = world.getBlockAt(base.getX(), base.getY() + 2, base.getZ());

        this.flagPhaseDuration = timerPhase;
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

    /**
     * Function to draw the {@link #hologram}. Retrieves hologram settings via
     * {@link FlagWarConfig#getHologramSettings()}. Creates a new {@link #hologram} supplying the plugin instance with
     * {@link FlagWar#getInstance()}, and a temporary {@link org.bukkit.Location} using the {@link #flagLightBlock}.
     * Disables visibility. Then, iterates through the hologram settings, adding lines corresponding to the line
     * type and data. Finally, adjusts the location of the hologram according to the height of the {@link #hologram},
     * and enables its visibility.
     * */
    public void drawHologram() {
        List<Map.Entry<String, String>> holoSettings = FlagWarConfig.getHologramSettings();
        Location loc = flagLightBlock.getLocation();
        hologram = HologramsAPI.createHologram(FlagWar.getInstance(), loc);
        hologram.getVisibilityManager().setVisibleByDefault(false);
        for (Map.Entry<String, String> holoSetting : holoSettings) {
            var type = holoSetting.getKey();
            var data = holoSetting.getValue();

            switch (type) {
                case "item" -> {
                    Material material = Material.matchMaterial(data);
                    if (material != null) {
                        hologram.appendItemLine(new ItemStack(material));
                    }
                }
                case "text" -> hologram.appendTextLine(data);
                case "timer" -> setTimerLine(data);
                default -> hologram.appendTextLine("");
            }
        }
        final double hOffset = 0.5d;
        final double vOffset = 0.9d;
        hologram.teleport(loc.add(hOffset, vOffset + hologram.getHeight(), hOffset));
        hologram.getVisibilityManager().setVisibleByDefault(true);
    }

    /**
     * Simple expression to set the timerLine for the hologram.
     * @param data the value of a hologram setting (defined in {@link #drawHologram()}
     */
    private void setTimerLine(final String data) {
        timerLine = hologram.appendTextLine(formatTime(flagLifeTime, data));
    }

    /**
     * Decreases {@link #flagLifeTime} by 1, and sets the {@link #hologram} {@link #timerLine} text using
     * {@link #formatTime(Duration, String)} with the updated {@link #flagLifeTime} and
     * {@link FlagWarConfig#getTimerText()} as the parameters.
     */
    public void updateHologram() {
        this.flagLifeTime = flagLifeTime.minusSeconds(1);
        timerLine.setText(formatTime(flagLifeTime, FlagWarConfig.getTimerText()));
    }

    /** Deletes the {@link #hologram}. */
    public void destroyHologram() {
        this.hologram.delete();
    }

    /**
     * Function used to format a {@link Duration} according to the formatting defined in
     * {@link FlagWarConfig#getTimerText()}.
     * @param duration Duration to extrapolate the hours, minutes, and seconds from.
     * @param formatString The string to format. Should contain one or more format specifiers with argument indexes
     *                 corresponding with seconds, minutes, and hours, respectively.
     * @return The formatted string.
     */
    public String formatTime(final Duration duration, final String formatString) {
        final int hoursInDay = 24;
        final long hours = duration.toHoursPart() + (duration.toDaysPart() * hoursInDay);
        final int minutes = duration.toMinutesPart();
        final int seconds = duration.toSecondsPart();
        return String.format(formatString, seconds, minutes, hours);
    }

    /**
     * Draw the initial phase of the flag and jumpstart both the {@link #thread} and {@link #hologramThread}.
     * <p>
     *     Uses the {@link #flagPhaseDuration} as both the repeat delay and runtime period for the {@link #thread}.
     *     The delay and period are derived from the phase duration in milliseconds, divided by 50.
     *     This value is floored to the last tick, and is not rounded.
     * </p>
     * <p>
     *     If {@link FlagWarConfig#isHologramEnabled()} returns true, execute {@link #drawHologram()}, and if
     *     {@link FlagWarConfig#hasTimerLine()} returns true, also start a {@link #hologramThread}, with
     *     20 ticks as both the repeat delay and runtime timer.
     * </p>
     */
    public void beginAttack() {
        drawFlag();
        final int tps = 20;
        final int milliTicks = 50;
        final long ticksFromMs = this.flagPhaseDuration.toMillis() / milliTicks;
        thread = plugin.getServer().getScheduler()
            .runTaskTimer(plugin, new CellAttackThread(this), ticksFromMs, ticksFromMs);
        if (FlagWarConfig.isHologramEnabled()) {
            drawHologram();
            if (FlagWarConfig.hasTimerLine()) {
                hologramThread = plugin.getServer().getScheduler()
                    .runTaskTimer(plugin, new HologramUpdateThread(this), tps, tps);
            }
        }
    }

    /**
     * Cancels the {@link #thread} task, started in {@link #beginAttack()}. Then runs {@link #destroyFlag()}.
     * Also cancels the {@link #hologramThread} task, if running, and destroys the {@link #hologram}, if it
     * exists, using {@link #destroyHologram()}.
     */
    public void cancel() {
        if (thread != null) {
            thread.cancel();
        }
        if (FlagWarConfig.isHologramEnabled() && hologramThread != null) {
            hologramThread.cancel();
        }
        destroyFlag();
        if (hologram != null) {
            destroyHologram();
        }
    }

    /** @return the string "%getWorldName% (%getX%, %getZ%)". */
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

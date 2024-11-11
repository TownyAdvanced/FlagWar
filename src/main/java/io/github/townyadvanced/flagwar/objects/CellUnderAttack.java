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
import com.palmergames.bukkit.towny.scheduling.ScheduledTask;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
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
import org.jetbrains.annotations.ApiStatus;

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
    /** A thread used to update the {@link #hdHologramsAPI}'s {@link #timerLine}. */
    private final HologramUpdateThread hologramThread;
    /** A task used to the hologramThread used to cancel the repeating task.*/
    private ScheduledTask hologramTask;
    /** Holds the war flag hologram. */
    private Hologram hdHologramsAPI;
    /** Holds the time, in seconds, assuming 20 ticks is 1 second, of the war flag. */
    private Duration flagLifeTime;
    /** Holds the {@link TextLine} of the hologram timer line. (HolographicDisplays) */
    private TextLine timerLine;
    /** Holds a line for DescentHolograms, mimicking the {@link #timerLine}. */
    private HologramLine timerLineDHAPI;

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

    /**
     * Function to draw the {@link #hdHologramsAPI}.
     * Retrieves hologram settings via {@link FlagWarConfig#getHologramSettings()}.
     * Will try to draw a hologram with the following priortiy: HolographicDisplays, DescentHolograms
     * */
    public void drawHologram() {
        List<Map.Entry<String, String>> holoSettings = FlagWarConfig.getHologramSettings();
        Location loc = flagLightBlock.getLocation();

        // HolographicDisplays
        Plugin holographicDisplays = FlagWar.getInstance().getServer().getPluginManager()
            .getPlugin("HolographicDisplays");
        if (holographicDisplays != null && holographicDisplays.isEnabled()) {
            drawHolographicDisplay(loc, holoSettings);
            return;
        }

        // DescentHolograms
        Plugin descentHolograms = FlagWar.getInstance().getServer().getPluginManager().getPlugin("DescentHolograms");
        if (descentHolograms != null && descentHolograms.isEnabled()) {
            drawDescentHologram(loc, holoSettings);
        }
    }

    /**
     * Draw Hologram using HolographicDisplays
     * <p>
     *     Process:
     *     <ol>
     *         <li>Creates a new {@link #hdHologramsAPI}, using the plugin instance and supplied location
     *         (from a {@link #flagLightBlock}).</li>
     *         <li>Disables default visibility</li>
     *         <li>Iterates through hologram settings, adding lines corresponding to the line type and line data.</li>
     *         <li>Adjusts the location of the hologram according to the height of the {@link #hdHologramsAPI}</li>
     *         <li>Enables visibility.</li>
     *     </ol>
     * </p>
     * @param location {@link #flagLightBlock} ({@link Block#getLocation})
     * @param holoSettings Map of 'holograms.lines' from the config.
     */
    private void drawHolographicDisplay(final Location location, final List<Map.Entry<String, String>> holoSettings) {
        hdHologramsAPI = HologramsAPI.createHologram(FlagWar.getInstance(), location);
        hdHologramsAPI.getVisibilityManager().setVisibleByDefault(false);
        for (Map.Entry<String, String> holoSetting : holoSettings) {
            var type = holoSetting.getKey();
            var data = holoSetting.getValue();

            switch (type) {
                case "item" -> {
                    Material material = Material.matchMaterial(data);
                    if (material != null) {
                        hdHologramsAPI.appendItemLine(new ItemStack(material));
                    }
                }
                case "text" -> hdHologramsAPI.appendTextLine(data);
                case "timer" -> setTimerLine(data);
                default -> hdHologramsAPI.appendTextLine("");
            }
        }
        final double hOffset = 0.5d;
        final double vOffset = 0.9d;
        hdHologramsAPI.teleport(location.add(hOffset, vOffset + hdHologramsAPI.getHeight(), hOffset));
        hdHologramsAPI.getVisibilityManager().setVisibleByDefault(true);
    }

    /**
     * Draw Hologram using DescentHolograms ({@link DHAPI})
     * <p>
     *     Process:
     *     <ol>
     *         <li>Create a DescentHolograms hologram using {@link DHAPI#createHologram(String, Location)}.
     *         Use the CellString as the hologram's name.</li>
     *         <li>Set Invisible</li>
     *         <li>Add Lines</li>
     *         <li>Set offset</li>
     *         <li>Set Visible</li>
     *     </ol>
     * </p>
     * @param location Location to initially spawn the hologram.
     * @param holoSettings Map of 'holograms.lines' from the config.
     */
    @ApiStatus.Experimental
    private void drawDescentHologram(final Location location, final List<Map.Entry<String, String>> holoSettings) {
        FlagWar.getFlagWar().getLogger().warning("DescentHolograms support is experimental.");

        //Holograph Name (CellString)
        String hologramName = this.getCellString();

        // Create Invisible
        eu.decentsoftware.holograms.api.holograms.Hologram hologram =
            DHAPI.createHologram(hologramName, location, false);
        hologram.setDefaultVisibleState(false);

        // Add Lines
        for (Map.Entry<String, String> holoSetting : holoSettings) {
            var type = holoSetting.getKey();
            var data = holoSetting.getValue();

            switch (type) {
                case "item" -> {
                    Material material = Material.matchMaterial(data);
                    if (material != null) {
                        DHAPI.addHologramLine(hologram, material);
                    }
                }
                case "text" -> DHAPI.addHologramLine(hologram, data);
                case "timer" -> setTimerLineDHAPI(hologram, data);
                default -> DHAPI.addHologramLine(hologram, "");
            }
        }

        //Teleport
        final double hOffset = 0.5d;
        final double vOffset = 0.9d;
        final double textHeight = 0.23d;
        Location offset = location.add(hOffset, vOffset + (hologram.getPage(0).size() * textHeight), hOffset);
        hologram.setLocation(offset);

        //Set Visible
        hologram.setDefaultVisibleState(true);

    }

    /**
     * Simple expression to set the {@link #timerLine} for HolographicDisplays .
     * @param data the value of a hologram setting (defined in {@link #drawHologram()}
     */
    private void setTimerLine(final String data) {
        timerLine = hdHologramsAPI.appendTextLine(formatTime(flagLifeTime, data));
    }

    /**
     * Simple expression to set the {@link #timerLineDHAPI} for DescentHolograms.
     * @param holo Parent Hologram
     * @param fmtT Time Format String
     */
    private void setTimerLineDHAPI(final eu.decentsoftware.holograms.api.holograms.Hologram holo, final String fmtT) {
        timerLineDHAPI = DHAPI.addHologramLine(holo, formatTime(flagLifeTime, fmtT));
    }

    /**
     * Decreases {@link #flagLifeTime} by 1, and sets the {@link #hdHologramsAPI} {@link #timerLine} text using
     * {@link #formatTime(Duration, String)} with the updated {@link #flagLifeTime} and
     * {@link FlagWarConfig#getTimerText()} as the parameters.
     */
    public void updateHologram() {
        this.flagLifeTime = flagLifeTime.minusSeconds(1);
        if (timerLine != null) {
            timerLine.setText(formatTime(flagLifeTime, FlagWarConfig.getTimerText()));
        }
        if (timerLineDHAPI != null) {
            timerLineDHAPI.setText(formatTime(flagLifeTime, FlagWarConfig.getTimerText()));
        }
    }

    /** Destroys the hologram, after some null-checking. */
    public void destroyHologram() {
        if (hdHologramsAPI != null) {
            this.hdHologramsAPI.delete();
        }
        if (timerLineDHAPI != null) {
            timerLineDHAPI.getParent().getParent().delete();
        }
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
        threadTask = scheduler.runRepeating(thread, ticksFromMs, ticksFromMs);
        if (FlagWarConfig.isHologramEnabled()) {
            drawHologram();
            if (FlagWarConfig.hasTimerLine()) {
                hologramTask = scheduler.runRepeating(hologramThread, tps, tps);
            }
        }
    }

    /**
     * Cancels the {@link #thread} task, started in {@link #beginAttack()}. Then runs {@link #destroyFlag()}.
     * Also cancels the {@link #hologramThread} task, if running, and destroys the {@link #hdHologramsAPI}, if it
     * exists, using {@link #destroyHologram()}.
     */
    public void cancel() {
        if (threadTask != null) {
            threadTask.cancel();
        }
        if (FlagWarConfig.isHologramEnabled() && hologramTask != null) {
            hologramTask.cancel();
        }
        destroyFlag();
        if (hdHologramsAPI != null) {
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

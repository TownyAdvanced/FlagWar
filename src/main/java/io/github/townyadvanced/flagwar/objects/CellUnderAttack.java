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
import com.palmergames.bukkit.towny.Towny;
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
    private final Towny towny;

    /** Holds the name of the war flag owner. */
    private final String nameOfFlagOwner;
    /** Holds the {@link Block} used as the base of the war flag. */
    private final Block flagBaseBlock;
    /** Holds the {@link Block} representing middle of the traditional war flag. */
    private final Block flagTimerBlock;
    /** Holds the {@link Block} representing the light-emitting top of a war flag. */
    private final Block flagLightBlock;
    /** Holds the value between timer phases for both the war flag and the beacon. */
    private final long flagPhaseInterval;
    /** {@link List} of {@link Block}s used in the war beacon's body. */
    private List<Block> beaconFlagBlocks;
    /** {@link List} of {@link Block}s used for the war beacon's wireframe. */
    private List<Block> beaconWireframeBlocks;
    /** Identifies the phase the war flag is in. **/
    private int flagPhaseID;
    /** A thread used to update the state of the {@link CellUnderAttack} using the scheduleSyncRepeatingTask. */
    private int thread;
    /** A thread used to update the {@link #hologram}'s {@link #timerLine}. */
    private int hologramThread;
    /** Holds the war flag hologram. */
    private Hologram hologram;
    /** Holds the time, in seconds, assuming 20 ticks is 1 second, of the war flag. */
    private int time;
    /** Holds the {@link TextLine} of the hologram timer line. */
    private TextLine timerLine;

    /**
     * Prepares the CellUnderAttack.
     *
     * @param townyInst Instance of {@link Towny}
     * @param flagOwner Name of the Resident that placed the flag
     * @param flagBase {@link Block} representing the "flag pole" of the block
     * @param phaseTime Time (as a long) between Material shifting the flag and beacon.
     */
    public CellUnderAttack(final Towny townyInst, final String flagOwner, final Block flagBase, final long phaseTime) {

        super(flagBase.getLocation());
        this.towny = townyInst;
        this.nameOfFlagOwner = flagOwner;
        this.flagBaseBlock = flagBase;
        this.flagPhaseID = 0;
        this.thread = -1;
        hologramThread = -1;
        final long ticksInSecond = 20L;
        this.time = (int) (FlagWarConfig.getFlagWaitingTime() / ticksInSecond);

        var world = flagBase.getWorld();
        this.flagTimerBlock = world.getBlockAt(flagBase.getX(), flagBase.getY() + 1, flagBase.getZ());
        this.flagLightBlock = world.getBlockAt(flagBase.getX(), flagBase.getY() + 2, flagBase.getZ());

        this.flagPhaseInterval = phaseTime;
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
            Messaging.debug("Top-of-flag: %d, Beacon Min-Height: %d",
                new Object[]{getTopOfFlagBlock().getY(), minHeight});
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
                        Messaging.debug("(Beacon) Spawning %s at %d, %d, %d", new Object[] {block.toString(), x, y, z});
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
        timerLine = hologram.appendTextLine(formatTime(time, data));
    }

    /**
     * Decreases {@link #time} by 1, and sets the {@link #hologram} {@link #timerLine} text using
     * {@link #formatTime(int, String)} with {@link #time} and {@link FlagWarConfig#getTimerText()}
     * as the parameters.
     * */
    public void updateHologram() {
        time -= 1;
        timerLine.setText(formatTime(time, FlagWarConfig.getTimerText()));
    }

    /** Deletes the {@link #hologram}. */
    public void destroyHologram() {
        this.hologram.delete();
    }

    /**
     * Function used to format the hologram {@link #time} according to the timer text defined in
     * {@link FlagWarConfig#getTimerText()}.
     * @param timeIn Time, in seconds.
     * @param toFormat The string to format. Should contain one or more format specifiers with argument indexes
     *                 corresponding with seconds, minutes, and hours, respectively.
     * @return The formatted string.
     * */
    public String formatTime(final int timeIn, final String toFormat) {
        final int secondsInMinute = 60;
        final int secondsInHour = 3600;
        final int seconds = (timeIn % secondsInMinute);
        final int minutes = (timeIn % secondsInHour) / secondsInMinute;
        final int hours = timeIn / secondsInHour;
        return String.format(toFormat, seconds, minutes, hours);
    }

    /**
     * {@link #drawFlag()}, then schedule a {@link CellAttackThread} synchronously on the main thread, using the
     * {@link #flagPhaseInterval} as both the repeat delay and runtime timer.
     * If {@link FlagWarConfig#isHologramEnabled()} returns true, also {@link #drawHologram()}, and if
     * {@link FlagWarConfig#hasTimerLine()} returns true, also start a {@link #hologramThread}, with
     * 20 ticks as both the repeat delay and runtime timer.
     */
    public void beginAttack() {
        drawFlag();
        thread = towny.getServer().getScheduler().scheduleSyncRepeatingTask(towny,
            new CellAttackThread(this),
            this.flagPhaseInterval,
            this.flagPhaseInterval);
        final long ticksInSecond = 20L;
        if (FlagWarConfig.isHologramEnabled()) {
            drawHologram();
            if (FlagWarConfig.hasTimerLine()) {
                hologramThread = towny.getServer().getScheduler().scheduleSyncRepeatingTask(towny,
                    new HologramUpdateThread(this),
                    ticksInSecond,
                    ticksInSecond);
            }
        }
    }

    /**
     * Cancels the {@link #thread} task, started in {@link #beginAttack()}. Then runs {@link #destroyFlag()}.
     * Also cancels the {@link #hologramThread} task, if running, and destroys the {@link #hologram}, if it
     * exists, using {@link #destroyHologram()}.
     */
    public void cancel() {
        if (thread != -1) {
            towny.getServer().getScheduler().cancelTask(thread);
        }
        if (hologramThread != -1) {
            towny.getServer().getScheduler().cancelTask(hologramThread);
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
        return beaconFlagBlocks.contains(block) || beaconWireframeBlocks.contains(block);
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

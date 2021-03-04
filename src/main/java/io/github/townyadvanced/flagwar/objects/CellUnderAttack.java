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

import io.github.townyadvanced.flagwar.CellAttackThread;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.townyadvanced.flagwar.i18n.Translate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Coord;

public class CellUnderAttack extends Cell {

    private static final Logger LOGGER = FlagWar.getInstance().getLogger();
    private final Towny towny;
	private final String nameOfFlagOwner;
	private final Block flagBaseBlock;
    private final Block flagBlock;
    private final Block flagLightBlock;
    private List<Block> beaconFlagBlocks;
    private List<Block> beaconWireframeBlocks;
	private int flagColorId;
	private int thread;
	private final long timeBetweenColorChange;

	/**
	 * CellUnderAttack class constructor
	 *
	 * @param towny Instance of {@link Towny}
	 * @param nameOfFlagOwner Name of the Resident that placed the flag
	 * @param flagBaseBlock Flag representing the "flag pole" of the block
	 * @param timeBetweenColorChange Time (as a long) between color shifting the flag and beacon.
	 */
	public CellUnderAttack(Towny towny, String nameOfFlagOwner, Block flagBaseBlock, long timeBetweenColorChange) {

		super(flagBaseBlock.getLocation());
		this.towny = towny;
		this.nameOfFlagOwner = nameOfFlagOwner;
		this.flagBaseBlock = flagBaseBlock;
		this.flagColorId = 0;
		this.thread = -1;

		World world = flagBaseBlock.getWorld();
		this.flagBlock = world.getBlockAt(flagBaseBlock.getX(), flagBaseBlock.getY() + 1, flagBaseBlock.getZ());
		this.flagLightBlock = world.getBlockAt(flagBaseBlock.getX(), flagBaseBlock.getY() + 2, flagBaseBlock.getZ());

		this.timeBetweenColorChange = timeBetweenColorChange;
	}

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void loadBeacon() {

		beaconFlagBlocks = new ArrayList<>();
		beaconWireframeBlocks = new ArrayList<>();

		if (!FlagWarConfig.isDrawingBeacon())
			return;

		int beaconSize = FlagWarConfig.getBeaconSize();
		if (Coord.getCellSize() < beaconSize)
			return;

		Block minBlock = getBeaconMinBlock(getFlagBaseBlock().getWorld());
		if (getMinimumHeightForBeacon() >= minBlock.getY())
			return;

		int outerEdge = beaconSize - 1;
		for (int y = 0; y < beaconSize; y++) {
			for (int z = 0; z < beaconSize; z++) {
				for (int x = 0; x < beaconSize; x++) {
					Block block = flagBaseBlock.getWorld().getBlockAt(minBlock.getX() + x, minBlock.getY() + y, minBlock.getZ() + z);
					if (block.isEmpty()) {
                        shouldAddBeaconFlagBlock(outerEdge, y, z, x, block);
                        shouldAddBeaconWireframeBlock(outerEdge, y, z, x, block);
                    }
				}
			}
		}
	}

    private void shouldAddBeaconWireframeBlock(int outerEdge, int y, int z, int x, Block block) {
        int edgeCount = getEdgeCount(x, y, z, outerEdge);
        if (edgeCount > 1) {
            beaconWireframeBlocks.add(block);
        }
    }

    private void shouldAddBeaconFlagBlock(int outerEdge, int y, int z, int x, Block block) {
        int edgeCount = getEdgeCount(x, y, z, outerEdge);
        if (edgeCount == 1) {
            beaconFlagBlocks.add(block);
        }
    }

    private Block getTopOfFlagBlock() {

		return flagLightBlock;
	}

	private int getMinimumHeightForBeacon() {

		return getTopOfFlagBlock().getY() + FlagWarConfig
            .getBeaconMinHeightAboveFlag();
	}

	private int getEdgeCount(int x, int y, int z, int outerEdge) {

		return (zeroOr(x, outerEdge) ? 1 : 0) + (zeroOr(y, outerEdge) ? 1 : 0) + (zeroOr(z, outerEdge) ? 1 : 0);
	}

	private boolean zeroOr(int n, int max) {

		return n == 0 || n == max;
	}

	private Block getBeaconMinBlock(World world) {

		int middle = (int) Math.floor(Coord.getCellSize() / 2.0);
		int radiusCenterExpansion = FlagWarConfig.getBeaconRadius() - 1;
		int fromCorner = middle - radiusCenterExpansion;

		int x = (getX() * Coord.getCellSize()) + fromCorner;
		int z = (getZ() * Coord.getCellSize()) + fromCorner;

		int maxY = world.getMaxHeight();
		int y = getTopOfFlagBlock().getY() + FlagWarConfig
            .getBeaconMaxHeightAboveFlag();
		if (y > maxY) {
			y = maxY - FlagWarConfig.getBeaconSize();
		}

		return world.getBlockAt(x, y, z);
	}

	public Block getFlagBaseBlock() {

		return flagBaseBlock;
	}

	public String getNameOfFlagOwner() {

		return nameOfFlagOwner;
	}

	public boolean hasEnded() {

		return flagColorId >= FlagWarConfig.getTimerBlocks().length;
	}

	public void changeFlag() {

		flagColorId += 1;
		updateFlag();
	}

	public void drawFlag() {

		loadBeacon();

		flagBaseBlock.setType(FlagWarConfig.getFlagBaseMaterial());
		updateFlag();
		flagLightBlock.setType(FlagWarConfig.getFlagLightMaterial());
		for (Block block : beaconWireframeBlocks)
			block.setType(FlagWarConfig.getBeaconWireFrameMaterial());
	}

	public void updateFlag() {

		Material[] woolColors = FlagWarConfig.getTimerBlocks();
		if (flagColorId < woolColors.length) {

            LOGGER.log(Level.INFO, () ->
                Translate.from("log.warflag-updated-color", getCellString(), woolColors[flagColorId].toString()));

			flagBlock.setType(woolColors[flagColorId]);

			for (Block block : beaconFlagBlocks)
				block.setType(woolColors[flagColorId]);

		}
	}

	public void destroyFlag() {

		flagLightBlock.setType(Material.AIR);
		flagBlock.setType(Material.AIR);
		flagBaseBlock.setType(Material.AIR);
		for (Block block : beaconFlagBlocks)
			block.setType(Material.AIR);
		for (Block block : beaconWireframeBlocks)
			block.setType(Material.AIR);
	}

	public void begin() {

		drawFlag();
		thread = towny.getServer().getScheduler().scheduleSyncRepeatingTask(towny, new CellAttackThread(this), this.timeBetweenColorChange, this.timeBetweenColorChange);
	}

	public void cancel() {

		if (thread != -1)
			towny.getServer().getScheduler().cancelTask(thread);

		destroyFlag();
	}

	public String getCellString() {
		return String.format("%s (%d, %d)", getWorldName(), getX(), getZ());
	}

	public boolean isFlagLight(Block block) {
		return this.flagLightBlock.equals(block);
	}

	public boolean isFlag(Block block) {
		return this.flagBlock.equals(block);
	}

	public boolean isFlagBase(Block block) {
		return this.flagBaseBlock.equals(block);
	}

	public boolean isPartOfBeacon(Block block) {
		return beaconFlagBlocks.contains(block) || beaconWireframeBlocks.contains(block);
	}

	public boolean isImmutableBlock(Block block) {
		return isPartOfBeacon(block) || isFlagBase(block) || isFlagLight(block);
	}
}

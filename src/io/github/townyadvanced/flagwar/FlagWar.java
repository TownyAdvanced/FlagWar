/*
 * Copyright (c) 2021 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.utils.AreaSelectionUtil;
import com.palmergames.bukkit.util.Version;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.CellAttackCanceledEvent;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.events.CellDefendedEvent;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import io.github.townyadvanced.flagwar.listeners.FlagWarBlockListener;
import io.github.townyadvanced.flagwar.listeners.FlagWarCustomListener;
import io.github.townyadvanced.flagwar.listeners.FlagWarEntityListener;
import io.github.townyadvanced.flagwar.listeners.WarzoneListener;
import io.github.townyadvanced.flagwar.objects.Cell;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FlagWar extends JavaPlugin {

    private static final PluginManager pluginManager = Bukkit.getPluginManager();
    private static final Map<Cell, CellUnderAttack> cellsUnderAttack = new HashMap<>();
	private static final Map<String, List<CellUnderAttack>> cellsUnderAttackByPlayer = new HashMap<>();
	private static final Map<Town, Long> lastFlag = new HashMap<>();
    private static final FlagWar plugin = FlagWar.getPlugin();
    private static final Version MIN_TOWNY_VER = Version.fromString("0.96.7.0");
    private final Logger logger;
    private final FlagWarBlockListener flagWarBlockListener = new FlagWarBlockListener(this);
	private final FlagWarCustomListener flagWarCustomListener = new FlagWarCustomListener(this);
	private final FlagWarEntityListener flagWarEntityListener = new FlagWarEntityListener();
	private final WarzoneListener warzoneListener = new WarzoneListener();

	public FlagWar(){
	    logger = this.getLogger();
    }

	@Override
    public void onEnable() {
        try {
            loadConfig();
        } catch (IOException e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

        brandingMessage();
        checkTowny();
        loadFlagWarMaterials();
        registerEvents();
    }

    @Override
    public void onDisable() {
        logger.info("Stopping Flag War");
        logger.info("Attempting to cancel all attacks gracefully.");
        try {
            for (CellUnderAttack cell : new ArrayList<>(cellsUnderAttack.values()))
                attackCanceled(cell);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        logger.info("Flag War Disabled.");
    }

    private void loadConfig() throws IOException, InvalidConfigurationException  {
        double needConfVer = 1.0;

        this.saveDefaultConfig();
        File configFile = new File("plugins/FlagWar/config.yml");
        this.getConfig().load(configFile);

        if (this.getConfig().getDouble("config_version") < needConfVer) {
            File backupFile = new File("plugins/FlagWar/config.old.yml");
            if (backupFile.createNewFile())
                logger.warning("Created new backup location: Flagwar/config.old.yml");
            regenerateConfiguration(configFile, backupFile);
        }
        // TODO: Check for an uneven node-count between config and defaultConfig.

        this.getConfig().load(configFile);
    }

    private void regenerateConfiguration(File configFile, File backupFile) throws IOException {
        if(!backupConfig(configFile, backupFile))
            onDisable();

        Files.delete(configFile.toPath());
        this.saveDefaultConfig();
    }

    private boolean backupConfig(File sourceFile, File targetFile) throws IOException {
	    logger.warning("Attempting to back up the configuration.");
        if (targetFile.exists()){
            Files.copy(sourceFile.toPath(), targetFile.toPath(), REPLACE_EXISTING);
            logger.warning("Configuration Backup Successful. Old Backup Replaced.");
            return true;
        }
        else {
            logger.severe("ABORTING: Unable to back up configuration! Please back up manually.");
            return false;
        }
	}

    private void checkTowny() {
	    logger.info("Checking against Towny...");
        Towny towny = Towny.getPlugin();
        if (towny == null) {
            logger.severe("Towny is not running, or was simply not found. Disabling Flag War!");
            onDisable();
        }
        else if (towny.isError()) {
            logger.severe("Towny is in Safe Mode! Disabling Flag War!");
            onDisable();
        } else {
            checkTownyVersionCompatibility(towny);
        }
    }

    private void checkTownyVersionCompatibility(Towny towny) {
        Version townyVersion = Version.fromString(towny.getVersion());
        if (townyVersion.compareTo(MIN_TOWNY_VER) < 0) {
            String message = "The running copy of Towny is too old! You need at least Towny v"
                + MIN_TOWNY_VER.toString() + " to use FlagWar with Towny.";
            logger.severe(message);
            onDisable();
        }
    }

	public void registerEvents() {
        logger.info("Registering Events...");
        pluginManager.registerEvents(flagWarBlockListener, this);
        pluginManager.registerEvents(flagWarCustomListener, this);
        pluginManager.registerEvents(flagWarEntityListener, this);
        pluginManager.registerEvents(warzoneListener, this);
        logger.info("Events registered.");
	}

    public static FlagWar getPlugin() {
        return plugin;
    }

    private void brandingMessage() {
        if (this.getConfig().getBoolean("show-startup-marquee")){
            String indent = "                  ";
            String marquee = String.format("%n%n"
                + "%s ▄████  █    ██     ▄▀        ▄ ▄   ██   █▄▄▄▄ %n"
                + "%s █▀   ▀ █    █ █  ▄▀         █   █  █ █  █  ▄▀ %n"
                + "%s █▀▀    █    █▄▄█ █ ▀▄      █ ▄   █ █▄▄█ █▀▀▌  %n"
                + "%s █      ███▄ █  █ █   █     █  █  █ █  █ █  █  %n"
                + "%s  █         ▀   █  ███       █ █ █     █   █   %n"
                + "%s   ▀           █              ▀ ▀     █   ▀    %n"
                + "%s              ▀                      ▀         %n"
                + "%s                 Version %s                    %n"
                + "%s        Copyright \u00a9 2021 TownyAdvanced    %n",
                indent, indent, indent, indent, indent, indent, indent,
                indent, this.getDescription().getVersion(), indent);
            logger.info(marquee);
        } else {
            String flagWarTM = String.format("Flag War, v%s - Copyright \u00a9 2021 TownyAdvanced",
                this.getDescription().getVersion());
            logger.info(flagWarTM);
        }
    }

	public static void registerAttack(CellUnderAttack cell) throws TownyException {

		CellUnderAttack attackCell = cellsUnderAttack.get(cell);

		if (attackCell != null)
			throw new AlreadyRegisteredException(Translation.of("msg_err_enemy_war_cell_already_under_attack", attackCell.getNameOfFlagOwner()));

		String playerName = cell.getNameOfFlagOwner();

		// Check that the user is under his limit of active war flags.
		int futureActiveFlagCount = getNumActiveFlags(playerName) + 1;
		if (futureActiveFlagCount > FlagWarConfig.getMaxActiveFlagsPerPerson())
			throw new TownyException(Translation.of("msg_err_enemy_war_reached_max_active_flags", FlagWarConfig
                .getMaxActiveFlagsPerPerson()));

		addFlagToPlayerCount(playerName, cell);
		cellsUnderAttack.put(cell, cell);
		cell.begin();
	}

	private void loadFlagWarMaterials() {
        logger.info("Loading Material Lists...");
        String flagLight = Objects.requireNonNull(this.getConfig().getString("flag.light_block"));
        String flagBase = Objects.requireNonNull(this.getConfig().getString("flag.base_block"));
        String beaconWireframe = Objects.requireNonNull(this.getConfig().getString("beacon.wireframe_block"));


        Material lightBlock = Material.matchMaterial(flagLight);
        if (lightBlock != null && lightBlock.isBlock() && !lightBlock.isAir() && !lightBlock.hasGravity())
            FlagWarConfig.setFlagLightMaterial(lightBlock);
        else {
            FlagWarConfig.setFlagLightMaterial(Material.TORCH);
            logger.warning("Flag light not defined properly. Defaulting to TORCH.");
        }

        Material baseBlock = Material.matchMaterial(flagBase);
        if (baseBlock != null && baseBlock.isBlock() && !baseBlock.isAir() && !baseBlock.hasGravity())
            FlagWarConfig.setFlagBaseMaterial(baseBlock);
        else {
            FlagWarConfig.setFlagBaseMaterial(Material.OAK_FENCE);
            logger.warning("Flag block not defined properly. Defaulting to OAK_FENCE.");
        }

        Material beaconFrame = Material.matchMaterial(beaconWireframe);
        if (beaconFrame != null && beaconFrame.isBlock() && !beaconFrame.isAir() && !beaconFrame.hasGravity())
            FlagWarConfig.setBeaconWireFrameMaterial(beaconFrame);
        else {
            FlagWarConfig.setBeaconWireFrameMaterial(Material.GLOWSTONE);
            logger.warning("Beacon wireframe not defined properly. Defaulting to GLOWSTONE");
        }
    }

	static int getNumActiveFlags(String playerName) {
		List<CellUnderAttack> activeFlags = cellsUnderAttackByPlayer.get(playerName);
		return activeFlags == null ? 0 : activeFlags.size();
	}

	static List<CellUnderAttack> getCellsUnderAttack() {
		return new ArrayList<>(cellsUnderAttack.values());
	}

	static List<CellUnderAttack> getCellsUnderAttack(Town town) {
		List<CellUnderAttack> cells = new ArrayList<>();
		for(CellUnderAttack cua : cellsUnderAttack.values()) {
			try {
				Town townUnderAttack = TownyAPI.getInstance().getTownBlock(cua.getFlagBaseBlock().getLocation()).getTown();
				if (townUnderAttack == null)
					continue;
				if(townUnderAttack == town)
				    cells.add(cua);
			}
			catch(NotRegisteredException nre) {
			    nre.printStackTrace();
            }
		}
		return cells;
	}

	static boolean isUnderAttack(Town town) {
		for(CellUnderAttack cua : cellsUnderAttack.values()) {
			try {
				Town townUnderAttack = TownyAPI.getInstance().getTownBlock(cua.getFlagBaseBlock().getLocation()).getTown();
				if (townUnderAttack == null)
					continue;
				if(townUnderAttack == town)
					return true;
			}
			catch(NotRegisteredException nre) {
			    nre.printStackTrace();
            }
		}
		return false;
	}

	static boolean isUnderAttack(Cell cell) {
		return cellsUnderAttack.containsKey(cell);
	}

	static CellUnderAttack getAttackData(Cell cell) {
		return cellsUnderAttack.get(cell);
	}

	static void removeCellUnderAttack(CellUnderAttack cell) {
		removeFlagFromPlayerCount(cell.getNameOfFlagOwner(), cell);
		cellsUnderAttack.remove(cell);
	}

	static void attackWon(CellUnderAttack cell) {
		CellWonEvent cellWonEvent = new CellWonEvent(cell);
		pluginManager.callEvent(cellWonEvent);
		cell.cancel();
		removeCellUnderAttack(cell);
	}

	static void attackDefended(Player player, CellUnderAttack cell) {
		CellDefendedEvent cellDefendedEvent = new CellDefendedEvent(player, cell);
		pluginManager.callEvent(cellDefendedEvent);
		cell.cancel();
		removeCellUnderAttack(cell);
	}

	static void attackCanceled(CellUnderAttack cell) {
		CellAttackCanceledEvent cellAttackCanceledEvent = new CellAttackCanceledEvent(cell);
		pluginManager.callEvent(cellAttackCanceledEvent);
		cell.cancel();
		removeCellUnderAttack(cell);
	}

	public static void removeAttackerFlags(String playerName) {
		List<CellUnderAttack> cells = cellsUnderAttackByPlayer.get(playerName);
		if (cells != null)
			for (CellUnderAttack cell : cells)
				attackCanceled(cell);
	}

	static List<CellUnderAttack> getCellsUnderAttackByPlayer(String playerName) {
		List<CellUnderAttack> cells = cellsUnderAttackByPlayer.get(playerName);
		if (cells == null) {
            return new ArrayList<>(0);
        } else
            return new ArrayList<>(cells);
	}

	private static void addFlagToPlayerCount(String playerName, CellUnderAttack cell) {
		List<CellUnderAttack> activeFlags = getCellsUnderAttackByPlayer(playerName);
		activeFlags.add(cell);
		cellsUnderAttackByPlayer.put(playerName, activeFlags);
	}

	private static void removeFlagFromPlayerCount(String playerName, Cell cell) {
		List<CellUnderAttack> activeFlags = cellsUnderAttackByPlayer.get(playerName);
		if (activeFlags != null) {
			if (activeFlags.size() <= 1)
				cellsUnderAttackByPlayer.remove(playerName);
			else {
				activeFlags.remove(cell);
				cellsUnderAttackByPlayer.put(playerName, activeFlags);
			}
		}
	}

	public static void checkBlock(Player player, Block block, Cancellable event) {
		if (FlagWarConfig.isAffectedMaterial(block.getType()))
            checkedBlockAffected(player, block, event);
	}

    private static void checkedBlockAffected(Player player, Block block, Cancellable event) {
        Cell cell = Cell.parse(block.getLocation());
        if (cell.isUnderAttack())
            checkedBlockCellUnderAttack(player, block, event, cell);
    }

    private static void checkedBlockCellUnderAttack(Player player, Block block, Cancellable event, Cell cell) {
        CellUnderAttack cellAttackData = cell.getAttackData();
        if (cellAttackData.isFlag(block)) {
            FlagWar.attackDefended(player, cellAttackData);
            event.setCancelled(true);
        } else if (cellAttackData.isImmutableBlock(block))
            event.setCancelled(true);
    }

    public static boolean callAttackCellEvent(Towny plugin, Player player, Block block, WorldCoord worldCoord) throws TownyException {
        checkFlagHeight(block);

        TownyUniverse townyUniverse = TownyUniverse.getInstance();
		Resident attackingResident = townyUniverse.getResident(player.getUniqueId());
		Town landOwnerTown;
		Town attackingTown = null;

		Nation landOwnerNation;
		Nation attackingNation = null;
		TownBlock townBlock;

		if (attackingResident == null || !attackingResident.hasNation())
			throw new TownyException(Translation.of("msg_err_dont_belong_nation"));

        if (attackingResident.hasTown())
            attackingTown = attackingResident.getTown();

        if (attackingTown != null && attackingTown.hasNation())
            attackingNation = attackingResident.getTown().getNation();

        if (attackingTown == null || attackingNation == null)
            return false;

		if (attackingTown.getTownBlocks().isEmpty())
			throw new TownyException(Translation.of("msg_err_enemy_war_your_town_has_no_claims"));

		try {
			landOwnerTown = worldCoord.getTownBlock().getTown();
			townBlock = worldCoord.getTownBlock();
			landOwnerNation = landOwnerTown.getNation();
		} catch (NotRegisteredException e) {
			throw new TownyException(Translation.of("msg_err_enemy_war_not_part_of_nation"));
		}

		checkTargetPeaceful(player, townyUniverse, landOwnerNation, attackingNation);

        checkPlayerLimits(landOwnerTown, attackingTown, landOwnerNation, attackingNation);

        // Check that attack takes place on the edge of a town
		if (FlagWarConfig.isAttackingBordersOnly() && !AreaSelectionUtil.isOnEdgeOfOwnership(landOwnerTown, worldCoord))
			throw new TownyException(Translation.of("msg_err_enemy_war_not_on_edge_of_town"));

		double costToPlaceWarFlag = FlagWarConfig.getCostToPlaceWarFlag();
		if (TownyEconomyHandler.isActive()) {
            calculateFeesAndFines(attackingResident, townBlock, costToPlaceWarFlag);
        }

		if (kickstartCellUnderAttack(plugin, player, block)) {
            return false;
        }

        // Successful Attack

        if (TownyEconomyHandler.isActive() && costToPlaceWarFlag > 0)
            payForWarFlag(attackingResident, costToPlaceWarFlag);

        setAttackerAsEnemy(landOwnerNation, attackingNation);

        // Update Cache
        updateTownyCache(plugin, worldCoord, townyUniverse);

        TownyMessaging.sendGlobalMessage(Translation.of("msg_enemy_war_area_under_attack", landOwnerTown.getFormattedName(), worldCoord.toString(), attackingResident.getFormattedName()));
		return true;
	}

    private static void checkFlagHeight(Block block) throws TownyException {
        int topY = block.getWorld().getHighestBlockYAt(block.getX(), block.getZ()) - 1;
        if (block.getY() < topY)
            throw new TownyException(Translation.of("msg_err_enemy_war_must_be_placed_above_ground"));
    }

    private static void setAttackerAsEnemy(Nation defendingNation, Nation attackingNation)
        throws AlreadyRegisteredException {
        if (!defendingNation.hasEnemy(attackingNation)) {
            defendingNation.addEnemy(attackingNation);
            defendingNation.save();
        }
    }

    private static void checkPlayerLimits(Town defendingTown, Town attackingTown, Nation defendingNation,
        Nation attackingNation) throws TownyException {
        checkIfTownHasMinOnlineForWar(defendingTown);
        checkIfNationHasMinOnlineForWar(defendingNation);
        checkIfTownHasMinOnlineForWar(attackingTown);
        checkIfNationHasMinOnlineForWar(attackingNation);
    }

    private static void updateTownyCache(Towny plugin, WorldCoord worldCoord,
        TownyUniverse townyUniverse) {
        townyUniverse.addWarZone(worldCoord);
        plugin.updateCache(worldCoord);
    }

    private static void payForWarFlag(Resident attackRes, double cost)
        throws TownyException {
        try {
            attackRes.getAccount().withdraw(cost, "War - WarFlag Cost");
            TownyMessaging.sendResidentMessage(attackRes, Translation
                .of("msg_enemy_war_purchased_warflag",
                    TownyEconomyHandler.getFormattedBalance(cost)));
        } catch (EconomyException e) {
            e.printStackTrace();
        }
    }

    private static boolean kickstartCellUnderAttack(Towny towny, Player player, Block block)
        throws TownyException {
        CellAttackEvent cellAttackEvent = new CellAttackEvent(towny, player, block);
        plugin.getServer().getPluginManager().callEvent(cellAttackEvent);
        if (cellAttackEvent.isCancelled()) {
            if (cellAttackEvent.hasReason())
                throw new TownyException(cellAttackEvent.getReason());
            else
                return true;
        }
        return false;
    }

    private static void calculateFeesAndFines(Resident attackRes, TownBlock townBlock,
        double costToPlaceWarFlag) throws TownyException {
        try {
            double requiredAmount = costToPlaceWarFlag;
            double balance = attackRes.getAccount().getHoldingBalance();

            // Check that the user can pay for the war flag.
            if (balance < costToPlaceWarFlag)
                throw new TownyException(Translation.of("msg_err_insuficient_funds_warflag", TownyEconomyHandler.getFormattedBalance(costToPlaceWarFlag)));

            // Check that the user can pay the fines from losing/winning all future war flags.
            int activeFlagCount = getNumActiveFlags(attackRes.getName());
            double defendedAttackCost = FlagWarConfig.getDefendedAttackReward() * (activeFlagCount + 1);
            double attackWinCost;

            double amount;
            amount = FlagWarConfig.getWonHomeBlockReward();
            double homeBlockFine = amount < 0 ? -amount : 0;
            amount = FlagWarConfig.getWonTownBlockReward();
            double townBlockFine = amount < 0 ? -amount : 0;

            attackWinCost = homeOrTownBlock(townBlock, activeFlagCount, homeBlockFine, townBlockFine);

            if (defendedAttackCost > 0 && attackWinCost > 0) {
                String reason;
                double cost;
                if (defendedAttackCost > attackWinCost) {
                    // Worst case scenario that all attacks are defended.
                    requiredAmount += defendedAttackCost;
                    cost = defendedAttackCost;
                    reason = Translation.of("name_defended_attack");
                } else {
                    // Worst case scenario that all attacks go through, but is forced to pay a rebuilding fine.
                    requiredAmount += attackWinCost;
                    cost = attackWinCost;
                    reason = Translation.of("name_rebuilding");
                }

                // Check if player can pay in worst case scenario.
                if (balance < requiredAmount)
                    throw new TownyException(Translation.of("msg_err_insuficient_funds_future", TownyEconomyHandler.getFormattedBalance(cost), String.format("%d %s", activeFlagCount + 1, reason + "(s)")));
            }
        } catch (EconomyException e) {
            throw new TownyException(e.getError());
        }
    }

    private static double homeOrTownBlock(TownBlock townBlock, int activeFlags, double homeBlockFine, double townBlockFine) {
        double attackWinCost;
        if (townBlock.isHomeBlock())
            attackWinCost = homeBlockFine + activeFlags * townBlockFine;
        else
            attackWinCost = (activeFlags + 1) * townBlockFine;
        return attackWinCost;
    }

    private static void checkTargetPeaceful(Player player, TownyUniverse townyUniverse,
        Nation landOwnerNation, Nation attackingNation) throws TownyException {
        if (landOwnerNation.isNeutral())
            throw new TownyException(Translation.of("msg_err_enemy_war_is_peaceful", landOwnerNation
                .getFormattedName()));
        if (!townyUniverse.getPermissionSource().isTownyAdmin(player) && attackingNation.isNeutral())
            throw new TownyException(Translation.of("msg_err_enemy_war_is_peaceful", attackingNation
                .getFormattedName()));
    }

    public static void checkIfTownHasMinOnlineForWar(Town town) throws TownyException {
		int requiredOnline = FlagWarConfig.getMinPlayersOnlineInTownForWar();
		int onlinePlayerCount = TownyAPI.getInstance().getOnlinePlayers(town).size();
		if (onlinePlayerCount < requiredOnline)
			throw new TownyException(Translation.of("msg_err_enemy_war_require_online", requiredOnline, town.getFormattedName()));
	}

	public static void checkIfNationHasMinOnlineForWar(Nation nation) throws TownyException {
		int requiredOnline = FlagWarConfig.getMinPlayersOnlineInNationForWar();
		int onlinePlayerCount = TownyAPI.getInstance().getOnlinePlayers(nation).size();
		if (onlinePlayerCount < requiredOnline)
			throw new TownyException(Translation.of("msg_err_enemy_war_require_online", requiredOnline, nation.getFormattedName()));
	}

	public static WorldCoord cellToWorldCoord(Cell cell) {
		return new WorldCoord(cell.getWorldName(), cell.getX(), cell.getZ());
	}

	static long lastFlagged(Town town) {
		if (lastFlag.containsKey(town))
			return lastFlag.get(town);
		else
			return 0;
	}

	public static void townFlagged(Town town) {
		if (lastFlag.containsKey(town))
			lastFlag.replace(town, System.currentTimeMillis());
		else
			lastFlag.put(town, System.currentTimeMillis());
	}
}

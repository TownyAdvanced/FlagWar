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

package io.github.townyadvanced.flagwar.config;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.TimeTools;

import org.bukkit.Material;
import org.bukkit.Tag;

public class FlagWarConfig {

    private FlagWarConfig() {
        super();
    }

	protected static final Material[] woolColors = new Material[] {
			Material.LIME_WOOL, Material.GREEN_WOOL, Material.BLUE_WOOL, Material.CYAN_WOOL,
			Material.LIGHT_BLUE_WOOL, Material.GRAY_WOOL, Material.WHITE_WOOL,
			Material.PINK_WOOL, Material.ORANGE_WOOL, Material.RED_WOOL };

	private static Material flagBaseMaterial = null;
	private static Material flagLightMaterial = null;
	private static Material beaconWireFrameMaterial = null;

	public static boolean isAffectedMaterial(Material material) {
		return Tag.WOOL.isTagged(material)
            || material == getFlagBaseMaterial()
            || material == getFlagLightMaterial()
            || material == getBeaconWireFrameMaterial();
	}

	public static Material[] getWoolColors() {
		return woolColors;
	}

	public static boolean isAllowingAttacks() {
	    return FlagWar.getPlugin().getConfig().getBoolean("rules.allow_attacks");
	}

	public static long getFlagWaitingTime() {
		return TimeTools.convertToTicks(
		    TimeTools.getSeconds(FlagWar.getPlugin().getConfig().getString("flag.waiting_time")));
	}

	public static long getTimeBetweenFlagColorChange() {
		return getFlagWaitingTime() / getWoolColors().length;
	}

	public static boolean isDrawingBeacon() {
        return FlagWar.getPlugin().getConfig().getBoolean("beacon.draw");
	}

	public static int getMaxActiveFlagsPerPerson() {
	    return FlagWar.getPlugin().getConfig().getInt("player_limits.max_active_flags_per_player");
	}

	public static Material getFlagBaseMaterial() {
		return flagBaseMaterial;
	}

	public static Material getFlagLightMaterial() {
		return flagLightMaterial;
	}

	public static Material getBeaconWireFrameMaterial() {
		return beaconWireFrameMaterial;
	}

	public static int getBeaconRadius() {
	    return FlagWar.getPlugin().getConfig().getInt("beacon.radius");
	}

	public static int getBeaconSize() {
		return getBeaconRadius() * 2 - 1;
	}

	public static int getBeaconMinHeightAboveFlag() {
	    return FlagWar.getPlugin().getConfig().getInt("beacon.height_above_flag_min");
	}

	public static long getTimeToWaitAfterFlagged() {
	    return FlagWar.getPlugin().getConfig().getLong("rules.get_time_to_wait_after_flagged");
    }

    public static boolean isFlaggedInteractionTown() {
        return FlagWar.getPlugin().getConfig().getBoolean("rules.prevent_interaction_while_flagged");
    }

    public static boolean isFlaggedInteractionNation() {
        return FlagWar.getPlugin().getConfig().getBoolean("rules.prevent_interaction_while_flagged");
    }

	public static int getBeaconMaxHeightAboveFlag() {
        return FlagWar.getPlugin().getConfig().getInt("beacon.height_above_flag_max");
	}

	public static void setFlagBaseMaterial(Material flagBaseMaterial) {
		FlagWarConfig.flagBaseMaterial = flagBaseMaterial;
	}

	public static void setFlagLightMaterial(Material flagLightMaterial) {
		FlagWarConfig.flagLightMaterial = flagLightMaterial;
	}

	public static void setBeaconWireFrameMaterial(Material beaconWireFrameMaterial) {
		FlagWarConfig.beaconWireFrameMaterial = beaconWireFrameMaterial;
	}

	public static int getMinPlayersOnlineInTownForWar() {
	    return FlagWar.getPlugin().getConfig().getInt("player_limits.min_online_in_town");
	}

	public static int getMinPlayersOnlineInNationForWar() {
        return FlagWar.getPlugin().getConfig().getInt("player_limits.min_online_in_nation");
	}

	public static double getWonTownBlockReward() {
		return FlagWar.getPlugin().getConfig().getDouble("economy.town_block_captured");
	}

	public static double getWonHomeBlockReward() {
        return (FlagWar.getPlugin().getConfig().getDouble("economy.home_block_captured"));
	}

	public static double getCostToPlaceWarFlag() {

        return FlagWar.getPlugin().getConfig().getDouble("economy.war_flag_cost");
	}

	public static double getDefendedAttackReward() {
	    return FlagWar.getPlugin().getConfig().getDouble("economy.attack_defended_reward");
	}

    public static boolean isAttackingBordersOnly() {
        return FlagWar.getPlugin().getConfig().getBoolean("rules.only_attack_borders");
    }

	public static boolean isFlaggedTownBlockTransferred() {
        return FlagWar.getPlugin().getConfig().getBoolean("rules.flag_takes_ownership_of_town_blocks");
	}
}

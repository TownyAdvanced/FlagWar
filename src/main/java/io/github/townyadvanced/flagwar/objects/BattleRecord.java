package io.github.townyadvanced.flagwar.objects;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * A record of a battle that can be stored in persistent storage.
 * @param contestedTown the contested town's name
 * @param attacker the attacking nation's name
 * @param defender the defending nation's name
 * @param homeX the X coordinate of the homeblock
 * @param homeZ the Z coordinate of the homeblock
 * @param stageStartTime the time, in milliseconds, when the current stage started
 * @param isCityState whether the town this battle is hosted in is a CityState
 * @param stage the {@link BattleStage} that this battle is currently on
 * @param worldID the {@link UUID} of the world that this battle is hosted in
 * @param townBlocksCoords the {@link Collection} of the {@link WorldCoord} of all {@link TownBlock}s that the contested town accommodated before the battle began
 * @param initialMayorID the {@link UUID} of the resident who was mayor before the battle began
 * @param spawn the town spawn that existed before the battle began
 * @param outpostSpawns the town outpost spawns that existed before the battle began
 */
public record BattleRecord (
    String contestedTown,
    String attacker,
    String defender,
    int homeX,
    int homeZ,
    long stageStartTime,
    boolean isCityState,
    BattleStage stage,
    UUID worldID,
    Collection<WorldCoord> townBlocksCoords,
    UUID initialMayorID,
    Location spawn,
    List<Location> outpostSpawns
)
{
    public static BattleRecord of(Battle b) {
        try {
            return new BattleRecord(
                b.getContestedTown() == null ? "_" : b.getContestedTown().getName(),
                b.getAttacker() == null ? "_" : b.getAttacker().getName(),
                b.getDefender() == null ? "_" : b.getDefender().getName(),
                b.getHomeBlockCoords().getX(),
                b.getHomeBlockCoords().getZ(),
                b.getStageStartTime(),
                b.isCityState(),
                b.getCurrentStage(),
                b.getContestedTown().getWorld().getUID(),
                BattleUtil.toWorldCoords(b.getInitialTownBlocks()),
                b.getInitialMayor().getUUID(),
                b.getInitialSpawn(),
                b.getInitialOutpostSpawns()
            );
        } catch (Exception e)  {
            FlagWar.getInstance().getLogger().severe("Error while creating BattleRecord: " + e.getMessage()
            + ". Ending battle...");
            b.prematurelyEndBattle();
            return null;
        }
    }
}

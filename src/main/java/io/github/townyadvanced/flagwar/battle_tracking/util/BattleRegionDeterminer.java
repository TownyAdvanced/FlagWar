package io.github.townyadvanced.flagwar.battle_tracking.util;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;

/** See {@link #determineRegionFor(Town)}. */
public final class BattleRegionDeterminer {

    /**
     * A static method that constructs a new {@link BattleRegionDeterminer} and returns {@link #determineRegion()}.
     * @param town the town
     */
    public static Collection<BoundingBox> determineRegionFor(Town town) {
        Battle battle = BannerWarAPI.getBattle(town);
        Set<WorldCoord> claims = battle != null
            ? new HashSet<>(battle.getInitialTownBlocksAsWorldCoords())
            : town.getTownBlocks().stream().map(TownBlock::getWorldCoord).collect(Collectors.toSet());
        return new BattleRegionDeterminer(town, claims).determineRegion();
    }

    /**
     * Determines the regions for a battle from the claims captured when that battle began.
     * @param battle the battle whose initial claims will be grouped into regions
     * @return the battle regions as expanded bounding boxes
     */
    public static Collection<BoundingBox> determineRegionFor(Battle battle) {
        return new BattleRegionDeterminer(
            battle.getContestedTown(),
            new HashSet<>(battle.getInitialTownBlocksAsWorldCoords())
        ).determineRegion();
    }

    /** Holds the amount the war region will be expanded by to encompass fights straying away from a town. */
    private static final double EXPANSION = 64;

    /** Holds the maximum Y level of the world. */
    private final double MIN_Y;

    /** Holds the maximum Y level of the world. */
    private final double MAX_Y;

    /** Holds the set of world coordinates that count as part of the town. */
    private final Set<WorldCoord> VALID_COORDS;

    private BattleRegionDeterminer(Town town, Set<WorldCoord> validCoords) {
        MIN_Y = town.getWorld().getMinHeight();
        MAX_Y = town.getWorld().getMaxHeight();
        VALID_COORDS = validCoords;
    }

    /**
     * Starts the process of determining the battle region of a town in the form of a list of bounding boxes.
     * @return the battle region of the town
     */
    private Collection<BoundingBox> determineRegion() {
        Set<WorldCoord> remainingCoords = new HashSet<>(VALID_COORDS);
        List<BoundingBox> battleRegions = new ArrayList<>();

        while (!remainingCoords.isEmpty()) {
            WorldCoord start = remainingCoords.iterator().next();
            remainingCoords.remove(start);

            BoundingBox region = flood(start, remainingCoords);
            battleRegions.add(region.expand(EXPANSION, 0, EXPANSION));
        }

        return battleRegions;
     }

    /**
     * Builds a bounding box for one 4-connected group of claims. A claim is removed from
     * {@code remainingCoords} when it is discovered, ensuring it can only be added to one region.
     * @param start the first claim in the region
     * @param remainingCoords the claims not yet assigned to a region
     * @return the bounding box covering the connected group
     */
    private BoundingBox flood(WorldCoord start, Set<WorldCoord> remainingCoords) {
        Queue<WorldCoord> queue = new ArrayDeque<>();
        queue.add(start);
        BoundingBox box = mark(new BoundingBox(), start);

        while (!queue.isEmpty()) {
            WorldCoord current = queue.poll();

            // Check 4-connected neighbors: North, South, East, West
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : directions) {
                WorldCoord other = current.add(d[0], d[1]);

                if (remainingCoords.remove(other)) {
                    box = mark(box, other);
                    queue.add(other);
                }
            }
        }

        return box;
    }

    private BoundingBox mark(BoundingBox box, WorldCoord worldCoord) {
        double minX = worldCoord.getX() * 16d;
        double minZ = worldCoord.getZ() * 16d;

        box = box.union(new BoundingBox(minX, MIN_Y, minZ, minX + 15, MAX_Y,  minZ + 15));
        return box;
    }

}

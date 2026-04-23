package io.github.townyadvanced.flagwar.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.config.BannerWarConfig;
import io.github.townyadvanced.flagwar.objects.Battle;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

public final class BattleUtil {

    private BattleUtil() {}

    /** Holds the delimiter used for splitting {@link String}s. */
    private static final String DELIMITER = ":";

    /** Holds the delimiter used for splitting {@link String}s that hold chunk coordinates. */
    private static final String CHUNK_DELIMITER = "_";

    /**
     * Returns a {@link Collection} of {@link WorldCoord}s from a {@link String} representation.
     * @param worldID the {@link UUID} of the world where the town blocks reside
     * @param listToSplit the {@link String} that will be split into a list.
     */
    public static Collection<WorldCoord> toWorldCoords(String worldID, String listToSplit) {
        World world = Bukkit.getServer().getWorld(UUID.fromString(worldID));
        if (world == null) return new ArrayList<>();

        List<WorldCoord> out = new ArrayList<>();
        String[] chunks = listToSplit.split(DELIMITER);

        for (String chunk : chunks) {
            String[] block = chunk.split(CHUNK_DELIMITER);
            int x = Integer.parseInt(block[0]);
            int z = Integer.parseInt(block[1]);


            out.add(new WorldCoord(world, x, z));
        }
        return out;
    }

    /**
     * Returns a {@link String} representation of a {@link Collection} of {@link TownBlock}s
     * @param coords the {@link Collection} of {@link WorldCoord}s
     */
    public static String fromWorldCoords(Collection<WorldCoord> coords) {

        Collection<String> chunks = new ArrayList<>();
        for (WorldCoord coord : coords)
            chunks.add(coord.getX() + CHUNK_DELIMITER + coord.getZ());

        return String.join(DELIMITER, chunks);

    }

    /**
     * Computes the stage times of a {@link Battle} based on its initial {@link TownBlock} count and a configurable multiplier.
     * @param b the {@link Battle} in question
     */
    public static Map<BattleStage, Duration> computeStageTimes(Battle b) {
        EnumMap<BattleStage, Duration> stageTimes = new EnumMap<>(BattleStage.class);

        int size = b.getInitialTownBlocks().size();

        stageTimes.put(BattleStage.PRE_FLAG, !b.isCityState() ? Duration.ofMinutes(Math.round(
            30 * BannerWarConfig.getTimeMultiplier(BattleStage.PRE_FLAG)
        )) : Duration.ofMinutes(5));

        stageTimes.put(BattleStage.FLAG, Duration.ofMinutes(Math.round(
            Math.max(5 * Math.sqrt(size), 60) * BannerWarConfig.getTimeMultiplier(BattleStage.FLAG))));

        stageTimes.put(BattleStage.RUINED, Duration.ofMinutes(Math.round(
            60 * BannerWarConfig.getTimeMultiplier(BattleStage.RUINED))));

        stageTimes.put(BattleStage.DORMANT, Duration.ofDays((long) Math.ceil(
            3 * BannerWarConfig.getTimeMultiplier(BattleStage.DORMANT))));

        return stageTimes;
    }

    /**
     * Returns the {@link BattleStage#PRE_FLAG} and {@link BattleStage#FLAG} durations added.
     * @param b the {@link Battle}.
     */
    public static Duration getActivePeriod(Battle b) {
        return b.getDuration(BattleStage.PRE_FLAG).plus(b.getDuration(BattleStage.FLAG));
    }

    /**
     * Returns a {@link Collection} of {@link ChunkSnapshot}s of the provided {@link Chunk}s
     * @param chunks the {@link Collection} of {@link Chunk}s
     */
    public static Collection<ChunkSnapshot> toChunkSnapshot(Collection<Chunk> chunks) {
        Collection<ChunkSnapshot> out = new ArrayList<>();
        for (Chunk c : chunks) out.add(c.getChunkSnapshot());
        return out;
    }

    /**
     * Returns the number of towny days that have passed since the specified day.
     * <p>
     * Calls {@link BannerWarConfig#getCurrentTownyDay()} and returns the difference between that and the specified day.
     * @param day the specified day
     */
    public static long daysSince(long day) {
        return BannerWarConfig.getCurrentTownyDay() - day;
    }

    /**
     * Returns the {@link WorldCoord} of every {@link TownBlock} in the {@link Collection} provided.
     * @param townBlocks the {@link Collection} of {@link TownBlock}s provided.
     */
    public static Collection<WorldCoord> toWorldCoords(Collection<TownBlock> townBlocks) {
        Collection<WorldCoord> out = new ArrayList<>();
        for (TownBlock tb : townBlocks) out.add(tb.getWorldCoord());
        return out;
    }

    /**
     * Determines and returns a collection of chunks from a collection of townblocks, filtering outposts out.
     * filtering out the chunks that don't have a valid world reference.
     * @param townBlocks the collection of townblocks
     */
    public static Collection<Chunk> chunksFrom(Collection<TownBlock> townBlocks) {
        return townBlocks
            .stream()
            .filter(tb -> !tb.isOutpost())
            .map(townBlock -> {
                World world = townBlock.getWorldCoord().getBukkitWorld();
                if (world == null) return null;
                int x = townBlock.getX();
                int z = townBlock.getZ();

                return world.getChunkAt(x, z);
            })
            .filter(Objects::nonNull)
            .toList();

    }

    /**
     * Computes and returns a {@link BoundingBox} representing the volume covered by a collection of chunks.
     * @param chunks the collection of chunks
     */
    public static BoundingBox boundingBoxFrom(Collection<Chunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return new BoundingBox();

        final int minY = -64;
        final int maxY = 319;

        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Chunk chunk : chunks) {
            int cX = chunk.getX() * 16;
            int cZ = chunk.getZ() * 16;

            minX = Math.min(minX, cX);
            minZ = Math.min(minZ, cZ);
            maxX = Math.max(maxX, cX + 15);
            maxZ = Math.max(maxZ, cZ + 15);
        }

        return BoundingBox.of(
            new org.bukkit.util.Vector(minX, minY, minZ),
            new Vector( maxX + 1, maxY, maxZ + 1)
        );
    }
}

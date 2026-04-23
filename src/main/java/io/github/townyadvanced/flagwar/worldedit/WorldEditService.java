package io.github.townyadvanced.flagwar.worldedit;

import com.fastasyncworldedit.core.util.TaskManager;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.palmergames.bukkit.towny.object.Town;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.townyadvanced.flagwar.BannerWarAPI;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.BattleUtil;
import io.github.townyadvanced.flagwar.util.Broadcasts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides static methods that use
 * FastAsyncWorldEdit (FAWE)
 * for copying a structure from a bounding box, and pasting it using the same bounding box.
 * <p>
 * It also saves the structure as a .schem file onto the "structures" folder in the plugin directory to be pasted later.
 * <p>
 * Because of FAWE's API's similarity to WorldEdit's API, this service is also compatible between the two.
 * It is still strongly recommended to use the correct dependency,
 * and you should remove the async wrapper where needed.
 */
public final class WorldEditService {

    private WorldEditService() {} // only static methods present.

    /** Holds the {@link Plugin} instance. */
    private static final Plugin PLUGIN = FlagWar.getInstance();

    /** Holds the {@link Logger} instance. */
    private static final Logger LOGGER = PLUGIN.getLogger();

    /** Holds the file type of schem files. */
    private static final String SCHEM_EXTENSION = ".schem";

    /** Holds the directory path of the folder containing the structures to be pasted. */
    private static final String
            STRUCTURE_PATH = Path.of(FlagWar.getInstance().getDataFolder().getPath())
            .resolve("structures").toString();

    /**
     * Copies the blocks at the specified {@link BoundingBox} to a .schem file with the town UUID as the name.
     * @param town the town
     * @param box the bounding box
     */
    public static void copyToDisk(Town town, BoundingBox box) {
        LOGGER.info("Copying " + town.getName() + " to disk.");
        World world = town.getWorld();
        TaskManager.taskManager().async(() ->  {
            CuboidRegion region = getRegionFrom(box, world);
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()
            );

            File schemFile = new File(STRUCTURE_PATH, town.getUUID() + SCHEM_EXTENSION);
            schemFile.getParentFile().mkdirs();

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
                Operations.complete(forwardExtentCopy);
                writer.write(clipboard);

            } catch (IOException|WorldEditException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Pastes the blocks in the .schem file with the name corresponding to the town UUID back to the world
     * @param town the specified town
     * @return a {@link CompletableFuture} of type {@link Void} for chaining.
     */
    @CanIgnoreReturnValue
    public static CompletableFuture<Void> pasteToWorld(Town town) {
        LOGGER.info("Pasting " + town.getName() + " to world.");
        World world = town.getWorld();
        File schemFile = new File(STRUCTURE_PATH, town.getUUID() + SCHEM_EXTENSION);

        return CompletableFuture.runAsync(() -> {
            ClipboardFormat format = ClipboardFormats.findByFile(schemFile);

            if (format == null) {
                throw new NullPointerException("Could not find schematic for " + town.getName());
            }

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
                clipboard = reader.read();
            } catch (IOException e) {
                throw new NullPointerException("Failed to read schematic, " + e.getMessage());
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(clipboard.getMinimumPoint())
                        .maskSource(WorldEditHelper.createBlockBlacklistMask(clipboard))
                        .copyEntities(false)
                        .ignoreAirBlocks(true)
                        .build();

                Operations.complete(operation);

                var chunks = BattleUtil.chunksFrom(BannerWarAPI.getBattle(town).getInitialTownBlocks());
                BoundingBox box = BattleUtil.boundingBoxFrom(chunks);
                unSuffocateNearbyEntities(box, town.getWorld());
                deleteItems(box, town.getWorld());

            } catch (WorldEditException e) {
                e.printStackTrace();
            } finally {
                deleteFile(town.getUUID().toString());
            }
        }, runnable -> TaskManager.taskManager().async(runnable));
    }


    /**
     * Returns a {@link CuboidRegion} from a {@link BoundingBox} and {@link World}.
     * @param box the bounding box
     * @param world the world
     */
    private static CuboidRegion getRegionFrom(BoundingBox box, World world) {
        return new CuboidRegion(
                BukkitAdapter.adapt(world),
                BlockVector3.at(box.getMinX(), box.getMinY(), box.getMinZ()),
                BlockVector3.at(box.getMaxX(), box.getMaxY(), box.getMaxZ())
        );
    }

    private static void deleteFile(String fileName) {
        Path path = Path.of(STRUCTURE_PATH, fileName + SCHEM_EXTENSION);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,"Could not delete file {}.schem!", fileName);
        }
    }

    /**
     * Gets every {@link LivingEntity} within the bounding box, and teleports it upward
     * if it is suffocating.
     * @param box the bounding box
     * @param world the world where the entities are retrieved
     */
    private static void unSuffocateNearbyEntities(BoundingBox box, World world) {
        Bukkit.getScheduler().runTask(FlagWar.getInstance(), () -> {
            var livingEntities = world.getNearbyEntities(box)
                .stream().filter(LivingEntity.class::isInstance).toList();

            for (var entity : livingEntities) {
                if (WorldEditHelper.checkUnSuffocate(entity) && entity instanceof Player p) {
                    Broadcasts.sendMessage(p,
                        "Teleported you to the top during chunk restoration!", ChatColor.YELLOW);
                }
            }
        });
    }

    /**
     * Deletes all items in the bounding box provided.
     * @param box the bounding box
     * @param world the world where the items are deleted
     */
    private static void deleteItems(BoundingBox box, World world) {
        Bukkit.getScheduler().runTask(FlagWar.getInstance(), () ->
             world.getNearbyEntities(box).stream().filter(Item.class::isInstance).forEach(Entity::remove)
        );
    }
}

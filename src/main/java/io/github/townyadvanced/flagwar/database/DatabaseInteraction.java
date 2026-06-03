package io.github.townyadvanced.flagwar.database;

import com.palmergames.bukkit.towny.TownyAPI;
import io.github.townyadvanced.flagwar.objects.BannerPlacerRecord;
import io.github.townyadvanced.flagwar.objects.BattleRecord;
import io.github.townyadvanced.flagwar.objects.BattleStage;
import io.github.townyadvanced.flagwar.util.BattleUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class DatabaseInteraction {

    /** Holds the name of the battle table. */
    private static final String BATTLE_TABLE = "Battle";

    /** Holds the name of the banner placer table. */
    private static final String BANNER_PLACER_TABLE = "BannerPlacer";

    /** Holds the {@link DatabaseManager} instance. */
    private final DatabaseManager MANAGER;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER;

    public DatabaseInteraction(Logger logger, DatabaseManager manager) {
        this.MANAGER = manager;
        this.LOGGER = logger;
    }

    public CompletableFuture<Collection<BattleRecord>> getBattles() {

        return CompletableFuture.supplyAsync(() -> {
            Collection<BattleRecord> battles = new ArrayList<>();
            String query = "SELECT * FROM " + BATTLE_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        battles.add(new BattleRecord(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getInt(4),
                            rs.getInt(5),
                            rs.getLong(6),
                            rs.getBoolean(7),
                            BattleStage.valueOf(rs.getString(8)),
                            UUID.fromString(rs.getString(9)),
                            BattleUtil.toWorldCoords(rs.getString(9), rs.getString(10)),
                            UUID.fromString(rs.getString(11)),
                            BattleUtil.toLocation(rs.getString(12)),
                            BattleUtil.toLocations(rs.getString(13))
                        ));
                    }
                    return battles;
                }
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> insertBattle(BattleRecord r) {

        return CompletableFuture.runAsync(() -> {
            String query = "INSERT INTO " + BATTLE_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {

                ps.setString(1, r.contestedTown());
                ps.setString(2, r.attacker());
                ps.setString(3, r.defender());
                ps.setInt(4, r.homeX());
                ps.setInt(5, r.homeZ());
                ps.setLong(6, r.stageStartTime());
                ps.setBoolean(7, r.isCityState());
                ps.setString(8, r.stage().name());
                ps.setString(9, r.worldID().toString());
                ps.setString(10, BattleUtil.fromWorldCoords(r.townBlocksCoords()));
                ps.setString(11, r.initialMayorID().toString());
                ps.setString(12, BattleUtil.fromLocation(r.spawn()));
                ps.setString(13, BattleUtil.fromLocations(r.outpostSpawns()));

                if (ps.executeUpdate() > 0)
                    LOGGER.info("Successfully added battle " + r.contestedTown() + " to database!");
                else
                    LOGGER.warning("Failed to add battle " + r.contestedTown() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> insertOrUpdate(BattleRecord r) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT OR REPLACE INTO " + BATTLE_TABLE + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {

                ps.setString(1, r.contestedTown());
                ps.setString(2, r.attacker());
                ps.setString(3, r.defender());
                ps.setInt(4, r.homeX());
                ps.setInt(5, r.homeZ());
                ps.setLong(6, r.stageStartTime());
                ps.setBoolean(7, r.isCityState());
                ps.setString(8, r.stage().name());
                ps.setString(9, r.worldID().toString());
                ps.setString(10, BattleUtil.fromWorldCoords(r.townBlocksCoords()));
                ps.setString(11, r.initialMayorID().toString());
                ps.setString(12, BattleUtil.fromLocation(r.spawn()));
                ps.setString(13, BattleUtil.fromLocations(r.outpostSpawns()));

                if (ps.executeUpdate() <= 0)
                    LOGGER.warning("Failed to add battle " + r.contestedTown() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> deleteBattle(String contestedTown) {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM " + BATTLE_TABLE + " WHERE ContestedTown = ?";
            try(PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, contestedTown);
                ps.executeUpdate();
            }
            catch(SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> reset() {
        return CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM  " + BATTLE_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    public CompletableFuture<Collection<BannerPlacerRecord>> getBannerPlacers() {

        return CompletableFuture.supplyAsync(() -> {
            Collection<BannerPlacerRecord> placers = new ArrayList<>();
            String query = "SELECT * FROM " + BANNER_PLACER_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        placers.add(
                            new BannerPlacerRecord(
                                TownyAPI.getInstance().getTown(UUID.fromString(rs.getString(1))),
                                rs.getLong(2))
                        );
                    }
                    return placers;
                }
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> insertOrUpdate(BannerPlacerRecord r) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT OR REPLACE INTO " + BANNER_PLACER_TABLE + " VALUES(?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {

                ps.setString(1, r.town().getUUID().toString());
                ps.setLong(2, r.dayOfAttack());

                if (ps.executeUpdate() <= 0)
                    LOGGER.warning("Failed to add battle " + r.town().getName() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }
}

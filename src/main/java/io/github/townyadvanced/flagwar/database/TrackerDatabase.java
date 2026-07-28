package io.github.townyadvanced.flagwar.database;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.Affiliation;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.DamageOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.FlagOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.KillOccurrence;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.PlayerSnapshot;
import io.github.townyadvanced.flagwar.battle_tracking.model.results.BattleSnapshot;
import io.github.townyadvanced.flagwar.battle_tracking.util.SerializationUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class TrackerDatabase {

    /** Immutable local package ready for upload to the battle website. */
    public record BattleResultPackage(long battleId, String summaryJson, Path archivePath) {}
    /** Holds the name of the battle table. */
    private static final String TRACKED_BATTLE_TABLE = "TrackedBattle";

    /** Holds the name of the banner placer table. */
    private static final String TRACKED_PLAYER_TABLE = "TrackedPlayer";

    /** Holds the name of the append-only damage occurrence table. */
    private static final String TRACKED_DAMAGE_TABLE = "TrackedDamageOccurrence";

    /** Holds the name of the immutable completed-battle result table. */
    private static final String BATTLE_RESULT_TABLE = "BattleResult";

    /** Holds the {@link DatabaseManager} instance. */
    private final DatabaseManager MANAGER;

    /** Holds the {@link Logger} of this class. */
    private final Logger LOGGER = FlagWar.getInstance().getLogger();

    public TrackerDatabase(DatabaseManager manager) {
        this.MANAGER = manager;
    }

    public CompletableFuture<Collection<BattleSnapshot>> getTrackedBattles() {
        return CompletableFuture.supplyAsync(() -> {
            Collection<BattleSnapshot> battles = new ArrayList<>();
            Map<String, Collection<DamageOccurrence>> legacyDamageOccurrences = new HashMap<>();
            String query = "SELECT * FROM " + TRACKED_BATTLE_TABLE;
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long unixStart = rs.getLong(4);
                        String townName = rs.getString(1);
                        var tbr = new BattleSnapshot(
                            townName,
                            BattleStatus.ONGOING,
                            rs.getString(2),
                            rs.getString(3),
                            unixStart,
                            Duration.ofMillis(System.currentTimeMillis() - unixStart),
                            getTrackedPlayersSync(townName),
                            List.of()
                        );
                        battles.add(tbr);
                        legacyDamageOccurrences.put(townName, DamageOccurrence.deserialize(rs.getString(5)));
                    }
                }

                legacyDamageOccurrences.forEach((townName, occurrences) -> {
                    if (!occurrences.isEmpty() && !migrateLegacyDamageOccurrences(townName, occurrences))
                        LOGGER.warning("Failed to migrate legacy damage logs for battle " + townName + ".");
                });
                return battles;
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public CompletableFuture<Void> insertOrUpdateBattle(BattleSnapshot r) {
        return CompletableFuture.runAsync(() -> {
            String query = "INSERT OR REPLACE INTO " + TRACKED_BATTLE_TABLE +
                " VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, r.townName());
                ps.setString(2, r.attackerNationName());
                ps.setString(3, r.defenderNationName());
                ps.setLong(4, r.unixStartTime());
                ps.setString(5, "");

                if (ps.executeUpdate() > 0)
                    LOGGER.info("Successfully added battle " + r.townName() + " to database!");
                else
                    LOGGER.warning("Failed to add battle " + r.townName() + " to database!");

            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }

        });
    }

    public CompletableFuture<Void> deleteBattle(String contestedTown) {
        return CompletableFuture.runAsync(() -> {
            deleteTrackedPlayers(contestedTown);
            deleteDamageOccurrences(contestedTown);
            String query = "DELETE FROM " + TRACKED_BATTLE_TABLE + " WHERE ContestedTown = ?";
            try(PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
                ps.setString(1, contestedTown);
                ps.executeUpdate();
            }
            catch(SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        });
    }

    /**
     * Appends new damage occurrences without rewriting any existing battle history.
     * @return {@code true} when every occurrence was committed
     */
    public boolean insertDamageOccurrencesSync(Collection<DamageOccurrence> occurrences, String battleTown) {
        if (occurrences.isEmpty()) return true;

        Connection conn = MANAGER.getConnection();
        try {
            conn.setAutoCommit(false);
            insertDamageOccurrences(conn, battleTown, occurrences);
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                LOGGER.warning("Failed to roll back damage occurrence insert: " + rollbackException.getMessage());
            }
            LOGGER.warning("Failed to persist damage occurrences: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.warning("Failed to restore auto-commit after damage occurrence insert: " + e.getMessage());
            }
        }
    }

    /**
     * Packages a completed battle into a durable result row and a compressed, line-delimited damage archive.
     * Active tracker rows are removed only after the archive and result row are complete.
     * @return {@code true} when the result package was completed
     */
    public BattleResultPackage finalizeBattleSync(BattleSnapshot result) {
        long battleId;
        try {
            battleId = createPackagingResult(result);
            Path archivePath = writeDamageArchive(battleId, result.townName());
            markResultComplete(battleId, archivePath);
            deleteTrackedPlayers(result.townName());
            deleteDamageOccurrences(result.townName());
            deleteTrackedBattle(result.townName());
            return new BattleResultPackage(battleId, serializeResultSummary(result), archivePath);
        } catch (SQLException | IOException e) {
            LOGGER.warning("Failed to package battle " + result.townName() + ": " + e.getMessage());
            return null;
        }
    }

    /** Rebuilds and completes any result packages interrupted by a server shutdown. */
    public CompletableFuture<Void> recoverPendingBattlePackages() {
        return CompletableFuture.runAsync(() -> {
            String query = "SELECT BattleID, ContestedTown FROM " + BATTLE_RESULT_TABLE + " WHERE PackageStatus = 'PACKAGING'";
            Map<Long, String> pendingPackages = new LinkedHashMap<>();
            try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pendingPackages.put(rs.getLong(1), rs.getString(2));
                }
            } catch (SQLException e) {
                LOGGER.warning("Failed to find pending battle result packages: " + e.getMessage());
                return;
            }

            pendingPackages.forEach((battleId, townName) -> {
                try {
                    Path archivePath = writeDamageArchive(battleId, townName);
                    markResultComplete(battleId, archivePath);
                    deleteTrackedPlayers(townName);
                    deleteDamageOccurrences(townName);
                    deleteTrackedBattle(townName);
                    LOGGER.info("Recovered battle result package " + battleId + ".");
                } catch (SQLException | IOException e) {
                    LOGGER.warning("Failed to recover battle result package " + battleId + ": " + e.getMessage());
                }
            });
        });
    }

    public void insertOrUpdatePlayersSync(Collection<PlayerSnapshot> players, String battleTown) throws SQLException {

        Connection conn = MANAGER.getConnection();
        conn.setAutoCommit(false);

        String query = "INSERT OR REPLACE INTO " + TRACKED_PLAYER_TABLE + " VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (var trackedPlayer : players) {
                ps.setString(1, trackedPlayer.playerName());
                ps.setString(2, battleTown);
                ps.setString(3, trackedPlayer.affiliation().name());
                ps.setDouble(4, trackedPlayer.damageDealt());
                ps.setDouble(5, trackedPlayer.damageTaken());
                ps.setString(6, KillOccurrence.serialize(trackedPlayer.kills()));
                ps.setString(7, KillOccurrence.serialize(trackedPlayer.deaths()));
                ps.setInt(8, trackedPlayer.gapsUsed());
                ps.setInt(9, trackedPlayer.potsUsed());
                ps.setString(10, FlagOccurrence.serialize(trackedPlayer.flags()));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {conn.rollback();
            LOGGER.warning("Failed to add players to database! " + e.getMessage());}
        finally {
            conn.setAutoCommit(true);
        }
    }

    private Map<String, PlayerSnapshot> getTrackedPlayersSync(String battleTown) {
        Map<String, PlayerSnapshot> results = new HashMap<>();
        String query = "SELECT * FROM " + TRACKED_PLAYER_TABLE + " WHERE BattleTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    results.put(name, new PlayerSnapshot(
                            name,
                            Affiliation.valueOf(rs.getString(3)),
                            KillOccurrence.deserialize(rs.getString(6)),
                            KillOccurrence.deserialize(rs.getString(7)),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getInt(9),
                            rs.getInt(8),
                            FlagOccurrence.deserialize(rs.getString(10))
                        )
                    );
                }
                return results;
            }
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            return new HashMap<>();
        }
    }

    private void deleteTrackedPlayers(String battleTown) {
        String query = "DELETE FROM " + TRACKED_PLAYER_TABLE + " WHERE BattleTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /** Inserts the packaging row and returns its database-generated battle ID. */
    private long createPackagingResult(BattleSnapshot result) throws SQLException {
        String query = "INSERT INTO " + BATTLE_RESULT_TABLE +
            " (ContestedTown, Attacker, Defender, Status, StartTime, EndTime, SummaryJson, PackageStatus) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, result.townName());
            ps.setString(2, result.attackerNationName());
            ps.setString(3, result.defenderNationName());
            ps.setString(4, result.status().name());
            ps.setLong(5, result.unixStartTime());
            ps.setLong(6, System.currentTimeMillis());
            ps.setString(7, serializeResultSummary(result));
            ps.setString(8, "PACKAGING");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Battle result insert did not return a generated battle ID.");
    }

    /** Streams the persisted raw damage events into a gzip-compressed NDJSON artifact. */
    private Path writeDamageArchive(long battleId, String battleTown) throws SQLException, IOException {
        Path resultsDirectory = FlagWar.getInstance().getDataFolder().toPath().resolve("battle-results");
        Files.createDirectories(resultsDirectory);

        Path archive = resultsDirectory.resolve(battleId + ".damage.ndjson.gz");
        Path temporaryArchive = resultsDirectory.resolve(battleId + ".damage.ndjson.gz.tmp");
        String query = "SELECT Hurter, Hurted, Damage, TimeStamp FROM " + TRACKED_DAMAGE_TABLE +
            " WHERE BattleTown = ? ORDER BY TimeStamp, ID";

        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query);
             GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(temporaryArchive));
             BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(gzip, StandardCharsets.UTF_8))) {
            ps.setString(1, battleTown);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DamageOccurrence occurrence = new DamageOccurrence(
                        rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getLong(4));
                    writer.write(occurrence.toJSON());
                    writer.newLine();
                }
            }
        }

        Files.move(temporaryArchive, archive, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return archive;
    }

    /** Stores the final archive location and makes the result visible to downstream consumers. */
    private void markResultComplete(long battleId, Path archivePath) throws SQLException {
        String query = "UPDATE " + BATTLE_RESULT_TABLE + " SET DamageArchivePath = ?, PackageStatus = 'COMPLETE' WHERE BattleID = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, archivePath.toString());
            ps.setLong(2, battleId);
            ps.executeUpdate();
        }
    }

    /** Serializes only web-facing result data, avoiding implementation-only snapshot fields. */
    private String serializeResultSummary(BattleSnapshot result) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("formatVersion", 1);
        summary.put("town", result.townName());
        summary.put("attacker", result.attackerNationName());
        summary.put("defender", result.defenderNationName());
        summary.put("outcome", result.status().properName());
        summary.put("status", result.status().name());
        summary.put("startedAt", result.unixStartTime());
        summary.put("endedAt", System.currentTimeMillis());
        summary.put("players", result.playerResultMap().values());
        return SerializationUtil.toJson(summary);
    }

    /** Deletes the active tracked-battle metadata after final packaging succeeds. */
    private void deleteTrackedBattle(String battleTown) throws SQLException {
        String query = "DELETE FROM " + TRACKED_BATTLE_TABLE + " WHERE ContestedTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            ps.executeUpdate();
        }
    }

    /** Migrates the old serialized damage-log column into the append-only damage table. */
    private boolean migrateLegacyDamageOccurrences(String battleTown, Collection<DamageOccurrence> occurrences) {
        Connection conn = MANAGER.getConnection();
        try {
            conn.setAutoCommit(false);
            insertDamageOccurrences(conn, battleTown, occurrences);
            clearLegacyDamageLogs(conn, battleTown);
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                LOGGER.warning("Failed to roll back legacy damage-log migration: " + rollbackException.getMessage());
            }
            LOGGER.warning("Failed to migrate legacy damage logs: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.warning("Failed to restore auto-commit after legacy damage-log migration: " + e.getMessage());
            }
        }
    }

    /** Inserts the provided occurrences using the caller's transaction. */
    private void insertDamageOccurrences(Connection conn, String battleTown, Collection<DamageOccurrence> occurrences) throws SQLException {
        String query = "INSERT INTO " + TRACKED_DAMAGE_TABLE + " (BattleTown, Hurter, Hurted, Damage, TimeStamp) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (DamageOccurrence occurrence : occurrences) {
                ps.setString(1, battleTown);
                ps.setString(2, occurrence.hurter());
                ps.setString(3, occurrence.hurted());
                ps.setDouble(4, occurrence.damage());
                ps.setLong(5, occurrence.timeStamp());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Clears the legacy serialized damage-log column using the caller's transaction. */
    private void clearLegacyDamageLogs(Connection conn, String battleTown) throws SQLException {
        String query = "UPDATE " + TRACKED_BATTLE_TABLE + " SET DamageLogs = '' WHERE ContestedTown = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, battleTown);
            ps.executeUpdate();
        }
    }

    /** Deletes all persisted damage occurrences belonging to a tracked battle. */
    private void deleteDamageOccurrences(String battleTown) {
        String query = "DELETE FROM " + TRACKED_DAMAGE_TABLE + " WHERE BattleTown = ?";
        try (PreparedStatement ps = MANAGER.getConnection().prepareStatement(query)) {
            ps.setString(1, battleTown);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}

package io.github.townyadvanced.flagwar.battle_tracking.model.results;

import io.github.townyadvanced.flagwar.battle_tracking.TrackedBattle;
import io.github.townyadvanced.flagwar.battle_tracking.model.enums.BattleStatus;
import io.github.townyadvanced.flagwar.battle_tracking.model.occurrences.DamageOccurrence;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A record that logs a serialized snapshot of a battle.
 * @param townName the name of the town that the battle is fought in
 * @param status the status of this battle
 * @param attackerNationName the name of the attacking nation
 * @param defenderNationName the name of the defending nation
 * @param unixStartTime the Unix epoch timestamp when the battle started
 * @param battleDuration the amount of time that has elapsed in this battle upon taking this snapshot
 * @param playerResultMap a {@link Map} of all {@link PlayerSnapshot}s and their names
 * @param damageOccurrences legacy occurrences awaiting migration to the append-only damage table
 */
public record BattleSnapshot(
    String townName,
    BattleStatus status,
    String attackerNationName,
    String defenderNationName,
    long unixStartTime,
    Duration battleDuration,
    Map<String, PlayerSnapshot> playerResultMap,
    Collection<DamageOccurrence> damageOccurrences
) {

    /**
     * Returns a {@link BattleSnapshot} of a {@link TrackedBattle} to be stored on a database or similar.
     * @param battle the tracked battle
     * @param status the status ({@link BattleStatus#ONGOING} if the battle isn't over)
     */
    public static BattleSnapshot parse(TrackedBattle battle, BattleStatus status) {
        Map<String, PlayerSnapshot> playerResultMap = new HashMap<>();

        for (var player : battle.getTrackedPlayers()) {
            String name = player.getOfflinePlayer().getName();
            PlayerSnapshot playerSnapshot = PlayerSnapshot.parse(player);
            playerResultMap.put(name, playerSnapshot);
        }

        return new BattleSnapshot(
            battle.getTown().getName(),
            status,
            battle.getAttacker().getName(),
            battle.getDefender().getName(),
            battle.getStartTime(),
            Duration.ofMillis(System.currentTimeMillis() - battle.getStartTime()),
            playerResultMap,
            List.of()
        );
    }

}

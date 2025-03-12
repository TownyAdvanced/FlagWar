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

package io.github.townyadvanced.flagwar.listeners;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.economy.NationPreTransactionEvent;
import com.palmergames.bukkit.towny.event.economy.TownPreTransactionEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreTownLeaveEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownPreSetHomeBlockEvent;
import com.palmergames.bukkit.towny.event.town.TownPreUnclaimCmdEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.economy.transaction.TransactionType;
import com.palmergames.bukkit.towny.object.WorldCoord;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.FlagWarAPI;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.events.CellAttackCanceledEvent;
import io.github.townyadvanced.flagwar.events.CellAttackEvent;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import io.github.townyadvanced.flagwar.events.CellDefendedEvent;
import io.github.townyadvanced.flagwar.events.CellWonEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * General-purpose event listener for tracking Towny and FlagWar events.
 */
public class FlagWarCustomListener implements Listener {
    /** Holds localized string for key: error.player-town-under-attack. */
    public static final String DENY_FLAG_TOWN_UNDER_ATTACK
        = Translate.fromPrefixed("error.player-town-under-attack");
    /** Holds localized string for key: error.player-was-recently-attacked. */
    public static final String DENY_FLAG_RECENTLY_ATTACKED
        = Translate.fromPrefixed("error.player-was-recently-attacked");

    /** Holds instance of {@link Towny}; defined by {@link #FlagWarCustomListener(FlagWar)}. */
    private Towny towny;
    /** Holds the instance of the TownyUniverse; set in {@link #FlagWarCustomListener(FlagWar)}. */
    private TownyUniverse universe;
    /** Holds the Bukkit logger; set in {@link #FlagWarCustomListener(FlagWar)}. */
    private final Logger logger;

    /**
     * Constructor: Sets the Towny instance, and passes on the logger from Bukkit.
     * @param flagWar The FlagWar instance.
     */
    public FlagWarCustomListener(final FlagWar flagWar) {
        if (flagWar.getServer().getPluginManager().getPlugin("Towny") != null) {
            towny = Towny.getPlugin();
            universe = TownyUniverse.getInstance();
        }
        logger = flagWar.getLogger();

    }

    /**
     * If the {@link CellAttackEvent} fires, and has not been canceled, this method tries running
     * {@link FlagWar#registerAttack(CellUnderAttack)} using the cell from the CellAttackEvent.
     * @param cellAttackEvent the associated CellAttackEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onCellAttackEvent(final CellAttackEvent cellAttackEvent) {
        if (cellAttackEvent.isCancelled()) {
            return;
        }

        try {
            FlagWar.registerAttack(cellAttackEvent.getData());
        } catch (Exception e) {
            cellAttackEvent.setCancelled(true);
            cellAttackEvent.setReason(e.getMessage());
        }
    }

    /**
     * If the {@link CellDefendedEvent} has not been canceled, update the town's lastFlagged timestamp, remove the
     * associated warzone, broadcast the area was defended, then calculate any defender rewards.
     *
     * @param cellDefendedEvent the CellDefendedEvent.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings({"unused", "deprecation"})
    public void onCellDefendedEvent(final CellDefendedEvent cellDefendedEvent) {
        if (cellDefendedEvent.isCancelled()) {
            return;
        }
        var player = cellDefendedEvent.getPlayer();
        CellUnderAttack cell = cellDefendedEvent.getCell().getAttackData();
        String broadcast =
            Translate.fromPrefixed("broadcast.area.defended", getPlayerOrGF(player), cell.getCellString());

        tryTownFlagged(cell);
        updateTownyCache(cell);
        towny.getServer().broadcastMessage(broadcast);

        calculateDefenderReward(player, cell);
    }

    /**
     * When a {@link CellUnderAttack} has been won, and the {@link CellWonEvent} has not been canceled, remove the
     * related WarZone, update the defending town's lastFlagged timestamp, handle payments, transfer ownership of the
     * plot (or not), update Towny cache, and finally send messages for who won and messages regarding payouts.
     *
     * @param cellWonEvent The event declaring the cell attack successfully completed, and triggers the processing.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCellWonEvent(final CellWonEvent cellWonEvent) {
        if (cellWonEvent.isCancelled()) {
            return;
        }

        var cell = cellWonEvent.getCellUnderAttack();

        try {
            var attackingResident = universe.getResident(cell.getNameOfFlagOwner());

            // Shouldn't happen
            if (attackingResident == null) {
                return;
            }

            var attackingTown = attackingResident.getTown();
            var attackingNation = attackingTown.getNation();

            var worldCoord = FlagWar.cellToWorldCoordinate(cell);
            updateTownyCache(cell);

            var townBlock = worldCoord.getTownBlock();
            var defendingTown = townBlock.getTown();

            FlagWar.townFlagged(defendingTown);

            // Payments
            double amount = 0;
            String moneyTransferMessage = null;
            if (TownyEconomyHandler.isActive()) {
                String townBlockType = townOrHomeBlock(townBlock);
                amount = realEstateValue(townBlockType);

                if (amount > 0) {
                    // Defending Town -> Attacker (Pillage)
                    var reason = String.format("War - Won Enemy %s (Pillage)", townBlockType);
                    amount = townPayAttackerSpoils(attackingResident, defendingTown, amount, reason);
                    moneyTransferMessage = Translate.fromPrefixed("broadcast.area.pillaged",
                        attackingResident.getFormattedName(),
                        TownyEconomyHandler.getFormattedBalance(amount),
                        defendingTown.getFormattedName()
                    );
                } else if (amount < 0) {
                    // Attacker -> Defending Town (Rebuild cost)
                    amount = -amount; // Inverse the amount so it's positive.
                    var reason = String.format("War - Won Enemy %s (Rebuild Cost)", townBlockType);
                    attackerPayTownRebuild(cell, attackingResident, attackingNation, defendingTown, amount, reason);
                    moneyTransferMessage = Translate.fromPrefixed("broadcast.area.rebuilding",
                        attackingResident.getFormattedName(),
                        TownyEconomyHandler.getFormattedBalance(amount),
                        defendingTown.getFormattedName()
                    );
                }
            }

            // Defender loses townblock
            transferOrUnclaimOrKeepTownblock(attackingTown, townBlock, defendingTown);

            // Cleanup
            towny.updateCache(worldCoord);

            // Event Message
            messageWon(cell, attackingResident, attackingNation);

            // Money Transfer message.
            if (TownyEconomyHandler.isActive() && amount != 0 && moneyTransferMessage != null) {
                messageResident(attackingResident, moneyTransferMessage);
                TownyMessaging.sendPrefixedTownMessage(defendingTown, moneyTransferMessage);
            }
        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }

    /**
     * If a {@link CellAttackCanceledEvent} is fired, and is not itself canceled, set the associated cell's lastFlagged
     * timestamp, remove any associated WarZone, then log the cell info ({@link CellUnderAttack#getCellString()}).
     *
     * @param cellAttackCanceledEvent the CellAttackCanceledEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onCellAttackCanceledEvent(final CellAttackCanceledEvent cellAttackCanceledEvent) {
        if (cellAttackCanceledEvent.isCancelled()) {
            return;
        }
        CellUnderAttack cell = cellAttackCanceledEvent.getCell();
        tryTownFlagged(cell);
        updateTownyCache(cell);
        logger.info(cell.getCellString());
    }

    /**
     * When a {@link Town} atempts to leave a {@link Nation}, check that there are no active or recent attacks. If there
     * are, cancel the {@link NationPreTownLeaveEvent} with the appropriate reason.
     *
     * @param nationPreTownLeaveEvent the NationPreTownLeaveEvent.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onTownLeaveNation(final NationPreTownLeaveEvent nationPreTownLeaveEvent) {
        if (FlagWarConfig.isAllowingAttacks()) {
            if (FlagWarAPI.isUnderAttack(nationPreTownLeaveEvent.getTown())
                && FlagWarConfig.isFlaggedInteractionTown()) {

                nationPreTownLeaveEvent.setCancelMessage(DENY_FLAG_TOWN_UNDER_ATTACK);
                nationPreTownLeaveEvent.setCancelled(true);
            }

            if (isAfterFlaggedCooldownActive(nationPreTownLeaveEvent.getTown())) {
                nationPreTownLeaveEvent.setCancelMessage(DENY_FLAG_RECENTLY_ATTACKED);
                nationPreTownLeaveEvent.setCancelled(true);
            }
        }
    }

    /**
     * Listens for attempts to interact with a {@link Nation}'s
     * {@link com.palmergames.bukkit.towny.object.EconomyAccount}.
     * <p>
     * If enabled, and an attempt to withdraw from the Nation's account while a {@link Town} in the Nation is under
     * attack occurs, check if the Town itself is under attack or if it is still in a post-flag cooldown period, and
     * cancel the transaction if either is true.
     * <p>
     * Intended to prevent players from taking the Nation's money and running whilst in the middle of a war.
     *
     * @param nationPreTransactionEvent Event fired by {@link Towny} prior to a Nation's EconomyAccount transaction
     *                                  being processed.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onNationWithdraw(final NationPreTransactionEvent nationPreTransactionEvent) {
        if (FlagWarConfig.isAllowingAttacks()
            && FlagWarConfig.isFlaggedInteractionNation()
            && nationPreTransactionEvent.getTransaction().getType().equals(TransactionType.WITHDRAW)) {

            for (Town town : nationPreTransactionEvent.getNation().getTowns()) {
                if (FlagWarAPI.isUnderAttack(town) || isAfterFlaggedCooldownActive(town)) {
                    nationPreTransactionEvent.setCancelMessage(Translate.fromPrefixed("error.nation-under-attack"));
                    nationPreTransactionEvent.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Similar to {@link #onNationWithdraw}, prevents a {@link Town}'s
     * {@link com.palmergames.bukkit.towny.object.EconomyAccount} from being looted by it's own players if the post-flag
     * cooldown is still active.
     *
     * @param townPreTransactionEvent Event fired by {@link Towny} prior to a Town's EconomyAccount transaction being
     *                                processed.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onTownWithdraw(final TownPreTransactionEvent townPreTransactionEvent) {
        if (FlagWarConfig.isAllowingAttacks() && isAfterFlaggedCooldownActive(townPreTransactionEvent.getTown())) {
            townPreTransactionEvent.setCancelMessage(DENY_FLAG_RECENTLY_ATTACKED);
            townPreTransactionEvent.setCancelled(true);
        }
    }

    /**
     * Listens for when a {@link Town}'s HomeBlock would be (re)set.
     * <p>
     * If a Town is under attack and Flagged Interaction is prevented, or, if the Town is still in a post-flagged
     * cooldown phase; cancel the event.
     *
     * @param townPreSetHomeBlockEvent Event fired by {@link Towny} prior to setting a Town's HomeBlock.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onTownSetHomeBlock(final TownPreSetHomeBlockEvent townPreSetHomeBlockEvent) {
        if (FlagWarConfig.isAllowingAttacks() && FlagWarConfig.isFlaggedInteractionTown()) {
            if (FlagWarAPI.isUnderAttack(townPreSetHomeBlockEvent.getTown())) {
                cancelTownPreSetHomeBlockEvent(townPreSetHomeBlockEvent, DENY_FLAG_TOWN_UNDER_ATTACK);
            } else if (isAfterFlaggedCooldownActive(townPreSetHomeBlockEvent.getTown())) {
                cancelTownPreSetHomeBlockEvent(townPreSetHomeBlockEvent, DENY_FLAG_RECENTLY_ATTACKED);
            }
        }
    }

    /**
     * Listens for if a {@link Nation} attempts to declare neutrality.
     * <p>
     * If a wartime nation cannot be neutral (according to {@link Towny}), cancel the neutrality declaration.
     * <p>
     * If the wartime states can be neutral and the future state of the NationToggleNeutralEvent would be true, then for
     * each {@link Resident} in the given Nation, cycle through and remove any active war flags from play.
     * <p>
     * Does not stop already neutral nations from going hostile.
     *
     * @param nationToggleNeutralEvent Event fired by Towny when a Nation attempts to toggle neutrality.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onNationToggleNeutral(final NationToggleNeutralEvent nationToggleNeutralEvent) {
        if (FlagWarConfig.isAllowingAttacks()) {
            if (!FlagWarConfig.isDeclaringNeutralAllowed() && nationToggleNeutralEvent.getFutureState()) {
                nationToggleNeutralEvent.setCancelled(true);
                nationToggleNeutralEvent.setCancelMessage(Translate.fromPrefixed("error.cannot-toggle-peaceful"));
            } else if (nationToggleNeutralEvent.getFutureState() && !FlagWarAPI.getCellsUnderAttack().isEmpty()) {
                for (Resident resident : nationToggleNeutralEvent.getNation().getResidents()) {
                    FlagWar.removeAttackerFlags(resident.getName());
                }
            }
        }
    }

    /**
     * Listen for if a {@link Resident} attempts to leave a {@link Town}.
     * <p>
     * If the Town is under attack and Flagged Interaction is prohibited, or if it was recently attacked and on
     * cooldown, prevent it's players from leaving the Town.
     *
     * @param townLeaveEvent Event fired by {@link Towny} when a Town attempts to leave a Nation.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onTownLeave(final TownLeaveEvent townLeaveEvent) {
        if (FlagWarConfig.isAllowingAttacks() && FlagWarConfig.isFlaggedInteractionTown()) {
            if (FlagWarAPI.isUnderAttack(townLeaveEvent.getTown())) {
                townLeaveEvent.setCancelled(true);
                townLeaveEvent.setCancelMessage(DENY_FLAG_TOWN_UNDER_ATTACK);
            } else if (isAfterFlaggedCooldownActive(townLeaveEvent.getTown())) {
                townLeaveEvent.setCancelled(true);
                townLeaveEvent.setCancelMessage(DENY_FLAG_RECENTLY_ATTACKED);
            }
        }
    }

    /**
     * Listens for if a Town attempts to unclaim land via commands.
     * <p>
     * If under attack and interaction prohibited, or if in an active cooldown phase, cancel and prevent the area from
     * being unclaimed.
     *
     * @param townPreUnclaimCmdEvent Event fired by {@link Towny} when the /unclaim command is ran, prior to any major
     *                               processing.
     */
    @EventHandler (priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    private void onWarPreUnclaimed(final TownPreUnclaimCmdEvent townPreUnclaimCmdEvent) {
        if (FlagWarConfig.isFlaggedInteractionTown()) {
            if (FlagWarAPI.isUnderAttack(townPreUnclaimCmdEvent.getTown())) {
                townPreUnclaimCmdEvent.setCancelMessage(DENY_FLAG_TOWN_UNDER_ATTACK);
                townPreUnclaimCmdEvent.setCancelled(true);
            } else if (isAfterFlaggedCooldownActive(townPreUnclaimCmdEvent.getTown())) {
                townPreUnclaimCmdEvent.setCancelMessage(DENY_FLAG_RECENTLY_ATTACKED);
                townPreUnclaimCmdEvent.setCancelled(true);
            }
        }
    }

    /**
     * Try getting the {@link Town} from the {@link CellUnderAttack}, and set the clock for when it was last flagged.
     *
     * @param cellUnderAttack the {@link CellUnderAttack} to extract the {@link Town} from.
     */
    private void tryTownFlagged(final CellUnderAttack cellUnderAttack) {
        try {
            FlagWar.townFlagged(FlagWar.cellToWorldCoordinate(cellUnderAttack).getTownBlock().getTown());
        } catch (NotRegisteredException e) {
            logger.warning(e.getMessage());
        }
    }

    private void attackerPayTownRebuild(final CellUnderAttack cell,
                                        final Resident atkRes,
                                        final Nation atkNat,
                                        final Town defTown,
                                        final double amount,
                                        final String reason) {
            if (!atkRes.getAccount().payTo(amount, defTown, reason)) {
                messageWon(cell, atkRes, atkNat);
            }
    }

    private double townPayAttackerSpoils(final Resident attackingResident,
                                         final Town defendingTown,
                                         final double amount,
                                         final String reason) {
            double total = Math.min(amount, defendingTown.getAccount().getHoldingBalance());
            defendingTown.getAccount().payTo(amount, attackingResident, reason);
            return total;
    }

    private void transferOrUnclaimOrKeepTownblock(final Town atkTown, final TownBlock townBlock, final Town defTown) {
        if (FlagWarConfig.isFlaggedTownBlockUnclaimed()) {
            unclaimTownBlock(townBlock);
        } else if (FlagWarConfig.isFlaggedTownBlockTransferred()) {
            transferOwnership(atkTown, townBlock);
        } else {
            String message = Translate.fromPrefixed("area.won.defender-keeps-claims");
            TownyMessaging.sendPrefixedTownMessage(atkTown, message);
            TownyMessaging.sendPrefixedTownMessage(defTown, message);
        }
    }

    private void messageWon(final CellUnderAttack cell, final Resident atkRes, final Nation atkNat) {
        String resName = atkRes.getFormattedName();
        String natName = atkNat.getFormattedName();
        String msg;
        if (atkNat.hasTag()) {
            msg = Translate.fromPrefixed("broadcast.area.won", resName, atkNat.getTag(), cell.getCellString());
        } else {
            msg = Translate.fromPrefixed("broadcast.area.won", resName, natName, cell.getCellString());
        }
        TownyMessaging.sendGlobalMessage(msg);
    }

    private double realEstateValue(final String reasonType) {
        double amount;
        if (reasonType.equals("Homeblock")) {
            amount = FlagWarConfig.getWonHomeBlockReward();
        } else {
            amount = FlagWarConfig.getWonTownBlockReward();
        }
        return amount;
    }

    @NotNull
    private String townOrHomeBlock(final TownBlock townBlock) {
        if (townBlock.isHomeBlock()) {
            return "Homeblock";
        } else {
            return "Townblock";
        }
    }

    private void unclaimTownBlock(final TownBlock townBlock) {
        TownyUniverse.getInstance().getDataSource().removeTownBlock(townBlock);
    }

    private void transferOwnership(final Town attackingTown, final TownBlock townBlock) {
        try {
            townBlock.setTown(attackingTown);
            townBlock.save();
        } catch (Exception te) {
            // Couldn't claim it.
            TownyMessaging.sendErrorMsg(te.getMessage());
            te.printStackTrace();
        }
    }

    private void cancelTownPreSetHomeBlockEvent(final TownPreSetHomeBlockEvent townPreSetHomeBlockEvent,
                                                final String cancelMessage) {
        townPreSetHomeBlockEvent.setCancelMessage(cancelMessage);
        townPreSetHomeBlockEvent.setCancelled(true);
    }

    /**
     * Determines if the given {@link Town} is still in the cooldown period defined by
     * {@link FlagWarConfig#getFlaggedInteractCooldown()}.
     *
     * @param town The Town to inspect for cooldown.
     * @return TRUE if the cooldown is still active.
     */
    private boolean isAfterFlaggedCooldownActive(final Town town) {
        Instant lastFlagged = FlagWarAPI.getFlaggedInstant(town);
        if (lastFlagged == Instant.MAX) {
            return false;
        }
        Duration timeToWait = FlagWarConfig.getFlaggedInteractCooldown();
        return Instant.now().isBefore(lastFlagged.plus(timeToWait));
    }

    /**
     * Updates the Towny cache for a given {@link CellUnderAttack}, which is no longer
     * under attack.
     *
     * @param cell the given CellUnderAttack related to the WarZone.
     */
    private void updateTownyCache(final CellUnderAttack cell) {
        towny.updateCache(new WorldCoord(cell.getWorldName(), cell.getX(), cell.getZ()));
    }

    /**
     * If {@link TownyEconomyHandler#isActive()}, attempt to reward the defender and run
     * {@link #notifyDefAndPayOrRefund(Resident, Resident, String)}.
     * Does not take into account if the attacker an pay, or even if they can cover the whole reward.
     *
     * @param dP the Defending {@link Player}.
     * @param cell the {@link CellUnderAttack} that was defended.
     */
    private void calculateDefenderReward(final Player dP, final CellUnderAttack cell) {
        if (TownyEconomyHandler.isActive()) {
                var attackingPlayer = universe.getResident(cell.getNameOfFlagOwner());
                Resident defendingPlayer = null;

                if (dP != null) {
                    defendingPlayer = universe.getResident(dP.getUniqueId());
                }

                String styledMoney = TownyEconomyHandler.getFormattedBalance(FlagWarConfig.getDefendedAttackReward());
                notifyDefAndPayOrRefund(attackingPlayer, defendingPlayer, styledMoney);
        }
    }

    /**
     * Send messages to the attacking and defending {@link Resident}s that the attack was defended and attempts to pay
     * the defender or refund the attacker if defender is null.
     * @param atkRes the attacking Resident.
     * @param defRes the defending Resident.
     * @param styledMoney the formatted string for the money balance.
     */
    private void notifyDefAndPayOrRefund(final Resident atkRes, final Resident defRes, final String styledMoney) {
        if (defRes == null
            && atkRes.getAccount().deposit(
            FlagWarConfig.getDefendedAttackReward(), "FlagWar Attack Defended (GF)")) {

            messageResident(atkRes, Translate.fromPrefixed("area.defended.attacker.greater-forces", styledMoney));
        } else if (atkRes.getAccount().payTo(
            FlagWarConfig.getDefendedAttackReward(), defRes, "FlagWar Attack Defended")
            && defRes != null) {

            msgAttackDefended(atkRes, defRes, styledMoney);
        }
    }

    /**
     * Takes a {@link Player} object, and gets the associated name. If the player is a valid {@link Resident}, get the
     * formatted name from it's Resident. If Null, "Greater&nbsp;Forces" is used instead.
     *
     * @param player the Player object to parse
     * @return a name for the associated Player, which may or not be formatted from an associated Resident, or "Greater
     * &nbsp;Forces" if the player was null.
     */
    private String getPlayerOrGF(final Player player) {
        String playerName;
        if (player == null) {
            playerName = "Greater Forces";
        } else {
            playerName = player.getName();
            var playerRes = universe.getResident(player.getUniqueId());
            if (playerRes != null) {
                playerName = playerRes.getFormattedName();
            }
        }
        return playerName;
    }

    private void messageResident(final Resident resident, final String message) {
        if (resident.isOnline()) {
            resident.getPlayer().sendMessage(message);
        }
    }

    private void msgAttackDefended(final Resident atkRes, final Resident defRes, final String formattedMoney) {
        String message;
        message = Translate.fromPrefixed("area.defended.attacker", defRes.getFormattedName(), formattedMoney);
        if (atkRes.isOnline()) {
            atkRes.getPlayer().sendMessage(message);
        }
        message = Translate.fromPrefixed("area.defended.defender", atkRes.getFormattedName(), formattedMoney);
        if (defRes.isOnline()) {
            defRes.getPlayer().sendMessage(message);
        }
    }
}

package io.github.townyadvanced.flagwar.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.i18n.Translate;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.jetbrains.annotations.NotNull;

public class TownyAdminReloadAddon extends BaseCommand implements TabExecutor {

    /**
     * Method which will register the flagwar subcommand in Towny's /ta reload.
     */
    public TownyAdminReloadAddon() {
        AddonCommand townyAdminReloadCommand = new AddonCommand(CommandType.TOWNYADMIN_RELOAD, "flagwar", this);
        TownyCommandAddonAPI.addSubCommand(townyAdminReloadCommand);
    }

    /**
     * onCommand class required by TabExecutor.
     */
    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command,
                             @NotNull final String label, final String[] args) {
        parseAdminReloadCommand(args, sender);
        return true;
    }

    @SuppressWarnings("unused")
    private void parseAdminReloadCommand(final String[] args, final CommandSender sender) {
        if (sender instanceof Player player) {
            if (!TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(player)) {
                Messaging.send(player, Translate.fromPrefixed("error.command.disabled"));
                return;
            }
        }

        // Load config.yml
        if (!FlagWar.getFlagWar().loadConfig()) {
            Messaging.send(sender, Translate.fromPrefixed("error.invalid.config"));
            return;
        }

        Messaging.send(sender, Translate.fromPrefixed("message.flag.war.config.has.been.reloaded"));
    }

}

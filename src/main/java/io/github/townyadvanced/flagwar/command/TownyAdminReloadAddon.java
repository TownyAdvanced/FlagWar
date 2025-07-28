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

public class TownyAdminReloadAddon extends BaseCommand implements TabExecutor {

    public TownyAdminReloadAddon() {
        AddonCommand townyAdminReloadCommand = new AddonCommand(CommandType.TOWNYADMIN_RELOAD, "flagwar", this);
        TownyCommandAddonAPI.addSubCommand(townyAdminReloadCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        parseAdminReloadCommand(args, sender);
        return true;
    }

    private void parseAdminReloadCommand(String[] args, CommandSender sender) {
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

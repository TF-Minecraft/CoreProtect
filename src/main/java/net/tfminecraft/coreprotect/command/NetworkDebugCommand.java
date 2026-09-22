package net.tfminecraft.coreprotect.command;

import org.bukkit.command.CommandSender;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.language.Phrase;
import net.tfminecraft.coreprotect.listener.channel.PluginChannelListener;
import net.tfminecraft.coreprotect.utility.Chat;
import net.tfminecraft.coreprotect.utility.Color;
import net.tfminecraft.coreprotect.utility.ErrorReporter;

public class NetworkDebugCommand {
    protected static void runCommand(CommandSender player, boolean permission, String[] args) {
        if (!permission || !Config.getGlobal().NETWORK_DEBUG) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            return;
        }

        try {
            PluginChannelListener.getInstance().sendTest(player, args.length == 2 ? args[1] : "");
        }
        catch (Exception e) {
            ErrorReporter.report(e);
        }
    }
}

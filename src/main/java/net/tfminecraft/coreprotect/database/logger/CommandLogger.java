package net.tfminecraft.coreprotect.database.logger;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.tfminecraft.coreprotect.CoreProtect;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.Database;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.CommandStatement;
import net.tfminecraft.coreprotect.database.statement.UserStatement;
import net.tfminecraft.coreprotect.event.CoreProtectPreLogEvent;
import net.tfminecraft.coreprotect.utility.WorldUtils;

public class CommandLogger {

    private CommandLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(ConsumerWriteBatch preparedStmt, int batchCount, long time, Location location, String user, String message) {
        try {
            if (ConfigHandler.isBlacklisted(user)) {
                return;
            }
            if (ConfigHandler.isBlacklisted(((message + " ").split(" "))[0])) {
                return;
            }

            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(user, location, CoreProtectPreLogEvent.Action.PLAYER_COMMAND, -1, null, null, message);
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return;
            }

            int userId = UserStatement.getId(preparedStmt, event.getUser(), true);
            Location eventLocation = event.getLocation();
            int wid = WorldUtils.getWorldId(eventLocation.getWorld().getName());
            int x = eventLocation.getBlockX();
            int y = eventLocation.getBlockY();
            int z = eventLocation.getBlockZ();
            CommandStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, message);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

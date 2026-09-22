package net.tfminecraft.coreprotect.database.logger;

import java.util.Locale;

import org.bukkit.Location;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;
import net.tfminecraft.coreprotect.database.statement.ChatStatement;
import net.tfminecraft.coreprotect.utility.WorldUtils;

public class ChatLogger {

    private ChatLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(ConsumerWriteBatch preparedStmt, int batchCount, long time, Location location, String user, String message) {
        try {
            if (ConfigHandler.isBlacklisted(user)) {
                return;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            int wid = WorldUtils.getWorldId(location.getWorld().getName());
            int userId = ConfigHandler.playerIdCache.get(user.toLowerCase(Locale.ROOT));
            ChatStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, message);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

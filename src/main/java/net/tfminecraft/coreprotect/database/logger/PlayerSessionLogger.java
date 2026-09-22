package net.tfminecraft.coreprotect.database.logger;

import java.util.Locale;

import org.bukkit.Location;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.Database;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.SessionStatement;
import net.tfminecraft.coreprotect.utility.WorldUtils;

public class PlayerSessionLogger {

    private PlayerSessionLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(ConsumerWriteBatch preparedStmt, int batchCount, String user, Location location, int time, int action) {
        try {
            if (ConfigHandler.isBlacklisted(user)) {
                return;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            int wid = WorldUtils.getWorldId(location.getWorld().getName());
            int userId = ConfigHandler.playerIdCache.get(user.toLowerCase(Locale.ROOT));
            SessionStatement.insert(preparedStmt, batchCount, time, userId, wid, x, y, z, action);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

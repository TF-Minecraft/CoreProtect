package net.tfminecraft.coreprotect.database.logger;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;

public class UsernameLogger {

    private UsernameLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(ConsumerWriteBatch batch, String user, String uuid, int configUsernames, int time) {
        try {
            if (ConfigHandler.isBlacklisted(user)) {
                return;
            }
            batch.recordUsername(user, uuid, configUsernames, time);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

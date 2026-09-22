package net.tfminecraft.coreprotect.consumer.process;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;

import org.bukkit.Location;

import net.tfminecraft.coreprotect.database.logger.PlayerSessionLogger;
import net.tfminecraft.coreprotect.model.action.SessionActions;

class PlayerLogoutProcess {

    static void process(ConsumerWriteBatch preparedStmt, int batchCount, Object object, int time, String user) {
        if (object instanceof Location) {
            Location location = (Location) object;
            PlayerSessionLogger.log(preparedStmt, batchCount, user, location, time, SessionActions.LOGOUT);
        }
    }
}

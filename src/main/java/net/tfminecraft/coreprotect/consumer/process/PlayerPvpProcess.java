package net.tfminecraft.coreprotect.consumer.process;

import org.bukkit.Location;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.logger.PlayerPvpLogger;
import net.tfminecraft.coreprotect.model.action.PvpEvent;

class PlayerPvpProcess {

    private PlayerPvpProcess() {
        throw new IllegalStateException("Process class");
    }

    static void process(ConsumerWriteBatch preparedStmt, int batchCount, Object object, String user) {
        if (object instanceof Object[]) {
            Object[] values = (Object[]) object;
            if (values.length > 3 && values[0] instanceof Location && values[1] instanceof String && values[2] instanceof Integer && values[3] instanceof PvpEvent) {
                PlayerPvpLogger.log(preparedStmt, batchCount, user, (Location) values[0], (String) values[1], (Integer) values[2], (PvpEvent) values[3]);
            }
        }
    }
}

package net.tfminecraft.coreprotect.consumer.process;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import java.util.Map;

import org.bukkit.Location;

import net.tfminecraft.coreprotect.consumer.Consumer;
import net.tfminecraft.coreprotect.database.logger.CommandLogger;

class PlayerCommandProcess {

    // These auxiliary queues still hold distinct legacy payloads consumed by the matching processors.
    @SuppressWarnings("deprecation")
    static void process(ConsumerWriteBatch preparedStmt, int batchCount, int processId, int id, Object object, String user) {
        if (!(object instanceof Object[])) {
            return;
        }

        Object[] data = (Object[]) object;
        if (data[1] instanceof Location) {
            Map<Integer, String> strings = Consumer.consumerStrings.get(processId);
            if (strings.get(id) != null) {
                String message = strings.get(id);
                Long timestamp = (Long) data[0];
                Location location = (Location) data[1];
                CommandLogger.log(preparedStmt, batchCount, timestamp, location, user, message);
            }
        }
    }
}

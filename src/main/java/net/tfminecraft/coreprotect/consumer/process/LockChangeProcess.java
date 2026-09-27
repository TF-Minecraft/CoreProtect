package net.tfminecraft.coreprotect.consumer.process;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.logger.PlayerInteractLogger;
import net.tfminecraft.coreprotect.model.action.LockChange;

class LockChangeProcess {

    private LockChangeProcess() {
        throw new IllegalStateException("Process class");
    }

    static void process(ConsumerWriteBatch preparedStmt, int batchCount, String user, Object object, Material type) {
        if (object instanceof Object[]) {
            Object[] values = (Object[]) object;
            if (values.length > 1 && values[0] instanceof BlockState && values[1] instanceof LockChange) {
                PlayerInteractLogger.log(preparedStmt, batchCount, user, (BlockState) values[0], type, (LockChange) values[1]);
            }
        }
    }
}

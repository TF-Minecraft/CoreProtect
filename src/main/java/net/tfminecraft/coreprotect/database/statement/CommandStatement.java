package net.tfminecraft.coreprotect.database.statement;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;

public class CommandStatement {

    private CommandStatement() {
        throw new IllegalStateException("Database class");
    }

    public static void insert(ConsumerWriteBatch batch, int batchCount, long time, int user, int wid, int x, int y, int z, String message) {
        try {
            batch.addCommand(batchCount, time, user, wid, x, y, z, message);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }
}

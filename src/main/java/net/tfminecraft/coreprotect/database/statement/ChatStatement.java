package net.tfminecraft.coreprotect.database.statement;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;

public class ChatStatement {

    private ChatStatement() {
        throw new IllegalStateException("Database class");
    }

    public static void insert(ConsumerWriteBatch batch, int batchCount, long time, int user, int wid, int x, int y, int z, String message) {
        try {
            batch.addChat(batchCount, time, user, wid, x, y, z, message);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }
}

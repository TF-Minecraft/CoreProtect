package net.tfminecraft.coreprotect.database.statement;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;

public class WorldStatement {

    private WorldStatement() {
        throw new IllegalStateException("Database class");
    }

    public static void insert(ConsumerWriteBatch batch, int batchCount, int id, String world) {
        try {
            batch.addReference(ConsumerWriteBatch.ReferenceKind.WORLD, batchCount, id, world);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }
}

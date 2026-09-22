package net.tfminecraft.coreprotect.database.logger;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.database.Database;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.model.item.ItemTransactionActions;
import net.tfminecraft.coreprotect.utility.HopperTransactionUtils;
import net.tfminecraft.coreprotect.utility.ItemUtils;

public class ContainerBreakLogger {

    private ContainerBreakLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(ConsumerWriteBatch preparedStmt, int batchCount, String player, Location l, Material type, ItemStack[] oldInventory) {
        try {
            ItemUtils.mergeItems(type, oldInventory);
            ContainerLogger.logTransaction(preparedStmt, batchCount, player, type, null, oldInventory, ItemTransactionActions.REMOVE, l);
            String loggingContainerId = HopperTransactionUtils.getLoggingId(player, l);

            // If there was a pending chest transaction, it would have already been processed.
            Queue.removeForceContainer(loggingContainerId);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

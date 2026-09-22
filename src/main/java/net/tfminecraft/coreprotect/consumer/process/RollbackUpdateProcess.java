package net.tfminecraft.coreprotect.consumer.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.tfminecraft.coreprotect.consumer.Consumer;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.model.rollback.RollbackUpdateTargets;
import net.tfminecraft.coreprotect.utility.MaterialUtils;

class RollbackUpdateProcess {

    static void process(ConsumerWriteBatch batch, int processId, int id, int action, int table) throws Exception {
        process(batch, processId, id, action, table, RollbackUpdateTargets.usesInventoryRollbackState(table));
    }

    // These auxiliary queues still hold distinct legacy payloads consumed by the matching processors.
    @SuppressWarnings("deprecation")
    static void process(ConsumerWriteBatch batch, int processId, int id, int action, int table, boolean inventoryRollback) throws Exception {
        Map<Integer, List<Object[]>> updateLists = Consumer.consumerObjectArrayList.get(processId);
        if (updateLists.get(id) != null) {
            List<Object[]> list = updateLists.get(id);
            process(batch, list, action, table, inventoryRollback);
        }
    }

    static void process(ConsumerWriteBatch batch, List<Object[]> list, int action, int table, boolean inventoryRollback) throws Exception {
        Map<Integer, List<Object[]>> rowsByValue = groupRowsByValue(list, action, inventoryRollback);
        for (Entry<Integer, List<Object[]>> entry : rowsByValue.entrySet()) {
            batch.updateRolledBackRows(table, entry.getKey(), entry.getValue());
        }
    }

    static void processChecked(ConsumerWriteBatch batch, List<Object[]> list, int action, int table, boolean inventoryRollback) throws Exception {
        process(batch, list, action, table, inventoryRollback);
    }

    private static Map<Integer, List<Object[]>> groupRowsByValue(List<Object[]> list, int action, boolean inventoryRollback) {
        Map<Integer, List<Object[]>> rowsByValue = new HashMap<>();
        for (Object[] listRow : list) {
            int rolledBack = (Integer) listRow[9];
            if (MaterialUtils.rolledBack(rolledBack, inventoryRollback) == action) { // 1 = restore, 0 = rollback
                int toggledValue = MaterialUtils.toggleRolledBack(rolledBack, inventoryRollback);
                rowsByValue.computeIfAbsent(toggledValue, key -> new ArrayList<>()).add(listRow);
            }
        }
        return rowsByValue;
    }
}

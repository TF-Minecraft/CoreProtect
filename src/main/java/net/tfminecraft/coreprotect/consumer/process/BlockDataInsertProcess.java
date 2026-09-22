package net.tfminecraft.coreprotect.consumer.process;

import java.sql.Statement;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.MaterialStatement;
import net.tfminecraft.coreprotect.language.Phrase;
import net.tfminecraft.coreprotect.language.Selector;
import net.tfminecraft.coreprotect.utility.Chat;

class BlockDataInsertProcess {

    static void process(ConsumerWriteBatch preparedStmt, Statement statement, int batchCount, Object name, int materialId) {
        if (name instanceof String) {
            String query = "SELECT id FROM " + ConfigHandler.prefix + "blockdata_map WHERE id = " + materialId + " LIMIT 1 OFFSET 0";
            boolean hasMaterial = !ConfigHandler.databaseType.isClickHouse() && MaterialStatement.hasMaterial(statement, query);
            if (!hasMaterial) {
                MaterialStatement.insert(preparedStmt, ConsumerWriteBatch.ReferenceKind.BLOCK_DATA, batchCount, materialId, (String) name);

                // validate ID maps to ensure mapping wasn't reloaded from database prior to this insertion completing
                ConfigHandler.blockdata.put((String) name, materialId);
                ConfigHandler.blockdataReversed.put(materialId, (String) name);
                if (materialId > ConfigHandler.blockdataId) {
                    ConfigHandler.blockdataId = materialId;
                }
            }
            else {
                Chat.console(Phrase.build(Phrase.CACHE_ERROR, "blockdata"));
                Chat.console(Phrase.build(Phrase.CACHE_RELOAD, Selector.FIRST));
                ConfigHandler.loadTypes(statement);
            }
        }
    }
}

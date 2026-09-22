package net.tfminecraft.coreprotect.consumer.process;

import java.sql.Statement;
import java.util.List;

import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.statement.EntityStatement;
import net.tfminecraft.coreprotect.utility.entity.EntityUtil;

class EntitySpawnProcess {

    static void process(Statement statement, Object object, int rowId) {
        if (object instanceof Object[]) {
            BlockState block = (BlockState) ((Object[]) object)[0];
            EntityType type = (EntityType) ((Object[]) object)[1];
            String query = "SELECT data FROM " + ConfigHandler.prefix + "entity WHERE rowid=" + rowId + " LIMIT 1 OFFSET 0";
            List<Object> data = EntityStatement.getData(statement, block, query);
            EntityUtil.spawnEntity(block, type, data);
        }
    }
}

package net.tfminecraft.coreprotect.consumer.process;

import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;

import net.tfminecraft.coreprotect.database.logger.EntitySpawnLogger;
import net.tfminecraft.coreprotect.model.entity.EntitySpawnData;
import net.tfminecraft.coreprotect.model.entity.EntitySpawnIdentity;

final class EntitySpawnLogProcess {

    private EntitySpawnLogProcess() {
    }

    static EntitySpawnIdentity process(ConsumerWriteBatch batch, Object object, String user) throws Exception {
        if (object instanceof EntitySpawnData) {
            EntitySpawnData data = (EntitySpawnData) object;
            return EntitySpawnLogger.logIdentity(batch, user, data);
        }
        return null;
    }
}

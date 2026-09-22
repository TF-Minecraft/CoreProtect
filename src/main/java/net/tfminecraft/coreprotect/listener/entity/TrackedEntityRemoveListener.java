package net.tfminecraft.coreprotect.listener.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;

import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.listener.player.EntityInteractionListener;
import net.tfminecraft.coreprotect.listener.player.InventoryChangeListener;
import net.tfminecraft.coreprotect.utility.EntitySpawnTracking;

public final class TrackedEntityRemoveListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveEvent event) {
        Entity entity = event.getEntity();
        switch (event.getCause()) {
            case UNLOAD:
            case DEATH:
            case HIT:
                return;
            case PLUGIN:
                if (EntitySpawnTracking.isCoreProtectRemoval(entity.getUniqueId())) {
                    return;
                }
                break;
            default:
                break;
        }

        if (EntitySpawnTracking.isTrackedOrPendingIdentity(entity)) {
            InventoryChangeListener.flushEntityContainer(entity);
            EntityInteractionListener.flushPendingInteractions(entity);
            Queue.queueEntitySpawnRemoved(entity);
            EntitySpawnTracking.clearTracking(entity.getUniqueId());
        }
    }
}

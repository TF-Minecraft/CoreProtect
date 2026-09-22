package net.tfminecraft.coreprotect.listener.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.consumer.Queue;

public final class ChunkPopulateListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onChunkPopulate(ChunkPopulateEvent event) {
        long chunkKey = event.getChunk().getX() & 0xffffffffL | (event.getChunk().getZ() & 0xffffffffL) << 32;
        ConfigHandler.populatedChunks.put(chunkKey, (System.currentTimeMillis() / 1000L));
    }

}

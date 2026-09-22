package net.tfminecraft.coreprotect.listener.player;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.consumer.Queue;

public final class PlayerBucketEmptyListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    protected void onPlayerBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent event) {
        String player = event.getPlayer().getName();
        World world = event.getPlayer().getWorld();
        int inspect = 0;

        if (ConfigHandler.inspecting.get(player) != null) {
            if (ConfigHandler.inspecting.get(player)) {
                inspect = 1;
                event.setCancelled(true);
            }
        }

        if (!event.isCancelled() && Config.getConfig(world).BUCKETS && inspect == 0) {
            Block block = event.getBlock();
            Material type = Material.WATER;
            if (event.getBucket().equals(Material.LAVA_BUCKET)) {
                type = Material.LAVA;
            }

            BlockState blockState = block.getState();
            queueBlockPlaceValidate(player, blockState, block, blockState, type, 1, 1, null, 0, true);
        }
    }
}

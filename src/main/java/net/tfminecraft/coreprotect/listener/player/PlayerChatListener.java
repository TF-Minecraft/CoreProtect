package net.tfminecraft.coreprotect.listener.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.consumer.Queue;

public final class PlayerChatListener extends Queue implements Listener {

    // Bukkit chat fallback remains supported; the Paper chat listener handles Adventure events.
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (message == null) {
            return;
        }

        Player player = event.getPlayer();
        if (event.isCancelled() && !Config.getConfig(player.getWorld()).LOG_CANCELLED_CHAT) {
            return;
        }

        if (!message.startsWith("/") && Config.getConfig(player.getWorld()).PLAYER_MESSAGES) {
            long timestamp = System.currentTimeMillis() / 1000L;
            Queue.queuePlayerChat(player, message, timestamp);
        }
    }
}

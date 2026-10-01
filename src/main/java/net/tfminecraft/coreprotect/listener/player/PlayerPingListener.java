package net.tfminecraft.coreprotect.listener.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.tfminecraft.coreprotect.CoreProtect;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.thread.Scheduler;

/**
 * Logs each online player's position as a session row every "player-pings" seconds.
 * A player's first ping is one interval after they join, since the login row already holds their position.
 */
public final class PlayerPingListener extends Queue implements Listener {

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final Map<UUID, Integer> NEXT_PING = new ConcurrentHashMap<>();

    public static void initialize(CoreProtect plugin) {
        NEXT_PING.clear();
        Scheduler.scheduleSyncRepeatingTask(plugin, () -> checkPlayers(plugin), null, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    private static void checkPlayers(CoreProtect plugin) {
        int time = (int) (System.currentTimeMillis() / 1000L);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (ConfigHandler.isFolia) {
                // A player's location may only be read on the thread that owns them
                Scheduler.runTask(plugin, () -> checkPlayer(player, time), player);
            }
            else {
                checkPlayer(player, time);
            }
        }
    }

    private static void checkPlayer(Player player, int time) {
        if (!player.isOnline()) {
            return;
        }

        int interval = Config.getConfig(player.getWorld()).PLAYER_PINGS;
        if (interval <= 0) {
            NEXT_PING.remove(player.getUniqueId());
            return;
        }

        Integer nextPing = NEXT_PING.putIfAbsent(player.getUniqueId(), time + interval);
        if (nextPing != null && time >= nextPing) {
            NEXT_PING.put(player.getUniqueId(), time + interval);
            Queue.queuePlayerPing(player, time);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        NEXT_PING.remove(event.getPlayer().getUniqueId());
    }
}

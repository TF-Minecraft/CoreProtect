package net.tfminecraft.coreprotect.listener.player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.model.action.SkillLog;
import net.tfminecraft.coreprotect.utility.ErrorReporter;

/**
 * Logs MythicLib skills that players activate, covering MMOCore class skills and MMOItems
 * abilities, plus any teleport a skill causes shortly afterwards. MythicLib is read through
 * reflection so CoreProtect builds and runs without it.
 */
public final class SkillCastListener extends Queue implements Listener {

    static final String MYTHICLIB = "MythicLib";
    static final String CAST_EVENT = "io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent";

    // Long enough for delayed and projectile teleport skills, short enough not to credit later teleports.
    static final long TELEPORT_WINDOW_MILLIS = 5000L;
    static final int MAX_TELEPORTS_PER_CAST = 5;

    private final SkillReader reader;
    private final Map<UUID, RecentCast> recentCasts = new ConcurrentHashMap<>();
    private volatile boolean readFailed;

    SkillCastListener(SkillReader reader) {
        this.reader = reader;
    }

    public static void register(Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin mythicLib = pluginManager.getPlugin(MYTHICLIB);
        if (mythicLib == null || !mythicLib.isEnabled()) {
            return;
        }

        try {
            Class<? extends Event> eventClass = Class.forName(CAST_EVENT, true, mythicLib.getClass().getClassLoader()).asSubclass(Event.class);
            SkillCastListener listener = new SkillCastListener(SkillReader.of(eventClass));
            pluginManager.registerEvent(eventClass, listener, EventPriority.MONITOR, (ignored, event) -> {
                if (eventClass.isInstance(event)) {
                    listener.onSkillCast(event);
                }
            }, plugin, true);
            pluginManager.registerEvents(listener, plugin);
        }
        catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            plugin.getLogger().warning("Skill logging is unavailable for this MythicLib version: " + e);
        }
    }

    void onSkillCast(Event event) {
        if (readFailed || !(event instanceof PlayerEvent)) {
            return;
        }

        Player player = ((PlayerEvent) event).getPlayer();
        if (!Config.getConfig(player.getWorld()).PLAYER_SKILLS) {
            return;
        }

        SkillCast cast;
        try {
            cast = reader.read(event);
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            readFailed = true;
            ErrorReporter.report(e);
            return;
        }
        // Automatic casts leave the window alone, so a frequent timer ability cannot cut short
        // the teleport tracking of a skill the player just used.
        if (cast.automatic()) {
            return;
        }

        String message = SkillLog.cast(cast.name(), cast.id());
        long now = System.currentTimeMillis();
        Queue.queuePlayerSkill(player, message, now / 1000L, player.getLocation());
        recentCasts.put(player.getUniqueId(), new RecentCast(message, now + TELEPORT_WINDOW_MILLIS));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        TeleportCause cause = event.getCause();
        if (cause != TeleportCause.PLUGIN && cause != TeleportCause.UNKNOWN) {
            return;
        }

        Player player = event.getPlayer();
        RecentCast cast = recentCasts.get(player.getUniqueId());
        if (cast == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now > cast.expiresAt) {
            recentCasts.remove(player.getUniqueId(), cast);
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || to.getWorld() == null || SkillLog.sameBlock(from, to)) {
            return;
        }
        if (!Config.getConfig(to.getWorld()).PLAYER_SKILLS || !cast.recordTeleport()) {
            return;
        }

        Queue.queuePlayerSkill(player, SkillLog.teleport(cast.message, from, to), now / 1000L, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        recentCasts.remove(event.getPlayer().getUniqueId());
    }

    record SkillCast(String id, String name, boolean automatic) {
    }

    private static final class RecentCast {
        private final String message;
        private final long expiresAt;
        private final AtomicInteger teleports = new AtomicInteger();

        private RecentCast(String message, long expiresAt) {
            this.message = message;
            this.expiresAt = expiresAt;
        }

        private boolean recordTeleport() {
            return teleports.incrementAndGet() <= MAX_TELEPORTS_PER_CAST;
        }
    }

    /**
     * Reads the skill from a MythicLib PlayerCastSkillEvent. Silent triggers, such as timers,
     * attacks and damage, fire without player input; MythicLib marks every click trigger as
     * passive, so silence is what separates automatic casts from activated ones. MMOCore and
     * MMOItems skills report their configured trigger; plain API casts report the silent API
     * trigger.
     */
    static final class SkillReader {
        private final Method getCast;
        private final Method getHandler;
        private final Method getTrigger;
        private final Method getId;
        private final Method getName;
        private final Method isSilent;

        private SkillReader(Method getCast, Method getHandler, Method getTrigger, Method getId, Method getName, Method isSilent) {
            this.getCast = getCast;
            this.getHandler = getHandler;
            this.getTrigger = getTrigger;
            this.getId = getId;
            this.getName = getName;
            this.isSilent = isSilent;
        }

        static SkillReader of(Class<?> eventClass) throws ReflectiveOperationException {
            Method getCast = eventClass.getMethod("getCast");
            Method getHandler = getCast.getReturnType().getMethod("getHandler");
            Method getTrigger = getCast.getReturnType().getMethod("getTrigger");
            Method getId = getHandler.getReturnType().getMethod("getId");
            Method getName = getHandler.getReturnType().getMethod("getName");
            Method isSilent = getTrigger.getReturnType().getMethod("isSilent");
            return new SkillReader(getCast, getHandler, getTrigger, getId, getName, isSilent);
        }

        SkillCast read(Event event) throws ReflectiveOperationException {
            Object skill = getCast.invoke(event);
            Object handler = getHandler.invoke(skill);
            Object trigger = getTrigger.invoke(skill);
            boolean automatic = trigger != null && Boolean.TRUE.equals(isSilent.invoke(trigger));
            return new SkillCast((String) getId.invoke(handler), (String) getName.invoke(handler), automatic);
        }
    }
}

package net.tfminecraft.coreprotect.listener.entity;

import java.lang.reflect.Method;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import net.tfminecraft.coreprotect.model.action.MythicIdentity;

/** Capture while Mythic still owns the mob, before Bukkit's MONITOR death logger. */
public final class MythicMobListener implements Listener {
    private static final String KEY = "coreprotect:mythic-death";
    private static volatile Plugin owner;
    private final Plugin plugin;
    private final Reader reader;
    private boolean warned;

    MythicMobListener(Plugin plugin, Reader reader) {
        this.plugin = plugin;
        this.reader = reader;
    }

    public static void register(Plugin plugin) {
        owner = plugin;
        Plugin mythic = plugin.getServer().getPluginManager().getPlugin("MythicMobs");
        if (mythic == null || !mythic.isEnabled()) {
            return;
        }
        try {
            Class<? extends Event> eventClass = Class.forName("io.lumine.mythic.bukkit.events.MythicMobDeathEvent",
                    true, mythic.getClass().getClassLoader()).asSubclass(Event.class);
            MythicMobListener listener = new MythicMobListener(plugin, Reader.of(eventClass));
            plugin.getServer().getPluginManager().registerEvent(eventClass, listener, EventPriority.MONITOR,
                    (ignored, event) -> listener.onDeath(event), plugin, true);
        }
        catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            plugin.getLogger().warning("MythicMobs audit identity is unavailable: " + exception);
        }
    }

    void onDeath(Event event) {
        try {
            Entity entity = (Entity) reader.entity.invoke(event);
            Object type = reader.type.invoke(event);
            MythicIdentity identity = new MythicIdentity((String) reader.id.invoke(type));
            entity.setMetadata(KEY, new FixedMetadataValue(plugin, identity.id()));
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            if (!warned) {
                warned = true;
                plugin.getLogger().warning("Unable to read MythicMobs death identity: " + exception);
            }
        }
    }

    public static MythicIdentity take(Entity entity) {
        return consume(entity, owner);
    }

    static MythicIdentity consume(Entity entity, Plugin plugin) {
        if (plugin == null) {
            return null;
        }
        for (MetadataValue value : entity.getMetadata(KEY)) {
            if (value.getOwningPlugin() == plugin) {
                entity.removeMetadata(KEY, plugin);
                try {
                    return new MythicIdentity(value.asString());
                }
                catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    record Reader(Method entity, Method type, Method id) {
        static Reader of(Class<?> event) throws ReflectiveOperationException {
            Method type = event.getMethod("getMobType");
            return new Reader(event.getMethod("getEntity"), type, type.getReturnType().getMethod("getInternalName"));
        }
    }
}

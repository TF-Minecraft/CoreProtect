package net.tfminecraft.coreprotect.listener.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

class MythicMobListenerTest {
    public static class MobType {
        public String getInternalName() { return "cursed_ghoul"; }
    }
    public static class Death extends Event {
        private final Entity entity;
        Death(Entity entity) { this.entity = entity; }
        public Entity getEntity() { return entity; }
        public MobType getMobType() { return new MobType(); }
        public HandlerList getHandlers() { return new HandlerList(); }
    }

    @Test
    void capturesBeforeBukkitDeathAndConsumesOnlyOwnIdentity() throws Exception {
        Plugin plugin = mock(Plugin.class);
        Entity entity = mock(Entity.class);
        List<MetadataValue> metadata = new ArrayList<>();
        metadata.add(new FixedMetadataValue(mock(Plugin.class), "unrelated"));
        when(entity.getMetadata(anyString())).thenAnswer(call -> List.copyOf(metadata));
        doAnswer(call -> { metadata.add(call.getArgument(1)); return null; })
            .when(entity).setMetadata(anyString(), any());
        doAnswer(call -> { metadata.removeIf(v -> v.getOwningPlugin() == plugin); return null; })
            .when(entity).removeMetadata(anyString(), eq(plugin));
        var listener = new MythicMobListener(plugin, MythicMobListener.Reader.of(Death.class));
        listener.onDeath(new Death(entity));
        // Mythic's active mob may now be removed. No live manager lookup occurs.
        assertEquals("cursed_ghoul", MythicMobListener.consume(entity, plugin).id());
        assertNull(MythicMobListener.consume(entity, plugin));
        assertEquals(1, metadata.size());
    }

    @Test
    void missingOptionalPluginAndVanillaMobHaveNoIdentity() {
        assertNull(MythicMobListener.consume(mock(Entity.class), null));
        assertNull(MythicMobListener.consume(mock(Entity.class), mock(Plugin.class)));
    }

    @Test
    void incompatibleEventWarnsOnceWithoutBreakingDeathLogging() throws Exception {
        Plugin plugin = mock(Plugin.class);
        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);
        var listener = new MythicMobListener(plugin, MythicMobListener.Reader.of(Death.class));
        listener.onDeath(mock(Event.class));
        listener.onDeath(mock(Event.class));
        verify(logger, times(1)).warning(anyString());
    }
}

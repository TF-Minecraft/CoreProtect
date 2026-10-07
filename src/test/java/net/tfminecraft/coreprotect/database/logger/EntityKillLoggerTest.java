package net.tfminecraft.coreprotect.database.logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.*;
import net.tfminecraft.coreprotect.model.action.MythicIdentity;
import net.tfminecraft.coreprotect.utility.EntityUtils;
import net.tfminecraft.coreprotect.utility.WorldUtils;

class EntityKillLoggerTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void writesOneKillAndPreservesRollbackPayloadAndTrackedUuid(boolean custom) {
        var blockBatch = mock(ConsumerWriteBatch.class);
        var entityBatch = mock(ConsumerWriteBatch.class);
        var linksBatch = mock(ConsumerWriteBatch.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        var state = new ArrayList<Object>(Arrays.asList(0, false, new ArrayList<>(), true, "Ghoul", null, null, "tracked-uuid"));
        MythicIdentity identity = custom ? new MythicIdentity("ghoul") : null;
        try (var config = mockStatic(Config.class);
             var handler = mockStatic(ConfigHandler.class);
             var types = mockStatic(EntityUtils.class);
             var worlds = mockStatic(WorldUtils.class);
             var users = mockStatic(UserStatement.class);
             var entities = mockStatic(EntityStatement.class);
             var links = mockStatic(EntitySpawnStatement.class);
             var blocks = mockStatic(BlockStatement.class)) {
            config.when(Config::getGlobal).thenReturn(mock(Config.class));
            types.when(() -> EntityUtils.getEntityType(9)).thenReturn(EntityType.ZOMBIE);
            worlds.when(() -> WorldUtils.getWorldId("world")).thenReturn(1);
            users.when(() -> UserStatement.getId(blockBatch, "Player", true)).thenReturn(2);
            entities.when(() -> EntityStatement.insert(eq(entityBatch), anyInt(), same(state))).thenReturn(42);
            EntityKillLogger.log(blockBatch, entityBatch, linksBatch, 0, "Player", new Location(world, 3, 4, 5), state, 9, identity);
            entities.verify(() -> EntityStatement.insert(eq(entityBatch), anyInt(), same(state)));
            links.verify(() -> EntitySpawnStatement.addKillLink(linksBatch, "tracked-uuid", 42));
            blocks.verify(() -> BlockStatement.insert(eq(blockBatch), eq(0), anyInt(), eq(2), eq(1), eq(3), eq(4), eq(5),
                    eq(9), eq(42), eq(custom ? identity.toMetadata() : null), isNull(), eq(3), eq(0)));
            blocks.verifyNoMoreInteractions();
        }
    }
}

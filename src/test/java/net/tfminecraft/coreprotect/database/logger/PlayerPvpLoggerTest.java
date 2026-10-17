package net.tfminecraft.coreprotect.database.logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.BlockStatement;
import net.tfminecraft.coreprotect.database.statement.UserStatement;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.model.action.PvpEvent;
import net.tfminecraft.coreprotect.utility.WorldUtils;

class PlayerPvpLoggerTest {
    @Test
    void writesOneRowWithTheAttackerVictimAndDetails() {
        var batch = mock(ConsumerWriteBatch.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        PvpEvent pvp = new PvpEvent("iron_sword", 4.5, "entity_attack");
        try (var config = mockStatic(Config.class);
             var handler = mockStatic(ConfigHandler.class);
             var worlds = mockStatic(WorldUtils.class);
             var users = mockStatic(UserStatement.class);
             var blocks = mockStatic(BlockStatement.class)) {
            config.when(Config::getGlobal).thenReturn(mock(Config.class));
            worlds.when(() -> WorldUtils.getWorldId("world")).thenReturn(1);
            users.when(() -> UserStatement.getId(batch, "Rushork", true)).thenReturn(2);
            users.when(() -> UserStatement.getId(batch, "SPShadar", true)).thenReturn(5);
            PlayerPvpLogger.log(batch, 0, "Rushork", new Location(world, 3748.7, 193, 1501.2), "SPShadar", LookupActions.PVP_HIT, pvp);
            blocks.verify(() -> BlockStatement.insert(eq(batch), eq(0), anyInt(), eq(2), eq(1), eq(3748), eq(193), eq(1501),
                    eq(0), eq(5), eq(pvp.toMetadata()), isNull(), eq(LookupActions.PVP_HIT), eq(0)));
            blocks.verifyNoMoreInteractions();
        }
    }

    @Test
    void blacklistedAttackersAndNonPvpActionsAreSkipped() {
        var batch = mock(ConsumerWriteBatch.class);
        World world = mock(World.class);
        PvpEvent pvp = new PvpEvent("hand", 1, "entity_attack");
        try (var handler = mockStatic(ConfigHandler.class);
             var blocks = mockStatic(BlockStatement.class)) {
            handler.when(() -> ConfigHandler.isBlacklisted("Hidden")).thenReturn(true);
            PlayerPvpLogger.log(batch, 0, "Hidden", new Location(world, 0, 0, 0), "SPShadar", LookupActions.PVP_HIT, pvp);
            PlayerPvpLogger.log(batch, 0, "Rushork", new Location(world, 0, 0, 0), "SPShadar", LookupActions.ENTITY_KILL, pvp);
            blocks.verifyNoInteractions();
        }
    }
}

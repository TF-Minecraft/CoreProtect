package net.tfminecraft.coreprotect.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.tfminecraft.coreprotect.model.action.EntityActionFilter;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.utility.EntitySpawnTracking;

class LookupRawActionPredicateTest {
    private static String predicate(String actions, List<Integer> actionList, EntityActionFilter filter) throws Exception {
        Method method = LookupRaw.class.getDeclaredMethod("buildActionPredicate", String.class, List.class, EntityActionFilter.class);
        method.setAccessible(true);
        return (String) method.invoke(null, actions, actionList, filter);
    }

    @Test
    void killLookupsKeepPlayerVictimsWhenNoPlacedEntityTypesAreKnown() throws Exception {
        try (var tracking = mockStatic(EntitySpawnTracking.class)) {
            tracking.when(EntitySpawnTracking::getPlacedEntityTypeIds).thenReturn(Set.of());
            String kill = predicate(Integer.toString(LookupActions.ENTITY_KILL), List.of(LookupActions.ENTITY_KILL), EntityActionFilter.NONE);
            // Player kills are stored with type 0, so they must not be excluded.
            assertFalse(kill.contains("NOT IN(0)"), kill);
            assertTrue(kill.contains("type NOT IN(-1)"), kill);
        }
    }

    @Test
    void onlyStaffWithThePvpPermissionSeeFightsInActionlessLookups() {
        var staff = mock(org.bukkit.command.CommandSender.class);
        when(staff.hasPermission("coreprotect.lookup.pvp")).thenReturn(true);
        var builder = mock(org.bukkit.command.CommandSender.class);
        assertTrue(LookupRaw.canLookupPvp(null));
        assertTrue(LookupRaw.canLookupPvp(staff));
        assertFalse(LookupRaw.canLookupPvp(builder));
    }

    @Test
    void pvpLookupsSelectOnlyTheirOwnActions() throws Exception {
        String pvp = predicate("14,15,16", LookupActions.PVP_ACTIONS, EntityActionFilter.NONE);
        assertEquals("(action IN(14,15,16))", pvp);
    }
}

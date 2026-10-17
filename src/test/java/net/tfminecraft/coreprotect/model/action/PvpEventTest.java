package net.tfminecraft.coreprotect.model.action;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.tfminecraft.coreprotect.database.DatabaseType;
import net.tfminecraft.coreprotect.database.statement.BlockStatement;

class PvpEventTest {
    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void everyCodecRoundTripsTheDetailsALookupShows(DatabaseType type) {
        PvpEvent event = new PvpEvent("IRON_SWORD", 4.5, "ENTITY_ATTACK");
        byte[] blob = BlockStatement.serializeMetadata(event.toMetadata(), type);
        assertNotNull(blob);
        PvpEvent read = PvpEvent.fromLookupString(new String(blob, StandardCharsets.ISO_8859_1));
        assertNotNull(read);
        assertEquals("iron_sword", read.getWeapon());
        assertEquals(4.5, read.getDamage());
        assertEquals("entity_attack", read.getCause());
        assertEquals(" with iron_sword (4.5 damage)", read.describe());
    }

    @Test
    void missingWeaponIsAnEmptyHandAndUnknownDamageIsLeftOut() {
        PvpEvent event = new PvpEvent(null, Double.NaN, null);
        assertEquals("hand", event.getWeapon());
        assertEquals(0, event.getDamage());
        assertEquals("", event.getCause());
        assertEquals(" with hand", event.describe());
        assertEquals(" with arrow", new PvpEvent("arrow", -2, "projectile").describe());
    }

    @Test
    void otherMetadataIsNotAPvpEvent() {
        assertNull(PvpEvent.fromMetadata((List<Object>) null));
        assertNull(PvpEvent.fromMetadata((byte[]) null));
        assertNull(PvpEvent.fromLookupString(null));
        assertNull(PvpEvent.fromLookupString(""));
        assertNull(PvpEvent.fromMetadata(List.of("coreprotect:lock", "Private", false)));
        assertNull(PvpEvent.fromMetadata(List.of(PvpEvent.MARKER, "sword", 1.0)));
        assertNull(PvpEvent.fromMetadata(Arrays.asList(PvpEvent.MARKER, 7, 1.0, "cause")));
        assertNull(PvpEvent.fromMetadata(Arrays.asList(PvpEvent.MARKER, "sword", "1", "cause")));
        assertNull(PvpEvent.fromMetadata(Arrays.asList(PvpEvent.MARKER, "sword", 1.0, null)));
    }

    @Test
    void pvpActionsAreNamedAndKeptOutOfBlockFilters() {
        assertEquals(List.of(LookupActions.PVP_HIT, LookupActions.PVP_KNOCKOUT, LookupActions.PVP_KILL), LookupActions.PVP_ACTIONS);
        assertEquals("hit", LookupActions.getActionString(LookupActions.PVP_HIT));
        assertEquals("knockout", LookupActions.getActionString(LookupActions.PVP_KNOCKOUT));
        assertEquals("pvp-kill", LookupActions.getActionString(LookupActions.PVP_KILL));
        assertTrue(LookupActions.isPvp(LookupActions.PVP_KILL));
        assertFalse(LookupActions.isPvp(LookupActions.ENTITY_KILL));
        for (int action : LookupActions.PVP_ACTIONS) {
            assertTrue(Arrays.asList(LookupActions.NON_BLOCK_ACTIONS.split(",")).contains(Integer.toString(action)));
        }
    }
}

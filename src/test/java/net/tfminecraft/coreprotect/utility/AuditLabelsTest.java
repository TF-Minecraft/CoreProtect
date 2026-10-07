package net.tfminecraft.coreprotect.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class AuditLabelsTest {
    @Test
    void distinguishesRenamedVanillaFromRecordedMmoItem() {
        assertEquals("iron_ingot (renamed: Mythril)", AuditLabels.item("iron_ingot", "Mythril", null, null));
        assertEquals("Mythril [MMOItems MATERIAL:MYTHRIL]", AuditLabels.item("iron_ingot", "Mythril", "MATERIAL", "MYTHRIL"));
        assertEquals("iron_ingot", AuditLabels.item("iron_ingot", null, null, null));
    }

    @Test
    void preservesAlloyNamesWhenStableIdsAreShared() {
        assertEquals("Mythril Alloy [MMOItems MATERIAL:ALLOY]", AuditLabels.item("iron_ingot", "Mythril Alloy", "MATERIAL", "ALLOY"));
        assertEquals("Bronze Alloy [MMOItems MATERIAL:ALLOY]", AuditLabels.item("iron_ingot", "Bronze Alloy", "MATERIAL", "ALLOY"));
    }

    @Test
    void usesIdentityWhenSavedNameIsAbsent() {
        assertEquals("Mythril Ingot [MMOItems MATERIAL:MYTHRIL_INGOT]", AuditLabels.item("iron_ingot", "", "MATERIAL", "MYTHRIL_INGOT"));
        assertEquals("Forest Goblin [MythicMobs FOREST_GOBLIN]", AuditLabels.mob("zombie", null, "FOREST_GOBLIN"));
    }

    @Test
    void rejectsPartialOrMalformedIdentities() {
        assertEquals("iron_ingot (renamed: Mythril)", AuditLabels.item("iron_ingot", "Mythril", "MATERIAL", null));
        assertEquals("iron_ingot (renamed: Mythril)", AuditLabels.item("iron_ingot", "Mythril", "MATERIAL", "MYTHRIL<COMPONENT>"));
        assertEquals("iron_ingot", AuditLabels.item("iron_ingot", null, "MATERIAL", "A".repeat(129)));
        assertEquals("zombie (named: Goblin)", AuditLabels.mob("zombie", entity("Goblin"), "goblin|bad"));
    }

    @Test
    void stripsColorsControlsAndComponentMarkup() {
        String result = AuditLabels.item("iron_ingot", "§x§1§2§3§4§5§6Mythril\n\t<COMPONENT>|\u0000§r&kAlloy", "MATERIAL", "ALLOY");
        assertEquals("Mythril COMPONENT Alloy [MMOItems MATERIAL:ALLOY]", result);
        assertFalse(result.contains("§"));
        assertFalse(result.contains("<"));
        assertFalse(result.contains("|"));
    }

    @Test
    void handlesHistoricalMobRowsWithoutClaimingMythicIdentity() {
        assertEquals("zombie (named: Goblin King)", AuditLabels.mob("zombie", entity("§aGoblin King"), null));
        assertEquals("Goblin King [MythicMobs GoblinKing]", AuditLabels.mob("zombie", entity("§aGoblin King"), "GoblinKing"));
        assertEquals("zombie", AuditLabels.mob("zombie", List.of(), null));
        assertEquals("zombie", AuditLabels.mob("zombie", entity(null), null));
        assertEquals("zombie", AuditLabels.mob("zombie", entity(123), null));
    }

    @Test
    void boundsLabels() {
        String result = AuditLabels.item("iron_ingot", "A".repeat(300), "MATERIAL", "ALLOY");
        assertEquals("A".repeat(159) + "… [MMOItems MATERIAL:ALLOY]", result);
        assertEquals("A".repeat(159) + "… [MythicMobs Goblin]", AuditLabels.mob("zombie", entity("A".repeat(300)), "Goblin"));
        assertTrue(AuditLabels.mob("zombie", entity("A".repeat(300)), null).endsWith("…)"));
    }

    private static List<Object> entity(Object name) {
        return Arrays.asList(null, null, null, false, name);
    }
}

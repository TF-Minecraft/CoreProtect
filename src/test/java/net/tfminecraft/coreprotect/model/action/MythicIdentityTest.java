package net.tfminecraft.coreprotect.model.action;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import net.tfminecraft.coreprotect.database.DatabaseType;
import net.tfminecraft.coreprotect.database.statement.BlockStatement;
import net.tfminecraft.coreprotect.database.rollback.RollbackUtil;

class MythicIdentityTest {
    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    void existingCodecsAndRollbackReaderAcceptIdentity(DatabaseType type) {
        MythicIdentity identity = new MythicIdentity("cursed_ghoul");
        byte[] blob = BlockStatement.serializeMetadata(identity.toMetadata(), type);
        assertNotNull(blob);
        assertEquals(identity, MythicIdentity.fromMetadata(BlockStatement.deserializeMetadata(blob)));
        assertEquals(identity, MythicIdentity.fromMetadata(RollbackUtil.deserializeMetadata(blob)));
        assertEquals(identity, MythicIdentity.fromLookupString(new String(blob, StandardCharsets.ISO_8859_1)));
    }

    @Test
    void otherMetadataIsNotAnIdentity() {
        assertNull(MythicIdentity.fromMetadata(null));
        assertNull(MythicIdentity.fromMetadata(List.of("coreprotect:lock", "ghoul")));
        assertNull(MythicIdentity.fromMetadata(List.of("coreprotect:mythic", "bad id")));
        assertNull(MythicIdentity.fromMetadata(List.of("coreprotect:mythic", "ghoul", "extra")));
        assertThrows(IllegalArgumentException.class, () -> new MythicIdentity("x".repeat(129)));
    }
}

package net.tfminecraft.coreprotect.model.action;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import net.tfminecraft.coreprotect.database.statement.BlockStatement;

/**
 * A lock state change logged by another plugin. It is stored as an interaction row whose block
 * metadata starts with {@link #MARKER}, so rollback, purge and user lookups treat it like any
 * other click.
 */
public final class LockChange {

    public static final String MARKER = "coreprotect:lock";

    private final String lockState;
    private final boolean staffOverride;

    public LockChange(String lockState, boolean staffOverride) {
        this.lockState = lockState;
        this.staffOverride = staffOverride;
    }

    public String getLockState() {
        return lockState;
    }

    public boolean isStaffOverride() {
        return staffOverride;
    }

    public List<Object> toMetadata() {
        return Arrays.asList(MARKER, lockState, staffOverride);
    }

    public static LockChange fromMetadata(List<Object> metadata) {
        if (metadata == null || metadata.size() < 3 || !MARKER.equals(metadata.get(0)) || !(metadata.get(1) instanceof String)) {
            return null;
        }
        return new LockChange((String) metadata.get(1), Boolean.TRUE.equals(metadata.get(2)));
    }

    public static LockChange fromMetadata(byte[] metadata) {
        return metadata == null ? null : fromMetadata(BlockStatement.deserializeMetadata(metadata));
    }

    // Lookup rows carry metadata as ISO-8859-1 strings; see LookupConverter.
    public static LockChange fromLookupString(String metadata) {
        return metadata == null || metadata.isEmpty() ? null : fromMetadata(metadata.getBytes(StandardCharsets.ISO_8859_1));
    }
}

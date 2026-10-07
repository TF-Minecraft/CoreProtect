package net.tfminecraft.coreprotect.model.action;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.tfminecraft.coreprotect.database.statement.BlockStatement;

/** Audit identity only; the original entity rollback payload remains unchanged. */
public record MythicIdentity(String id) {
    public static final String MARKER = "coreprotect:mythic";
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_.:-]{1,128}");

    public MythicIdentity {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid MythicMobs identity");
        }
    }

    public List<Object> toMetadata() {
        return new ArrayList<>(List.of(MARKER, id));
    }

    public static MythicIdentity fromMetadata(List<Object> data) {
        if (data == null || data.size() != 2 || !MARKER.equals(data.get(0))
                || !(data.get(1) instanceof String id) || !ID.matcher(id).matches()) {
            return null;
        }
        return new MythicIdentity(id);
    }

    public static MythicIdentity fromLookupString(String data) {
        return data == null || data.isEmpty() ? null
                : fromMetadata(BlockStatement.deserializeMetadata(data.getBytes(StandardCharsets.ISO_8859_1)));
    }
}

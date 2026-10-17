package net.tfminecraft.coreprotect.model.action;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class LookupActions {
    public static final int BLOCK_BREAK = 0;
    public static final int BLOCK_PLACE = 1;
    public static final int INTERACTION = 2;
    public static final int ENTITY_KILL = 3;
    public static final int CONTAINER = 4;
    public static final int CHAT = 6;
    public static final int COMMAND = 7;
    public static final int SESSION = 8;
    public static final int USERNAME = 9;
    public static final int SIGN = 10;
    public static final int ITEM = 11;
    public static final int ENTITY_SPAWN = 13;
    // Player versus player rows live in the block table, but are never rolled back or matched by block filters
    public static final int PVP_HIT = 14;
    public static final int PVP_KNOCKOUT = 15;
    public static final int PVP_KILL = 16;
    public static final List<Integer> PVP_ACTIONS = Collections.unmodifiableList(Arrays.asList(PVP_HIT, PVP_KNOCKOUT, PVP_KILL));
    // Block table actions whose type column is not a block, so block filters must skip them
    public static final String NON_BLOCK_ACTIONS = ENTITY_KILL + "," + ENTITY_SPAWN + "," + PVP_HIT + "," + PVP_KNOCKOUT + "," + PVP_KILL;

    private LookupActions() {
        throw new IllegalStateException("Model class");
    }

    public static String getActionString(int action) {
        switch (action) {
            case BLOCK_BREAK:
                return "break";
            case BLOCK_PLACE:
                return "place";
            case INTERACTION:
                return "click";
            case ENTITY_KILL:
                return "kill";
            case CONTAINER:
                return "container";
            case CHAT:
                return "chat";
            case COMMAND:
                return "command";
            case SESSION:
                return "session";
            case USERNAME:
                return "username";
            case SIGN:
                return "sign";
            case ITEM:
                return "item";
            case ENTITY_SPAWN:
                return "spawn";
            case PVP_HIT:
                return "hit";
            case PVP_KNOCKOUT:
                return "knockout";
            case PVP_KILL:
                return "pvp-kill";
            default:
                return "unknown";
        }
    }

    public static boolean isPvp(int action) {
        return PVP_ACTIONS.contains(action);
    }

    public static boolean isInventoryLookup(Collection<Integer> actions) {
        return actions.contains(CONTAINER) && actions.contains(ITEM);
    }
}

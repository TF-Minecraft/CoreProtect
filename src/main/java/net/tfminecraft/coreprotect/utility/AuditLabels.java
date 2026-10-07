package net.tfminecraft.coreprotect.utility;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/** Labels derived from the recorded stack or entity, never from a current item definition. */
public final class AuditLabels {
    private static final int MAX_LABEL_LENGTH = 160;
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_.:-]{1,128}");
    private static final Pattern COLORS = Pattern.compile("(?i)[§&][0-9a-fk-orx]");
    private static final Pattern UNSAFE_TEXT = Pattern.compile("[\\p{Cntrl}\\p{Cf}\\s<>|]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static volatile NbtReader nbtReader;

    private AuditLabels() {
    }

    /** Resolve optional MythicLib access once, using its own plugin classloader. */
    public static void register(Plugin plugin) {
        nbtReader = null;
        Plugin mythicLib = plugin.getServer().getPluginManager().getPlugin("MythicLib");
        if (mythicLib == null || !mythicLib.isEnabled()) {
            return;
        }
        try {
            Class<?> nbtItem = Class.forName("io.lumine.mythic.lib.api.item.NBTItem", true, mythicLib.getClass().getClassLoader());
            nbtReader = new NbtReader(nbtItem.getMethod("get", ItemStack.class), nbtItem.getMethod("getString", String.class));
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().warning("MMOItems audit identities are unavailable for this MythicLib version: " + exception);
        }
    }

    @SuppressWarnings("deprecation")
    public static String item(byte[] metadata, int type, int amount, String vanilla) {
        if (metadata == null) {
            return bounded(clean(vanilla));
        }
        ItemStack stack;
        String name = null;
        try {
            stack = ItemUtils.getItemStack(metadata, type, amount);
            if (stack == null) {
                return bounded(clean(vanilla));
            }
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                name = meta.hasDisplayName() ? meta.getDisplayName() : meta.hasItemName() ? meta.getItemName() : null;
            }
        }
        catch (RuntimeException | LinkageError exception) {
            return bounded(clean(vanilla));
        }

        String itemType = null;
        String itemId = null;
        NbtReader reader = nbtReader;
        if (reader != null) {
            try {
                // The reconstructed stack is detached from inventories; MythicLib may wrap it.
                Object nbt = reader.get.invoke(null, stack);
                itemType = (String) reader.getString.invoke(nbt, "MMOITEMS_ITEM_TYPE");
                itemId = (String) reader.getString.invoke(nbt, "MMOITEMS_ITEM_ID");
            }
            catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                // A malformed historical stack must not prevent the remaining lookup rows.
                itemType = null;
                itemId = null;
            }
        }
        return item(vanilla, name, itemType, itemId);
    }

    static String item(String vanilla, String name, String type, String id) {
        String base = clean(vanilla);
        String savedName = clean(name);
        if (validId(type) && validId(id)) {
            return (savedName.isEmpty() ? humanize(id) : savedName) + " [MMOItems " + type + ":" + id + "]";
        }
        return savedName.isEmpty() ? base : base + " (renamed: " + savedName + ")";
    }

    public static String mob(String vanilla, List<Object> entityData, String identity) {
        String name = entityData != null && entityData.size() > 4 && entityData.get(4) instanceof String
                ? clean((String) entityData.get(4)) : "";
        if (validId(identity)) {
            return (name.isEmpty() ? humanize(identity) : name) + " [MythicMobs " + identity + "]";
        }
        String base = clean(vanilla);
        return name.isEmpty() ? base : base + " (named: " + name + ")";
    }

    private static boolean validId(String value) {
        return value != null && ID.matcher(value).matches();
    }

    private static String clean(String value) {
        return value == null ? "" : bounded(UNSAFE_TEXT.matcher(COLORS.matcher(value).replaceAll("")).replaceAll(" ").trim());
    }

    private static String humanize(String id) {
        String words = id.replace('_', ' ').toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(words.length());
        boolean first = true;
        for (char character : words.toCharArray()) {
            result.append(first ? Character.toUpperCase(character) : character);
            first = character == ' ';
        }
        return words.isEmpty() ? "" : result.toString();
    }

    private static String bounded(String value) {
        if (value.length() <= MAX_LABEL_LENGTH) {
            return value;
        }
        int end = MAX_LABEL_LENGTH - 1;
        if (Character.isHighSurrogate(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(0, end) + "…";
    }

    private record NbtReader(Method get, Method getString) {
    }
}

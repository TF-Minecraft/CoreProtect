package net.tfminecraft.coreprotect.model.action;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * A skill cast, or a teleport caused by one. It is stored as a command row whose message starts
 * with {@link #PREFIX}. Commands always start with "/", so a skill row never matches a command
 * prefix filter, and "a:skill" lookups are command lookups filtered to this prefix.
 */
public final class SkillLog {

    public static final String PREFIX = "[skill]";

    // Legacy "&" and section-sign colour codes, including "&#rrggbb" hex colours.
    private static final Pattern COLOUR_CODES = Pattern.compile("(?i)[&\u00A7](#[0-9a-f]{6}|[0-9a-fk-orx])");

    private SkillLog() {
        throw new IllegalStateException("Model class");
    }

    public static String cast(String name, String id) {
        String cleanName = clean(name);
        String cleanId = clean(id);
        if (cleanName.isEmpty()) {
            return PREFIX + " " + cleanId;
        }
        if (cleanId.isEmpty() || cleanId.equalsIgnoreCase(cleanName)) {
            return PREFIX + " " + cleanName;
        }
        return PREFIX + " " + cleanName + " (" + cleanId + ")";
    }

    public static String teleport(String cast, Location from, Location to) {
        boolean sameWorld = sameWorld(from.getWorld(), to.getWorld());
        return cast + " teleported from " + position(from, !sameWorld) + " to " + position(to, !sameWorld);
    }

    public static boolean sameBlock(Location from, Location to) {
        return sameWorld(from.getWorld(), to.getWorld()) && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ();
    }

    // Prefix filters match the start of the message, so each user filter is narrowed to skill rows.
    public static List<String> lookupFilters(List<String> filters) {
        if (filters == null || filters.isEmpty()) {
            return List.of(PREFIX);
        }
        return filters.stream().map(filter -> filter.toLowerCase(Locale.ROOT).startsWith(PREFIX) ? filter : PREFIX + " " + filter).collect(Collectors.toList());
    }

    private static boolean sameWorld(World from, World to) {
        return from == null ? to == null : to != null && from.getUID().equals(to.getUID());
    }

    private static String position(Location location, boolean includeWorld) {
        String coordinates = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        if (!includeWorld || location.getWorld() == null) {
            return coordinates;
        }
        return location.getWorld().getName() + " " + coordinates;
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return COLOUR_CODES.matcher(value).replaceAll("").trim();
    }
}

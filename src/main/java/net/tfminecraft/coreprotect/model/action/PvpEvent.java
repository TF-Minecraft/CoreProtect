package net.tfminecraft.coreprotect.model.action;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.tfminecraft.coreprotect.database.statement.BlockStatement;

/**
 * Details of a hit, knockout or kill between two players. It is stored as block metadata starting
 * with {@link #MARKER} on a row whose user is the attacker and whose data is the victim's user id.
 */
public final class PvpEvent {

    public static final String MARKER = "coreprotect:pvp";

    private final String weapon;
    private final double damage;
    private final String cause;

    /**
     * @param weapon
     *            The item or projectile that dealt the damage, for example "iron_sword" or "arrow"
     * @param damage
     *            The damage dealt, or 0 when it is not known
     * @param cause
     *            The damage cause, for example "entity_attack"
     */
    public PvpEvent(String weapon, double damage, String cause) {
        this.weapon = weapon == null || weapon.isEmpty() ? "hand" : weapon.toLowerCase(Locale.ROOT);
        this.damage = Double.isFinite(damage) && damage > 0 ? damage : 0;
        this.cause = cause == null ? "" : cause.toLowerCase(Locale.ROOT);
    }

    public String getWeapon() {
        return weapon;
    }

    public double getDamage() {
        return damage;
    }

    public String getCause() {
        return cause;
    }

    public List<Object> toMetadata() {
        return Arrays.asList(MARKER, weapon, damage, cause);
    }

    /** The text after the victim's name in a lookup, such as " with iron_sword (4.5 damage)". */
    public String describe() {
        String text = " with " + weapon;
        if (damage > 0) {
            text += String.format(Locale.ROOT, " (%.1f damage)", damage);
        }
        return text;
    }

    public static PvpEvent fromMetadata(List<Object> metadata) {
        if (metadata == null || metadata.size() < 4 || !MARKER.equals(metadata.get(0)) || !(metadata.get(1) instanceof String)
                || !(metadata.get(2) instanceof Number) || !(metadata.get(3) instanceof String)) {
            return null;
        }
        return new PvpEvent((String) metadata.get(1), ((Number) metadata.get(2)).doubleValue(), (String) metadata.get(3));
    }

    public static PvpEvent fromMetadata(byte[] metadata) {
        return metadata == null ? null : fromMetadata(BlockStatement.deserializeMetadata(metadata));
    }

    // Lookup rows carry metadata as ISO-8859-1 strings; see LookupConverter.
    public static PvpEvent fromLookupString(String metadata) {
        return metadata == null || metadata.isEmpty() ? null : fromMetadata(metadata.getBytes(StandardCharsets.ISO_8859_1));
    }
}

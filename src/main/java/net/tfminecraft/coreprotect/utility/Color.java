package net.tfminecraft.coreprotect.utility;


public final class Color {

    // we define our own constants here to eliminate string concatenation
    // javadoc taken from org.bukkit.ChatColor

    /**
     * Represents black.
     */
    public static final String BLACK = "\u00a70";

    /**
     * Represents dark blue.
     */
    public static final String DARK_BLUE = "\u00a71";

    /**
     * Represents dark green.
     */
    public static final String DARK_GREEN = "\u00a72";

    /**
     * Represents dark blue (aqua).
     */
    public static String DARK_AQUA = "\u00a73";

    /**
     * Represents dark red.
     */
    public static final String DARK_RED = "\u00a74";

    /**
     * Represents dark purple.
     */
    public static final String DARK_PURPLE = "\u00a75";

    /**
     * Represents gold.
     */
    public static final String GOLD = "\u00a76";

    /**
     * Represents grey.
     */
    public static final String GREY = "\u00a77";

    /**
     * Represents dark grey.
     */
    public static final String DARK_GREY = "\u00a78";

    /**
     * Represents blue.
     */
    public static final String BLUE = "\u00a79";

    /**
     * Represents green.
     */
    public static final String GREEN = "\u00a7a";

    /**
     * Represents aqua.
     */
    public static final String AQUA = "\u00a7b";

    /**
     * Represents red.
     */
    public static final String RED = "\u00a7c";

    /**
     * Represents light purple.
     */
    public static final String LIGHT_PURPLE = "\u00a7d";

    /**
     * Represents yellow.
     */
    public static final String YELLOW = "\u00a7e";

    /**
     * Represents white.
     */
    public static final String WHITE = "\u00a7f";

    /**
     * Represents magical characters that change around randomly.
     */
    public static final String MAGIC = "\u00a7k";

    /**
     * Makes the text bold.
     */
    public static final String BOLD = "\u00a7l";

    /**
     * Makes a line appear through the text.
     */
    public static final String STRIKETHROUGH = "\u00a7m";

    /**
     * Makes the text appear underlined.
     */
    public static final String UNDERLINE = "\u00a7n";

    /**
     * Makes the text italic.
     */
    public static final String ITALIC = "\u00a7o";

    /**
     * Resets all previous chat colors or formats.
     */
    public static final String RESET = "\u00a7r";

    private Color() {
        throw new IllegalStateException("Utility class");
    }

}

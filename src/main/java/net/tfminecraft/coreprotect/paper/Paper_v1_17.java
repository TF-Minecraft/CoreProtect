package net.tfminecraft.coreprotect.paper;

import org.bukkit.block.Sign;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Paper_v1_17 extends PaperHandler {

    // Version-specific adapter: retain the API available on the older servers handled here.
    @SuppressWarnings("deprecation")
    @Override
    public String getLine(Sign sign, int line) {
        if (line >= 4) {
            return "";
        }

        // https://docs.adventure.kyori.net/serializer/
        return LegacyComponentSerializer.legacySection().serialize(sign.line(line));
    }

}

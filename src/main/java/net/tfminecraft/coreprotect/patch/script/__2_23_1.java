package net.tfminecraft.coreprotect.patch.script;

import java.sql.Statement;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.language.Phrase;
import net.tfminecraft.coreprotect.language.Selector;
import net.tfminecraft.coreprotect.patch.Patch;
import net.tfminecraft.coreprotect.utility.Chat;
import net.tfminecraft.coreprotect.utility.ErrorReporter;

public class __2_23_1 {

    protected static boolean patch(Statement statement) {
        try {
            if (Config.getGlobal().MYSQL) {
                try {
                    statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "skull ADD COLUMN skin TEXT;");
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "skull", Selector.FIRST, Selector.FIRST));
                }
            }
            else {
                try {
                    statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "skull ADD COLUMN skin TEXT;");
                }
                catch (Exception e) {
                    Chat.console(Phrase.build(Phrase.PATCH_SKIP_UPDATE, ConfigHandler.prefix + "skull", Selector.FIRST, Selector.FIRST));
                }

                if (!Patch.continuePatch()) {
                    return false;
                }
            }
        }
        catch (Exception e) {
            ErrorReporter.report(e);
        }

        return true;
    }

}

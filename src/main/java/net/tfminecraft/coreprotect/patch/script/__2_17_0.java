package net.tfminecraft.coreprotect.patch.script;

import java.sql.Statement;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.utility.ErrorReporter;

public class __2_17_0 {

    protected static boolean patch(Statement statement) {
        try {
            if (Config.getGlobal().MYSQL) {
                statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN color int");
            }
            else {
                statement.executeUpdate("ALTER TABLE " + ConfigHandler.prefix + "sign ADD COLUMN color INTEGER");
            }
        }
        catch (Exception e) {
            ErrorReporter.report(e);
        }

        return true;
    }

}

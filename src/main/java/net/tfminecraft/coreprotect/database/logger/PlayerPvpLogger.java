package net.tfminecraft.coreprotect.database.logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import net.tfminecraft.coreprotect.CoreProtect;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.Database;
import net.tfminecraft.coreprotect.database.statement.BlockStatement;
import net.tfminecraft.coreprotect.database.statement.UserStatement;
import net.tfminecraft.coreprotect.event.CoreProtectPreLogEvent;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.model.action.PvpEvent;
import net.tfminecraft.coreprotect.utility.WorldUtils;

public class PlayerPvpLogger {

    private PlayerPvpLogger() {
        throw new IllegalStateException("Database class");
    }

    /**
     * Writes one hit, knockout or kill. The row's user is the attacker, its data the victim's user id
     * and its location the victim's position.
     */
    public static void log(ConsumerWriteBatch preparedStmt, int batchCount, String attacker, Location location, String victim, int action, PvpEvent pvp) {
        try {
            if (!LookupActions.isPvp(action) || ConfigHandler.isBlacklisted(attacker)) {
                return;
            }

            int victimId = UserStatement.getId(preparedStmt, victim, true);

            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(attacker, location.clone(), CoreProtectPreLogEvent.Action.PLAYER_PVP, action, null, EntityType.PLAYER, pvp.getWeapon());
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return;
            }

            int userId = UserStatement.getId(preparedStmt, event.getUser(), true);
            Location eventLocation = event.getLocation();
            int wid = WorldUtils.getWorldId(eventLocation.getWorld().getName());
            int time = (int) (System.currentTimeMillis() / 1000L);
            BlockStatement.insert(preparedStmt, batchCount, time, userId, wid, eventLocation.getBlockX(), eventLocation.getBlockY(), eventLocation.getBlockZ(), 0, victimId, pvp.toMetadata(), null, action, 0);
        }
        catch (Exception e) {
            Database.handleWriteFailure(e);
        }
    }

}

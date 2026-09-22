package net.tfminecraft.coreprotect.database.logger;


import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.tfminecraft.coreprotect.CoreProtect;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.database.ConsumerWriteBatch;
import net.tfminecraft.coreprotect.database.statement.EntityInteractionStatement;
import net.tfminecraft.coreprotect.database.statement.UserStatement;
import net.tfminecraft.coreprotect.event.CoreProtectPreLogEvent;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.model.entity.EntityInteraction;
import net.tfminecraft.coreprotect.model.entity.EntitySpawnIdentity;

public final class EntityInteractionLogger {

    private EntityInteractionLogger() {
        throw new IllegalStateException("Database class");
    }

    public static LogContext prepare(String user, EntityInteraction interaction) {
        if (interaction == null || ConfigHandler.isBlacklisted(user) || ConfigHandler.isBlacklisted(user, interaction.getEntityType().getKey().toString())) {
            return null;
        }

        Location currentLocation = interaction.getCurrentLocation();
        CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(user, currentLocation.clone(), CoreProtectPreLogEvent.Action.ENTITY_INTERACTION, LookupActions.INTERACTION, null, interaction.getEntityType(), interaction.getAction().name());
        if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
            CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
        }
        return event.isCancelled() ? null : new LogContext(event);
    }

    public static boolean log(ConsumerWriteBatch batch, EntitySpawnIdentity identity, EntityInteraction interaction, LogContext context) throws Exception {
        Location currentLocation = interaction.getCurrentLocation();
        boolean identityActive = EntityInteractionStatement.checkpoint(batch, identity, currentLocation);

        int worldId = identity.getOriginalWorldId();
        int x = identity.getOriginalX();
        int y = identity.getOriginalY();
        int z = identity.getOriginalZ();

        int userId = UserStatement.getId(batch, context.event.getUser(), true);
        EntityInteractionStatement.insert(batch, interaction.getTime(), userId, identity, worldId, x, y, z, interaction);
        return identityActive;
    }

    public static final class LogContext {

        private final CoreProtectPreLogEvent event;

        private LogContext(CoreProtectPreLogEvent event) {
            this.event = event;
        }
    }
}

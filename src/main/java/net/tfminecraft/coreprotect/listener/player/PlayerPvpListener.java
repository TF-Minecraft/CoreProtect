package net.tfminecraft.coreprotect.listener.player;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.model.action.PvpEvent;

/**
 * Logs every hit and kill one player lands on another, so staff can see who attacked first.
 * Hits that another plugin cancels, such as a nonlethal knockout blow, are not hits; plugins log
 * knockouts through the API instead.
 */
public final class PlayerPvpListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    protected void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim) || event.getFinalDamage() <= 0) {
            return;
        }
        Player attacker = attacker(event);
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId()) || !Config.getConfig(victim.getWorld()).PLAYER_PVP) {
            return;
        }

        PvpEvent pvp = new PvpEvent(weapon(event, attacker), event.getFinalDamage(), event.getCause().name());
        Queue.queuePlayerPvp(attacker.getName(), victim.getLocation(), victim.getName(), LookupActions.PVP_HIT, pvp);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    protected void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        Player attacker = lastDamage == null ? null : attacker(lastDamage);
        if (attacker == null) {
            attacker = victim.getKiller();
        }
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId()) || !Config.getConfig(victim.getWorld()).PLAYER_PVP) {
            return;
        }

        PvpEvent pvp = lastDamage == null ? new PvpEvent(heldItem(attacker), 0, "")
                : new PvpEvent(weapon(lastDamage, attacker), lastDamage.getFinalDamage(), lastDamage.getCause().name());
        Queue.queuePlayerPvp(attacker.getName(), victim.getLocation(), victim.getName(), LookupActions.PVP_KILL, pvp);
    }

    /** The player responsible for the damage, including through projectiles, or null. */
    public static Player attacker(EntityDamageEvent event) {
        DamageSource source = event.getDamageSource();
        if (source != null && source.getCausingEntity() instanceof Player player) {
            return player;
        }
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            if (damager instanceof Player player) {
                return player;
            }
            if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    /** The projectile or other entity that dealt the damage, or else the attacker's held item. */
    static String weapon(EntityDamageEvent event, Player attacker) {
        DamageSource source = event.getDamageSource();
        Entity direct = source == null ? null : source.getDirectEntity();
        if (direct == null && event instanceof EntityDamageByEntityEvent byEntity) {
            direct = byEntity.getDamager();
        }
        if (direct != null && !direct.getUniqueId().equals(attacker.getUniqueId())) {
            return direct.getType().name().toLowerCase(Locale.ROOT);
        }
        return heldItem(attacker);
    }

    public static String heldItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            return "hand";
        }
        return item.getType().name().toLowerCase(Locale.ROOT);
    }
}

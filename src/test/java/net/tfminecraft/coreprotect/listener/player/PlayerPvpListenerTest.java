package net.tfminecraft.coreprotect.listener.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.consumer.Queue;
import net.tfminecraft.coreprotect.model.action.LookupActions;
import net.tfminecraft.coreprotect.model.action.PvpEvent;

class PlayerPvpListenerTest extends Queue {
    private final PlayerPvpListener listener = new PlayerPvpListener();
    private MockedStatic<Queue> queue;
    private MockedStatic<Config> config;
    private Config worldConfig;
    private World world;
    private Player attacker;
    private Player victim;

    @BeforeEach
    void setUp() {
        queue = mockStatic(Queue.class);
        config = mockStatic(Config.class);
        world = mock(World.class);
        worldConfig = mock(Config.class);
        worldConfig.PLAYER_PVP = true;
        config.when(() -> Config.getConfig(world)).thenReturn(worldConfig);
        attacker = player("Rushork", Material.IRON_SWORD);
        victim = player("SPShadar", Material.AIR);
    }

    @AfterEach
    void tearDown() {
        config.close();
        queue.close();
    }

    private Player player(String name, Material held) {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(world);
        when(player.getType()).thenReturn(EntityType.PLAYER);
        when(player.getLocation()).thenReturn(new Location(world, 3748, 193, 1501));
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(held);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getItemInMainHand()).thenReturn(item);
        when(player.getInventory()).thenReturn(inventory);
        return player;
    }

    private DamageSource source(org.bukkit.entity.Entity causing, org.bukkit.entity.Entity direct) {
        DamageSource source = mock(DamageSource.class);
        when(source.getCausingEntity()).thenReturn(causing);
        when(source.getDirectEntity()).thenReturn(direct);
        return source;
    }

    private EntityDamageByEntityEvent melee(Player damager, Player damaged, double damage) {
        DamageSource source = source(damager, damager);
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(damaged);
        when(event.getDamager()).thenReturn(damager);
        when(event.getDamageSource()).thenReturn(source);
        when(event.getFinalDamage()).thenReturn(damage);
        when(event.getCause()).thenReturn(DamageCause.ENTITY_ATTACK);
        return event;
    }

    private PvpEvent loggedHit(String attackerName, String victimName, int action) {
        ArgumentCaptor<PvpEvent> pvp = ArgumentCaptor.forClass(PvpEvent.class);
        queue.verify(() -> Queue.queuePlayerPvp(eq(attackerName), any(Location.class), eq(victimName), eq(action), pvp.capture()));
        return pvp.getValue();
    }

    @Test
    void meleeHitLogsTheAttackerVictimWeaponAndDamage() {
        listener.onPlayerDamage(melee(attacker, victim, 4.5));
        PvpEvent pvp = loggedHit("Rushork", "SPShadar", LookupActions.PVP_HIT);
        assertEquals("iron_sword", pvp.getWeapon());
        assertEquals(4.5, pvp.getDamage());
        assertEquals("entity_attack", pvp.getCause());
    }

    @Test
    void emptyHandedHitsAndProjectilesNameTheirWeapon() {
        listener.onPlayerDamage(melee(victim, attacker, 1));
        assertEquals("hand", loggedHit("SPShadar", "Rushork", LookupActions.PVP_HIT).getWeapon());

        Arrow arrow = mock(Arrow.class);
        when(arrow.getUniqueId()).thenReturn(UUID.randomUUID());
        when(arrow.getType()).thenReturn(EntityType.ARROW);
        when(arrow.getShooter()).thenReturn(attacker);
        EntityDamageByEntityEvent shot = mock(EntityDamageByEntityEvent.class);
        when(shot.getEntity()).thenReturn(victim);
        when(shot.getDamager()).thenReturn(arrow);
        when(shot.getFinalDamage()).thenReturn(6.0);
        when(shot.getCause()).thenReturn(DamageCause.PROJECTILE);
        assertSame(attacker, PlayerPvpListener.attacker(shot));
        assertEquals("arrow", PlayerPvpListener.weapon(shot, attacker));
    }

    @Test
    void indirectDamageIsTracedThroughTheDamageSource() {
        DamageSource magicSource = source(attacker, null);
        EntityDamageEvent magic = mock(EntityDamageEvent.class);
        when(magic.getEntity()).thenReturn(victim);
        when(magic.getDamageSource()).thenReturn(magicSource);
        when(magic.getFinalDamage()).thenReturn(2.0);
        when(magic.getCause()).thenReturn(DamageCause.MAGIC);
        listener.onPlayerDamage(magic);
        PvpEvent pvp = loggedHit("Rushork", "SPShadar", LookupActions.PVP_HIT);
        assertEquals("iron_sword", pvp.getWeapon());
        assertEquals("magic", pvp.getCause());
    }

    @Test
    void selfHarmMobsZeroDamageAndDisabledWorldsAreNotLogged() {
        listener.onPlayerDamage(melee(victim, victim, 3));
        listener.onPlayerDamage(melee(attacker, victim, 0));
        EntityDamageByEntityEvent mob = mock(EntityDamageByEntityEvent.class);
        Zombie zombie = mock(Zombie.class);
        when(mob.getEntity()).thenReturn(victim);
        when(mob.getDamager()).thenReturn(zombie);
        DamageSource mobSource = source(zombie, zombie);
        when(mob.getDamageSource()).thenReturn(mobSource);
        when(mob.getFinalDamage()).thenReturn(3.0);
        listener.onPlayerDamage(mob);
        EntityDamageEvent onMob = mock(EntityDamageEvent.class);
        when(onMob.getEntity()).thenReturn(zombie);
        listener.onPlayerDamage(onMob);
        worldConfig.PLAYER_PVP = false;
        listener.onPlayerDamage(melee(attacker, victim, 3));
        queue.verifyNoInteractions();
    }

    @Test
    void deathByAnotherPlayerLogsAKillWithTheKillingBlow() {
        EntityDamageByEntityEvent blow = melee(attacker, victim, 7);
        when(victim.getLastDamageCause()).thenReturn(blow);
        PlayerDeathEvent death = mock(PlayerDeathEvent.class);
        when(death.getEntity()).thenReturn(victim);
        listener.onPlayerDeath(death);
        PvpEvent pvp = loggedHit("Rushork", "SPShadar", LookupActions.PVP_KILL);
        assertEquals("iron_sword", pvp.getWeapon());
        assertEquals(7, pvp.getDamage());
    }

    @Test
    void deathFallsBackToTheTrackedKillerAndIgnoresOtherDeaths() {
        PlayerDeathEvent death = mock(PlayerDeathEvent.class);
        when(death.getEntity()).thenReturn(victim);
        listener.onPlayerDeath(death);
        queue.verifyNoInteractions();

        when(victim.getKiller()).thenReturn(attacker);
        listener.onPlayerDeath(death);
        PvpEvent pvp = loggedHit("Rushork", "SPShadar", LookupActions.PVP_KILL);
        assertEquals("iron_sword", pvp.getWeapon());
        assertEquals(0, pvp.getDamage());
    }
}

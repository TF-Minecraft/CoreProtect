package net.tfminecraft.coreprotect.listener;

import org.bukkit.plugin.PluginManager;

import net.tfminecraft.coreprotect.CoreProtect;
import net.tfminecraft.coreprotect.listener.block.BlockBreakListener;
import net.tfminecraft.coreprotect.listener.block.BlockBurnListener;
import net.tfminecraft.coreprotect.listener.block.BlockDispenseListener;
import net.tfminecraft.coreprotect.listener.block.BlockExplodeListener;
import net.tfminecraft.coreprotect.listener.block.BlockFadeListener;
import net.tfminecraft.coreprotect.listener.block.BlockFertilizeListener;
import net.tfminecraft.coreprotect.listener.block.BlockFormListener;
import net.tfminecraft.coreprotect.listener.block.BlockFromToListener;
import net.tfminecraft.coreprotect.listener.block.BlockIgniteListener;
import net.tfminecraft.coreprotect.listener.block.BlockPistonListener;
import net.tfminecraft.coreprotect.listener.block.BlockPlaceListener;
import net.tfminecraft.coreprotect.listener.block.BlockSpreadListener;
import net.tfminecraft.coreprotect.listener.block.CampfireStartListener;
import net.tfminecraft.coreprotect.listener.block.TNTPrimeListener;
import net.tfminecraft.coreprotect.listener.block.TNTPrimeUtil;
import net.tfminecraft.coreprotect.listener.channel.PluginChannelHandshakeListener;
import net.tfminecraft.coreprotect.listener.channel.PluginChannelListener;
import net.tfminecraft.coreprotect.listener.entity.CreatureSpawnListener;
import net.tfminecraft.coreprotect.listener.entity.EntityBlockFormListener;
import net.tfminecraft.coreprotect.listener.entity.EntityChangeBlockListener;
import net.tfminecraft.coreprotect.listener.entity.EntityDamageByBlockListener;
import net.tfminecraft.coreprotect.listener.entity.EntityDamageByEntityListener;
import net.tfminecraft.coreprotect.listener.entity.EntityDeathListener;
import net.tfminecraft.coreprotect.listener.entity.EntityExplodeListener;
import net.tfminecraft.coreprotect.listener.entity.EntityInteractListener;
import net.tfminecraft.coreprotect.listener.entity.EntityPlaceListener;
import net.tfminecraft.coreprotect.listener.entity.EntityChunkListener;
import net.tfminecraft.coreprotect.listener.entity.LegacyEntityChunkListener;
import net.tfminecraft.coreprotect.listener.entity.EntityPickupItemListener;
import net.tfminecraft.coreprotect.listener.entity.EntitySpawnListener;
import net.tfminecraft.coreprotect.listener.entity.EntityTransformListener;
import net.tfminecraft.coreprotect.listener.entity.HangingBreakByEntityListener;
import net.tfminecraft.coreprotect.listener.entity.HangingBreakListener;
import net.tfminecraft.coreprotect.listener.entity.HangingPlaceListener;
import net.tfminecraft.coreprotect.listener.entity.VehicleDestroyListener;
import net.tfminecraft.coreprotect.listener.entity.TrackedEntityRemoveListener;
import net.tfminecraft.coreprotect.listener.entity.TrackedEntityTeleportListener;
import net.tfminecraft.coreprotect.listener.player.ArmorStandManipulateListener;
import net.tfminecraft.coreprotect.listener.player.CraftItemListener;
import net.tfminecraft.coreprotect.listener.player.EntityInteractionListener;
import net.tfminecraft.coreprotect.listener.player.FoodLevelChangeListener;
import net.tfminecraft.coreprotect.listener.player.InventoryChangeListener;
import net.tfminecraft.coreprotect.listener.player.InventoryClickListener;
import net.tfminecraft.coreprotect.listener.player.PlayerBucketEmptyListener;
import net.tfminecraft.coreprotect.listener.player.PlayerBucketFillListener;
import net.tfminecraft.coreprotect.listener.player.PlayerChatListener;
import net.tfminecraft.coreprotect.listener.player.PlayerCommandListener;
import net.tfminecraft.coreprotect.listener.player.PlayerDeathListener;
import net.tfminecraft.coreprotect.listener.player.PlayerDropItemListener;
import net.tfminecraft.coreprotect.listener.player.PlayerInteractEntityListener;
import net.tfminecraft.coreprotect.listener.player.PlayerInteractListener;
import net.tfminecraft.coreprotect.listener.player.PlayerItemBreakListener;
import net.tfminecraft.coreprotect.listener.player.PlayerJoinListener;
import net.tfminecraft.coreprotect.listener.player.PlayerPickupArrowListener;
import net.tfminecraft.coreprotect.listener.player.PlayerQuitListener;
import net.tfminecraft.coreprotect.listener.player.PlayerTakeLecternBookListener;
import net.tfminecraft.coreprotect.listener.player.ProjectileLaunchListener;
import net.tfminecraft.coreprotect.listener.player.SignChangeListener;
import net.tfminecraft.coreprotect.listener.player.SpawnEggUseListener;
import net.tfminecraft.coreprotect.listener.world.ChunkPopulateListener;
import net.tfminecraft.coreprotect.listener.world.LeavesDecayListener;
import net.tfminecraft.coreprotect.listener.world.PortalCreateListener;
import net.tfminecraft.coreprotect.listener.world.StructureGrowListener;
import net.tfminecraft.coreprotect.paper.listener.BlockPreDispenseListener;
import net.tfminecraft.coreprotect.paper.listener.CopperGolemChestListener;
import net.tfminecraft.coreprotect.paper.listener.FlowerPotManipulateListener;
import net.tfminecraft.coreprotect.paper.listener.LegacyTNTPrimeListener;
import net.tfminecraft.coreprotect.paper.listener.PaperChatListener;

public final class ListenerHandler {

    public ListenerHandler(CoreProtect plugin) {

        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Paper Listeners / Fallbacks (Block Listeners)
        try {
            Class.forName("io.papermc.paper.event.block.BlockPreDispenseEvent"); // Paper 1.16+
            pluginManager.registerEvents(new BlockPreDispenseListener(), plugin);
        }
        catch (Exception e) {
            BlockPreDispenseListener.useBlockPreDispenseEvent = false;
        }

        try {
            Class.forName("io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent"); // Paper 1.21.10+
            pluginManager.registerEvents(new CopperGolemChestListener(plugin), plugin);
        }
        catch (Exception e) {
            // Ignore registration failures to remain compatible with older servers.
        }

        // Block Listeners
        pluginManager.registerEvents(new BlockBreakListener(), plugin);
        pluginManager.registerEvents(new BlockBurnListener(), plugin);
        pluginManager.registerEvents(new BlockDispenseListener(), plugin);
        pluginManager.registerEvents(new BlockExplodeListener(), plugin);
        pluginManager.registerEvents(new BlockFadeListener(), plugin);
        pluginManager.registerEvents(new BlockFertilizeListener(), plugin);
        pluginManager.registerEvents(new BlockFormListener(), plugin);
        pluginManager.registerEvents(new BlockFromToListener(), plugin);
        pluginManager.registerEvents(new BlockIgniteListener(), plugin);
        pluginManager.registerEvents(new BlockPistonListener(), plugin);
        pluginManager.registerEvents(new BlockPlaceListener(), plugin);
        pluginManager.registerEvents(new BlockSpreadListener(), plugin);
        try {
            Class.forName("org.bukkit.event.block.CampfireStartEvent"); // Bukkit 1.20+
            pluginManager.registerEvents(new CampfireStartListener(), plugin);
        }
        catch (Exception e) {
            CampfireStartListener.useCampfireStartEvent = false;
        }
        try {
            Class.forName("org.bukkit.event.block.TNTPrimeEvent"); // Bukkit 1.20+
            pluginManager.registerEvents(new TNTPrimeListener(), plugin);
            TNTPrimeUtil.useTNTPrimeEvent = true;
        }
        catch (Exception e) {
            try {
                Class.forName("com.destroystokyo.paper.event.block.TNTPrimeEvent"); // Paper 1.16+
                pluginManager.registerEvents(new LegacyTNTPrimeListener(), plugin);
                TNTPrimeUtil.useTNTPrimeEvent = true;
            }
            catch (Exception ignored) {
                // Ignore registration failures to remain compatible with older servers.
            }
        }

        // Entity Listeners
        pluginManager.registerEvents(new CreatureSpawnListener(), plugin);
        pluginManager.registerEvents(new EntityBlockFormListener(), plugin);
        pluginManager.registerEvents(new EntityChangeBlockListener(), plugin);
        pluginManager.registerEvents(new EntityDamageByBlockListener(), plugin);
        pluginManager.registerEvents(new EntityDamageByEntityListener(), plugin);
        pluginManager.registerEvents(new EntityDeathListener(), plugin);
        pluginManager.registerEvents(new EntityExplodeListener(), plugin);
        pluginManager.registerEvents(new EntityInteractListener(), plugin);
        pluginManager.registerEvents(new EntityPlaceListener(), plugin);
        pluginManager.registerEvents(new EntityPickupItemListener(), plugin);
        pluginManager.registerEvents(new EntitySpawnListener(), plugin);
        pluginManager.registerEvents(new EntityTransformListener(), plugin);
        pluginManager.registerEvents(new HangingPlaceListener(), plugin);
        pluginManager.registerEvents(new HangingBreakListener(), plugin);
        pluginManager.registerEvents(new HangingBreakByEntityListener(), plugin);
        pluginManager.registerEvents(new VehicleDestroyListener(), plugin);
        pluginManager.registerEvents(new TrackedEntityTeleportListener(), plugin);
        try {
            Class.forName("org.bukkit.event.world.EntitiesUnloadEvent");
            pluginManager.registerEvents(new EntityChunkListener(), plugin);
        }
        catch (Exception e) {
            pluginManager.registerEvents(new LegacyEntityChunkListener(), plugin);
        }
        try {
            Class.forName("org.bukkit.event.entity.EntityRemoveEvent");
            pluginManager.registerEvents(new TrackedEntityRemoveListener(), plugin);
        }
        catch (Exception e) {
            // Entity removal causes are unavailable on older Bukkit versions.
        }
        // Paper Listeners / Fallbacks (Player Listeners)
        try {
            Class.forName("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer"); // Paper 1.16+
            pluginManager.registerEvents(new PaperChatListener(), plugin);
        }
        catch (Exception e) {
            pluginManager.registerEvents(new PlayerChatListener(), plugin);
        }
        try {
            Class.forName("io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent");
            pluginManager.registerEvents(new FlowerPotManipulateListener(), plugin);
        }
        catch (Exception e) {
            // Ignore registration failures to remain compatible with older servers.
        }

        // Player Listeners
        pluginManager.registerEvents(new ArmorStandManipulateListener(), plugin);
        pluginManager.registerEvents(new CraftItemListener(), plugin);
        pluginManager.registerEvents(new EntityInteractionListener(), plugin);
        pluginManager.registerEvents(new FoodLevelChangeListener(), plugin);
        pluginManager.registerEvents(new InventoryChangeListener(), plugin);
        pluginManager.registerEvents(new InventoryClickListener(), plugin);
        pluginManager.registerEvents(new PlayerBucketEmptyListener(), plugin);
        pluginManager.registerEvents(new PlayerBucketFillListener(), plugin);
        pluginManager.registerEvents(new PlayerCommandListener(), plugin);
        pluginManager.registerEvents(new PlayerDeathListener(), plugin);
        pluginManager.registerEvents(new PlayerDropItemListener(), plugin);
        pluginManager.registerEvents(new PlayerPickupArrowListener(), plugin);
        pluginManager.registerEvents(new PlayerInteractEntityListener(), plugin);
        pluginManager.registerEvents(new PlayerInteractListener(), plugin);
        pluginManager.registerEvents(new PlayerItemBreakListener(), plugin);
        pluginManager.registerEvents(new PlayerJoinListener(), plugin);
        pluginManager.registerEvents(new PlayerQuitListener(), plugin);
        pluginManager.registerEvents(new SignChangeListener(), plugin);
        pluginManager.registerEvents(new SpawnEggUseListener(), plugin);
        pluginManager.registerEvents(new PlayerTakeLecternBookListener(), plugin);
        pluginManager.registerEvents(new ProjectileLaunchListener(), plugin);

        // World Listeners
        pluginManager.registerEvents(new ChunkPopulateListener(), plugin);
        pluginManager.registerEvents(new LeavesDecayListener(), plugin);
        pluginManager.registerEvents(new PortalCreateListener(), plugin);
        pluginManager.registerEvents(new StructureGrowListener(), plugin);

        // Plugin channel events
        pluginManager.registerEvents(new PluginChannelListener(), plugin);
    }

    public static void registerNetworking() {
        PluginChannelHandshakeListener handshakeListener = new PluginChannelHandshakeListener();
        CoreProtect.getInstance().getServer().getMessenger().registerIncomingPluginChannel(CoreProtect.getInstance(), PluginChannelHandshakeListener.pluginChannel, handshakeListener);
        CoreProtect.getInstance().getServer().getPluginManager().registerEvents(handshakeListener, CoreProtect.getInstance());
        CoreProtect.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(CoreProtect.getInstance(), PluginChannelHandshakeListener.pluginChannel);
        CoreProtect.getInstance().getServer().getMessenger().registerOutgoingPluginChannel(CoreProtect.getInstance(), PluginChannelListener.pluginChannel);
    }

    public static void unregisterNetworking() {
        CoreProtect.getInstance().getServer().getMessenger().unregisterIncomingPluginChannel(CoreProtect.getInstance(), PluginChannelHandshakeListener.pluginChannel);
        CoreProtect.getInstance().getServer().getMessenger().unregisterOutgoingPluginChannel(CoreProtect.getInstance(), PluginChannelHandshakeListener.pluginChannel);
        CoreProtect.getInstance().getServer().getMessenger().unregisterOutgoingPluginChannel(CoreProtect.getInstance(), PluginChannelListener.pluginChannel);
    }

}

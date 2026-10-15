package net.tfminecraft.coreprotect.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import net.tfminecraft.coreprotect.command.parser.ActionParser;
import net.tfminecraft.coreprotect.command.parser.UserParser;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;

class TabHandlerTest {
    private final TabHandler handler = new TabHandler();
    private final Set<String> permissions = new HashSet<>();
    private Player player;
    private ConsoleCommandSender console;
    private MockedStatic<Bukkit> bukkit;
    private MockedStatic<Config> config;
    private MockedStatic<Material> materialTypes;
    private Config settings;

    @BeforeEach
    void setUp() {
        player = sender(Player.class, "TabTester");
        console = sender(ConsoleCommandSender.class, "Console");
        Player alice = mock(Player.class);
        Player bob = mock(Player.class);
        when(alice.getName()).thenReturn("Alice");
        when(bob.getName()).thenReturn("Bob");
        World world = mock(World.class);
        when(world.getName()).thenReturn("World");
        Server server = mock(Server.class);
        when(server.getWorlds()).thenReturn(List.of(world));
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        bukkit = mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getServer).thenReturn(server);
        bukkit.when(Bukkit::getWorlds).thenReturn(List.of(world));
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(List.of(alice, bob));
        settings = mock(Config.class);
        config = mockStatic(Config.class);
        config.when(Config::getGlobal).thenReturn(settings);

        // Material registry access requires a server; use a small block/item fixture.
        Material stone = material("STONE", true);
        Material diamond = material("DIAMOND", false);
        materialTypes = mockStatic(Material.class, CALLS_REAL_METHODS);
        materialTypes.when(Material::values).thenReturn(new Material[] { stone, diamond });
        ConfigHandler.entities.put("zombie", 1);
        ConfigHandler.entities.put("invalid_entity", 2);
        ConfigHandler.worlds.put("World", 7);
        ConfigHandler.worlds.put("unloaded", 8);
        ConfigHandler.lookupType.remove("TabTester");
        ConfigHandler.lookupPage.remove("TabTester");
        ConfigHandler.lastRollback.remove("TabTester");
    }

    @AfterEach
    void tearDown() {
        materialTypes.close();
        config.close();
        bukkit.close();
        ConfigHandler.entities.clear();
        ConfigHandler.worlds.clear();
        ConfigHandler.lastRollback.remove("TabTester");
    }

    @ParameterizedTest
    @ValueSource(strings = { "co", "core", "coreprotect" })
    void canonicalCommandsRespectPermissionsAndContextForEveryRoot(String root) {
        permissions.addAll(List.of("coreprotect.teleport", "coreprotect.give", "coreprotect.rollback", "coreprotect.networking"));
        settings.NETWORK_DEBUG = true;
        ConfigHandler.lastRollback.put("TabTester", Arrays.asList(0L, 0L, new String[] { "rollback", "#preview" }));
        assertEquals(List.of("teleport"), complete(player, root, "TeLe"));
        assertEquals(List.of("give"), complete(player, root, "gi"));
        assertEquals(List.of("apply"), complete(player, root, "app"));
        assertEquals(List.of("cancel"), complete(player, root, "can"));
        assertEquals(List.of("network-debug"), complete(player, root, "network-"));
        ConfigHandler.lastRollback.remove("TabTester");
        assertEquals(List.of(), complete(player, root, "app"));
        settings.NETWORK_DEBUG = false;
        assertEquals(List.of(), complete(player, root, "network-"));
        permissions.clear();
        assertEquals(List.of(), complete(player, root, ""));
    }

    @Test
    void consoleGetsSupportedCommandsAndConsumerControlsOnly() {
        permissions.addAll(List.of("coreprotect.consumer", "coreprotect.help", "coreprotect.lookup", "coreprotect.purge", "coreprotect.teleport", "coreprotect.give"));
        assertEquals(List.of("consumer"), complete(console, "co", "con"));
        assertEquals(List.of("pause"), complete(console, "co", "consumer", "PA"));
        assertEquals(List.of("resume"), complete(console, "co", "consumer", "re"));
        assertTrue(complete(console, "co", "").containsAll(List.of("help", "lookup", "purge")));
        assertFalse(complete(console, "co", "").contains("teleport"));
        assertFalse(complete(console, "co", "").contains("give"));
        assertEquals(List.of(), complete(player, "co", "con"));
        assertEquals(List.of(), complete(player, "co", "consumer", ""));
        assertEquals(List.of(), complete(console, "co", "consumer", "pause", ""));
        permissions.remove("coreprotect.consumer");
        assertEquals(List.of(), complete(console, "co", "consumer", "pa"));
    }

    @Test
    void nearRequiresALocatedSenderAndAcceptsEitherExecutionPermission() {
        BlockCommandSender block = sender(BlockCommandSender.class, "CommandBlock");
        for (String permission : List.of("coreprotect.lookup", "coreprotect.lookup.near")) {
            permissions.clear();
            permissions.add(permission);
            assertEquals(List.of("near"), complete(player, "co", "nE"));
            assertEquals(List.of("near"), complete(block, "co", "nE"));
            assertEquals(List.of(), complete(console, "co", "nE"));
        }
        permissions.clear();
        assertEquals(List.of(), complete(player, "co", "near"));
        assertEquals(List.of(), complete(block, "co", "near"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "teleport", "tp" })
    void teleportCompletesLoadedWorldsAndLeavesCoordinatesEmpty(String verb) {
        permissions.add("coreprotect.teleport");
        assertEquals(List.of("World"), complete(player, "co", verb, "wO"));
        assertEquals(List.of("wid:7"), complete(player, "co", verb, "wid:"));
        assertEquals(List.of("wid:7"), complete(player, "co", verb, "WID:"));
        assertEquals(List.of(), complete(player, "co", verb, "unloaded"));
        assertEquals(List.of(), complete(player, "co", verb, "World", ""));
        assertEquals(List.of(), complete(player, "co", verb, "World", "1", "a:ch"));
        assertEquals(List.of(), complete(console, "co", verb, ""));
        permissions.clear();
        assertEquals(List.of(), complete(player, "co", verb, "w"));
    }

    @Test
    void actionValuesReplaceOnlyTheCurrentInlineOrWhitespaceToken() {
        permissions.addAll(List.of("coreprotect.lookup", "coreprotect.lookup.chat", "coreprotect.rollback"));
        assertEquals(List.of("chat"), tab("lookup", "a:", "Ch"));
        assertEquals(List.of("a:chat"), tab("lookup", "a:Ch"));
        assertEquals(List.of("block"), tab("rollback", "action:", "BL"));
        assertTrue(tab("rollback", "action:", "").contains("block"));
        assertFalse(tab("rollback", "action:", "").contains("action:block"));
        assertEquals(List.of("i:diamond"), tab("rollback", "action:", "i:di"));
    }

    @Test
    void teleportUsesAnExistingWorldIdWhenTheWorldNameContainsSpaces() {
        permissions.add("coreprotect.teleport");
        World world = mock(World.class);
        when(world.getName()).thenReturn("World With Spaces");
        bukkit.when(Bukkit::getWorlds).thenReturn(List.of(world));
        ConfigHandler.worlds.put("World With Spaces", 9);
        assertEquals(List.of("wid:9"), tab("teleport", ""));
        assertEquals(List.of("wid:9"), tab("teleport", "wid:"));
        assertEquals(List.of(), tab("teleport", "World", "With"));
    }

    @Test
    void commaListsPreserveCompletedTextAndUseTheFinalPrefix() {
        permissions.addAll(List.of("coreprotect.lookup", "coreprotect.lookup.block", "coreprotect.lookup.chat"));
        assertEquals(List.of("u:Alice,Bob"), tab("lookup", "u:Alice,Bo"));
        assertEquals(List.of("Alice,Bob"), tab("lookup", "u:", "Alice,bO"));
        assertEquals(List.of("Bob"), tab("lookup", "u:Alice,", "Bo"));
        assertEquals(List.of("a:block,chat"), tab("lookup", "a:block,CH"));
        assertEquals(List.of("block,chat"), tab("lookup", "action:", "block,ch"));
        assertEquals(List.of("i:stone,diamond"), tab("lookup", "i:stone,di"));
        assertEquals(List.of("e:stone,diamond"), tab("lookup", "e:stone,di"));
        assertEquals(List.of("diamond"), tab("lookup", "i:stone,", "di"));
        assertEquals(List.of("diamond"), tab("lookup", "include:", "stone,", "di"));
        assertEquals(List.of("Bob"), tab("lookup", "user:", "Alice,", "bo"));
        assertTrue(ActionParser.parseAction(new String[] { "lookup", "a:block,chat" }, true).size() > 2);
        assertEquals(List.of("alice", "bob"), UserParser.parseUsers(new String[] { "lookup", "u:Alice,Bob" }));
        permissions.add("coreprotect.rollback");
        assertEquals(List.of(), tab("rollback", "a:block,bl"));
    }

    @Test
    void materialsIncludeKnownEntitiesAndExclusionsIncludePlayers() {
        permissions.addAll(List.of("coreprotect.lookup", "coreprotect.lookup.kill"));
        assertTrue(tab("lookup", "a:kill", "i:zomb").contains("i:zombie"));
        assertTrue(tab("lookup", "a:kill", "e:zomb").contains("e:zombie"));
        assertEquals(List.of("e:Bob"), tab("lookup", "e:Bo"));
        assertEquals(List.of("Bob"), tab("lookup", "exclude:", "Bo"));
        assertEquals(List.of("e:stone,Bob"), tab("lookup", "e:stone,Bo"));
        assertEquals(List.of(), tab("lookup", "i:Bo"));
        assertEquals(List.of(), tab("lookup", "i:invalid_entity"));
        ConfigHandler.entities.remove("zombie");
        assertFalse(tab("lookup", "i:zomb").contains("i:zombie"));
    }

    @Test
    void purgeOnlyPermissionsCompleteIncludeValuesAndAllParameterOrders() {
        permissions.add("coreprotect.purge");
        assertEquals(List.of("i:diamond"), tab("purge", "i:di"));
        assertEquals(List.of("i:diamond"), tab("purge", "t:30d", "i:di"));
        assertTrue(tab("purge", "time:30d", "r").containsAll(List.of("r:", "radius:")));
        assertTrue(tab("purge", "t:30d", "r:#World", "i").containsAll(List.of("i:", "include:")));
        assertTrue(tab("purge", "include:stone", "radius:#World", "t").containsAll(List.of("t:", "time:")));
        assertEquals(List.of("#optimize"), tab("purge", "#opt"));
        assertEquals(List.of(), tab("purge", "#optimize", "#opt"));
        assertEquals(List.of(), tab("purge", "time:30d", "t"));
        assertEquals(List.of("diamond"), tab("purge", "include:", "stone,", "di"));
        assertEquals(List.of("r:#World"), tab("purge", "r:#wo"));
        assertEquals(List.of(), tab("purge", "i:Bo"));
        assertEquals(List.of(), tab("purge", "e:Bo"));
        assertEquals(List.of(), tab("purge", "a:ch"));
        assertEquals(List.of(), tab("purge", "unknown"));
        permissions.clear();
        permissions.add("coreprotect.lookup");
        assertEquals(List.of(), tab("purge", "i:di"));
    }

    @Test
    void actionsUseTheSelectedCommandAndSpecificLookupPermissions() {
        permissions.addAll(List.of("coreprotect.lookup", "coreprotect.rollback", "coreprotect.purge"));
        assertEquals(List.of(), tab("lookup", "a:"));
        permissions.add("coreprotect.lookup.chat");
        assertEquals(List.of("a:chat"), tab("lookup", "a:ch"));
        assertEquals(List.of(), tab("rollback", "a:ch"));
        assertEquals(List.of(), tab("purge", "a:ch"));
        assertEquals(List.of(), tab("help", "a:ch"));
        assertEquals(List.of(), tab("restore", "i:di"));
        assertEquals(List.of(), tab("rollback", "a:item"));
        assertEquals(List.of("a:inventory"), tab("rollback", "a:inv"));
        permissions.add("coreprotect.lookup.inventory");
        assertEquals(List.of("a:inventory"), tab("lookup", "a:inv"));
        assertEquals(List.of(), tab("lookup", "a:item"));
        permissions.add("coreprotect.lookup.command");
        assertEquals(List.of("a:skill"), tab("lookup", "a:sk"));
        assertEquals(List.of(), tab("lookup", "a:skill,ch"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "lookup", "l", "rollback", "rb", "ro", "restore", "rs", "re" })
    void lookupFamilyAliasesRetainInlineWhitespaceAndPermissionBehavior(String verb) {
        String permission = List.of("lookup", "l").contains(verb) ? "lookup" : List.of("rollback", "rb", "ro").contains(verb) ? "rollback" : "restore";
        permissions.add("coreprotect." + permission);
        permissions.add("coreprotect.lookup.block");
        assertEquals(List.of("a:block"), tab(verb, "a:bl"));
        assertEquals(List.of("block"), tab(verb, "action:", "bl"));
        assertEquals(List.of("i:stone,diamond"), tab(verb, "i:stone,di"));
        assertEquals(List.of("Bob"), tab(verb, "user:", "bo"));
        assertEquals(List.of("time:"), tab(verb, "ti"));
        permissions.remove("coreprotect." + permission);
        assertEquals(List.of(), tab(verb, "a:bl"));
        assertEquals(List.of(), tab(verb, "i:di"));
    }

    @Test
    void eachLookupActionUsesItsOwnPermissionIncludingInventoryAndSessionVariants() {
        permissions.add("coreprotect.lookup");
        List<List<String>> cases = List.of(
            List.of("coreprotect.lookup.block", "a:block", "a:+block", "a:-block"),
            List.of("coreprotect.lookup.click", "a:click"),
            List.of("coreprotect.lookup.kill", "a:kill"),
            List.of("coreprotect.lookup.spawn", "a:spawn"),
            List.of("coreprotect.lookup.container", "a:+container", "a:-container", "a:container"),
            List.of("coreprotect.lookup.chat", "a:chat"),
            List.of("coreprotect.lookup.command", "a:command", "a:skill"),
            List.of("coreprotect.lookup.inventory", "a:+inventory", "a:-inventory", "a:inventory"),
            List.of("coreprotect.lookup.item", "a:item", "a:+item", "a:-item"),
            List.of("coreprotect.lookup.sign", "a:sign"),
            List.of("coreprotect.lookup.session", "a:session", "a:+session", "a:-session", "a:ping"),
            List.of("coreprotect.lookup.username", "a:username"));
        for (List<String> permissionCase : cases) {
            permissions.add(permissionCase.getFirst());
            assertEquals(permissionCase.subList(1, permissionCase.size()), tab("lookup", "a:"));
            permissions.remove(permissionCase.getFirst());
            assertEquals(List.of(), tab("lookup", "a:"));
        }
    }

    @Test
    void verbosityModifiersAndReloadHelpAreFilteredAndDeduplicated() {
        permissions.addAll(List.of("coreprotect.help", "coreprotect.lookup", "coreprotect.rollback", "coreprotect.purge"));
        assertEquals(List.of("reload"), tab("help", "rEL"));
        assertEquals(List.of("#silent"), tab("rollback", "t:1h", "#si"));
        assertEquals(List.of("#silent"), tab("rollback", "t:1h", "#silent"));
        assertEquals(List.of("#verbose"), tab("lookup", "t:1h", "#ve"));
        assertEquals(List.of(), tab("lookup", "#verbose", "#ve"));
        assertEquals(List.of(), tab("rollback", "#silent", "#si"));
        assertEquals(List.of(), tab("purge", "#ve"));
        assertEquals(List.of(), complete(console, "co", "rollback", "#pr"));
    }

    @Test
    void emptyOrUnhandledArgumentsNeverReturnNullOrAnEmptyStringCandidate() {
        permissions.add("coreprotect.lookup");
        assertEquals(List.of(), complete(player, "co"));
        assertEquals(List.of(), tab("unknown", ""));
        assertEquals(List.of(), tab("give", ""));
        assertEquals(List.of(), tab("lookup", "page:x"));
        assertEquals(List.of("Bob"), tab("lookup", "u:", "Bo"));
    }

    private List<String> tab(String... args) {
        return complete(player, "co", args);
    }

    private List<String> complete(CommandSender sender, String root, String... args) {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn(root);
        List<String> result = handler.onTabComplete(sender, command, root, args);
        assertNotNull(result, "Explicit completion result for " + Arrays.toString(args));
        assertFalse(result.contains(""), "No blank candidate for " + Arrays.toString(args));
        return result;
    }

    private <T extends CommandSender> T sender(Class<T> type, String name) {
        T sender = mock(type);
        when(sender.getName()).thenReturn(name);
        when(sender.hasPermission(anyString())).thenAnswer(call -> permissions.contains(call.getArgument(0)));
        return sender;
    }

    private Material material(String name, boolean block) {
        Material material = mock(Material.class);
        when(material.name()).thenReturn(name);
        when(material.isBlock()).thenReturn(block);
        when(material.isItem()).thenReturn(true);
        return material;
    }
}

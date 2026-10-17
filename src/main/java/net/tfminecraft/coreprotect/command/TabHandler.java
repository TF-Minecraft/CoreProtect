package net.tfminecraft.coreprotect.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.tfminecraft.coreprotect.command.parser.ActionParser;
import net.tfminecraft.coreprotect.command.parser.RollbackStateParser;
import net.tfminecraft.coreprotect.config.Config;
import net.tfminecraft.coreprotect.config.ConfigHandler;
import net.tfminecraft.coreprotect.model.action.LookupActions;

public class TabHandler implements TabCompleter {

    // private static String[] COMMANDS = new String[] { "help", "inspect", "rollback", "restore", "lookup", "purge", "reload", "status", "near", "undo" }; // max 10!
    private static final String[] HELP = new String[] { "inspect", "rollback", "restore", "lookup", "purge", "teleport", "reload", "status", "params", "users", "time", "radius", "action", "include", "exclude", "filter" };
    private static final String[] PARAMS = new String[] { "user:", "time:", "radius:", "action:", "include:", "exclude:", "filter:", "#container" };
    private static final String[] ACTIONS = new String[] { "block", "+block", "-block", "click", "kill", "pvp", "hit", "knockout", "pvp-kill", "spawn", "+container", "-container", "container", "chat", "command", "skill", "+inventory", "-inventory", "inventory", "item", "+item", "-item", "sign", "session", "+session", "-session", "ping", "username" };
    private static final String[] NUMBERS = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private static final String[] TIMES = new String[] { "w", "d", "h", "m", "s" };
    private static ArrayList<String> materials = null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return List.of();
        }

        if (args.length == 1) {
            return getFirstLevelCompletions(sender, args[0]);
        }

        String argument0 = args[0].toLowerCase(Locale.ROOT);
        String currentArg = args[args.length - 1].trim();
        String currentLower = currentArg.toLowerCase(Locale.ROOT);
        String lastArg = pendingParameter(args);
        if (currentArg.contains(":")) {
            lastArg = "";
        }

        if (argument0.equals("teleport") || argument0.equals("tp")) {
            if (args.length == 2 && sender instanceof Player && sender.hasPermission("coreprotect.teleport")) {
                return handleTeleportCompletions(currentArg);
            }
            return List.of();
        }
        if (argument0.equals("consumer")) {
            if (args.length == 2 && sender instanceof ConsoleCommandSender && sender.hasPermission("coreprotect.consumer")) {
                return StringUtil.copyPartialMatches(currentArg, List.of("pause", "resume"), new ArrayList<>());
            }
            return List.of();
        }

        boolean lookupCommand = hasLookupCommand(argument0, sender);
        boolean purgeCommand = argument0.equals("purge") && sender.hasPermission("coreprotect.purge");

        ParamState paramState = getParamState(sender, args);

        // Handle param-specific completions
        if (isActionParam(lastArg, currentLower) && lookupCommand) {
            return handleActionParamCompletions(sender, argument0, currentArg);
        }
        else if (isUserParam(lastArg, currentLower) && lookupCommand) {
            return completeValues(currentArg, onlineUsers(), true);
        }
        else if (isTimeParam(lastArg, currentLower) && (lookupCommand || purgeCommand)) {
            return handleTimeParamCompletions(currentArg, lastArg);
        }
        else if (isPageParam(lastArg, currentLower) && (argument0.equals("lookup") || argument0.equals("l") || argument0.equals("page")) && hasPagePermission(sender)) {
            return handlePageParamCompletions(currentArg, lastArg);
        }
        else if (isRadiusParam(lastArg, currentLower) && (lookupCommand || purgeCommand)) {
            return handleRadiusParamCompletions(currentArg, lastArg, argument0);
        }
        else if (isMaterialParam(lastArg, currentLower) && (lookupCommand || purgeCommand)) {
            return handleMaterialParamCompletions(currentArg, lastArg, purgeCommand);
        }
        else if (purgeCommand) {
            return handlePurgeCompletions(currentArg, paramState);
        }
        else if (args.length == 2) {
            return handleSecondArgCompletions(sender, argument0, args[1], paramState);
        }
        else if (lookupCommand && (!argument0.equals("l") && !argument0.equals("lookup") || !paramState.hasPage)) {
            return handleGenericLookupCompletions(argument0, currentArg, paramState, sender instanceof Player);
        }

        return List.of();
    }

    private List<String> getFirstLevelCompletions(CommandSender sender, String argument) {
        String arg = argument.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();

        addCompletionIfPermitted(sender, "coreprotect.help", "help", completions);
        addCompletionIfPermitted(sender, "coreprotect.inspect", "inspect", completions);
        addCompletionIfPermitted(sender, "coreprotect.rollback", "rollback", completions);
        addCompletionIfPermitted(sender, "coreprotect.restore", "restore", completions);
        addCompletionIfPermitted(sender, "coreprotect.lookup", "lookup", completions);
        addCompletionIfPermitted(sender, "coreprotect.purge", "purge", completions);
        addCompletionIfPermitted(sender, "coreprotect.reload", "reload", completions);
        addCompletionIfPermitted(sender, "coreprotect.status", "status", completions);
        if ((sender instanceof Player || sender instanceof BlockCommandSender)
                && (sender.hasPermission("coreprotect.lookup") || sender.hasPermission("coreprotect.lookup.near"))) {
            completions.add("near");
        }
        addCompletionIfPermitted(sender, "coreprotect.restore", "undo", completions);
        if (sender instanceof Player) {
            addCompletionIfPermitted(sender, "coreprotect.teleport", "teleport", completions);
            addCompletionIfPermitted(sender, "coreprotect.give", "give", completions);
            if (hasActivePreview(sender) && (sender.hasPermission("coreprotect.rollback") || sender.hasPermission("coreprotect.restore"))) {
                completions.add("apply");
                completions.add("cancel");
            }
        }
        if (sender instanceof ConsoleCommandSender) {
            addCompletionIfPermitted(sender, "coreprotect.consumer", "consumer", completions);
        }
        if (Config.getGlobal().NETWORK_DEBUG) {
            addCompletionIfPermitted(sender, "coreprotect.networking", "network-debug", completions);
        }

        return StringUtil.copyPartialMatches(arg, completions, new ArrayList<>(completions.size()));
    }

    private void addCompletionIfPermitted(CommandSender sender, String permission, String completion, List<String> completions) {
        if (sender.hasPermission(permission)) {
            completions.add(completion);
        }
    }

    private boolean hasActivePreview(CommandSender sender) {
        List<Object> rollback = ConfigHandler.lastRollback.get(sender.getName());
        return rollback != null && rollback.size() > 2 && rollback.get(2) instanceof String[] arguments && Arrays.asList(arguments).contains("#preview");
    }

    private boolean hasPagePermission(CommandSender sender) {
        return sender.hasPermission("coreprotect.lookup") || sender.hasPermission("coreprotect.lookup.near") || sender.hasPermission("coreprotect.inspect");
    }

    private boolean hasLookupCommand(String cmd, CommandSender sender) {
        return (sender.hasPermission("coreprotect.lookup") && (cmd.equals("l") || cmd.equals("lookup"))) || (sender.hasPermission("coreprotect.rollback") && (cmd.equals("rollback") || cmd.equals("rb") || cmd.equals("ro"))) || (sender.hasPermission("coreprotect.restore") && (cmd.equals("restore") || cmd.equals("rs") || cmd.equals("re")));
    }

    private boolean isActionParam(String lastArg, String currentArg) {
        return lastArg.equals("a:") || lastArg.equals("action:") || currentArg.startsWith("a:") || currentArg.startsWith("action:");
    }

    private boolean isUserParam(String lastArg, String currentArg) {
        return lastArg.equals("u:") || lastArg.equals("user:") || lastArg.equals("users:") || lastArg.equals("p:") || currentArg.startsWith("u:") || currentArg.startsWith("user:") || currentArg.startsWith("users:") || currentArg.startsWith("p:");
    }

    private boolean isTimeParam(String lastArg, String currentArg) {
        return lastArg.equals("t:") || lastArg.equals("time:") || currentArg.startsWith("t:") || currentArg.startsWith("time:");
    }

    private boolean isPageParam(String lastArg, String currentArg) {
        return lastArg.equals("page:") || currentArg.startsWith("page:");
    }

    private boolean isRadiusParam(String lastArg, String currentArg) {
        return lastArg.equals("r:") || lastArg.equals("radius:") || currentArg.startsWith("r:") || currentArg.startsWith("radius:");
    }

    private boolean isMaterialParam(String lastArg, String currentArg) {
        return lastArg.equals("i:") || lastArg.equals("include:") || lastArg.equals("item:") || lastArg.equals("items:") || lastArg.equals("b:") || lastArg.equals("block:") || lastArg.equals("blocks:") || currentArg.startsWith("i:") || currentArg.startsWith("include:") || currentArg.startsWith("item:") || currentArg.startsWith("items:") || currentArg.startsWith("b:") || currentArg.startsWith("block:") || currentArg.startsWith("blocks:") || lastArg.equals("e:") || lastArg.equals("exclude:") || currentArg.startsWith("e:") || currentArg.startsWith("exclude:");
    }

    // Only user/material lists continue across spaces after a comma. Action lists do not.
    private String pendingParameter(String[] args) {
        String pending = "";
        for (int index = 1; index < args.length - 1; index++) {
            String token = args[index].toLowerCase(Locale.ROOT).trim();
            int colon = token.indexOf(':');
            String marker = colon >= 0 ? token.substring(0, colon + 1) : pending;
            String value = colon >= 0 ? token.substring(colon + 1) : token;
            boolean list = isUserParam("", marker) || isMaterialParam("", marker);
            boolean parameter = list || isActionParam("", marker) || isTimeParam("", marker) || isRadiusParam("", marker) || isPageParam("", marker);
            pending = parameter && ((colon >= 0 && value.isEmpty()) || (list && value.endsWith(","))) ? marker : "";
        }
        return pending;
    }

    private List<String> onlineUsers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    // Bukkit replaces the current space-delimited token, including its marker/list prefix.
    private List<String> completeValues(String currentArg, List<String> values, boolean allowList) {
        int colon = currentArg.indexOf(':');
        int start = colon + 1;
        if (allowList) {
            start = Math.max(start, currentArg.lastIndexOf(',') + 1);
        }
        else if (currentArg.indexOf(',') >= 0) {
            return List.of();
        }
        String prefix = currentArg.substring(0, start);
        String partial = currentArg.substring(start);
        List<String> matches = StringUtil.copyPartialMatches(partial, values, new ArrayList<>());
        return matches.stream().distinct().map(value -> prefix + value).collect(Collectors.toList());
    }

    private List<String> handleTeleportCompletions(String currentArg) {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            boolean singleToken = world.getName().chars().noneMatch(Character::isWhitespace);
            if (singleToken) {
                worlds.add(world.getName());
            }
            Integer id = ConfigHandler.worlds.get(world.getName());
            if (id != null && (!singleToken || StringUtil.startsWithIgnoreCase(currentArg, "wid:"))) {
                worlds.add("wid:" + id);
            }
        }
        return StringUtil.copyPartialMatches(currentArg, worlds, new ArrayList<>());
    }

    private static class ParamState {
        boolean hasUser;
        boolean hasAction;
        boolean hasInclude;
        boolean hasExclude;
        boolean hasFilter;
        boolean hasRadius;
        boolean hasTime;
        boolean hasContainer;
        boolean hasCount;
        boolean hasSummary;
        boolean hasRollbackState;
        boolean hasPreview;
        boolean hasVerbose;
        boolean hasSilent;
        boolean hasOptimize;
        boolean hasPage;
        boolean validContainer;
        boolean pageLookup;
    }

    private ParamState getParamState(CommandSender sender, String[] args) {
        ParamState state = new ParamState();
        String senderName = sender.getName();

        if (ConfigHandler.lookupType.get(senderName) != null && ConfigHandler.lookupPage.get(senderName) != null) {
            state.pageLookup = true;
        }

        for (int i = 1; i < args.length - 1; i++) {
            String arg = args[i].toLowerCase(Locale.ROOT);
            if (arg.equals("#container")) {
                state.hasContainer = true;
            }
            else if (arg.equals("#count") || arg.equals("#sum")) {
                state.hasCount = true;
            }
            else if (arg.equals("#summary")) {
                state.hasSummary = true;
            }
            else if (RollbackStateParser.isModifier(arg)) {
                state.hasRollbackState = true;
            }
            else if (arg.equals("#preview")) {
                state.hasPreview = true;
            }
            else if (arg.equals("#verbose") || arg.equals("#v") || arg.equals("verbose") || arg.equals("v") || arg.equals("noisy") || arg.equals("n")) {
                state.hasVerbose = true;
            }
            else if (arg.equals("#silent")) {
                state.hasSilent = true;
            }
            else if (arg.equals("#optimize")) {
                state.hasOptimize = true;
            }
            else if (arg.startsWith("f:") || arg.startsWith("filter:")) {
                state.hasFilter = true;
            }
            else if ((!arg.contains(":") && !args[i - 1].contains(":") && args.length > (i + 1)) || arg.contains("u:") || arg.contains("user:") || arg.contains("users:") || arg.contains("p:")) {
                state.hasUser = true;
            }
            else if (arg.contains("page:")) {
                state.hasPage = true;
            }
            else if (arg.contains("a:") || arg.contains("action:")) {
                state.hasAction = true;
            }
            else if (arg.contains("i:") || arg.contains("include:") || arg.contains("item:") || arg.contains("items:") || arg.contains("b:") || arg.contains("block:") || arg.contains("blocks:")) {
                state.hasInclude = true;
            }
            else if (arg.contains("t:") || arg.contains("time:")) {
                state.hasTime = true;
            }
            else if (arg.contains("e:") || arg.contains("exclude:")) {
                state.hasExclude = true;
            }
            else if (arg.contains("r:") || arg.contains("radius:")) {
                state.hasRadius = true;
            }
        }

        if (!state.hasContainer) {
            Integer lookupType = ConfigHandler.lookupType.get(senderName);
            if (lookupType != null) {
                if (lookupType == 1) {
                    state.validContainer = true;
                }
                else if (lookupType == 5) {
                    List<String> lookupUsers = ConfigHandler.lookupUlist.get(senderName);
                    if (lookupUsers != null && lookupUsers.contains("#container")) {
                        state.validContainer = true;
                    }
                }
            }
        }

        return state;
    }

    private List<String> handleActionParamCompletions(CommandSender sender, String command, String currentArg) {
        boolean lookup = command.equals("lookup") || command.equals("l");
        List<String> actions = new ArrayList<>();
        for (String action : ACTIONS) {
            String category = action.replaceFirst("^[+-]", "");
            if (lookup) {
                String permission = category.equals("skill") ? "command" : category.equals("ping") ? "session" : List.of("hit", "knockout", "pvp-kill").contains(category) ? "pvp" : category;
                if (!sender.hasPermission("coreprotect.lookup." + permission)) {
                    continue;
                }
            }
            else if (!List.of("block", "kill", "spawn", "container", "inventory").contains(category)) {
                continue;
            }
            actions.add(action);
        }
        List<String> completions = completeValues(currentArg, actions, lookup);
        if (lookup) {
            completions.removeIf(value -> {
                String argument = value.contains(":") ? value : "a:" + value;
                ActionParser.ParseResult parsed = ActionParser.parseActions(new String[] { "lookup", argument }, true);
                return parsed.isSkillLookup() && parsed.getActions().stream().anyMatch(action -> action != LookupActions.COMMAND);
            });
        }
        return completions;
    }

    private List<String> handleTimeParamCompletions(String currentArg, String lastArg) {
        String filter = lastArg;
        String arg = "";
        if (currentArg.contains(":")) {
            String[] split = currentArg.split(":", 2);
            filter = split[0] + ":";
            if (split.length > 1) {
                arg = split[1];
            }
        }
        else {
            filter = "";
            arg = currentArg;
        }

        List<String> completions = new ArrayList<>();
        if (arg.chars().allMatch(Character::isDigit)) {
            boolean addNumbers = true;
            if (currentArg.length() > 0) {
                char lastChar = currentArg.charAt(currentArg.length() - 1);
                if (Character.isDigit(lastChar)) {
                    completions.addAll(Arrays.asList(TIMES));
                    addNumbers = false;
                }
            }
            if (addNumbers) {
                completions.addAll(Arrays.asList(NUMBERS));
            }
        }

        completions = new ArrayList<>(completions);
        for (int index = 0; index < completions.size(); index++) {
            completions.set(index, filter + arg + completions.get(index));
        }

        return StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList<>(completions.size()));
    }

    private List<String> handlePageParamCompletions(String currentArg, String lastArg) {
        String filter = lastArg;
        String arg = "";
        if (currentArg.contains(":")) {
            String[] split = currentArg.split(":", 2);
            filter = split[0] + ":";
            if (split.length > 1) {
                arg = split[1];
            }
        }
        else {
            filter = "";
            arg = currentArg;
        }

        if (arg.chars().allMatch(Character::isDigit)) {
            List<String> completions = new ArrayList<>(Arrays.asList(NUMBERS));
            if (arg.length() < 1) {
                for (int index = 0; index < completions.size(); index++) {
                    completions.set(index, filter + arg + completions.get(index));
                }
                if (arg.length() == 0) {
                    completions.remove(0);
                }
            }
            return StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList<>(completions.size()));
        }

        return List.of();
    }

    private List<String> handleRadiusParamCompletions(String currentArg, String lastArg, String baseCommand) {
        String filter = lastArg;
        String arg = "";
        if (currentArg.contains(":")) {
            String[] split = currentArg.split(":", 2);
            filter = split[0] + ":";
            if (split.length > 1) {
                arg = split[1];
            }
        }
        else {
            filter = "";
            arg = currentArg;
        }

        if (!baseCommand.equals("purge") && arg.chars().allMatch(Character::isDigit)) {
            List<String> completions = new ArrayList<>(Arrays.asList(NUMBERS));
            if (arg.length() < 2) {
                for (int index = 0; index < completions.size(); index++) {
                    completions.set(index, filter + arg + completions.get(index));
                }
            }
            return StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList<>(completions.size()));
        }
        else if (baseCommand.equals("purge") || arg.startsWith("#")) {
            ArrayList<String> params = new ArrayList<>();
            params.add("#global");
            if (!baseCommand.equals("purge") && Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                params.add("#worldedit");
            }
            List<World> worlds = Bukkit.getServer().getWorlds();
            for (World world : worlds) {
                params.add("#" + world.getName());
            }
            List<String> completions = new ArrayList<>(params);
            for (int index = 0; index < completions.size(); index++) {
                completions.set(index, filter + completions.get(index));
            }
            return StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList<>(completions.size()));
        }

        return List.of();
    }

    private List<String> handleMaterialParamCompletions(String currentArg, String lastArg, boolean purge) {
        String marker = currentArg.contains(":") ? currentArg.substring(0, currentArg.indexOf(':') + 1).toLowerCase(Locale.ROOT) : lastArg;
        boolean exclude = marker.equals("e:") || marker.equals("exclude:");
        if (purge && exclude) {
            return List.of();
        }
        initializeMaterialsIfNeeded();
        List<String> completions = new ArrayList<>(materials);
        if (!purge) {
            // The parser only accepts entity types already recorded in its identifier cache.
            for (EntityType type : EntityType.values()) {
                String name = type.name().toLowerCase(Locale.ROOT);
                if (type != EntityType.UNKNOWN && ConfigHandler.entities.containsKey(name)) {
                    completions.add(name);
                }
            }
            if (exclude) {
                completions.addAll(onlineUsers());
            }
        }
        return completeValues(currentArg, completions, true);
    }

    private void initializeMaterialsIfNeeded() {
        if (materials == null) {
            List<Material> addList = Arrays.asList(Material.ARMOR_STAND);
            List<Material> excludeList = Arrays.asList();
            Set<String> materialList = new HashSet<>();

            Material[] materialValues = Material.values();
            for (Material material : materialValues) {
                if (material.isBlock() || material.isItem()) {
                    materialList.add(material.name().toLowerCase(Locale.ROOT));
                }
            }
            for (Material exclude : excludeList) {
                materialList.remove(exclude.name().toLowerCase(Locale.ROOT));
            }
            for (Material add : addList) {
                materialList.add(add.name().toLowerCase(Locale.ROOT));
            }

            // add custom tags
            for (String tag : CommandParser.getTags().keySet()) {
                materialList.add(tag);
            }

            materials = new ArrayList<>(materialList);
        }
    }

    private List<String> handleSecondArgCompletions(CommandSender sender, String cmd0, String cmd1, ParamState paramState) {
        String argument0 = cmd0.toLowerCase(Locale.ROOT);
        String argument1 = cmd1.toLowerCase(Locale.ROOT);

        if (argument0.equals("help") && sender.hasPermission("coreprotect.help")) {
            List<String> completions = new ArrayList<>(Arrays.asList(HELP));
            return StringUtil.copyPartialMatches(argument1, completions, new ArrayList<>(completions.size()));
        }
        else if (hasLookupCommand(argument0, sender)) {
            List<String> completions = new ArrayList<>(filterParams(true, argument0, argument1, paramState, sender instanceof Player));
            completions.addAll(Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().toLowerCase(Locale.ROOT).startsWith(argument1)).map(Player::getName).collect(Collectors.toList()));
            return StringUtil.copyPartialMatches(argument1, completions, new ArrayList<>(completions.size()));
        }

        return List.of();
    }

    private List<String> handlePurgeCompletions(String currentArg, ParamState state) {
        List<String> params = new ArrayList<>();
        if (!state.hasTime) {
            params.addAll(List.of("t:", "time:"));
        }
        if (!state.hasRadius) {
            params.addAll(List.of("r:", "radius:"));
        }
        if (!state.hasInclude) {
            params.addAll(List.of("i:", "include:"));
        }
        if (!state.hasOptimize) {
            params.add("#optimize");
        }
        return StringUtil.copyPartialMatches(currentArg, params, new ArrayList<>());
    }

    private List<String> handleGenericLookupCompletions(String cmd, String currentArg, ParamState paramState, boolean player) {
        ArrayList<String> params = filterParams(false, cmd, currentArg, paramState, player);
        List<String> completions = new ArrayList<>(params);
        return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>(completions.size()));
    }

    private ArrayList<String> filterParams(boolean firstParam, String lastArgument, String argument, ParamState state, boolean player) {
        ArrayList<String> params = new ArrayList<>();
        for (String param : PARAMS) {
            if (param.equals("user:") && !state.hasUser) {
                params.add(param);
            }
            else if (param.equals("action:") && !state.hasAction) {
                params.add(param);
            }
            else if (param.equals("include:") && !state.hasInclude) {
                params.add(param);
            }
            else if (param.equals("exclude:") && !state.hasExclude) {
                params.add(param);
            }
            else if (param.equals("filter:") && !state.hasFilter && (lastArgument.equals("l") || lastArgument.equals("lookup"))) {
                params.add(param);
            }
            else if (param.equals("radius:") && !state.hasRadius) {
                params.add(param);
            }
            else if (param.equals("time:") && !state.hasTime) {
                params.add(param);
            }
            else if (param.equals("#container") && !state.hasContainer && !state.hasRadius && state.validContainer) {
                params.add(param);
            }
        }
        if (firstParam && state.pageLookup && (lastArgument.equals("l") || lastArgument.equals("lookup"))) {
            params.add("page:");
        }
        if (argument.startsWith("#")) {
            if (!state.hasCount && !state.hasSummary && (lastArgument.equals("l") || lastArgument.equals("lookup"))) {
                params.add("#summary");
            }
            if (!state.hasRollbackState && (lastArgument.equals("l") || lastArgument.equals("lookup"))) {
                params.add("#rolledback");
                params.add("#restored");
            }
            if (!state.hasPreview && player && !lastArgument.equals("lookup") && !lastArgument.equals("l")) {
                params.add("#preview");
            }
            if (!state.hasVerbose) {
                params.add("#verbose");
            }
            if (!state.hasSilent) {
                params.add("#silent");
            }
        }

        return params;
    }
}

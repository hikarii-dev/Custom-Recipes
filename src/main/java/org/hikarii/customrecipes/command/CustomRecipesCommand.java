package org.hikarii.customrecipes.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.subcommands.DeleteSubcommand;
import org.hikarii.customrecipes.command.subcommands.DisableSubcommand;
import org.hikarii.customrecipes.command.subcommands.EnableSubcommand;
import org.hikarii.customrecipes.command.subcommands.GuiSubcommand;
import org.hikarii.customrecipes.command.subcommands.ListSubcommand;
import org.hikarii.customrecipes.command.subcommands.ReloadSubcommand;
import org.hikarii.customrecipes.util.MessageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Main command executor for /customrecipes
 */
public class CustomRecipesCommand implements CommandExecutor, TabCompleter {

    private final CustomRecipes plugin;
    private final Map<String, SubCommand> subCommands;

    public CustomRecipesCommand(CustomRecipes plugin) {
        this.plugin = plugin;
        this.subCommands = new HashMap<>();

        // Register subcommands
        registerSubcommand(new ReloadSubcommand(plugin));
        registerSubcommand(new ListSubcommand(plugin));
        registerSubcommand(new GuiSubcommand(plugin));
        registerSubcommand(new EnableSubcommand(plugin));
        registerSubcommand(new DisableSubcommand(plugin));
        registerSubcommand(new DeleteSubcommand(plugin));
    }

    /**
     * Registers a subcommand
     *
     * @param subCommand the subcommand to register
     */
    private void registerSubcommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);

        // Also register aliases
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        // No arguments - show help
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        // Get subcommand
        String subCommandName = args[0].toLowerCase();

        // Help command
        if (subCommandName.equals("help")) {
            showHelp(sender);
            return true;
        }

        // Find and execute subcommand
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand != null) {
            // Check permission
            if (!sender.hasPermission(subCommand.getPermission())) {
                MessageUtil.sendError(sender, "You don't have permission to use this command.");
                return true;
            }

            // Execute subcommand
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            subCommand.execute(sender, subArgs);
            return true;
        }

        // Unknown subcommand
        MessageUtil.sendError(sender, "Unknown subcommand. Use <yellow>/customrecipes help<red> for help.");
        return true;
    }

    /**
     * Shows help message
     *
     * @param sender the command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.colorize(
                "<gradient:#00D4FF:#7B2CBF>====== CustomRecipes Help ======</gradient>"
        ));

        sender.sendMessage(Component.empty());

        // Show available subcommands
        Set<SubCommand> uniqueCommands = new HashSet<>(subCommands.values());
        for (SubCommand subCommand : uniqueCommands) {
            if (sender.hasPermission(subCommand.getPermission())) {
                Component commandComponent = Component.text("  /" + subCommand.getUsage(), NamedTextColor.AQUA);
                Component descComponent = Component.text(" - " + subCommand.getDescription(), NamedTextColor.GRAY);
                sender.sendMessage(commandComponent.append(descComponent));
            }
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(MessageUtil.colorize("<gray>Use <yellow>/cr<gray> as a shortcut"));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommand names
            completions.add("help");

            for (SubCommand subCommand : new HashSet<>(subCommands.values())) {
                if (sender.hasPermission(subCommand.getPermission())) {
                    completions.add(subCommand.getName());
                }
            }

            // Filter based on what user typed
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .sorted()
                    .toList();
        }

        if (args.length > 1) {
            // Tab complete for subcommands
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.tabComplete(sender, subArgs);
            }
        }

        return completions;
    }

    /**
     * Interface for subcommands
     */
    public interface SubCommand {
        String getName();
        List<String> getAliases();
        String getDescription();
        String getUsage();
        String getPermission();
        void execute(CommandSender sender, String[] args);
        List<String> tabComplete(CommandSender sender, String[] args);
    }
}
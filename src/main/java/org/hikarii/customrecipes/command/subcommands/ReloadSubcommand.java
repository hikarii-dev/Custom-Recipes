package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.Collections;
import java.util.List;

/**
 * Subcommand for reloading plugin configuration
 */
public class ReloadSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public ReloadSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> getAliases() {
        return List.of("rl", "refresh");
    }

    @Override
    public String getDescription() {
        return "Reload plugin configuration and recipes";
    }

    @Override
    public String getUsage() {
        return "customrecipes reload";
    }

    @Override
    public String getPermission() {
        return "customrecipes.reload";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        MessageUtil.sendInfo(sender, "Reloading CustomRecipes configuration...");

        long startTime = System.currentTimeMillis();

        // Reload configuration
        boolean success = plugin.loadConfiguration();

        long duration = System.currentTimeMillis() - startTime;

        if (success) {
            int recipeCount = plugin.getRecipeManager().getRecipeCount();
            MessageUtil.sendSuccess(sender,
                    "Successfully reloaded! Loaded <white>" + recipeCount + "<green> recipes in <white>" + duration + "ms");
        } else {
            MessageUtil.sendError(sender, "Failed to reload configuration. Check console for errors.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
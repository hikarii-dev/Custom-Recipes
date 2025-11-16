package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand for enabling recipes
 */
public class EnableSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public EnableSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public List<String> getAliases() {
        return List.of("on", "activate");
    }

    @Override
    public String getDescription() {
        return "Enable a recipe";
    }

    @Override
    public String getUsage() {
        return "customrecipes enable <recipe>";
    }

    @Override
    public String getPermission() {
        return "customrecipes.manage";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /cr enable <recipe>");
            return;
        }

        String recipeKey = args[0];

        if (!plugin.getRecipeManager().hasRecipe(recipeKey)) {
            MessageUtil.sendError(sender, "Recipe '" + recipeKey + "' does not exist.");
            return;
        }

        if (plugin.getRecipeManager().isRecipeEnabled(recipeKey)) {
            MessageUtil.sendWarning(sender, "Recipe '" + recipeKey + "' is already enabled.");
            return;
        }

        if (plugin.getRecipeManager().enableRecipe(recipeKey)) {
            MessageUtil.sendSuccess(sender, "Enabled recipe: <white>" + recipeKey);

            // Update config
            plugin.getConfigManager().addEnabledRecipe(recipeKey);
        } else {
            MessageUtil.sendError(sender, "Failed to enable recipe '" + recipeKey + "'.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> disabled = new ArrayList<>();
            plugin.getRecipeManager().getAllRecipes().forEach(recipe -> {
                if (!plugin.getRecipeManager().isRecipeEnabled(recipe.getKey())) {
                    disabled.add(recipe.getKey());
                }
            });

            String input = args[0].toLowerCase();
            return disabled.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
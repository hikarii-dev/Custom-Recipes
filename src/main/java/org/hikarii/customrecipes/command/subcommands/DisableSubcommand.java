package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Subcommand for disabling recipes
 */
public class DisableSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public DisableSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public List<String> getAliases() {
        return List.of("off", "deactivate");
    }

    @Override
    public String getDescription() {
        return "Disable a recipe";
    }

    @Override
    public String getUsage() {
        return "customrecipes disable <recipe>";
    }

    @Override
    public String getPermission() {
        return "customrecipes.manage";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /cr disable <recipe>");
            return;
        }

        String recipeKey = args[0];

        if (!plugin.getRecipeManager().hasRecipe(recipeKey)) {
            MessageUtil.sendError(sender, "Recipe '" + recipeKey + "' does not exist.");
            return;
        }

        if (!plugin.getRecipeManager().isRecipeEnabled(recipeKey)) {
            MessageUtil.sendWarning(sender, "Recipe '" + recipeKey + "' is already disabled.");
            return;
        }

        if (plugin.getRecipeManager().disableRecipe(recipeKey)) {
            MessageUtil.sendSuccess(sender, "Disabled recipe: <white>" + recipeKey);

            // Update config
            plugin.getConfigManager().removeEnabledRecipe(recipeKey);
        } else {
            MessageUtil.sendError(sender, "Failed to disable recipe '" + recipeKey + "'.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> enabled = new ArrayList<>();
            plugin.getRecipeManager().getAllRecipes().forEach(recipe -> {
                if (plugin.getRecipeManager().isRecipeEnabled(recipe.getKey())) {
                    enabled.add(recipe.getKey());
                }
            });

            String input = args[0].toLowerCase();
            return enabled.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
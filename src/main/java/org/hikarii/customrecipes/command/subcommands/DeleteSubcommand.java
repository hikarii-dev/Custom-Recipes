package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Subcommand for deleting recipes
 */
public class DeleteSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public DeleteSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public List<String> getAliases() {
        return List.of("remove", "del");
    }

    @Override
    public String getDescription() {
        return "Delete a recipe";
    }

    @Override
    public String getUsage() {
        return "customrecipes delete <recipe>";
    }

    @Override
    public String getPermission() {
        return "customrecipes.manage";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtil.sendError(sender, "Usage: /cr delete <recipe>");
            return;
        }

        String recipeKey = args[0];

        if (!plugin.getRecipeManager().hasRecipe(recipeKey)) {
            MessageUtil.sendError(sender, "Recipe '" + recipeKey + "' does not exist.");
            return;
        }

        if (plugin.getRecipeManager().deleteRecipe(recipeKey)) {
            MessageUtil.sendSuccess(sender, "Deleted recipe: <white>" + recipeKey);
            MessageUtil.sendWarning(sender, "Note: Recipe is removed from memory. To remove from config file, use /cr reload or edit config.yml manually.");

            // Remove from enabled list
            plugin.getConfigManager().removeEnabledRecipe(recipeKey);
        } else {
            MessageUtil.sendError(sender, "Failed to delete recipe '" + recipeKey + "'.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return plugin.getRecipeManager().getAllRecipes().stream()
                    .map(recipe -> recipe.getKey())
                    .filter(key -> key.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
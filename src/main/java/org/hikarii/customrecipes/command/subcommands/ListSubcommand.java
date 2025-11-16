package org.hikarii.customrecipes.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Subcommand for listing all custom recipes
 */
public class ListSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public ListSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public List<String> getAliases() {
        return List.of("l", "recipes");
    }

    @Override
    public String getDescription() {
        return "List all loaded custom recipes";
    }

    @Override
    public String getUsage() {
        return "customrecipes list";
    }

    @Override
    public String getPermission() {
        return "customrecipes.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Collection<CustomRecipe> recipes = plugin.getRecipeManager().getAllRecipes();

        if (recipes.isEmpty()) {
            MessageUtil.sendWarning(sender, "No custom recipes are loaded.");
            return;
        }

        sender.sendMessage(MessageUtil.colorize(
                "<gradient:#00D4FF:#7B2CBF>====== Custom Recipes (" + recipes.size() + ") ======</gradient>"
        ));
        sender.sendMessage(Component.empty());

        for (CustomRecipe recipe : recipes) {
            // Create recipe info line
            Component keyComponent = Component.text("  • ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(recipe.getKey(), NamedTextColor.AQUA));

            Component typeComponent = Component.text(" [" + recipe.getType() + "]", NamedTextColor.GRAY);

            Component resultComponent = Component.text(" → ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(
                            MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                            NamedTextColor.GREEN
                    ));

            if (recipe.getResultAmount() > 1) {
                resultComponent = resultComponent.append(
                        Component.text(" x" + recipe.getResultAmount(), NamedTextColor.YELLOW)
                );
            }

            sender.sendMessage(keyComponent.append(typeComponent).append(resultComponent));
        }

        sender.sendMessage(Component.empty());
        sender.sendMessage(MessageUtil.colorize(
                "<gray>Use <yellow>/cr gui<gray> to manage recipes in a GUI"
        ));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
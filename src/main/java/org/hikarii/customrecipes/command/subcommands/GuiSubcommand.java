package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.gui.RecipeListGUI;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.Collections;
import java.util.List;

/**
 * Subcommand for opening recipe management GUI
 */
public class GuiSubcommand implements CustomRecipesCommand.SubCommand {

    private final CustomRecipes plugin;

    public GuiSubcommand(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public List<String> getAliases() {
        return List.of("menu", "open");
    }

    @Override
    public String getDescription() {
        return "Open recipe management GUI";
    }

    @Override
    public String getUsage() {
        return "customrecipes gui";
    }

    @Override
    public String getPermission() {
        return "customrecipes.gui";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendError(sender, "Only players can use this command.");
            return;
        }

        // Open recipe list GUI
        RecipeListGUI gui = new RecipeListGUI(plugin, player);
        gui.open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
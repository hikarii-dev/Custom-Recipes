package org.hikarii.customrecipes.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.gui.PlayerRecipeListGUI;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.Collections;
import java.util.List;

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
        return List.of("recipes", "browse", "view");
    }

    @Override
    public String getDescription() {
        return "Browse all custom recipes";
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
        if (!(sender instanceof Player player)) {
            MessageUtil.sendError(sender, "Only players can use this command.");
            return;
        }
        PlayerRecipeListGUI gui = new PlayerRecipeListGUI(plugin, player);
        gui.open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}

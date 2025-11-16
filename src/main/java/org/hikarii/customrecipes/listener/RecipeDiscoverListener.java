package org.hikarii.customrecipes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.hikarii.customrecipes.CustomRecipes;

/**
 * Listens for recipe discovery events
 */
public class RecipeDiscoverListener implements Listener {

    private final CustomRecipes plugin;

    public RecipeDiscoverListener(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRecipeDiscover(PlayerRecipeDiscoverEvent event) {
        String recipeKey = event.getRecipe().getKey();

        // Check if this is one of our custom recipes
        if (recipeKey.startsWith(plugin.getName().toLowerCase() + ":")) {
            String customRecipeKey = recipeKey.split(":", 2)[1];

            plugin.debug("Player " + event.getPlayer().getName() +
                    " discovered custom recipe: " + customRecipeKey);
        }
    }
}
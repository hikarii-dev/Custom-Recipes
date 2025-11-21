package org.hikarii.customrecipes.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.bukkit.World;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeInfo;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeManager;
import java.util.List;

public class RecipeHidingListener implements Listener {
    private final CustomRecipes plugin;

    public RecipeHidingListener(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        NamespacedKey key = null;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            key = shapedRecipe.getKey();
        } else if (recipe instanceof org.bukkit.inventory.ShapelessRecipe shapelessRecipe) {
            key = shapelessRecipe.getKey();
        } else {
            return;
        }

        if (key.getNamespace().equals("minecraft")) {
            String recipeKey = key.getKey();
            if (plugin.getVanillaRecipeManager().isRecipeDisabled(recipeKey)) {
                CraftingInventory inventory = event.getInventory();
                inventory.setResult(null);
                plugin.debug("Blocked disabled vanilla recipe: " + recipeKey);
                return;
            }
        }
        if (!key.getNamespace().equals(plugin.getName().toLowerCase())) {
            return;
        }

        String recipeKey = key.getKey();
        CustomRecipe customRecipe = plugin.getRecipeManager().getRecipe(recipeKey);
        if (customRecipe == null || !customRecipe.isHidden()) {
            return;
        }

        if (event.getView().getPlayer() instanceof Player player) {
            if (!plugin.getRecipeWorldManager().isRecipeAllowedForPlayer(recipeKey, player)) {
                CraftingInventory inventory = event.getInventory();
                inventory.setResult(null);
                plugin.debug("Recipe " + recipeKey + " is disabled in world " + player.getWorld().getName());
                return;
            }
            if (customRecipe.isHidden() && !plugin.getRecipeDataManager().hasDiscovered(player, recipeKey)) {
                CraftingInventory inventory = event.getInventory();
                inventory.setResult(null);
                plugin.debug("Hiding recipe " + recipeKey + " from " + player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Recipe recipe = event.getRecipe();
        NamespacedKey key = null;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            key = shapedRecipe.getKey();
        } else if (recipe instanceof org.bukkit.inventory.ShapelessRecipe shapelessRecipe) {
            key = shapelessRecipe.getKey();
        } else {
            return;
        }

        if (!key.getNamespace().equals(plugin.getName().toLowerCase())) {
            return;
        }

        String recipeKey = key.getKey();
        CustomRecipe customRecipe = plugin.getRecipeManager().getRecipe(recipeKey);
        if (customRecipe == null || !customRecipe.isHidden()) {
            return;
        }

        if (event.getWhoClicked() instanceof Player player) {
            if (!plugin.getRecipeDataManager().hasDiscovered(player, recipeKey)) {
                plugin.getRecipeDataManager().markDiscovered(player, recipeKey);
            }
        }
    }
}
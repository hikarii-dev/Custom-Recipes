package org.hikarii.customrecipes.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.NamespacedKey;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import java.util.List;

public class RecipeAmountListener implements Listener {
    private final CustomRecipes plugin;

    public RecipeAmountListener(CustomRecipes plugin) {
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
        }

        if (key == null || !key.getNamespace().equals(plugin.getName().toLowerCase())) {
            return;
        }

        String recipeKey = key.getKey();
        CustomRecipe customRecipe = plugin.getRecipeManager().getRecipe(recipeKey);
        if (customRecipe == null || customRecipe.getType() != RecipeType.SHAPED) {
            return;
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        List<RecipeIngredient> ingredients = customRecipe.getRecipeData().ingredients();
        boolean ignoreMetadata = plugin.getConfig().getBoolean("ignore-metadata", false);
        for (int i = 0; i < Math.min(matrix.length, ingredients.size()); i++) {
            RecipeIngredient required = ingredients.get(i);
            ItemStack actual = matrix[i];
            if (required.material() == Material.AIR) {
                continue;
            }

            ItemStack requiredItem = required.hasExactItem() ? required.getExactItem() : new ItemStack(required.material(), required.amount());
            if (!org.hikarii.customrecipes.recipe.data.IngredientMatcher.matches(requiredItem, actual, ignoreMetadata)) {
                inventory.setResult(null);
                return;
            }

            if (actual.getAmount() < required.amount()) {
                inventory.setResult(null);
                return;
            }
        }
    }
}
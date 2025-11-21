package org.hikarii.customrecipes.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import java.util.List;

public class RecipePreviewListener implements Listener {
    private final CustomRecipes plugin;

    public RecipePreviewListener(CustomRecipes plugin) {
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
        CustomRecipe exactMatchRecipe = findExactMatchRecipe(matrix);
        if (exactMatchRecipe != null) {
            if (!exactMatchRecipe.getKey().equals(recipeKey)) {
                customRecipe = exactMatchRecipe;
                boolean useCraftedNames = plugin.isUseCraftedCustomNames();
                boolean keepSpawnEggNames = plugin.isKeepSpawnEggNames();
                ItemStack correctResult = exactMatchRecipe.createResult(useCraftedNames, keepSpawnEggNames);
                inventory.setResult(correctResult);
            }
        }

        List<RecipeIngredient> ingredients = customRecipe.getRecipeData().ingredients();
        for (int i = 0; i < Math.min(matrix.length, ingredients.size()); i++) {
            RecipeIngredient required = ingredients.get(i);
            ItemStack actual = matrix[i];
            if (required.material() == Material.AIR) {
                continue;
            }

            if (actual == null || actual.getType() != required.material()) {
                continue;
            }

            if (required.hasExactItem()) {
                ItemStack exactItem = required.getExactItem();
                if (exactItem.getType() == Material.ENCHANTED_BOOK &&
                    exactItem.hasItemMeta() && actual.hasItemMeta()) {
                    if (exactItem.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta &&
                        actual.getItemMeta() instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                        org.bukkit.inventory.meta.EnchantmentStorageMeta exactBook =
                            (org.bukkit.inventory.meta.EnchantmentStorageMeta) exactItem.getItemMeta();
                        org.bukkit.inventory.meta.EnchantmentStorageMeta actualBook =
                            (org.bukkit.inventory.meta.EnchantmentStorageMeta) actual.getItemMeta();
                        if (!actualBook.getStoredEnchants().equals(exactBook.getStoredEnchants())) {
                            inventory.setResult(null);
                            return;
                        }
                    }
                }
                if (exactItem.hasItemMeta() && actual.hasItemMeta()) {
                    org.bukkit.inventory.meta.ItemMeta exactMeta = exactItem.getItemMeta();
                    org.bukkit.inventory.meta.ItemMeta actualMeta = actual.getItemMeta();
                    if (exactMeta instanceof org.bukkit.inventory.meta.Damageable &&
                        actualMeta instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable exactDamageable =
                            (org.bukkit.inventory.meta.Damageable) exactMeta;
                        org.bukkit.inventory.meta.Damageable actualDamageable =
                            (org.bukkit.inventory.meta.Damageable) actualMeta;
                        if (exactDamageable.hasDamage() &&
                            exactDamageable.getDamage() != actualDamageable.getDamage()) {
                            inventory.setResult(null);
                            return;
                        }
                    }
                    if (!actualMeta.getEnchants().equals(exactMeta.getEnchants())) {
                        inventory.setResult(null);
                        return;
                    }
                }
            }
        }
    }

    private CustomRecipe findExactMatchRecipe(ItemStack[] matrix) {
        if (matrix == null || matrix.length != 9) {
            return null;
        }

        for (CustomRecipe recipe : plugin.getRecipeManager().getAllRecipes()) {
            if (recipe.getType() != RecipeType.SHAPED) {
                continue;
            }

            List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();
            if (ingredients.size() != 9) {
                continue;
            }

            boolean hasExactIngredient = false;
            for (RecipeIngredient ingredient : ingredients) {
                if (ingredient.hasExactItem()) {
                    hasExactIngredient = true;
                    break;
                }
            }

            if (!hasExactIngredient) {
                continue;
            }

            boolean allIngredientsMatch = true;
            for (int i = 0; i < 9; i++) {
                RecipeIngredient required = ingredients.get(i);
                ItemStack actual = matrix[i];

                if (required.material() == Material.AIR) {
                    if (actual != null && actual.getType() != Material.AIR) {
                        allIngredientsMatch = false;
                        break;
                    }
                    continue;
                }

                if (actual == null || actual.getType() != required.material()) {
                    allIngredientsMatch = false;
                    break;
                }

                if (required.hasExactItem()) {
                    ItemStack exactItem = required.getExactItem();
                    if (!actual.hasItemMeta() && exactItem.hasItemMeta()) {
                        allIngredientsMatch = false;
                        break;
                    }
                    if (actual.hasItemMeta() && exactItem.hasItemMeta()) {
                        org.bukkit.inventory.meta.ItemMeta actualMeta = actual.getItemMeta();
                        org.bukkit.inventory.meta.ItemMeta exactMeta = exactItem.getItemMeta();
                        if (exactItem.getType() == Material.ENCHANTED_BOOK) {
                            if (exactMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta &&
                                actualMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                                org.bukkit.inventory.meta.EnchantmentStorageMeta exactBook =
                                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) exactMeta;
                                org.bukkit.inventory.meta.EnchantmentStorageMeta actualBook =
                                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) actualMeta;
                                if (!actualBook.getStoredEnchants().equals(exactBook.getStoredEnchants())) {
                                    allIngredientsMatch = false;
                                    break;
                                }
                            }
                        }
                        if (exactMeta instanceof org.bukkit.inventory.meta.Damageable &&
                            actualMeta instanceof org.bukkit.inventory.meta.Damageable) {
                            org.bukkit.inventory.meta.Damageable exactDamageable =
                                (org.bukkit.inventory.meta.Damageable) exactMeta;
                            org.bukkit.inventory.meta.Damageable actualDamageable =
                                (org.bukkit.inventory.meta.Damageable) actualMeta;
                            if (exactDamageable.hasDamage() &&
                                exactDamageable.getDamage() != actualDamageable.getDamage()) {
                                allIngredientsMatch = false;
                                break;
                            }
                        }
                        if (!actualMeta.getEnchants().equals(exactMeta.getEnchants())) {
                            allIngredientsMatch = false;
                            break;
                        }
                    }
                }
            }
            if (allIngredientsMatch) {
                return recipe;
            }
        }
        return null;
    }
}

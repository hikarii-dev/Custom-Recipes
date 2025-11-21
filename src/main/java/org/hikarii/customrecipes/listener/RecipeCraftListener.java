package org.hikarii.customrecipes.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import java.util.List;

public class RecipeCraftListener implements Listener {
    private final CustomRecipes plugin;

    public RecipeCraftListener(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraftItem(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

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

        if (plugin.isDebugMode()) {
            plugin.getLogger().info("[CRAFT] Recipe selected by Bukkit: " + recipeKey);
        }

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();
        CustomRecipe exactMatchRecipe = findExactMatchRecipe(matrix);
        if (exactMatchRecipe != null && !exactMatchRecipe.getKey().equals(recipeKey)) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("[CRAFT] Found more specific recipe with exact-ingredients: " +
                    exactMatchRecipe.getKey());
            }
            customRecipe = exactMatchRecipe;
            boolean useCraftedNames = plugin.isUseCraftedCustomNames();
            boolean keepSpawnEggNames = plugin.isKeepSpawnEggNames();
            ItemStack correctResult = exactMatchRecipe.createResult(useCraftedNames, keepSpawnEggNames);
            inventory.setResult(correctResult);
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("[CRAFT] Replaced result with: " + correctResult.getType());
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

            if (plugin.isDebugMode()) {
                plugin.getLogger().info("[CRAFT] Slot " + i + ": hasExactItem=" + required.hasExactItem());
            }

            if (required.hasExactItem()) {
                ItemStack exactItem = required.getExactItem();
                if (plugin.isDebugMode()) {
                    plugin.getLogger().info("[CRAFT] Slot " + i + " exact: " + exactItem.getType() +
                        " actual: " + actual.getType());
                }

                if (!actual.hasItemMeta() && exactItem.hasItemMeta()) {
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("[CRAFT] CANCELLED: actual has no meta but exact does");
                    }
                    event.setCancelled(true);
                    return;
                }
                if (actual.hasItemMeta() && exactItem.hasItemMeta()) {
                    org.bukkit.inventory.meta.ItemMeta actualMeta = actual.getItemMeta();
                    org.bukkit.inventory.meta.ItemMeta exactMeta = exactItem.getItemMeta();
                    if (exactMeta instanceof org.bukkit.inventory.meta.Damageable &&
                        actualMeta instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable exactDamageable = (org.bukkit.inventory.meta.Damageable) exactMeta;
                        org.bukkit.inventory.meta.Damageable actualDamageable = (org.bukkit.inventory.meta.Damageable) actualMeta;
                        if (plugin.isDebugMode()) {
                            plugin.getLogger().info("[CRAFT] Durability check: exact=" + exactDamageable.getDamage() +
                                " actual=" + actualDamageable.getDamage());
                        }

                        if (exactDamageable.hasDamage() &&
                            exactDamageable.getDamage() != actualDamageable.getDamage()) {
                            if (plugin.isDebugMode()) {
                                plugin.getLogger().info("[CRAFT] CANCELLED: durability mismatch");
                            }
                            event.setCancelled(true);
                            return;
                        }
                    }
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("[CRAFT] Enchants: exact=" + exactMeta.getEnchants() +
                            " actual=" + actualMeta.getEnchants());
                    }
                    if (!actualMeta.getEnchants().equals(exactMeta.getEnchants())) {
                        if (plugin.isDebugMode()) {
                            plugin.getLogger().info("[CRAFT] CANCELLED: enchantments mismatch");
                        }
                        event.setCancelled(true);
                        return;
                    }

                    if (exactMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta &&
                        actualMeta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                        org.bukkit.inventory.meta.EnchantmentStorageMeta exactBook =
                            (org.bukkit.inventory.meta.EnchantmentStorageMeta) exactMeta;
                        org.bukkit.inventory.meta.EnchantmentStorageMeta actualBook =
                            (org.bukkit.inventory.meta.EnchantmentStorageMeta) actualMeta;
                        if (plugin.isDebugMode()) {
                            plugin.getLogger().info("[CRAFT] Stored enchants: exact=" + exactBook.getStoredEnchants() +
                                " actual=" + actualBook.getStoredEnchants());
                        }

                        if (!actualBook.getStoredEnchants().equals(exactBook.getStoredEnchants())) {
                            if (plugin.isDebugMode()) {
                                plugin.getLogger().info("[CRAFT] CANCELLED: stored enchantments mismatch");
                            }
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (!actualMeta.getPersistentDataContainer().getKeys()
                            .equals(exactMeta.getPersistentDataContainer().getKeys())) {
                        if (plugin.isDebugMode()) {
                            plugin.getLogger().info("[CRAFT] CANCELLED: NBT mismatch");
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (plugin.isDebugMode()) {
            plugin.getLogger().info("[CRAFT] All checks passed!");
        }

        for (int i = 0; i < Math.min(matrix.length, ingredients.size()); i++) {
            RecipeIngredient required = ingredients.get(i);
            ItemStack actual = matrix[i];
            if (required.material() == Material.AIR) {
                continue;
            }

            if (actual != null && actual.getType() == required.material()) {
                if (required.amount() > 1) {
                    actual.setAmount(actual.getAmount() - (required.amount() - 1));
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
                if (plugin.isDebugMode()) {
                    plugin.getLogger().info("[CRAFT] Found recipe with exact-ingredients: " + recipe.getKey());
                }
                return recipe;
            }
        }

        return null;
    }
}
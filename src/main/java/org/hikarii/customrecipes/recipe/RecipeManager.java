package org.hikarii.customrecipes.recipe;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.Material;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.config.JsonRecipeFileManager;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

/**
 * Manages registration and unregistration of custom recipes
 */
public class RecipeManager {

    private final CustomRecipes plugin;
    private final Map<String, CustomRecipe> recipes;
    private final Set<NamespacedKey> registeredKeys;

    public RecipeManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipes = new LinkedHashMap<>();
        this.registeredKeys = new HashSet<>();
    }

    /**
     * Adds a recipe to the manager
     *
     * @param recipe the recipe to add
     */
    public void addRecipe(CustomRecipe recipe) {
        recipes.put(recipe.getKey(), recipe);
        plugin.debug("Added recipe: " + recipe.getKey());
    }

    /**
     * Removes a recipe from the manager
     *
     * @param key the recipe key
     * @return the removed recipe, or null if not found
     */
    public CustomRecipe removeRecipe(String key) {
        CustomRecipe removed = recipes.remove(key.toLowerCase());
        if (removed != null) {
            plugin.debug("Removed recipe: " + key);
        }
        return removed;
    }

    /**
     * Gets a recipe by key
     *
     * @param key the recipe key
     * @return the recipe, or null if not found
     */
    public CustomRecipe getRecipe(String key) {
        return recipes.get(key.toLowerCase());
    }

    /**
     * Gets all loaded recipes
     *
     * @return unmodifiable collection of recipes
     */
    public Collection<CustomRecipe> getAllRecipes() {
        return Collections.unmodifiableCollection(recipes.values());
    }

    /**
     * Gets the number of loaded recipes
     *
     * @return recipe count
     */
    public int getRecipeCount() {
        return recipes.size();
    }

    /**
     * Clears all recipes from the manager
     */
    public void clearRecipes() {
        recipes.clear();
        plugin.debug("Cleared all recipes from manager");
    }

    /**
     * Registers all recipes with the server
     */
    public void registerAllRecipes() {
        // First unregister any existing recipes
        unregisterAll();

        int registered = 0;
        for (CustomRecipe recipe : recipes.values()) {
            if (registerRecipe(recipe)) {
                registered++;
            }
        }

        plugin.getLogger().info("Registered " + registered + " custom recipes");
    }

    /**
     * Registers a single recipe without clearing existing ones
     *
     * @param recipe the recipe to register
     * @return true if successful
     */
    public boolean registerSingleRecipe(CustomRecipe recipe) {
        // Add to recipes map
        recipes.put(recipe.getKey(), recipe);

        // Register with server
        boolean success = registerRecipe(recipe);

        if (success) {
            plugin.getLogger().info("Registered new recipe: " + recipe.getKey());
        }

        return success;
    }

    /**
     * Registers a single recipe with the server
     *
     * @param recipe the recipe to register
     * @return true if successful, false otherwise
     */
    private boolean registerRecipe(CustomRecipe recipe) {
        try {
            NamespacedKey key = new NamespacedKey(plugin, recipe.getKey());

            if (recipe.getType() == RecipeType.SHAPED) {
                boolean useCraftedNames = plugin.isUseCraftedCustomNames();
                boolean keepSpawnEggNames = plugin.isKeepSpawnEggNames();

                ShapedRecipe shapedRecipe = new ShapedRecipe(key, recipe.createResult(useCraftedNames, keepSpawnEggNames));

                // Set the shape (can be 1-3 rows)
                String[] shape = recipe.getRecipeData().toShapeArray();
                if (shape.length == 1) {
                    shapedRecipe.shape(shape[0]);
                } else if (shape.length == 2) {
                    shapedRecipe.shape(shape[0], shape[1]);
                } else {
                    shapedRecipe.shape(shape[0], shape[1], shape[2]);
                }

                // Set the ingredients
                Map<Character, Material> ingredients = recipe.getRecipeData().getIngredientMap();
                for (Map.Entry<Character, Material> entry : ingredients.entrySet()) {
                    shapedRecipe.setIngredient(entry.getKey(), entry.getValue());
                }

                // Register with server
                Bukkit.addRecipe(shapedRecipe);
                registeredKeys.add(key);

                plugin.debug("Registered shaped recipe: " + recipe.getKey());
                return true;
            } else if (recipe.getType() == RecipeType.SHAPELESS) {
                boolean useCraftedNames = plugin.isUseCraftedCustomNames();
                boolean keepSpawnEggNames = plugin.isKeepSpawnEggNames();

                org.bukkit.inventory.ShapelessRecipe shapelessRecipe =
                        new org.bukkit.inventory.ShapelessRecipe(key, recipe.createResult(useCraftedNames, keepSpawnEggNames));

                // Add ingredients with counts
                Map<Material, Integer> ingredients = recipe.getShapelessData().ingredients();
                for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
                    shapelessRecipe.addIngredient(entry.getValue(), entry.getKey());
                }

                Bukkit.addRecipe(shapelessRecipe);
                registeredKeys.add(key);
                plugin.debug("Registered shapeless recipe: " + recipe.getKey());
                return true;
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register recipe '" + recipe.getKey() + "': " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Unregisters all custom recipes from the server
     */
    public void unregisterAll() {
        int unregistered = 0;

        for (NamespacedKey key : registeredKeys) {
            if (Bukkit.removeRecipe(key)) {
                unregistered++;
                plugin.debug("Unregistered recipe: " + key.getKey());
            }
        }

        registeredKeys.clear();

        if (unregistered > 0) {
            plugin.getLogger().info("Unregistered " + unregistered + " custom recipes");
        }
    }

    /**
     * Checks if a recipe exists
     *
     * @param key the recipe key
     * @return true if exists, false otherwise
     */
    public boolean hasRecipe(String key) {
        return recipes.containsKey(key.toLowerCase());
    }

    /**
     * Enables a recipe (adds to enabled list and registers)
     *
     * @param key the recipe key
     * @return true if successful, false otherwise
     */
    public boolean enableRecipe(String key) {
        CustomRecipe recipe = getRecipe(key);
        if (recipe == null) {
            return false;
        }

        // Check if already enabled
        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipe.getKey());
        if (registeredKeys.contains(namespacedKey)) {
            return false; // Already enabled
        }

        // Register the recipe
        return registerRecipe(recipe);
    }

    /**
     * Disables a recipe (unregisters but keeps in config)
     *
     * @param key the recipe key
     * @return true if successful, false otherwise
     */
    public boolean disableRecipe(String key) {
        CustomRecipe recipe = getRecipe(key);
        if (recipe == null) {
            return false;
        }

        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipe.getKey());
        if (!registeredKeys.contains(namespacedKey)) {
            return false; // Already disabled
        }

        // Unregister from server
        if (Bukkit.removeRecipe(namespacedKey)) {
            registeredKeys.remove(namespacedKey);
            plugin.debug("Disabled recipe: " + key);
            return true;
        }

        return false;
    }

    /**
     * Deletes a recipe completely (removes from manager and config)
     *
     * @param key the recipe key
     * @return true if successful, false otherwise
     */
    public boolean deleteRecipe(String key) {
        CustomRecipe recipe = removeRecipe(key);
        if (recipe == null) {
            return false;
        }

        // Unregister if registered
        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipe.getKey());
        if (registeredKeys.contains(namespacedKey)) {
            Bukkit.removeRecipe(namespacedKey);
            registeredKeys.remove(namespacedKey);
        }

        plugin.debug("Deleted recipe: " + key);
        return true;
    }

    /**
     * Deletes a recipe permanently (removes from manager, server, and FILES)
     *
     * @param key the recipe key
     * @return true if successful, false otherwise
     */
    public boolean deleteRecipePermanently(String key) {
        CustomRecipe recipe = getRecipe(key);
        if (recipe == null) {
            return false;
        }

        // Remove from manager
        removeRecipe(key);

        // Unregister from server
        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipe.getKey());
        if (registeredKeys.contains(namespacedKey)) {
            Bukkit.removeRecipe(namespacedKey);
            registeredKeys.remove(namespacedKey);
        }

        // Delete YAML file
        boolean yamlDeleted = plugin.getConfigManager().getRecipeFileManager().deleteRecipe(key);

        // Delete JSON file
        JsonRecipeFileManager jsonManager = new JsonRecipeFileManager(plugin);
        boolean jsonDeleted = jsonManager.deleteRecipe(key);

        plugin.debug("Permanently deleted recipe: " + key +
                " (YAML: " + yamlDeleted + ", JSON: " + jsonDeleted + ")");

        return true;
    }

    /**
     * Updates the result item for a recipe
     *
     * @param recipeKey the recipe key
     * @param newResult the new result item
     * @return true if successful
     */
    public boolean updateRecipeResult(String recipeKey, ItemStack newResult,
                                      String newGuiName, List<String> newGuiDescription,
                                      String newCraftedName, List<String> newCraftedDescription) {
        CustomRecipe recipe = getRecipe(recipeKey);
        if (recipe == null) {
            return false;
        }

        // Create new recipe with updated result AND all text fields
        CustomRecipe updatedRecipe = new CustomRecipe(
                recipe.getKey(),
                newGuiName != null ? newGuiName : recipe.getGuiName(),
                newGuiDescription != null ? newGuiDescription : recipe.getGuiDescription(),
                newCraftedName != null ? newCraftedName : recipe.getCraftedName(),
                newCraftedDescription != null ? newCraftedDescription : recipe.getCraftedDescription(),
                recipe.getType(),
                recipe.getRecipeData(),
                recipe.getShapelessData(),
                newResult,
                recipe.isHidden()
        );

        // Replace in map
        recipes.put(recipeKey.toLowerCase(), updatedRecipe);

        // Re-register with server
        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipeKey);
        if (registeredKeys.contains(namespacedKey)) {
            Bukkit.removeRecipe(namespacedKey);
            registeredKeys.remove(namespacedKey);
            registerRecipe(updatedRecipe);
        }

        // Save to config
        saveRecipeToFile(updatedRecipe);

        plugin.debug("Updated result item for recipe: " + recipeKey);
        return true;
    }

    private void saveRecipeToFile(CustomRecipe recipe) {
        try {
            Map<String, Object> recipeData = new HashMap<>();

            // Save all recipe data
            if (recipe.getGuiName() != null) {
                recipeData.put("gui-name", recipe.getGuiName());
            }
            if (recipe.getGuiDescription() != null && !recipe.getGuiDescription().isEmpty()) {
                recipeData.put("gui-description", recipe.getGuiDescription());
            }
            if (recipe.getCraftedName() != null) {
                recipeData.put("crafted-name", recipe.getCraftedName());
            }
            if (recipe.getCraftedDescription() != null && !recipe.getCraftedDescription().isEmpty()) {
                recipeData.put("crafted-description", recipe.getCraftedDescription());
            }

            recipeData.put("type", recipe.getType().name());
            recipeData.put("hidden", recipe.isHidden());

            // Save GUI fields
            if (recipe.getGuiName() != null && !recipe.getGuiName().isEmpty()) {
                recipeData.put("gui-name", recipe.getGuiName());
            }
            if (recipe.getGuiDescription() != null && !recipe.getGuiDescription().isEmpty()) {
                recipeData.put("gui-description", recipe.getGuiDescription());
            }

            // Save Crafted fields
            if (recipe.getCraftedName() != null && !recipe.getCraftedName().isEmpty()) {
                recipeData.put("crafted-name", recipe.getCraftedName());
            }
            if (recipe.getCraftedDescription() != null && !recipe.getCraftedDescription().isEmpty()) {
                recipeData.put("crafted-description", recipe.getCraftedDescription());
            }

            // Save recipe pattern
            if (recipe.getType() == RecipeType.SHAPED) {
                String[] pattern = recipe.getRecipeData().toShapeArray();
                List<String> patternList = new ArrayList<>();

                // Convert to config format
                for (String row : pattern) {
                    StringBuilder rowBuilder = new StringBuilder();
                    Map<Character, Material> ingredients = recipe.getRecipeData().getIngredientMap();

                    for (char c : row.toCharArray()) {
                        if (c == ' ') {
                            rowBuilder.append("AIR ");
                        } else {
                            Material mat = ingredients.get(c);
                            rowBuilder.append(mat.name()).append(" ");
                        }
                    }
                    patternList.add(rowBuilder.toString().trim());
                }
                recipeData.put("recipe", patternList);
            } else {
                recipeData.put("ingredients", recipe.getShapelessData().toConfigList());
            }

            // Save result item
            Map<String, Object> resultData = ItemStackSerializer.toMap(recipe.getResultItem());
            recipeData.put("result", resultData);
            recipeData.put("material", recipe.getResultMaterial().name());
            recipeData.put("amount", recipe.getResultAmount());

            // Save to file
            plugin.getConfigManager().getRecipeFileManager().saveRecipe(recipe.getKey(), recipeData);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save recipe: " + e.getMessage());
        }
    }

    /**
     * Checks if a recipe is enabled (registered)
     *
     * @param key the recipe key
     * @return true if enabled, false otherwise
     */
    public boolean isRecipeEnabled(String key) {
        CustomRecipe recipe = getRecipe(key);
        if (recipe == null) {
            return false;
        }

        NamespacedKey namespacedKey = new NamespacedKey(plugin, recipe.getKey());
        return registeredKeys.contains(namespacedKey);
    }
}
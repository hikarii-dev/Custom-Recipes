package org.hikarii.customrecipes.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages plugin configuration and recipe loading
 */
public class ConfigManager {

    private final CustomRecipes plugin;
    private final RecipeConfigLoader recipeLoader;

    public ConfigManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipeLoader = new RecipeConfigLoader(plugin);
    }

    /**
     * Loads all recipes from configuration
     *
     * @throws ValidationException if configuration is invalid
     */
    public void loadRecipes() throws ValidationException {
        FileConfiguration config = plugin.getConfig();
        RecipeManager recipeManager = plugin.getRecipeManager();

        // Clear existing recipes
        recipeManager.clearRecipes();

        // Get enabled recipes list
        List<String> enabledRecipes = config.getStringList("enabled-recipes");
        if (enabledRecipes.isEmpty()) {
            plugin.getLogger().warning("No recipes are enabled in configuration");
            return;
        }

        // Track loaded recipe keys for duplicate detection
        Set<String> loadedKeys = new HashSet<>();
        int successCount = 0;
        int failCount = 0;

        // Load each enabled recipe
        for (String key : enabledRecipes) {
            try {
                // Validate key
                recipeLoader.validateKey(key);

                // Check for duplicates
                String keyLower = key.toLowerCase();
                if (loadedKeys.contains(keyLower)) {
                    plugin.getLogger().warning("Duplicate recipe key in enabled-recipes: " + key);
                    continue;
                }

                // Get recipe configuration section
                ConfigurationSection recipeSection = config.getConfigurationSection(key);
                if (recipeSection == null) {
                    throw new ValidationException("Recipe configuration not found for key: " + key);
                }

                // Load the recipe
                CustomRecipe recipe = recipeLoader.loadRecipe(key, recipeSection);

                // Add to manager
                recipeManager.addRecipe(recipe);
                loadedKeys.add(keyLower);
                successCount++;

                plugin.debug("Loaded recipe: " + key);

            } catch (ValidationException e) {
                plugin.getLogger().severe("Failed to load recipe '" + key + "': " + e.getMessage());
                if (plugin.isDebugMode() && e.getCause() != null) {
                    e.getCause().printStackTrace();
                }
                failCount++;
            }
        }

        // Log summary
        plugin.getLogger().info("Loaded " + successCount + " recipes successfully" +
                (failCount > 0 ? " (" + failCount + " failed)" : ""));
    }

    /**
     * Validates the entire configuration
     *
     * @return true if valid, false otherwise
     */
    public boolean validateConfiguration() {
        try {
            FileConfiguration config = plugin.getConfig();

            // Check if enabled-recipes exists
            if (!config.contains("enabled-recipes")) {
                plugin.getLogger().severe("Configuration is missing 'enabled-recipes' section");
                return false;
            }

            // Try to load recipes (this will validate them)
            loadRecipes();
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Configuration validation failed: " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Adds a recipe to the enabled-recipes list and saves config
     *
     * @param recipeKey the recipe key to add
     */
    public void addEnabledRecipe(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        List<String> enabled = config.getStringList("enabled-recipes");

        if (!enabled.contains(recipeKey)) {
            enabled.add(recipeKey);
            config.set("enabled-recipes", enabled);
            plugin.saveConfig();
            plugin.debug("Added '" + recipeKey + "' to enabled-recipes");
        }
    }

    /**
     * Removes a recipe from the enabled-recipes list and saves config
     *
     * @param recipeKey the recipe key to remove
     */
    public void removeEnabledRecipe(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        List<String> enabled = config.getStringList("enabled-recipes");

        if (enabled.remove(recipeKey)) {
            config.set("enabled-recipes", enabled);
            plugin.saveConfig();
            plugin.debug("Removed '" + recipeKey + "' from enabled-recipes");
        }
    }

    /**
     * Checks if a recipe is in the enabled-recipes list
     *
     * @param recipeKey the recipe key to check
     * @return true if in list, false otherwise
     */
    public boolean isRecipeInEnabledList(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        List<String> enabled = config.getStringList("enabled-recipes");
        return enabled.contains(recipeKey);
    }
}
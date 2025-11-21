package org.hikarii.customrecipes.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeManager;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final CustomRecipes plugin;
    private final RecipeConfigLoader recipeLoader;
    private final RecipeFileManager recipeFileManager;
    public ConfigManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipeLoader = new RecipeConfigLoader(plugin);
        this.recipeFileManager = new RecipeFileManager(plugin);
    }

    public void loadRecipes() throws ValidationException {
        FileConfiguration config = plugin.getConfig();
        RecipeManager recipeManager = plugin.getRecipeManager();
        recipeManager.clearRecipes();
        List<String> enabledRecipes = config.getStringList("enabled-recipes");
        if (enabledRecipes.isEmpty()) {
            plugin.getLogger().warning("No recipes are enabled in configuration");
            return;
        }
        Set<String> loadedKeys = new HashSet<>();
        int successCount = 0;
        int failCount = 0;
        for (String key : enabledRecipes) {
            try {
                recipeLoader.validateKey(key);
                String keyLower = key.toLowerCase();
                if (loadedKeys.contains(keyLower)) {
                    plugin.getLogger().warning("Duplicate recipe key in enabled-recipes: " + key);
                    continue;
                }
                File recipeFile = new File(recipeFileManager.getRecipesFolder(), key + ".yml");
                ConfigurationSection recipeSection;
                if (recipeFile.exists()) {
                    YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(recipeFile);
                    recipeSection = fileConfig;
                    plugin.debug("Loading recipe '" + key + "' from file");
                } else {
                    recipeSection = config.getConfigurationSection(key);
                    if (recipeSection == null) {
                        throw new ValidationException("Recipe file not found and not in main config: " + key);
                    }
                    plugin.debug("Loading recipe '" + key + "' from main config (legacy)");
                }
                CustomRecipe recipe = recipeLoader.loadRecipe(key, recipeSection);
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
        plugin.getLogger().info("Loaded " + successCount + " recipes successfully" +
                (failCount > 0 ? " (" + failCount + " failed)" : ""));
    }

    public boolean validateConfiguration() {
        try {
            FileConfiguration config = plugin.getConfig();
            if (!config.contains("enabled-recipes")) {
                plugin.getLogger().severe("Configuration is missing 'enabled-recipes' section");
                return false;
            }
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

    public void removeEnabledRecipe(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        List<String> enabled = config.getStringList("enabled-recipes");
        boolean removed = false;
        Iterator<String> iterator = enabled.iterator();
        while (iterator.hasNext()) {
            String existing = iterator.next();
            if (existing.equalsIgnoreCase(recipeKey)) {
                iterator.remove();
                removed = true;
                plugin.debug("Removed '" + existing + "' from enabled-recipes (matched: " + recipeKey + ")");
            }
        }
        if (removed) {
            config.set("enabled-recipes", enabled);
            plugin.saveConfig();
            plugin.debug("Removed '" + recipeKey + "' from enabled-recipes");
        }
    }

    public boolean isRecipeInEnabledList(String recipeKey) {
        FileConfiguration config = plugin.getConfig();
        List<String> enabled = config.getStringList("enabled-recipes");
        return enabled.contains(recipeKey);
    }

    public RecipeFileManager getRecipeFileManager() {
        return recipeFileManager;
    }
}
package org.hikarii.customrecipes.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages individual recipe files
 */
public class RecipeFileManager {

    private final CustomRecipes plugin;
    private final File recipesFolder;

    public RecipeFileManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipesFolder = new File(plugin.getDataFolder(), "recipes");

        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }
    }

    /**
     * Loads all recipes from files
     */
    public Map<String, CustomRecipe> loadAllRecipes() throws ValidationException {
        Map<String, CustomRecipe> recipes = new HashMap<>();
        RecipeConfigLoader loader = new RecipeConfigLoader(plugin);

        File[] files = recipesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().info("No recipe files found in recipes folder");
            return recipes;
        }

        int successCount = 0;
        int failCount = 0;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String recipeKey = file.getName().replace(".yml", "");

                CustomRecipe recipe = loader.loadRecipe(recipeKey, config);
                recipes.put(recipeKey.toLowerCase(), recipe);
                successCount++;

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load recipe from " + file.getName() + ": " + e.getMessage());
                failCount++;
                if (plugin.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }

        plugin.getLogger().info("Loaded " + successCount + " recipes from files" +
                (failCount > 0 ? " (" + failCount + " failed)" : ""));

        return recipes;
    }

    /**
     * Saves a recipe to its own file
     */
    public void saveRecipe(String recipeKey, ConfigurationSection recipeData) throws IOException {
        File recipeFile = new File(recipesFolder, recipeKey + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Copy all data from section
        for (String key : recipeData.getKeys(false)) {
            config.set(key, recipeData.get(key));
        }

        config.save(recipeFile);
        plugin.debug("Saved recipe to file: " + recipeFile.getName());
    }

    /**
     * Saves a recipe with direct values
     */
    public void saveRecipe(String recipeKey, Map<String, Object> data) throws IOException {
        File recipeFile = new File(recipesFolder, recipeKey + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        config.save(recipeFile);
        plugin.debug("Saved recipe to file: " + recipeFile.getName());
    }

    /**
     * Deletes a recipe file
     */
    public boolean deleteRecipe(String recipeKey) {
        File recipeFile = new File(recipesFolder, recipeKey + ".yml");
        if (recipeFile.exists()) {
            return recipeFile.delete();
        }
        return false;
    }

    /**
     * Checks if a recipe file exists
     */
    public boolean recipeFileExists(String recipeKey) {
        File recipeFile = new File(recipesFolder, recipeKey + ".yml");
        return recipeFile.exists();
    }

    public File getRecipesFolder() {
        return recipesFolder;
    }
}
package org.hikarii.customrecipes.config;

import org.hikarii.customrecipes.CustomRecipes;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class DefaultRecipesManager {
    private final CustomRecipes plugin;
    private final File recipesFolder;

    private static final List<String> DEFAULT_RECIPES = Arrays.asList(
            "BeeSpawnEgg",
            "CowSpawnEgg",
            "CreeperSpawnEgg",
            "EndermanSpawnEgg",
            "SkeletonSpawnEgg",
            "ZombieSpawnEgg",
            "DiamondFromCoal",
            "EnderPearlStack",
            "GoldenAppleStack"
    );

    public DefaultRecipesManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipesFolder = new File(plugin.getDataFolder(), "recipes");
    }

    public void initializeDefaultRecipes() {
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }

        RecipeStateTracker stateTracker = plugin.getRecipeStateTracker();
        List<String> enabledRecipes = plugin.getConfig().getStringList("enabled-recipes");
        int added = 0;
        int skipped = 0;
        boolean configChanged = false;
        for (String recipeKey : DEFAULT_RECIPES) {
            File recipeFile = new File(recipesFolder, recipeKey + ".yml");
            if (stateTracker.wasDefaultRecipeDeleted(recipeKey)) {
                plugin.debug("Skipping deleted default recipe: " + recipeKey);
                skipped++;
                if (enabledRecipes.contains(recipeKey)) {
                    enabledRecipes.remove(recipeKey);
                    configChanged = true;
                    plugin.debug("Removed deleted recipe from enabled-recipes: " + recipeKey);
                }
                continue;
            }

            if (!recipeFile.exists()) {
                if (copyDefaultRecipe(recipeKey)) {
                    
                    if (!enabledRecipes.contains(recipeKey)) {
                        enabledRecipes.add(recipeKey);
                        configChanged = true;
                    }
                    added++;
                    plugin.debug("Added default recipe: " + recipeKey);
                }
            }
        }

        if (configChanged) {
            plugin.getConfig().set("enabled-recipes", enabledRecipes);
            plugin.saveConfig();
            if (added > 0) {
                plugin.getLogger().info("Added " + added + " default recipes" +
                        (skipped > 0 ? " (" + skipped + " previously deleted)" : ""));
            }
        } else if (skipped > 0) {
            plugin.debug("Skipped " + skipped + " previously deleted default recipes");
        }
    }

    private boolean copyDefaultRecipe(String recipeKey) {
        try {
            String resourcePath = "default-recipes/" + recipeKey + ".yml";
            InputStream resource = plugin.getResource(resourcePath);
            if (resource == null) {
                plugin.getLogger().warning("Default recipe not found in jar: " + recipeKey);
                return false;
            }

            File targetFile = new File(recipesFolder, recipeKey + ".yml");
            Files.copy(resource, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            resource.close();
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to copy default recipe " + recipeKey + ": " + e.getMessage());
            return false;
        }
    }

    public static List<String> getDefaultRecipeKeys() {
        return DEFAULT_RECIPES;
    }

    public static boolean isDefaultRecipe(String recipeKey) {
        for (String defaultRecipe : DEFAULT_RECIPES) {
            if (defaultRecipe.equalsIgnoreCase(recipeKey)) {
                return true;
            }
        }
        return false;
    }
}
package org.hikarii.customrecipes.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hikarii.customrecipes.CustomRecipes;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.LinkedHashSet;

public class RecipeStateTracker {
    private final CustomRecipes plugin;
    private final File stateFile;
    private YamlConfiguration stateConfig;
    private Set<String> deletedDefaultRecipes;
    private Set<String> disabledRecipes;
    private String lastVersion;

    public RecipeStateTracker(CustomRecipes plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "recipe-state.yml");
        this.deletedDefaultRecipes = new HashSet<>();
        this.disabledRecipes = new HashSet<>();
        load();
    }

    private void load() {
        if (!stateFile.exists()) {
            createDefaultState();
            return;
        }
        try {
            stateConfig = YamlConfiguration.loadConfiguration(stateFile);
            List<String> deleted = stateConfig.getStringList("deleted-default-recipes");
            deletedDefaultRecipes.addAll(deleted);
            List<String> disabled = stateConfig.getStringList("disabled-recipes");
            disabledRecipes.addAll(disabled);
            lastVersion = stateConfig.getString("last-version", "1.1.2");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load recipe state: " + e.getMessage());
            e.printStackTrace();
            createDefaultState();
        }
    }

    private void createDefaultState() {
        stateConfig = new YamlConfiguration();
        stateConfig.set("version", 1);
        stateConfig.set("last-version", plugin.getDescription().getVersion());
        stateConfig.set("deleted-default-recipes", new ArrayList<String>());
        stateConfig.set("disabled-recipes", new ArrayList<String>());
        stateConfig.set("migration-notes", Arrays.asList(
                "This file tracks which recipes have been deleted or disabled",
                "Do not edit manually unless you know what you're doing"
        ));
        save();
    }

    public void save() {
        try {
            stateConfig.set("deleted-default-recipes", new ArrayList<>(deletedDefaultRecipes));
            stateConfig.set("disabled-recipes", new ArrayList<>(disabledRecipes));
            stateConfig.set("last-version", plugin.getDescription().getVersion());
            stateConfig.set("last-updated", System.currentTimeMillis());
            stateConfig.save(stateFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save recipe state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void markDefaultRecipeDeleted(String recipeKey) {
        String correctCaseName = recipeKey;
        for (String defaultRecipe : DefaultRecipesManager.getDefaultRecipeKeys()) {
            if (defaultRecipe.equalsIgnoreCase(recipeKey)) {
                correctCaseName = defaultRecipe;
                break;
            }
        }
        deletedDefaultRecipes.add(correctCaseName);
        save();
    }

    public boolean wasDefaultRecipeDeleted(String recipeKey) {
        for (String deletedRecipe : deletedDefaultRecipes) {
            if (deletedRecipe.equalsIgnoreCase(recipeKey)) {
                return true;
            }
        }
        return false;
    }

    public void markRecipeDisabled(String recipeKey) {
        String normalizedKey = recipeKey.toLowerCase();
        disabledRecipes.add(normalizedKey);
        save();
    }

    public void markRecipeEnabled(String recipeKey) {
        disabledRecipes.removeIf(disabled -> disabled.equalsIgnoreCase(recipeKey));
        save();
    }

    public boolean isRecipeDisabled(String recipeKey) {
        for (String disabledRecipe : disabledRecipes) {
            if (disabledRecipe.equalsIgnoreCase(recipeKey)) {
                return true;
            }
        }
        return false;
    }

    public void unmarkDefaultRecipeDeleted(String recipeKey) {
        deletedDefaultRecipes.remove(recipeKey);
        save();
        plugin.debug("Unmarked default recipe as deleted: " + recipeKey);
    }

    public Set<String> getDeletedDefaultRecipes() {
        return new HashSet<>(deletedDefaultRecipes);
    }

    public Set<String> getDisabledRecipes() {
        return new HashSet<>(disabledRecipes);
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public boolean isFreshInstall() {
        return !stateFile.exists() && deletedDefaultRecipes.isEmpty();
    }

    public void syncEnabledRecipes() {
        List<String> enabledRecipes = plugin.getConfig().getStringList("enabled-recipes");
        Set<String> syncedRecipes = new LinkedHashSet<>();
        File recipesFolder = plugin.getConfigManager().getRecipeFileManager().getRecipesFolder();
        Map<String, String> existingRecipeFiles = getExistingRecipeFiles(recipesFolder);
        int removed = 0;
        int kept = 0;
        for (String recipeKey : new ArrayList<>(enabledRecipes)) {
            String actualFileName = existingRecipeFiles.get(recipeKey.toLowerCase());
            if (actualFileName != null) {
                if (!wasDefaultRecipeDeleted(recipeKey)) {
                    syncedRecipes.add(actualFileName); 
                    kept++;
                } else {
                    plugin.debug("Removing deleted recipe from enabled-recipes: " + recipeKey);
                    removed++;
                }
            } else {
                plugin.debug("Removing non-existent recipe from enabled-recipes: " + recipeKey);
                removed++;
            }
        }
        if (removed > 0 || syncedRecipes.size() != enabledRecipes.size()) {
            plugin.getConfig().set("enabled-recipes", new ArrayList<>(syncedRecipes));
            plugin.saveConfig();

            plugin.getLogger().info("Synchronized enabled-recipes: " + kept + " kept" +
                    (removed > 0 ? ", " + removed + " removed" : ""));
        } else {
            plugin.debug("enabled-recipes already synchronized");
        }
    }

    private Map<String, String> getExistingRecipeFiles(File recipesFolder) {
        Map<String, String> files = new HashMap<>();
        if (!recipesFolder.exists()) {
            return files;
        }

        File[] recipeFiles = recipesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (recipeFiles != null) {
            for (File file : recipeFiles) {
                String fileName = file.getName().replace(".yml", "");
                files.put(fileName.toLowerCase(), fileName);
            }
        }

        return files;
    }

    public boolean needsMigration() {
        String currentVersion = plugin.getDescription().getVersion();
        return !currentVersion.equals(lastVersion);
    }

    public void cleanup() {
        Set<String> defaultRecipeKeys = new HashSet<>(DefaultRecipesManager.getDefaultRecipeKeys());

        deletedDefaultRecipes.removeIf(key -> !defaultRecipeKeys.contains(key));

        plugin.debug("Cleaned up recipe state");
        save();
    }
}
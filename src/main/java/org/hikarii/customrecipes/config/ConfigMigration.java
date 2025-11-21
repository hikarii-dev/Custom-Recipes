package org.hikarii.customrecipes.config;

import org.hikarii.customrecipes.CustomRecipes;
import java.util.*;
import java.io.File;
import java.io.IOException;

public class ConfigMigration {
    private final CustomRecipes plugin;

    public ConfigMigration(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    public boolean checkAndMigrate() {
        int configVersion = plugin.getConfig().getInt(MigrationVersions.CONFIG_VERSION_KEY, 1);
        if (configVersion == MigrationVersions.CONFIG_VERSION) {
            plugin.debug("Config is up to date (v" + configVersion + ")");
            return true;
        }

        plugin.getLogger().info("Detected old config version (" + configVersion + "), migrating to v" + MigrationVersions.CONFIG_VERSION + "...");
        try {
            Set<String> preservedDeletedRecipes = preserveDeletedRecipesInfo();

            createBackup();

            plugin.getLogger().info("Running universal migration from v" + configVersion + " to v" + MigrationVersions.CONFIG_VERSION + "...");
            detectDeletedDefaultRecipes();

            plugin.getLogger().info("Rewriting config.yml with new template...");
            ConfigRewriter rewriter = new ConfigRewriter(plugin);
            if (rewriter.rewriteConfig()) {
                plugin.getLogger().info("✓ Config.yml rewritten successfully!");
            } else {
                plugin.getLogger().warning("Failed to rewrite config.yml, but migration continues");
            }
            restoreDeletedRecipesInfo(preservedDeletedRecipes);
            plugin.getConfig().set(MigrationVersions.CONFIG_VERSION_KEY, MigrationVersions.CONFIG_VERSION);
            plugin.saveConfig();
            plugin.getLogger().info("✓ Config migration completed successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to migrate config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Set<String> preserveDeletedRecipesInfo() {
        Set<String> deletedRecipes = new HashSet<>();
        try {
            Set<String> existingDeleted = plugin.getRecipeStateTracker().getDeletedDefaultRecipes();
            deletedRecipes.addAll(existingDeleted);
            plugin.getLogger().info("Preserved " + deletedRecipes.size() + " deleted recipe(s) from previous state");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not preserve deleted recipes info: " + e.getMessage());
        }
        return deletedRecipes;
    }

    private void restoreDeletedRecipesInfo(Set<String> preservedDeletedRecipes) {
        if (preservedDeletedRecipes == null || preservedDeletedRecipes.isEmpty()) {
            return;
        }
        try {
            for (String recipeKey : preservedDeletedRecipes) {
                plugin.getRecipeStateTracker().markDefaultRecipeDeleted(recipeKey);
            }
            plugin.getLogger().info("Restored " + preservedDeletedRecipes.size() + " deleted recipe(s) to state tracker");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not restore deleted recipes info: " + e.getMessage());
        }
    }

    private void detectDeletedDefaultRecipes() {
        plugin.getLogger().info("Detecting deleted default recipes...");
        List<String> defaultRecipeKeys = DefaultRecipesManager.getDefaultRecipeKeys();
        List<String> enabledRecipes = plugin.getConfig().getStringList("enabled-recipes");
        File recipesFolder = new File(plugin.getDataFolder(), "recipes");
        int deletedCount = 0;
        for (String defaultKey : defaultRecipeKeys) {
            File recipeFile = new File(recipesFolder, defaultKey + ".yml");

            if (!recipeFile.exists() && !enabledRecipes.contains(defaultKey)) {
                plugin.getRecipeStateTracker().markDefaultRecipeDeleted(defaultKey);
                plugin.getLogger().info("Marked as deleted: " + defaultKey);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            plugin.getLogger().info("Found " + deletedCount + " deleted default recipes");
        } else {
            plugin.debug("No deleted default recipes found");
        }
    }

    private void createBackup() {
        try {
            File dataFolder = plugin.getDataFolder();
            File backupFolder = new File(dataFolder, "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupFile = new File(backupFolder, "config-backup-" + timestamp + ".yml");
            File mainConfig = new File(dataFolder, "config.yml");
            if (mainConfig.exists()) {
                java.nio.file.Files.copy(
                        mainConfig.toPath(),
                        backupFile.toPath()
                );
                plugin.getLogger().info("Created config backup: " + backupFile.getName());
            }

            File recipesFolder = new File(dataFolder, "recipes");
            if (recipesFolder.exists()) {
                File recipesBackup = new File(backupFolder, "recipes-backup-" + timestamp);
                copyFolder(recipesFolder, recipesBackup);
                plugin.getLogger().info("Created recipes backup: " + recipesBackup.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create backup: " + e.getMessage());
        }
    }

    private void copyFolder(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            java.nio.file.Files.copy(source.toPath(), destination.toPath());
        }
    }
}
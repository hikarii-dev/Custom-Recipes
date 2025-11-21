package org.hikarii.customrecipes.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hikarii.customrecipes.CustomRecipes;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ConfigRewriter {
    private final CustomRecipes plugin;
    private final File configFile;

    private static final List<String> PRESERVED_KEYS = Arrays.asList(
            "debug",
            "use-crafted-custom-names",
            "spawn-egg-keep-custom-name",
            "ignore-metadata",
            "world-restrictions",
            "recipe-world-settings",
            "enabled-recipes"
    );

    public ConfigRewriter(CustomRecipes plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public boolean rewriteConfig() {
        try {
            plugin.getLogger().info("Rewriting config.yml with preserved settings...");
            Map<String, Object> preservedValues = saveCurrentSettings();
            createBackup();
            if (configFile.exists()) {
                configFile.delete();
            }

            plugin.saveResource("config.yml", false);
            restoreSettings(preservedValues);

            plugin.getLogger().info("✓ Config rewritten successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to rewrite config: " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private Map<String, Object> saveCurrentSettings() {
        Map<String, Object> settings = new HashMap<>();
        FileConfiguration config = plugin.getConfig();
        for (String key : PRESERVED_KEYS) {
            if (config.contains(key)) {
                Object value = config.get(key);
                settings.put(key, deepCopy(value));
                plugin.debug("Preserved setting: " + key + " = " + value);
            }
        }
        return settings;
    }

    private void restoreSettings(Map<String, Object> preservedValues) throws IOException {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        for (Map.Entry<String, Object> entry : preservedValues.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
            plugin.debug("Restored setting: " + entry.getKey());
        }
        plugin.saveConfig();
    }

    private Object deepCopy(Object value) {
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            Map<String, Object> copy = new HashMap<>();
            for (String key : section.getKeys(false)) {
                copy.put(key, deepCopy(section.get(key)));
            }
            return copy;
        } else if (value instanceof List) {
            return new ArrayList<>((List<?>) value);
        } else if (value instanceof Map) {
            return new HashMap<>((Map<?, ?>) value);
        }
        return value;
    }

    private void createBackup() {
        try {
            File backupFolder = new File(plugin.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            String timestamp = String.valueOf(System.currentTimeMillis());
            File backupFile = new File(backupFolder, "config-backup-" + timestamp + ".yml");
            if (configFile.exists()) {
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created config backup: " + backupFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create config backup: " + e.getMessage());
        }
    }

    public boolean needsRewrite(int currentVersion, int targetVersion) {
        return currentVersion < targetVersion;
    }
}
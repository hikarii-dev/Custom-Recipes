package org.hikarii.customrecipes;

import org.bukkit.plugin.java.JavaPlugin;
import org.hikarii.customrecipes.command.CustomRecipesCommand;
import org.hikarii.customrecipes.config.ConfigManager;
import org.hikarii.customrecipes.listener.RecipeDiscoverListener;
import org.hikarii.customrecipes.recipe.RecipeManager;
import org.hikarii.customrecipes.util.MessageUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

/**
 * CustomRecipes - Advanced recipe management plugin
 *
 * @author hikarii
 * @version 1.0.0
 */
public final class CustomRecipes extends JavaPlugin {

    private static CustomRecipes instance;
    private ConfigManager configManager;
    private RecipeManager recipeManager;
    private boolean debugMode = false;
    private boolean keepSpawnEggNames = false;
    private boolean useCraftedCustomNames = true;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config if not exists
        saveDefaultConfig();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.recipeManager = new RecipeManager(this);

        // Load configuration and recipes
        if (!loadConfiguration()) {
            getLogger().severe("Failed to load configuration! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("CustomRecipes has been enabled!");
        getLogger().info("Loaded " + recipeManager.getRecipeCount() + " custom recipes");

        // Initialize bStats
        initializeMetrics();
    }

    @Override
    public void onDisable() {
        // Unregister all custom recipes
        if (recipeManager != null) {
            recipeManager.unregisterAll();
        }

        getLogger().info("CustomRecipes has been disabled!");
    }

    /**
     * Loads/reloads configuration and recipes
     *
     * @return true if successful, false otherwise
     */
    public boolean loadConfiguration() {
        try {
            // Reload config from disk
            reloadConfig();

            // Load debug mode
            debugMode = getConfig().getBoolean("debug", false);

            // Load crafted item customization settings
            useCraftedCustomNames = getConfig().getBoolean("use-crafted-custom-names", true);
            keepSpawnEggNames = getConfig().getBoolean("spawn-egg-keep-custom-name", false);

            // Load recipes from config
            configManager.loadRecipes();

            // Register recipes with server
            recipeManager.registerAllRecipes();

            return true;
        } catch (Exception e) {
            getLogger().severe("Error loading configuration: " + e.getMessage());
            if (debugMode) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Registers plugin commands
     */
    private void registerCommands() {
        CustomRecipesCommand commandExecutor = new CustomRecipesCommand(this);
        getCommand("customrecipes").setExecutor(commandExecutor);
        getCommand("customrecipes").setTabCompleter(commandExecutor);
    }

    /**
     * Registers event listeners
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new RecipeDiscoverListener(this), this
        );
    }

    // Getters

    public static CustomRecipes getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isKeepSpawnEggNames() {
        return keepSpawnEggNames;
    }

    public boolean isUseCraftedCustomNames() {
        return useCraftedCustomNames;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        getConfig().set("debug", debugMode);
        saveConfig();
    }

    /**
     * Logs debug message if debug mode is enabled
     *
     * @param message the debug message
     */
    public void debug(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Initializes bStats metrics
     */
    private void initializeMetrics() {
        int pluginId = 27998;
        Metrics metrics = new Metrics(this, pluginId);

        // Add custom chart: Total recipes
        metrics.addCustomChart(new SingleLineChart("total_recipes", () -> recipeManager.getRecipeCount()));

        // Add custom chart: Enabled recipes
        metrics.addCustomChart(new SingleLineChart("enabled_recipes", () -> {
            return (int) recipeManager.getAllRecipes().stream()
                    .filter(recipe -> recipeManager.isRecipeEnabled(recipe.getKey()))
                    .count();
        }));

        // Add custom chart: Using custom names
        metrics.addCustomChart(new SimplePie("using_custom_names", () ->
                useCraftedCustomNames ? "Yes" : "No"
        ));

        debug("bStats metrics initialized");
    }
}
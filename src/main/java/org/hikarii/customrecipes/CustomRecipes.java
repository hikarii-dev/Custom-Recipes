    package org.hikarii.customrecipes;

    import org.bukkit.plugin.java.JavaPlugin;
    import org.hikarii.customrecipes.command.CustomRecipesCommand;
    import org.hikarii.customrecipes.config.ConfigManager;
    import org.hikarii.customrecipes.listener.RecipeDiscoverListener;
    import org.hikarii.customrecipes.listener.RecipeHidingListener;
    import org.hikarii.customrecipes.recipe.RecipeDataManager;
    import org.hikarii.customrecipes.recipe.RecipeManager;
    import org.hikarii.customrecipes.recipe.RecipeWorldManager;
    import org.hikarii.customrecipes.update.UpdateChecker;
    import org.hikarii.customrecipes.update.UpdateNotifier;
    import org.hikarii.customrecipes.update.UpdateSource;
    import org.hikarii.customrecipes.util.MessageUtil;
    import org.bstats.bukkit.Metrics;
    import org.bstats.charts.SimplePie;
    import org.bstats.charts.SingleLineChart;

    /**
     * Custom Recipes - Create your own custom Crafting, Furnace, Anvil, and other recipes with tons of configuration option
     *
     * @author hikarii
     * @version 1.1.0
     */
    public final class CustomRecipes extends JavaPlugin {
        private static CustomRecipes instance;
        private ConfigManager configManager;
        private RecipeManager recipeManager;
        private boolean debugMode = false;
        private boolean keepSpawnEggNames = false;
        private boolean useCraftedCustomNames = true;
        private UpdateChecker updateChecker;
        private RecipeDataManager recipeDataManager;
        private RecipeWorldManager recipeWorldManager;

        @Override
        public void onEnable() {
            instance = this;
            saveDefaultConfig();
            this.configManager = new ConfigManager(this);
            this.recipeManager = new RecipeManager(this);
            this.recipeDataManager = new RecipeDataManager(this);
            this.recipeWorldManager = new RecipeWorldManager(this);


            if (!loadConfiguration()) {
                getLogger().severe("Failed to load configuration! Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            registerCommands();
            registerListeners();

            getLogger().info("CustomRecipes has been enabled!");
            getLogger().info("Loaded " + recipeManager.getRecipeCount() + " custom recipes");

            initializeMetrics();
            initializeUpdateChecker();
        }

        @Override
        public void onDisable() {
            if (recipeManager != null) {
                recipeManager.unregisterAll();
            }

            if (updateChecker != null) {
                updateChecker.stopPeriodicCheck();
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
                reloadConfig();
                debugMode = getConfig().getBoolean("debug", false);
                useCraftedCustomNames = getConfig().getBoolean("use-crafted-custom-names", true);
                keepSpawnEggNames = getConfig().getBoolean("spawn-egg-keep-custom-name", false);
                configManager.loadRecipes();
                recipeManager.registerAllRecipes();
                // Reload world restrictions
                if (recipeWorldManager != null) {
                    recipeWorldManager.loadWorldRestrictions();
                }
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

            getServer().getPluginManager().registerEvents(
                    new RecipeHidingListener(this), this
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

        public RecipeDataManager getRecipeDataManager() {
            return recipeDataManager;
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

            // Total recipes
            metrics.addCustomChart(new SingleLineChart("total_recipes", () -> recipeManager.getRecipeCount()));

            // Enabled recipes
            metrics.addCustomChart(new SingleLineChart("enabled_recipes", () -> {
                return (int) recipeManager.getAllRecipes().stream()
                        .filter(recipe -> recipeManager.isRecipeEnabled(recipe.getKey()))
                        .count();
            }));

            // Using custom names
            metrics.addCustomChart(new SimplePie("using_custom_names", () ->
                    useCraftedCustomNames ? "Yes" : "No"
            ));

            debug("bStats metrics initialized");
        }

        public RecipeWorldManager getRecipeWorldManager() {
            return recipeWorldManager;
        }

        public UpdateChecker getUpdateChecker() {
            return updateChecker;
        }

        /**
         * Initializes Updates
         */
        private void initializeUpdateChecker() {
            boolean enabled = getConfig().getBoolean("update-checker.enabled", true);
            if (!enabled) {
                return;
            }

            String sourceStr = getConfig().getString("update-checker.source", "GITHUB");
            UpdateSource source = UpdateSource.valueOf(sourceStr.toUpperCase());

            String spigotId = getConfig().getString("update-checker.spigot-resource-id", "");
            String githubRepo = getConfig().getString("update-checker.github-repo", "");

            updateChecker = new UpdateChecker(this, source, spigotId, githubRepo);
            updateChecker.startPeriodicCheck();

            getServer().getPluginManager().registerEvents(new UpdateNotifier(this, updateChecker), this);
            debug("Update checker initialized with periodic checks");
        }
    }
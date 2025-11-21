package org.hikarii.customrecipes.recipe.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.RecipeType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class VanillaRecipeManager {
    private final CustomRecipes plugin;
    private final File vanillaRecipesFolder;
    private final File vanillaRecipesDataFile;
    private final Map<String, VanillaRecipeInfo> allVanillaRecipes;
    private final Map<String, VanillaRecipeState> modifiedRecipes;

    public VanillaRecipeManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.vanillaRecipesFolder = new File(plugin.getDataFolder(), "vanillarecipes");
        this.vanillaRecipesDataFile = new File(plugin.getDataFolder(), "vanilla-recipes.yml");
        this.allVanillaRecipes = new LinkedHashMap<>();
        this.modifiedRecipes = new HashMap<>();
        if (!vanillaRecipesFolder.exists()) {
            vanillaRecipesFolder.mkdirs();
        }

        VanillaRecipesMigration migration = new VanillaRecipesMigration(plugin);
        if (!migration.checkAndMigrate()) {
            plugin.getLogger().warning("Vanilla recipes migration failed, but continuing...");
        }
        loadVanillaRecipesData();
        loadModifiedRecipes();
        applyModifications();
    }

    private void loadVanillaRecipesData() {
        if (!vanillaRecipesDataFile.exists()) {
            plugin.saveResource("vanilla-recipes.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(vanillaRecipesDataFile);
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            plugin.getLogger().warning("No recipes found in vanilla-recipes.yml");
            return;
        }

        int loaded = 0;
        for (String recipeKey : recipesSection.getKeys(false)) {
            try {
                ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeKey);
                if (recipeSection == null) continue;
                String displayName = recipeSection.getString("name", recipeKey);
                String materialStr = recipeSection.getString("material");
                int amount = recipeSection.getInt("amount", 1);
                String typeStr = recipeSection.getString("type", "SHAPED");
                List<String> patternStrings = recipeSection.getStringList("pattern");
                List<List<IngredientChoice>> ingredientGrid = new ArrayList<>();
                for (String rowString : patternStrings) {
                    List<IngredientChoice> row = new ArrayList<>();
                    String[] items = rowString.split(" ");
                    for (String item : items) {
                        row.add(IngredientChoice.fromString(item));
                    }
                    ingredientGrid.add(row);
                }
                String categoryStr = recipeSection.getString("category", "MISC");
                String stationStr = recipeSection.getString("station", "CRAFTING_TABLE");
                Material material = Material.getMaterial(materialStr);
                if (material == null) {
                    plugin.getLogger().warning("Invalid material for recipe " + recipeKey + ": " + materialStr);
                    continue;
                }
                Map<Integer, ItemStack> variantResults = new HashMap<>();
                ConfigurationSection resultsSection = recipeSection.getConfigurationSection("variant-results");
                if (resultsSection != null) {
                    for (String variantKey : resultsSection.getKeys(false)) {
                        try {
                            int variantIndex = Integer.parseInt(variantKey);
                            ConfigurationSection variantResultSection = resultsSection.getConfigurationSection(variantKey);
                            if (variantResultSection != null) {
                                String variantMaterial = variantResultSection.getString("material", materialStr);
                                Material mat = Material.getMaterial(variantMaterial);
                                if (mat == null) continue;
                                int variantAmount = variantResultSection.getInt("amount", 1);
                                ItemStack variantItem = new ItemStack(mat, variantAmount);
                                if (variantResultSection.contains("potion-effects")) {
                                    ItemMeta meta = variantItem.getItemMeta();
                                    if (meta instanceof org.bukkit.inventory.meta.SuspiciousStewMeta stewMeta) {
                                        List<Map<?, ?>> effectsList = variantResultSection.getMapList("potion-effects");
                                        for (Map<?, ?> effectMap : effectsList) {
                                            try {
                                                String effectName = (String) effectMap.get("type");
                                                int duration = effectMap.containsKey("duration") ?
                                                        ((Number) effectMap.get("duration")).intValue() : 160;
                                                int amplifier = effectMap.containsKey("amplifier") ?
                                                        ((Number) effectMap.get("amplifier")).intValue() : 0;
                                                org.bukkit.potion.PotionEffectType effectType =
                                                        org.bukkit.potion.PotionEffectType.getByName(effectName);
                                                if (effectType != null) {
                                                    stewMeta.addCustomEffect(
                                                            new org.bukkit.potion.PotionEffect(effectType, duration, amplifier),
                                                            true
                                                    );
                                                }
                                            } catch (Exception e) {
                                                plugin.getLogger().warning("Failed to parse effect: " + e.getMessage());
                                            }
                                        }
                                        variantItem.setItemMeta(stewMeta);
                                    }
                                }
                                variantResults.put(variantIndex, variantItem);
                            }
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid variant index: " + variantKey);
                        }
                    }
                }
                RecipeType type = RecipeType.fromString(typeStr);
                VanillaRecipeInfo.RecipeCategory category = VanillaRecipeInfo.RecipeCategory.valueOf(categoryStr);
                VanillaRecipeInfo.RecipeStation station = VanillaRecipeInfo.RecipeStation.valueOf(stationStr);
                VanillaRecipeInfo info = new VanillaRecipeInfo(
                        "minecraft:" + recipeKey,
                        displayName,
                        material,
                        amount,
                        type,
                        ingredientGrid,
                        category,
                        station,
                        variantResults
                );
                allVanillaRecipes.put(recipeKey, info);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load vanilla recipe " + recipeKey + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + loaded + " vanilla recipes from vanilla-recipes.yml");
    }

    private ItemStack createItemStackFromConfig(ConfigurationSection section) {
        String materialStr = section.getString("material");
        Material material = Material.getMaterial(materialStr);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialStr);
        }

        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);
        if (section.contains("nbt")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                ConfigurationSection nbtSection = section.getConfigurationSection("nbt");
                if (nbtSection != null) {
                    if (nbtSection.contains("suspicious_stew_effects")) {
                        List<Map<?, ?>> effects = nbtSection.getMapList("suspicious_stew_effects");
                        if (!effects.isEmpty()) {
                            Map<?, ?> firstEffect = effects.get(0);
                            String effectId = (String) firstEffect.get("id");
                            int duration = firstEffect.containsKey("duration") ?
                                    ((Number) firstEffect.get("duration")).intValue() : 160;
                            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
                            pdc.set(
                                    new org.bukkit.NamespacedKey(plugin, "stew_effect"),
                                    org.bukkit.persistence.PersistentDataType.STRING,
                                    effectId
                            );
                            pdc.set(
                                    new org.bukkit.NamespacedKey(plugin, "stew_duration"),
                                    org.bukkit.persistence.PersistentDataType.INTEGER,
                                    duration
                            );
                        }
                    }
                }
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    private void loadModifiedRecipes() {
        File[] files = vanillaRecipesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        int loaded = 0;
        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String recipeKey = file.getName().replace(".yml", "");
                boolean disabled = config.getBoolean("disabled", false);
                String originalKey = config.getString("original-recipe-key");
                int currentVariant = config.getInt("current-variant-index", 0);
                VanillaRecipeState state = new VanillaRecipeState(disabled, originalKey);
                state.setCurrentVariantIndex(currentVariant);
                if (config.contains("custom-result-amount")) {
                    state.setResultAmount(config.getInt("custom-result-amount"));
                }

                ConfigurationSection variantsSection = config.getConfigurationSection("variants");
                if (variantsSection != null) {
                    for (String variantKey : variantsSection.getKeys(false)) {
                        try {
                            int variantIndex = Integer.parseInt(variantKey);
                            List<String> pattern = variantsSection.getStringList(variantKey + ".pattern");
                            String typeStr = variantsSection.getString(variantKey + ".type", "SHAPED");
                            RecipeType type = RecipeType.fromString(typeStr);
                            state.setPatternForVariant(variantIndex, pattern);
                            state.setTypeForVariant(variantIndex, type);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                modifiedRecipes.put(recipeKey, state);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load modified recipe " + file.getName() + ": " + e.getMessage());
            }
        }
        if (loaded > 0) {
            plugin.getLogger().info("Loaded " + loaded + " modified vanilla recipes");
        }
    }

    private void applyModifications() {
        int disabled = 0;
        int changed = 0;
        for (Map.Entry<String, VanillaRecipeState> entry : modifiedRecipes.entrySet()) {
            String recipeKey = entry.getKey();
            VanillaRecipeState state = entry.getValue();
            if (state.isDisabled()) {
                NamespacedKey key = NamespacedKey.minecraft(recipeKey);
                if (Bukkit.removeRecipe(key)) {
                    disabled++;
                    plugin.debug("Disabled vanilla recipe: " + recipeKey);
                }
            } else if (state.hasChangedRecipe()) {
                NamespacedKey key = NamespacedKey.minecraft(recipeKey);
                Bukkit.removeRecipe(key);
                registerAllVariants(recipeKey, state);
                changed++;
                plugin.debug("Changed vanilla recipe with variants: " + recipeKey);
            }
        }
        if (disabled > 0 || changed > 0) {
            plugin.getLogger().info("Applied vanilla recipe modifications: " +
                    disabled + " disabled, " + changed + " changed");
        }
    }

    public void toggleRecipe(String recipeKey) {
        VanillaRecipeState state = modifiedRecipes.get(recipeKey);
        if (state == null) {
            state = new VanillaRecipeState(true, "minecraft:" + recipeKey);
            modifiedRecipes.put(recipeKey, state);
        } else {
            state.setDisabled(!state.isDisabled());
        }
        saveRecipeState(recipeKey, state);
        if (state.isDisabled()) {
            NamespacedKey originalKey = NamespacedKey.minecraft(recipeKey);
            Bukkit.removeRecipe(originalKey);
            VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
            if (info != null) {
                int maxVariants = getMaxVariantsForRecipe(recipeKey);
                for (int i = 0; i < maxVariants; i++) {
                    NamespacedKey variantKey = new NamespacedKey(plugin, recipeKey + "_variant_" + i);
                    Bukkit.removeRecipe(variantKey);
                }
            }
            plugin.debug("Disabled vanilla recipe: " + recipeKey);
        } else {
            VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
            if (info != null) {
                if (state.hasChangedRecipe()) {
                    registerAllVariants(recipeKey, state);
                    plugin.getLogger().info("Re-enabled modified recipe: " + recipeKey);
                } else {
                    NamespacedKey key = NamespacedKey.minecraft(recipeKey);
                    registerRecipe(key, info, info.getPattern(), 0);
                    plugin.getLogger().info("Re-enabled vanilla recipe: " + recipeKey);
                }
            }
        }
    }

    public void updateRecipeVariant(String recipeKey, int variantIndex, List<String> newPattern, RecipeType newType) {
        VanillaRecipeState state = modifiedRecipes.get(recipeKey);
        if (state == null) {
            state = new VanillaRecipeState(false, "minecraft:" + recipeKey);
            modifiedRecipes.put(recipeKey, state);
            initializeAllVariants(recipeKey, state);
        }
        state.setPatternForVariant(variantIndex, newPattern);
        state.setTypeForVariant(variantIndex, newType);
        state.setDisabled(false);
        saveRecipeState(recipeKey, state);
        NamespacedKey originalKey = NamespacedKey.minecraft(recipeKey);
        Bukkit.removeRecipe(originalKey);
        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info != null) {
            int maxVariants = getMaxVariantsForRecipe(recipeKey);
            for (int i = 0; i < maxVariants; i++) {
                NamespacedKey variantKey = new NamespacedKey(plugin, recipeKey + "_variant_" + i);
                Bukkit.removeRecipe(variantKey);
            }
        }
        registerAllVariants(recipeKey, state);
    }

    private void registerAllVariants(String recipeKey, VanillaRecipeState state) {
        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info == null) return;
        int maxVariants = getMaxVariantsForRecipe(recipeKey);
        NamespacedKey originalKey = NamespacedKey.minecraft(recipeKey);
        Bukkit.removeRecipe(originalKey);
        for (int i = 0; i < maxVariants; i++) {
            NamespacedKey key = new NamespacedKey(plugin, recipeKey + "_variant_" + i);
            Bukkit.removeRecipe(key);
        }

        for (int variantIndex = 0; variantIndex < maxVariants; variantIndex++) {
            List<String> pattern = state.getPatternForVariant(variantIndex);
            if (pattern == null || pattern.isEmpty()) {
                pattern = createPatternForVariant(info, variantIndex);
            }
            if (pattern != null && !pattern.isEmpty()) {
                NamespacedKey key = new NamespacedKey(plugin, recipeKey + "_variant_" + variantIndex);
                registerRecipe(key, info, pattern, variantIndex);
            }
        }
    }

    public void setCurrentVariant(String recipeKey, int variantIndex) {
        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info == null) return;
        for (int row = 0; row < info.getIngredientGrid().size(); row++) {
            List<IngredientChoice> rowList = info.getIngredientGrid().get(row);
            for (int col = 0; col < rowList.size(); col++) {
                IngredientChoice choice = rowList.get(col);
                if (choice.hasMultipleOptions() && variantIndex < choice.getOptions().size()) {
                    choice.setSelectedIndex(variantIndex);
                }
            }
        }
        VanillaRecipeState state = modifiedRecipes.get(recipeKey);
        if (state != null) {
            state.setCurrentVariantIndex(variantIndex);
            saveRecipeState(recipeKey, state);
        }
    }

    public int getMaxVariantsForRecipe(String recipeKey) {
        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info == null) return 1;
        int maxVariants = 1;
        for (List<IngredientChoice> row : info.getIngredientGrid()) {
            for (IngredientChoice choice : row) {
                if (choice.hasMultipleOptions()) {
                    maxVariants = Math.max(maxVariants, choice.getOptions().size());
                }
            }
        }
        return maxVariants;
    }

    private void initializeAllVariants(String recipeKey, VanillaRecipeState state) {
        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info == null) return;
        int maxVariants = getMaxVariantsForRecipe(recipeKey);
        for (int variantIndex = 0; variantIndex < maxVariants; variantIndex++) {
            if (state.getPatternForVariant(variantIndex) == null) {
                List<String> variantPattern = createPatternForVariant(info, variantIndex);
                state.setPatternForVariant(variantIndex, variantPattern);
                state.setTypeForVariant(variantIndex, info.getType());
            }
        }
    }

    private List<String> createAnyVariantPattern(VanillaRecipeInfo info) {
        List<String> pattern = new ArrayList<>();
        for (List<IngredientChoice> row : info.getIngredientGrid()) {
            StringBuilder rowPattern = new StringBuilder();
            for (IngredientChoice choice : row) {
                if (rowPattern.length() > 0) {
                    rowPattern.append(" ");
                }
                if (choice.hasMultipleOptions()) {
                    StringBuilder options = new StringBuilder();
                    for (Material mat : choice.getOptions()) {
                        if (options.length() > 0) options.append("|");
                        options.append(mat.name());
                    }
                    rowPattern.append(options);
                } else {
                    rowPattern.append(choice.getSelected().name());
                }
            }
            pattern.add(rowPattern.toString());
        }
        return pattern;
    }

    private List<String> createPatternForVariant(VanillaRecipeInfo info, int variantIndex) {
        List<String> pattern = new ArrayList<>();
        for (List<IngredientChoice> row : info.getIngredientGrid()) {
            StringBuilder rowPattern = new StringBuilder();
            for (IngredientChoice choice : row) {
                if (rowPattern.length() > 0) {
                    rowPattern.append(" ");
                }
                if (choice.hasMultipleOptions() && variantIndex < choice.getOptions().size()) {
                    rowPattern.append(choice.getOptions().get(variantIndex).name());
                } else if (choice.hasMultipleOptions() && variantIndex >= choice.getOptions().size()) {
                    rowPattern.append(choice.getOptions().get(0).name());
                } else {
                    rowPattern.append(choice.getSelected().name());
                }
            }
            pattern.add(rowPattern.toString());
        }
        return pattern;
    }

    public void resetRecipe(String recipeKey) {
        modifiedRecipes.remove(recipeKey);
        File file = new File(vanillaRecipesFolder, recipeKey + ".yml");
        if (file.exists()) {
            file.delete();
        }

        VanillaRecipeInfo info = allVanillaRecipes.get(recipeKey);
        if (info != null) {
            int maxVariants = getMaxVariantsForRecipe(recipeKey);
            for (int i = 0; i < maxVariants; i++) {
                NamespacedKey variantKey = new NamespacedKey(plugin, recipeKey + "_variant_" + i);
                Bukkit.removeRecipe(variantKey);
            }
        }
        NamespacedKey key = NamespacedKey.minecraft(recipeKey);
        Bukkit.removeRecipe(key);
        if (info != null) {
            registerOriginalRecipe(key, info);
            plugin.getLogger().info("Reset vanilla recipe: " + recipeKey);
        }
    }

    private boolean registerChangedRecipe(NamespacedKey key, VanillaRecipeInfo info, VanillaRecipeState state) {
        return registerRecipe(key, info, state.getChangedPattern(), state.getCurrentVariantIndex());
    }

    private boolean registerOriginalRecipe(NamespacedKey key, VanillaRecipeInfo info) {
        return registerRecipe(key, info, info.getPattern(), 0);
    }

    private boolean registerRecipe(NamespacedKey key, VanillaRecipeInfo info, List<String> pattern, int variantIndex) {
        try {
            ItemStack resultItem = info.hasVariantResults() ?
                    info.getResultForVariant(variantIndex) :
                    new ItemStack(info.getResultMaterial(), info.getResultAmount());
            if (info.getType() == RecipeType.SHAPED) {
                ShapedRecipe recipe = new ShapedRecipe(key, resultItem);
                Map<Material, Character> materialToChar = new HashMap<>();
                char currentChar = 'A';
                List<String> bukkitPattern = new ArrayList<>();
                for (String row : pattern) {
                    StringBuilder bukkitRow = new StringBuilder();
                    String[] items = row.split(" ");
                    for (String item : items) {
                        if (item.equals("AIR") || item.isEmpty()) {
                            bukkitRow.append(' ');
                            continue;
                        }
                        String firstMaterial = item.contains("|") ? item.split("\\|")[0] : item;
                        Material mat = Material.getMaterial(firstMaterial);
                        if (mat == null || mat == Material.AIR) {
                            bukkitRow.append(' ');
                        } else {
                            if (!materialToChar.containsKey(mat)) {
                                materialToChar.put(mat, currentChar);
                                currentChar++;
                            }
                            bukkitRow.append(materialToChar.get(mat));
                        }
                    }
                    bukkitPattern.add(bukkitRow.toString());
                }
                if (bukkitPattern.isEmpty()) {
                    return false;
                }

                if (bukkitPattern.size() == 1) {
                    recipe.shape(bukkitPattern.get(0));
                } else if (bukkitPattern.size() == 2) {
                    recipe.shape(bukkitPattern.get(0), bukkitPattern.get(1));
                } else if (bukkitPattern.size() == 3) {
                    recipe.shape(bukkitPattern.get(0), bukkitPattern.get(1), bukkitPattern.get(2));
                }

                for (Map.Entry<Material, Character> entry : materialToChar.entrySet()) {
                    recipe.setIngredient(entry.getValue(), entry.getKey());
                }
                Bukkit.addRecipe(recipe);
                return true;
            } else if (info.getType() == RecipeType.SHAPELESS) {
                org.bukkit.inventory.ShapelessRecipe recipe =
                        new org.bukkit.inventory.ShapelessRecipe(key, resultItem);
                for (String row : pattern) {
                    String[] items = row.split(" ");
                    for (String item : items) {
                        Material mat = Material.getMaterial(item);
                        if (mat != null && mat != Material.AIR) {
                            recipe.addIngredient(mat);
                        }
                    }
                }
                Bukkit.addRecipe(recipe);
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register recipe " + key.getKey() + ": " + e.getMessage());
        }
        return false;
    }

    private void saveRecipeState(String recipeKey, VanillaRecipeState state) {
        try {
            File file = new File(vanillaRecipesFolder, recipeKey + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("disabled", state.isDisabled());
            config.set("original-recipe-key", state.getOriginalRecipeKey());
            config.set("current-variant-index", state.getCurrentVariantIndex());
            if (state.getCustomResultAmount() != null) {
                config.set("custom-result-amount", state.getCustomResultAmount());
            }

            for (Map.Entry<Integer, List<String>> entry : state.getAllVariantPatterns().entrySet()) {
                int variantIndex = entry.getKey();
                config.set("variants." + variantIndex + ".pattern", entry.getValue());
                config.set("variants." + variantIndex + ".type",
                        state.getTypeForVariant(variantIndex).name());
            }
            config.save(file);
            plugin.debug("Saved vanilla recipe state: " + recipeKey);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save vanilla recipe state: " + e.getMessage());
        }
    }

    public Map<String, VanillaRecipeInfo> getAllVanillaRecipes() {
        return Collections.unmodifiableMap(allVanillaRecipes);
    }

    public VanillaRecipeInfo getRecipeInfo(String recipeKey) {
        return allVanillaRecipes.get(recipeKey);
    }

    public VanillaRecipeState getRecipeState(String recipeKey) {
        return modifiedRecipes.get(recipeKey);
    }

    public boolean isRecipeDisabled(String recipeKey) {
        VanillaRecipeState state = modifiedRecipes.get(recipeKey);
        return state != null && state.isDisabled();
    }

    public boolean isRecipeChanged(String recipeKey) {
        VanillaRecipeState state = modifiedRecipes.get(recipeKey);
        return state != null && state.hasChangedRecipe();
    }

    public List<VanillaRecipeInfo> searchRecipes(String query) {
        String lowerQuery = query.toLowerCase();
        return allVanillaRecipes.values().stream()
                .filter(recipe -> recipe.getDisplayName().toLowerCase().contains(lowerQuery) ||
                        recipe.getResultMaterial().name().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public List<VanillaRecipeInfo> getRecipesByCategory(VanillaRecipeInfo.RecipeCategory category) {
        return allVanillaRecipes.values().stream()
                .filter(recipe -> recipe.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<VanillaRecipeInfo> getRecipesByStation(VanillaRecipeInfo.RecipeStation station) {
        return allVanillaRecipes.values().stream()
                .filter(recipe -> recipe.getStation() == station)
                .collect(Collectors.toList());
    }

    public static class VanillaRecipeState {
        private boolean disabled;
        private final String originalRecipeKey;
        private int currentVariantIndex;
        private Map<Integer, List<String>> variantPatterns;
        private Map<Integer, RecipeType> variantTypes;
        private Integer customResultAmount;

        public VanillaRecipeState(boolean disabled, String originalRecipeKey) {
            this.disabled = disabled;
            this.originalRecipeKey = originalRecipeKey;
            this.currentVariantIndex = 0;
            this.variantPatterns = new HashMap<>();
            this.variantTypes = new HashMap<>();
            this.customResultAmount = null;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public String getOriginalRecipeKey() {
            return originalRecipeKey;
        }

        public int getCurrentVariantIndex() {
            return currentVariantIndex;
        }

        public void setCurrentVariantIndex(int index) {
            this.currentVariantIndex = index;
        }

        public List<String> getChangedPattern() {
            return variantPatterns.get(currentVariantIndex);
        }

        public List<String> getPatternForVariant(int variantIndex) {
            return variantPatterns.get(variantIndex);
        }

        public void setPatternForVariant(int variantIndex, List<String> pattern) {
            variantPatterns.put(variantIndex, pattern);
        }

        public RecipeType getChangedType() {
            return variantTypes.getOrDefault(currentVariantIndex, RecipeType.SHAPED);
        }

        public RecipeType getTypeForVariant(int variantIndex) {
            return variantTypes.getOrDefault(variantIndex, RecipeType.SHAPED);
        }

        public void setTypeForVariant(int variantIndex, RecipeType type) {
            variantTypes.put(variantIndex, type);
        }

        public boolean hasChangedRecipe() {
            return !variantPatterns.isEmpty();
        }

        public Map<Integer, List<String>> getAllVariantPatterns() {
            return variantPatterns;
        }

        public Map<Integer, RecipeType> getAllVariantTypes() {
            return variantTypes;
        }

        public void setResultAmount(int amount) {
            this.customResultAmount = amount;
        }

        public Integer getCustomResultAmount() {
            return customResultAmount;
        }
    }
}
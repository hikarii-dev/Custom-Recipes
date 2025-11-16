package org.hikarii.customrecipes.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.recipe.data.ShapelessRecipeData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads recipes from YAML configuration
 */
public class RecipeConfigLoader {

    private final CustomRecipes plugin;

    public RecipeConfigLoader(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a single recipe from configuration
     *
     * @param key the recipe key
     * @param section the configuration section for this recipe
     * @return the loaded recipe
     * @throws ValidationException if the recipe is invalid
     */
    public CustomRecipe loadRecipe(String key, ConfigurationSection section) throws ValidationException {
        if (section == null) {
            throw new ValidationException("Recipe '" + key + "' has no configuration section");
        }

        try {
            // Load GUI display properties (always used in GUI)
            String guiName = section.getString("gui-name");
            List<String> guiDescription = section.getStringList("gui-description");

            // Load crafted item properties (optional, used on crafted items)
            String craftedName = section.getString("crafted-name");
            List<String> craftedDescription = section.getStringList("crafted-description");

            // Backward compatibility: if gui-name missing, try old 'name' field
            if (guiName == null || guiName.isEmpty()) {
                guiName = section.getString("name");
            }
            if (guiDescription == null || guiDescription.isEmpty()) {
                guiDescription = section.getStringList("description");
            }

            // Load recipe type (default to SHAPED)
            String typeStr = section.getString("type", "SHAPED");
            RecipeType type = RecipeType.fromString(typeStr);

            // Load result item
            ItemStack resultItem;

            // Try loading full ItemStack first (new format)
            if (section.contains("result")) {
                try {
                    Object resultObj = section.get("result");
                    Map<String, Object> resultMap = null;

                    if (resultObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) resultObj;
                        resultMap = map;
                    } else if (resultObj instanceof ConfigurationSection) {
                        // Convert ConfigurationSection to Map recursively
                        ConfigurationSection resultSection = (ConfigurationSection) resultObj;
                        resultMap = configSectionToMap(resultSection);
                    }

                    if (resultMap != null) {
                        resultItem = ItemStackSerializer.fromMap(resultMap);
                    } else {
                        throw new ValidationException("Invalid result format - not a Map or ConfigurationSection");
                    }
                } catch (Exception e) {
                    throw new ValidationException("Failed to load result item: " + e.getMessage(), e);
                }
            } else {
                // Legacy format - load material and amount separately
                String materialStr = section.getString("material");
                if (materialStr == null || materialStr.isEmpty()) {
                    throw new ValidationException("Recipe '" + key + "' is missing 'material' field");
                }

                Material resultMaterial = Material.getMaterial(materialStr.toUpperCase());
                if (resultMaterial == null) {
                    throw new ValidationException("Recipe '" + key + "' has invalid material: " + materialStr);
                }

                int resultAmount = section.getInt("amount", 1);
                if (resultAmount < 1 || resultAmount > 64) {
                    throw new ValidationException("Recipe '" + key + "' amount must be between 1 and 64");
                }

                resultItem = new ItemStack(resultMaterial, resultAmount);
            }

            // Load hidden status (default to false)
            boolean hidden = section.getBoolean("hidden", false);

            // Load recipe data based on type
            ShapedRecipeData recipeData = null;
            ShapelessRecipeData shapelessData = null;

            if (type == RecipeType.SHAPED) {
                recipeData = loadShapedRecipeData(key, section);
            } else if (type == RecipeType.SHAPELESS) {
                shapelessData = loadShapelessRecipeData(key, section);
            } else {
                throw new ValidationException("Recipe type '" + type + "' is not yet supported");
            }

            // Create and return the recipe
            return new CustomRecipe(key, guiName, guiDescription, craftedName, craftedDescription,
                    type, recipeData, shapelessData, resultItem, hidden);

        } catch (IllegalArgumentException e) {
            throw new ValidationException("Error loading recipe '" + key + "': " + e.getMessage(), e);
        }
    }

    /**
     * Loads shaped recipe data from configuration
     *
     * @param key the recipe key
     * @param section the configuration section
     * @return the shaped recipe data
     * @throws ValidationException if the data is invalid
     */
    private ShapedRecipeData loadShapedRecipeData(String key, ConfigurationSection section)
            throws ValidationException {

        List<String> recipePattern = section.getStringList("recipe");
        if (recipePattern.isEmpty() || recipePattern.size() > 3) {
            throw new ValidationException("Recipe '" + key + "' has invalid recipe data: Recipe must have 1-3 rows");
        }

        // Pad to 3 rows if needed
        while (recipePattern.size() < 3) {
            recipePattern.add("AIR AIR AIR");
        }

        // Pad each row to 3 items if needed
        for (int i = 0; i < recipePattern.size(); i++) {
            String row = recipePattern.get(i);
            String[] items = row.split(" ");

            if (items.length < 3) {
                StringBuilder paddedRow = new StringBuilder(row);
                for (int j = items.length; j < 3; j++) {
                    if (paddedRow.length() > 0) paddedRow.append(" ");
                    paddedRow.append("AIR");
                }
                recipePattern.set(i, paddedRow.toString());
            }
        }

        // Now convert to ShapedRecipeData
        try {
            return ShapedRecipeData.fromConfigList(recipePattern);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Recipe '" + key + "' has invalid recipe data: " + e.getMessage(), e);
        }
    }

    /**
     * Loads shapeless recipe data from config section
     */
    private ShapelessRecipeData loadShapelessRecipeData(String key, ConfigurationSection section)
            throws ValidationException {

        List<String> ingredientList = section.getStringList("ingredients");
        if (ingredientList.isEmpty()) {
            throw new ValidationException("Recipe '" + key + "' has no ingredients");
        }

        return ShapelessRecipeData.fromConfigList(ingredientList);
    }

    /**
     * Recursively converts ConfigurationSection to Map
     */
    private Map<String, Object> configSectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                // Recursively convert nested sections
                map.put(key, configSectionToMap((ConfigurationSection) value));
            } else if (value instanceof List) {
                // Handle lists
                map.put(key, value);
            } else {
                // Primitive values
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * Validates a recipe key
     *
     * @param key the key to validate
     * @throws ValidationException if the key is invalid
     */
    public void validateKey(String key) throws ValidationException {
        if (key == null || key.isEmpty()) {
            throw new ValidationException("Recipe key cannot be empty");
        }

        if (!key.matches("[a-zA-Z0-9_-]+")) {
            throw new ValidationException(
                    "Recipe key '" + key + "' contains invalid characters. Only letters, numbers, underscores, and hyphens are allowed."
            );
        }
    }
}
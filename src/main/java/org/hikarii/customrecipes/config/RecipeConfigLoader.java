package org.hikarii.customrecipes.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;

import java.util.ArrayList;
import java.util.List;

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

            // Load result material
            String materialStr = section.getString("material");
            if (materialStr == null || materialStr.isEmpty()) {
                throw new ValidationException("Recipe '" + key + "' is missing 'material' field");
            }

            Material resultMaterial = Material.getMaterial(materialStr.toUpperCase());
            if (resultMaterial == null) {
                throw new ValidationException("Recipe '" + key + "' has invalid material: " + materialStr);
            }

            // Load result amount (default to 1)
            int resultAmount = section.getInt("amount", 1);
            if (resultAmount < 1 || resultAmount > 64) {
                throw new ValidationException("Recipe '" + key + "' amount must be between 1 and 64");
            }

            // Load recipe data based on type
            ShapedRecipeData recipeData;
            if (type == RecipeType.SHAPED) {
                recipeData = loadShapedRecipeData(key, section);
            } else {
                throw new ValidationException("Recipe type '" + type + "' is not yet supported");
            }

            // Create and return the recipe
            return new CustomRecipe(key, guiName, guiDescription, craftedName, craftedDescription,
                    type, recipeData, resultMaterial, resultAmount);

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

        List<?> recipeList = section.getList("recipe");
        if (recipeList == null || recipeList.isEmpty()) {
            throw new ValidationException("Recipe '" + key + "' is missing 'recipe' field");
        }

        try {
            return ShapedRecipeData.fromConfigList(recipeList);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Recipe '" + key + "' has invalid recipe data: " + e.getMessage(), e);
        }
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
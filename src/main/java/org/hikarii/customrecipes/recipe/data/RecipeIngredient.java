package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;

/**
 * Represents a single ingredient in a recipe slot
 *
 * @param material the material type
 */
public record RecipeIngredient(Material material) {

    /**
     * Creates an empty ingredient (AIR)
     *
     * @return empty ingredient
     */
    public static RecipeIngredient empty() {
        return new RecipeIngredient(Material.AIR);
    }

    /**
     * Checks if this ingredient is empty
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return material == null || material == Material.AIR;
    }

    /**
     * Creates ingredient from string material name
     *
     * @param materialName the material name
     * @return the ingredient
     * @throws IllegalArgumentException if material is invalid
     */
    public static RecipeIngredient fromString(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return empty();
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + materialName);
        }

        return new RecipeIngredient(material);
    }
}
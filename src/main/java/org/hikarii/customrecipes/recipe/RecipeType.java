package org.hikarii.customrecipes.recipe;

/**
 * Enum representing different types of recipes
 */
public enum RecipeType {
    /**
     * Shaped recipe - requires specific pattern
     */
    SHAPED,

    /**
     * Shapeless recipe - any arrangement works
     */
    SHAPELESS;

    /**
     * Gets recipe type from string
     *
     * @param type the type string
     * @return the RecipeType, defaults to SHAPED if invalid
     */
    public static RecipeType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SHAPED; // Default to shaped
        }
    }
}
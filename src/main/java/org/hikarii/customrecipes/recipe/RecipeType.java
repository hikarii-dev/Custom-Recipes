package org.hikarii.customrecipes.recipe;

public enum RecipeType {
    SHAPED,
    SHAPELESS;
    public static RecipeType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SHAPED;
        }
    }
}
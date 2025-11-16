package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the pattern data for a shaped recipe
 *
 * @param ingredients 3x3 grid of ingredients (9 total)
 */
public record ShapedRecipeData(List<RecipeIngredient> ingredients) {

    /**
     * Validates the recipe data
     */
    public ShapedRecipeData {
        if (ingredients == null || ingredients.size() != 9) {
            throw new IllegalArgumentException("Shaped recipe must have exactly 9 ingredients (3x3 grid)");
        }
    }

    /**
     * Gets ingredient at specific position
     *
     * @param row the row (0-2)
     * @param col the column (0-2)
     * @return the ingredient
     */
    public RecipeIngredient getIngredient(int row, int col) {
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            throw new IndexOutOfBoundsException("Row and column must be between 0 and 2");
        }
        return ingredients.get(row * 3 + col);
    }

    /**
     * Converts the recipe data to Bukkit shape format
     *
     * @return array of 3 strings representing the shape
     */
    public String[] toShapeArray() {
        Map<Material, Character> materialToChar = new HashMap<>();
        char currentChar = 'A';

        // Build material to character mapping
        for (RecipeIngredient ingredient : ingredients) {
            Material material = ingredient.material();
            if (!materialToChar.containsKey(material)) {
                materialToChar.put(material, currentChar++);
            }
        }

        // Build shape strings
        String[] shape = new String[3];
        for (int row = 0; row < 3; row++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                Material material = getIngredient(row, col).material();
                rowBuilder.append(materialToChar.get(material));
            }
            shape[row] = rowBuilder.toString();
        }

        return shape;
    }

    /**
     * Gets the ingredient mapping for Bukkit recipe
     *
     * @return map of characters to materials
     */
    public Map<Character, Material> getIngredientMap() {
        Map<Material, Character> materialToChar = new HashMap<>();
        Map<Character, Material> charToMaterial = new HashMap<>();
        char currentChar = 'A';

        for (RecipeIngredient ingredient : ingredients) {
            Material material = ingredient.material();
            if (!materialToChar.containsKey(material)) {
                materialToChar.put(material, currentChar);
                charToMaterial.put(currentChar, material);
                currentChar++;
            }
        }

        return charToMaterial;
    }

    /**
     * Creates ShapedRecipeData from config list
     *
     * @param rows list of 3 rows, each containing 3 space-separated materials
     * @return the shaped recipe data
     * @throws IllegalArgumentException if format is invalid
     */
    public static ShapedRecipeData fromConfigList(List<?> rows) {
        if (rows == null || rows.size() != 3) {
            throw new IllegalArgumentException("Recipe must have exactly 3 rows");
        }

        List<RecipeIngredient> ingredients = new ArrayList<>(9);

        for (int i = 0; i < 3; i++) {
            String row = rows.get(i).toString();
            String[] materials = row.split(" ");

            if (materials.length != 3) {
                throw new IllegalArgumentException(
                        "Row " + (i + 1) + " must have exactly 3 materials (found " + materials.length + ")"
                );
            }

            for (int j = 0; j < 3; j++) {
                try {
                    ingredients.add(RecipeIngredient.fromString(materials[j]));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Invalid material at row " + (i + 1) + ", col " + (j + 1) + ": " + e.getMessage()
                    );
                }
            }
        }

        return new ShapedRecipeData(ingredients);
    }
}
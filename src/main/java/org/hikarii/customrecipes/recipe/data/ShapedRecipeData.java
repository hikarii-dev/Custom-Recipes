package org.hikarii.customrecipes.recipe.data;

import org.hikarii.customrecipes.config.ValidationException;
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
        // Find bounds to create minimal pattern
        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                RecipeIngredient ingredient = ingredients.get(row * 3 + col);
                if (ingredient.material() != Material.AIR) {
                    if (row < minRow) minRow = row;
                    if (row > maxRow) maxRow = row;
                    if (col < minCol) minCol = col;
                    if (col > maxCol) maxCol = col;
                }
            }
        }

        // If no items, return single row with one space
        if (minRow > maxRow) {
            return new String[]{" "};
        }

        int height = maxRow - minRow + 1;
        int width = maxCol - minCol + 1;
        String[] shape = new String[height];

        for (int row = 0; row < height; row++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                RecipeIngredient ingredient = ingredients.get((minRow + row) * 3 + (minCol + col));
                if (ingredient.material() == Material.AIR) {
                    rowBuilder.append(' '); // SPACE for empty
                } else {
                    rowBuilder.append(getCharForMaterial(ingredient.material()));
                }
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
            if (material == Material.AIR) {
                continue;
            }
            if (!materialToChar.containsKey(material)) {
                materialToChar.put(material, currentChar);
                charToMaterial.put(currentChar, material);
                currentChar++;
            }
        }

        return charToMaterial;
    }

    /**
     * Gets character for a material (or space for AIR)
     */
    private char getCharForMaterial(Material material) {
        if (material == Material.AIR) {
            return ' ';
        }

        Map<Material, Character> materialToChar = new HashMap<>();
        char currentChar = 'A';

        for (RecipeIngredient ingredient : ingredients) {
            Material mat = ingredient.material();
            if (mat == Material.AIR) {
                continue;
            }
            if (!materialToChar.containsKey(mat)) {
                materialToChar.put(mat, currentChar);
                currentChar++;
            }
        }

        return materialToChar.getOrDefault(material, ' ');
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

    /**
     * Creates ShapedRecipeData from a pattern array
     *
     * @param pattern array of 3 strings representing rows
     * @return new ShapedRecipeData instance
     * @throws ValidationException if pattern is invalid
     */
    public static ShapedRecipeData fromPattern(String[] pattern) throws ValidationException {
        if (pattern == null || pattern.length == 0) {
            throw new ValidationException("Pattern cannot be empty");
        }

        if (pattern.length > 3) {
            throw new ValidationException("Pattern cannot have more than 3 rows");
        }

        // Parse pattern into RecipeIngredient list
        List<RecipeIngredient> ingredients = new ArrayList<>();

        for (String row : pattern) {
            if (row == null || row.trim().isEmpty()) {
                row = "AIR AIR AIR";
            }

            String[] items = row.split(" ");

            // Pad row to 3 items if needed
            int itemCount = Math.min(items.length, 3);
            for (int i = 0; i < 3; i++) {
                String itemName = i < itemCount ? items[i] : "AIR";

                Material material = Material.getMaterial(itemName.toUpperCase());
                if (material == null) {
                    throw new ValidationException("Invalid material: " + itemName);
                }
                ingredients.add(new RecipeIngredient(material));
            }
        }

        // Pad to 3 rows if needed
        while (ingredients.size() < 9) {
            ingredients.add(new RecipeIngredient(Material.AIR));
        }

        return new ShapedRecipeData(ingredients);
    }
}
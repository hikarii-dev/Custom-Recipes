package org.hikarii.customrecipes.recipe.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.config.ValidationException;
import org.bukkit.Material;
import java.util.*;

public record ShapedRecipeData(List<RecipeIngredient> ingredients, List<ItemStack> exactItems) {
    public ShapedRecipeData {
        if (ingredients == null || ingredients.size() != 9) {
            throw new IllegalArgumentException("Shaped recipe must have exactly 9 ingredients (3x3 grid)");
        }
        if (exactItems == null) {
            exactItems = new ArrayList<>();
        }
    }

    public RecipeIngredient getIngredient(int row, int col) {
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            throw new IndexOutOfBoundsException("Row and column must be between 0 and 2");
        }
        return ingredients.get(row * 3 + col);
    }

    public String[] toShapeArray() {
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
                    rowBuilder.append(' ');
                } else {
                    rowBuilder.append(getCharForMaterial(ingredient.material()));
                }
            }
            shape[row] = rowBuilder.toString();
        }
        return shape;
    }

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

    public static ShapedRecipeData fromConfigList(List<?> rows, List<ItemStack> exactItems) {
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
                int index = i * 3 + j;
                ItemStack exactItem = null;
                if (exactItems != null && index < exactItems.size()) {
                    exactItem = exactItems.get(index);
                }
                try {
                    RecipeIngredient baseIngredient = RecipeIngredient.fromString(materials[j]);
                    if (exactItem != null && exactItem.getType() != Material.AIR && exactItem.hasItemMeta()) {
                        ItemMeta meta = exactItem.getItemMeta();
                        boolean shouldSaveExact = false;
                        if (meta.hasEnchants()) {
                            shouldSaveExact = true;
                        }
                        if (exactItem.getType() == Material.ENCHANTED_BOOK &&
                                meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                            org.bukkit.inventory.meta.EnchantmentStorageMeta bookMeta =
                                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) meta;
                            if (bookMeta.hasStoredEnchants()) {
                                shouldSaveExact = true;
                                System.out.println("DEBUG: Found ENCHANTED_BOOK with StoredEnchants at index " + index +
                                        ": " + bookMeta.getStoredEnchants());
                            }
                        }
                        if (!meta.getPersistentDataContainer().getKeys().isEmpty()) {
                            shouldSaveExact = true;
                        }

                        if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                            org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                            if (damageable.hasDamage() && damageable.getDamage() > 0) {
                                shouldSaveExact = true;
                            }
                        }

                        if (shouldSaveExact) {
                            ingredients.add(new RecipeIngredient(exactItem));
                            System.out.println("DEBUG: Saved exactItem at index " + index + ": " +
                                    exactItem.getType() + " with enchants: " + meta.hasEnchants());
                        } else {
                            ingredients.add(baseIngredient);
                        }
                    } else {
                        ingredients.add(baseIngredient);
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Invalid material at row " + (i + 1) + ", col " + (j + 1) + ": " + e.getMessage()
                    );
                }
            }
        }
        return new ShapedRecipeData(ingredients, exactItems);
    }

    public static ShapedRecipeData fromPattern(String[] pattern) throws ValidationException {
        if (pattern == null || pattern.length == 0) {
            throw new ValidationException("Pattern cannot be empty");
        }

        if (pattern.length > 3) {
            throw new ValidationException("Pattern cannot have more than 3 rows");
        }

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (String row : pattern) {
            if (row == null || row.trim().isEmpty()) {
                row = "AIR AIR AIR";
            }
            String[] items = row.split(" ");
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
        while (ingredients.size() < 9) {
            ingredients.add(new RecipeIngredient(Material.AIR));
        }
        return new ShapedRecipeData(ingredients, null);
    }
}
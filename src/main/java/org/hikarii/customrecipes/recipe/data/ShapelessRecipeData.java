package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;
import org.hikarii.customrecipes.config.ValidationException;
import java.util.*;

/**
 * Holds shapeless recipe ingredient data
 */
public record ShapelessRecipeData(Map<Material, Integer> ingredients) {

    /**
     * Creates shapeless recipe data from ingredient map
     *
     * @param ingredients map of material to count
     */
    public ShapelessRecipeData {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be null or empty");
        }

        // Validate max 9 total items (3x3 grid)
        int totalItems = ingredients.values().stream().mapToInt(Integer::intValue).sum();
        if (totalItems > 9) {
            throw new IllegalArgumentException("Too many ingredients (max 9 items)");
        }

        ingredients = Collections.unmodifiableMap(new HashMap<>(ingredients));
    }

    /**
     * Creates from config format list
     * Example: ["STICK:3", "DIAMOND:2"]
     *
     * @param ingredientList list of "MATERIAL:COUNT" strings
     * @return new ShapelessRecipeData
     * @throws ValidationException if format is invalid
     */
    public static ShapelessRecipeData fromConfigList(List<String> ingredientList) throws ValidationException {
        Map<Material, Integer> ingredients = new HashMap<>();

        for (String entry : ingredientList) {
            String[] parts = entry.split(":");
            if (parts.length != 2) {
                throw new ValidationException("Invalid ingredient format: " + entry + " (expected MATERIAL:COUNT)");
            }

            Material material = Material.getMaterial(parts[0].toUpperCase());
            if (material == null) {
                throw new ValidationException("Invalid material: " + parts[0]);
            }

            int count;
            try {
                count = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid count: " + parts[1]);
            }

            if (count < 1 || count > 9) {
                throw new ValidationException("Count must be 1-9: " + count);
            }

            ingredients.put(material, count);
        }
        return new ShapelessRecipeData(ingredients);
    }

    /**
     * Creates from GUI grid items
     * Counts how many of each material type
     *
     * @param gridItems array of 9 ItemStacks from grid
     * @return new ShapelessRecipeData
     */
    public static ShapelessRecipeData fromGridItems(org.bukkit.inventory.ItemStack[] gridItems) {
        Map<Material, Integer> counts = new HashMap<>();

        for (org.bukkit.inventory.ItemStack item : gridItems) {
            if (item != null && item.getType() != Material.AIR) {
                Material mat = item.getType();
                counts.put(mat, counts.getOrDefault(mat, 0) + 1);
            }
        }

        return new ShapelessRecipeData(counts);
    }

    /**
     * Converts to config format list
     *
     * @return list of "MATERIAL:COUNT" strings
     */
    public List<String> toConfigList() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            list.add(entry.getKey().name() + ":" + entry.getValue());
        }
        return list;
    }
}
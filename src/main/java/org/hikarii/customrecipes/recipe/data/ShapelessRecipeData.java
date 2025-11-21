package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;
import org.hikarii.customrecipes.config.ValidationException;
import java.util.*;

public record ShapelessRecipeData(Map<Material, Integer> ingredients) {
    public ShapelessRecipeData {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Ingredients cannot be null or empty");
        }

        int totalItems = ingredients.values().stream().mapToInt(Integer::intValue).sum();
        if (totalItems > 9) {
            throw new IllegalArgumentException("Too many ingredients (max 9 items)");
        }
        ingredients = Collections.unmodifiableMap(new HashMap<>(ingredients));
    }

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

    public List<String> toConfigList() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            list.add(entry.getKey().name() + ":" + entry.getValue());
        }
        return list;
    }
}
package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class RecipeIngredient {
    private final Material material;
    private final int amount;
    private final ItemStack exactItem;

    public RecipeIngredient(Material material, int amount, ItemStack exactItem) {
        if (amount < 1 || amount > 64) {
            throw new IllegalArgumentException("Amount must be between 1 and 64");
        }
        this.material = material;
        this.amount = amount;
        this.exactItem = exactItem;
    }

    public RecipeIngredient(ItemStack exactItem) {
        this(exactItem.getType(), exactItem.getAmount(), exactItem.clone());
    }

    public RecipeIngredient(Material material, int amount) {
        this(material, amount, null);
    }

    public RecipeIngredient(Material material) {
        this(material, 1, null);
    }

    public static RecipeIngredient empty() {
        return new RecipeIngredient(Material.AIR, 1, null);
    }

    public boolean isEmpty() {
        return material == null || material == Material.AIR;
    }

    public Material material() {
        return material;
    }

    public int amount() {
        return amount;
    }

    public ItemStack getExactItem() {
        return exactItem != null ? exactItem.clone() : null;
    }

    public boolean hasExactItem() {
        return exactItem != null;
    }

    public static RecipeIngredient fromString(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return empty();
        }

        String[] parts = materialName.split(":");
        Material material = Material.getMaterial(parts[0].toUpperCase());
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + parts[0]);
        }

        int amount = 1;
        if (parts.length > 1) {
            try {
                amount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount: " + parts[1]);
            }
        }
        return new RecipeIngredient(material, amount, null);
    }

    @Override
    public String toString() {
        return material.name() + (amount > 1 ? ":" + amount : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RecipeIngredient other)) return false;
        return material == other.material && amount == other.amount;
    }

    @Override
    public int hashCode() {
        return 31 * material.hashCode() + amount;
    }
}
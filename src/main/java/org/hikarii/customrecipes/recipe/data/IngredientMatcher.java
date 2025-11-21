package org.hikarii.customrecipes.recipe.data;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Map;

public class IngredientMatcher {
    public static boolean matches(ItemStack required, ItemStack actual, boolean ignoreMetadata) {
        if (actual == null || actual.getType() == Material.AIR) {
            return required == null || required.getType() == Material.AIR;
        }

        if (required == null || required.getType() == Material.AIR) {
            return false;
        }

        if (required.getType() != actual.getType()) {
            return false;
        }

        if (ignoreMetadata) {
            return true;
        }

        ItemMeta requiredMeta = required.getItemMeta();
        ItemMeta actualMeta = actual.getItemMeta();
        if (requiredMeta == null) {
            return true;
        }

        if (actualMeta == null) {
            return !requiredMeta.hasEnchants();
        }

        if (requiredMeta.hasEnchants()) {
            Map<Enchantment, Integer> requiredEnchants = requiredMeta.getEnchants();
            Map<Enchantment, Integer> actualEnchants = actualMeta.getEnchants();

            for (Map.Entry<Enchantment, Integer> entry : requiredEnchants.entrySet()) {
                if (!actualEnchants.containsKey(entry.getKey()) ||
                        actualEnchants.get(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }

        if (requiredMeta.getPersistentDataContainer().getKeys().size() > 0) {
            for (NamespacedKey key : requiredMeta.getPersistentDataContainer().getKeys()) {
                String requiredValue = requiredMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                String actualValue = actualMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (!requiredValue.equals(actualValue)) {
                    return false;
                }
            }
        }
        return true;
    }
}
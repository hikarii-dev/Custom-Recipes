package org.hikarii.customrecipes.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializes ItemStack to/from various formats
 */
public class ItemStackSerializer {

    /**
     * Serializes ItemStack to Base64 string (includes NBT, PDC, CMD)
     */
    public static String toBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    /**
     * Deserializes ItemStack from Base64 string
     */
    public static ItemStack fromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    /**
     * Converts ItemStack to Map (for JSON)
     */
    public static Map<String, Object> toMap(ItemStack item) {
        Map<String, Object> map = new HashMap<>();

        map.put("type", item.getType().name());
        map.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Custom Model Data
            if (meta.hasCustomModelData()) {
                map.put("custom-model-data", meta.getCustomModelData());
            }

            // Display name
            if (meta.hasDisplayName()) {
                map.put("display-name", net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                        .serialize(meta.displayName()));
            }

            // Lore
            if (meta.hasLore() && meta.lore() != null) {
                map.put("lore", meta.lore().stream()
                        .map(line -> net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                                .serialize(line))
                        .toList());
            }

            // Full serialized data (includes PDC)
            map.put("meta-serialized", toBase64(item));
        }

        return map;
    }

    /**
     * Creates ItemStack from Map
     */
    public static ItemStack fromMap(Map<String, Object> map) {
        // Try full deserialization first (preserves PDC)
        if (map.containsKey("meta-serialized")) {
            try {
                return fromBase64((String) map.get("meta-serialized"));
            } catch (Exception e) {
                // Fall back to manual construction
            }
        }

        // Manual construction
        Material material = Material.getMaterial((String) map.get("type"));
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + map.get("type"));
        }

        int amount = map.containsKey("amount") ? ((Number) map.get("amount")).intValue() : 1;
        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Custom Model Data
            if (map.containsKey("custom-model-data")) {
                meta.setCustomModelData(((Number) map.get("custom-model-data")).intValue());
            }

            // Display name
            if (map.containsKey("display-name")) {
                meta.displayName(net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                        .deserialize((String) map.get("display-name")));
            }

            // Lore
            if (map.containsKey("lore")) {
                @SuppressWarnings("unchecked")
                java.util.List<String> loreJson = (java.util.List<String>) map.get("lore");
                meta.lore(loreJson.stream()
                        .map(line -> net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                                .deserialize(line))
                        .toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
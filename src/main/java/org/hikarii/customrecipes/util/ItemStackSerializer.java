package org.hikarii.customrecipes.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ItemStackSerializer {
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

    public static Map<String, Object> toMap(ItemStack item) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", item.getType().name());
        map.put("amount", item.getAmount());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasCustomModelData()) {
                map.put("custom-model-data", meta.getCustomModelData());
            }

            if (meta.hasDisplayName()) {
                map.put("display-name", net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                        .serialize(meta.displayName()));
            }

            if (meta.hasLore() && meta.lore() != null) {
                map.put("lore", meta.lore().stream()
                        .map(line -> net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                                .serialize(line))
                        .toList());
            }
            map.put("meta-serialized", toBase64(item));
        }
        return map;
    }

    public static ItemStack fromMap(Map<String, Object> map) {
        if (map.containsKey("meta-serialized")) {
            try {
                return fromBase64((String) map.get("meta-serialized"));
            } catch (Exception e) {
            }
        }

        Material material = Material.getMaterial((String) map.get("type"));
        if (material == null) {
            throw new IllegalArgumentException("Invalid material: " + map.get("type"));
        }

        int amount = map.containsKey("amount") ? ((Number) map.get("amount")).intValue() : 1;
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (map.containsKey("custom-model-data")) {
                meta.setCustomModelData(((Number) map.get("custom-model-data")).intValue());
            }

            if (map.containsKey("display-name")) {
                meta.displayName(net.kyori.adventure.text.serializer.json.JSONComponentSerializer.json()
                        .deserialize((String) map.get("display-name")));
            }

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
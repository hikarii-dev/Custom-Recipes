package org.hikarii.customrecipes.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.recipe.data.ShapelessRecipeData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeConfigLoader {
    private final CustomRecipes plugin;
    public RecipeConfigLoader(CustomRecipes plugin) {
        this.plugin = plugin;
    }

    public CustomRecipe loadRecipe(String key, ConfigurationSection section) throws ValidationException {
        if (section == null) {
            throw new ValidationException("Recipe '" + key + "' has no configuration section");
        }
        try {
            String name = section.getString("crafted-name");
            List<String> description = section.getStringList("crafted-description");
            if (name == null || name.isEmpty()) {
                name = section.getString("crafted-name");
                if (name == null || name.isEmpty()) {
                    name = section.getString("gui-name");
                }
            }

            if (description == null || description.isEmpty()) {
                description = section.getStringList("crafted-description");
                if (description == null || description.isEmpty()) {
                    description = section.getStringList("gui-description");
                }
            }

            String typeStr = section.getString("type", "SHAPED");
            RecipeType type = RecipeType.fromString(typeStr);
            ItemStack resultItem;
            if (section.contains("result-full")) {
                try {
                    Object resultObj = section.get("result-full");
                    Map<String, Object> resultMap = null;
                    if (resultObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) resultObj;
                        resultMap = map;
                    } else if (resultObj instanceof ConfigurationSection) {
                        ConfigurationSection resultSection = (ConfigurationSection) resultObj;
                        resultMap = configSectionToMap(resultSection);
                    }

                    if (resultMap != null) {
                        resultItem = ItemStackSerializer.fromMap(resultMap);
                    } else {
                        throw new ValidationException("Invalid result-full format");
                    }
                } catch (Exception e) {
                    throw new ValidationException("Failed to load result-full item: " + e.getMessage(), e);
                }
            }
            else if (section.contains("result")) {
                try {
                    Object resultObj = section.get("result");
                    Map<String, Object> resultMap = null;
                    if (resultObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> map = (Map<String, Object>) resultObj;
                        resultMap = map;
                    } else if (resultObj instanceof ConfigurationSection) {
                        ConfigurationSection resultSection = (ConfigurationSection) resultObj;
                        resultMap = configSectionToMap(resultSection);
                    }

                    if (resultMap != null) {
                        resultItem = ItemStackSerializer.fromMap(resultMap);
                    } else {
                        throw new ValidationException("Invalid result format - not a Map or ConfigurationSection");
                    }
                } catch (Exception e) {
                    throw new ValidationException("Failed to load result item: " + e.getMessage(), e);
                }
            }
            else {
                String materialStr = section.getString("material");
                if (materialStr == null || materialStr.isEmpty()) {
                    throw new ValidationException("Recipe '" + key + "' is missing 'material' field");
                }

                Material resultMaterial = Material.getMaterial(materialStr.toUpperCase());
                if (resultMaterial == null) {
                    throw new ValidationException("Recipe '" + key + "' has invalid material: " + materialStr);
                }

                int resultAmount = section.getInt("amount", 1);
                if (resultAmount < 1 || resultAmount > 64) {
                    throw new ValidationException("Recipe '" + key + "' amount must be between 1 and 64");
                }
                resultItem = new ItemStack(resultMaterial, resultAmount);
            }
            if ((name == null || name.isEmpty()) && resultItem != null) {
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(meta.displayName());
                }
            }
            if ((description == null || description.isEmpty()) && resultItem != null) {
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null && meta.hasLore() && meta.lore() != null) {
                    description = new ArrayList<>();
                    for (Component line : meta.lore()) {
                        String loreText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                                .serialize(line);
                        if (loreText != null && !loreText.isEmpty()) {
                            description.add(loreText);
                        }
                    }
                }
            }
            boolean hidden = section.getBoolean("hidden", false);
            ShapedRecipeData recipeData = null;
            ShapelessRecipeData shapelessData = null;

            if (type == RecipeType.SHAPED) {
                recipeData = loadShapedRecipeData(key, section);
            } else if (type == RecipeType.SHAPELESS) {
                shapelessData = loadShapelessRecipeData(key, section);
            } else {
                throw new ValidationException("Recipe type '" + type + "' is not yet supported");
            }
            List<PotionEffect> potionEffects = new ArrayList<>();
            if (section.contains("potion-effects")) {
                List<Map<?, ?>> effectsList = section.getMapList("potion-effects");
                for (Map<?, ?> effectMap : effectsList) {
                    try {
                        String effectName = (String) effectMap.get("type");
                        int duration = effectMap.containsKey("duration") ?
                                ((Number) effectMap.get("duration")).intValue() : 160;
                        int amplifier = effectMap.containsKey("amplifier") ?
                                ((Number) effectMap.get("amplifier")).intValue() : 0;
                        PotionEffectType effectType = PotionEffectType.getByName(effectName);
                        if (effectType != null) {
                            potionEffects.add(new PotionEffect(effectType, duration, amplifier));
                        } else {
                            plugin.getLogger().warning("Unknown potion effect: " + effectName);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to parse potion effect: " + e.getMessage());
                    }
                }
            }
            return new CustomRecipe(key, name, description, type, recipeData, shapelessData, resultItem, hidden, potionEffects);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Error loading recipe '" + key + "': " + e.getMessage(), e);
        }
    }

    private ShapedRecipeData loadShapedRecipeData(String key, ConfigurationSection section)
            throws ValidationException {
        List<String> recipePattern = section.getStringList("recipe");
        if (recipePattern.isEmpty() || recipePattern.size() > 3) {
            throw new ValidationException("Recipe '" + key + "' has invalid recipe data: Recipe must have 1-3 rows");
        }
        while (recipePattern.size() < 3) {
            recipePattern.add("AIR AIR AIR");
        }
        for (int i = 0; i < recipePattern.size(); i++) {
            String row = recipePattern.get(i);
            String[] items = row.split(" ");
            if (items.length < 3) {
                StringBuilder paddedRow = new StringBuilder(row);
                for (int j = items.length; j < 3; j++) {
                    if (paddedRow.length() > 0) paddedRow.append(" ");
                    paddedRow.append("AIR");
                }
                recipePattern.set(i, paddedRow.toString());
            }
        }
        List<ItemStack> exactItems = null;
        if (section.contains("exact-ingredients")) {
            List<?> exactItemsList = section.getList("exact-ingredients");
            exactItems = new ArrayList<>();
            for (Object obj : exactItemsList) {
                if (obj == null) {
                    exactItems.add(null);
                } else if (obj instanceof String) {
                    String base64 = (String) obj;
                    try {
                        ItemStack item = ItemStackSerializer.fromBase64(base64);
                        exactItems.add(item);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to deserialize exact item from Base64: " + e.getMessage());
                        exactItems.add(null);
                    }
                } else {
                    exactItems.add(null);
                }
            }
        }
        else if (section.contains("exact-items")) {
            List<?> exactItemsList = section.getList("exact-items");
            exactItems = new ArrayList<>();
            for (Object obj : exactItemsList) {
                if (obj == null) {
                    exactItems.add(null);
                } else if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) obj;
                    try {
                        ItemStack item = ItemStackSerializer.fromMap(itemMap);
                        exactItems.add(item);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to deserialize exact item: " + e.getMessage());
                        exactItems.add(null);
                    }
                } else {
                    exactItems.add(null);
                }
            }
        }
        try {
            return ShapedRecipeData.fromConfigList(recipePattern, exactItems);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Recipe '" + key + "' has invalid recipe data: " + e.getMessage(), e);
        }
    }

    private ShapelessRecipeData loadShapelessRecipeData(String key, ConfigurationSection section)
            throws ValidationException {
        List<String> ingredientList = section.getStringList("ingredients");
        if (ingredientList.isEmpty()) {
            throw new ValidationException("Recipe '" + key + "' has no ingredients");
        }
        return ShapelessRecipeData.fromConfigList(ingredientList);
    }

    private Map<String, Object> configSectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, configSectionToMap((ConfigurationSection) value));
            } else if (value instanceof List) {
                map.put(key, value);
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    public void validateKey(String key) throws ValidationException {
        if (key == null || key.isEmpty()) {
            throw new ValidationException("Recipe key cannot be empty");
        }
        if (!key.matches("[a-zA-Z0-9_-]+")) {
            throw new ValidationException(
                    "Recipe key '" + key + "' contains invalid characters. Only letters, numbers, underscores, and hyphens are allowed."
            );
        }
    }
}
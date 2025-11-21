package org.hikarii.customrecipes.recipe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.recipe.data.ShapelessRecipeData;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;

public class CustomRecipe {
    private final String key;
    private final String name;
    private final List<String> description;
    private final RecipeType type;
    private final ShapedRecipeData recipeData;
    private final ShapelessRecipeData shapelessData;
    private final ItemStack resultItem;
    private final boolean hidden;
    private final List<PotionEffect> potionEffects;

    public CustomRecipe(
            String key,
            String name,
            List<String> description,
            RecipeType type,
            ShapedRecipeData recipeData,
            ShapelessRecipeData shapelessData,
            ItemStack resultItem,
            boolean hidden,
            List<PotionEffect> potionEffects) {

        this.key = key.toLowerCase();
        this.name = name;
        this.description = description;
        this.type = type;
        this.recipeData = recipeData;
        this.shapelessData = shapelessData;
        this.resultItem = resultItem.clone();
        this.hidden = hidden;
        this.potionEffects = potionEffects != null ? potionEffects : new ArrayList<>();
    }

    public ItemStack createResult(boolean useCustomNames, boolean keepSpawnEggName) {
        ItemStack item = resultItem.clone();
        if (useCustomNames) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                boolean isSpawnEgg = resultItem.getType().name().endsWith("_SPAWN_EGG");
                boolean shouldSetName = !isSpawnEgg || keepSpawnEggName;
                if (shouldSetName && name != null && !name.isEmpty()) {
                    Component nameComponent = MessageUtil.colorize(name)
                            .decoration(TextDecoration.ITALIC, false);
                    meta.displayName(nameComponent);
                }

                if (description != null && !description.isEmpty()) {
                    List<Component> loreComponents = description.stream()
                            .map(line -> MessageUtil.colorize(line)
                                    .decoration(TextDecoration.ITALIC, false))
                            .toList();
                    meta.lore(loreComponents);
                }

                if (item.getType() == Material.SUSPICIOUS_STEW && !potionEffects.isEmpty()) {
                    if (meta instanceof org.bukkit.inventory.meta.SuspiciousStewMeta stewMeta) {
                        for (PotionEffect effect : potionEffects) {
                            stewMeta.addCustomEffect(effect, true);
                        }
                    }
                }
                item.setItemMeta(meta);
            }
        }
        ItemMeta finalMeta = item.getItemMeta();
        if (finalMeta != null) {
            ItemMeta originalMeta = resultItem.getItemMeta();
            if (originalMeta != null) {
                for (org.bukkit.inventory.ItemFlag flag : originalMeta.getItemFlags()) {
                    finalMeta.addItemFlags(flag);
                }
            }
            item.setItemMeta(finalMeta);
        }
        return item;
    }

    public ItemStack createResult(boolean keepSpawnEggName) {
        return createResult(true, keepSpawnEggName);
    }

    public ItemStack createResult() {
        return createResult(true, true);
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public RecipeType getType() {
        return type;
    }

    public ShapedRecipeData getRecipeData() {
        return recipeData;
    }

    public ShapelessRecipeData getShapelessData() {
        return shapelessData;
    }

    public Material getResultMaterial() {
        return resultItem.getType();
    }

    public int getResultAmount() {
        return resultItem.getAmount();
    }

    public ItemStack getResultItem() {
        return resultItem.clone();
    }

    public boolean isHidden() {
        return hidden;
    }

    @Override
    public String toString() {
        return "CustomRecipe{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", result=" + resultItem.getType() +
                "x" + resultItem.getAmount() +
                '}';
    }
}
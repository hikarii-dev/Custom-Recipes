package org.hikarii.customrecipes.recipe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.List;

/**
 * Represents a custom recipe configuration
 */
public class CustomRecipe {

    private final String key;
    private final String guiName;
    private final List<String> guiDescription;
    private final String craftedName;
    private final List<String> craftedDescription;
    private final RecipeType type;
    private final ShapedRecipeData recipeData;
    private final Material resultMaterial;
    private final int resultAmount;

    /**
     * Creates a new custom recipe
     *
     * @param key unique identifier for this recipe
     * @param guiName display name in GUI (always shown in menus)
     * @param guiDescription lore in GUI (always shown in menus)
     * @param craftedName display name on crafted item (null for vanilla)
     * @param craftedDescription lore on crafted item (null for vanilla)
     * @param type recipe type
     * @param recipeData the recipe pattern data
     * @param resultMaterial the result material
     * @param resultAmount amount of items to craft (1-64)
     */
    public CustomRecipe(
            String key,
            String guiName,
            List<String> guiDescription,
            String craftedName,
            List<String> craftedDescription,
            RecipeType type,
            ShapedRecipeData recipeData,
            Material resultMaterial,
            int resultAmount) {

        this.key = key.toLowerCase();
        this.guiName = guiName;
        this.guiDescription = guiDescription;
        this.craftedName = craftedName;
        this.craftedDescription = craftedDescription;
        this.type = type;
        this.recipeData = recipeData;
        this.resultMaterial = resultMaterial;
        this.resultAmount = Math.max(1, Math.min(64, resultAmount));
    }

    /**
     * Creates the result ItemStack for crafting (uses crafted name/description)
     *
     * @param useCraftedCustomNames whether to use custom names from config
     * @param keepSpawnEggName whether to keep custom names on spawn eggs
     * @return the crafted item
     */
    public ItemStack createResult(boolean useCraftedCustomNames, boolean keepSpawnEggName) {
        ItemStack item = new ItemStack(resultMaterial, resultAmount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null && useCraftedCustomNames) {
            boolean isSpawnEgg = resultMaterial.name().endsWith("_SPAWN_EGG");
            boolean shouldSetName = !isSpawnEgg || keepSpawnEggName;

            if (shouldSetName && craftedName != null && !craftedName.isEmpty()) {
                Component nameComponent = MessageUtil.colorize(craftedName)
                        .decoration(TextDecoration.ITALIC, false);
                meta.displayName(nameComponent);
            }

            if (craftedDescription != null && !craftedDescription.isEmpty()) {
                List<Component> loreComponents = craftedDescription.stream()
                        .map(line -> MessageUtil.colorize(line)
                                .decoration(TextDecoration.ITALIC, false))
                        .toList();
                meta.lore(loreComponents);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates GUI display ItemStack (uses GUI name/description, always shown)
     *
     * @return the GUI display item
     */
    public ItemStack createGUIDisplay() {
        ItemStack item = new ItemStack(resultMaterial, resultAmount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (guiName != null && !guiName.isEmpty()) {
                Component nameComponent = MessageUtil.colorize(guiName)
                        .decoration(TextDecoration.ITALIC, false);
                meta.displayName(nameComponent);
            }

            if (guiDescription != null && !guiDescription.isEmpty()) {
                List<Component> loreComponents = guiDescription.stream()
                        .map(line -> MessageUtil.colorize(line)
                                .decoration(TextDecoration.ITALIC, false))
                        .toList();
                meta.lore(loreComponents);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Creates result with default settings (for backward compatibility)
     */
    public ItemStack createResult(boolean keepSpawnEggName) {
        return createResult(true, keepSpawnEggName);
    }

    public ItemStack createResult() {
        return createResult(true, true);
    }

    // Getters

    public String getKey() {
        return key;
    }

    public String getGuiName() {
        return guiName;
    }

    public List<String> getGuiDescription() {
        return guiDescription;
    }

    public String getCraftedName() {
        return craftedName;
    }

    public List<String> getCraftedDescription() {
        return craftedDescription;
    }

    public RecipeType getType() {
        return type;
    }

    public ShapedRecipeData getRecipeData() {
        return recipeData;
    }

    public Material getResultMaterial() {
        return resultMaterial;
    }

    public int getResultAmount() {
        return resultAmount;
    }

    @Override
    public String toString() {
        return "CustomRecipe{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", result=" + resultMaterial +
                "x" + resultAmount +
                '}';
    }
}
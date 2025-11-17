package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.RecipeWorldManager;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced GUI for viewing and editing recipe patterns with world restrictions
 */
public class RecipeEditorGUI implements Listener {

    private final CustomRecipes plugin;
    private final Player player;
    private final CustomRecipe recipe;
    private final Inventory inventory;
    private boolean deleteConfirmation = false;

    // Crafting grid slots (centered in inventory)
    private static final int[] GRID_SLOTS = {
            10, 11, 12,  // Top row
            19, 20, 21,  // Middle row
            28, 29, 30   // Bottom row
    };

    public RecipeEditorGUI(CustomRecipes plugin, Player player, CustomRecipe recipe) {
        this.plugin = plugin;
        this.player = player;
        this.recipe = recipe;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                Component.text("Recipe Editor: " + recipe.getKey(), NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void updateInventory() {
        inventory.clear();
        fillBorders();
        addCraftingGrid();
        addEqualsSign();
        addResultItem();
        addInfoBook();
        addHiddenToggleButton();
        addToggleButton();
        addWorldSettingsButton(); // NEW
        addDeleteButton();
        addEditItemButton(); // NEW
        addBackButton();
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }

        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int slot : gridSlots) {
            inventory.setItem(slot, null);
        }

        inventory.setItem(25, null);
    }

    private void addEqualsSign() {
        ItemStack equals = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = equals.getItemMeta();
        meta.displayName(Component.text("=", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        equals.setItemMeta(meta);
        inventory.setItem(23, equals);
    }

    private void addCraftingGrid() {
        if (recipe.getType() == RecipeType.SHAPED) {
            addShapedCraftingGrid();
        } else if (recipe.getType() == RecipeType.SHAPELESS) {
            addShapelessCraftingGrid();
        }
    }

    private void addShapedCraftingGrid() {
        List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        for (int i = 0; i < ingredients.size() && i < gridSlots.length; i++) {
            RecipeIngredient ingredient = ingredients.get(i);

            if (ingredient.material() == Material.AIR) {
                continue;
            }

            ItemStack item = new ItemStack(ingredient.material());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.displayName(Component.text(
                        MessageUtil.formatMaterialName(ingredient.material().name()),
                        NamedTextColor.WHITE
                ).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(meta);
            }

            inventory.setItem(gridSlots[i], item);
        }
    }

    private void addShapelessCraftingGrid() {
        Map<Material, Integer> ingredients = recipe.getShapelessData().ingredients();
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        int slotIndex = 0;
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            Material mat = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count && slotIndex < gridSlots.length; i++) {
                ItemStack item = new ItemStack(mat);

                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text(
                        MessageUtil.formatMaterialName(mat.name()),
                        NamedTextColor.WHITE
                ).decoration(TextDecoration.ITALIC, false));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Shapeless ingredient", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position doesn't matter", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);

                item.setItemMeta(meta);
                inventory.setItem(gridSlots[slotIndex], item);
                slotIndex++;
            }
        }
    }

    private void addResultItem() {
        ItemStack result = recipe.getResultItem().clone();
        ItemMeta meta = result.getItemMeta();

        if (meta != null) {
            // Clear default name
            meta.displayName(Component.empty());

            // Build complete preview lore
            List<Component> completeLore = new ArrayList<>();

            completeLore.add(Component.text("Preview of Result Item:", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            completeLore.add(Component.empty());

            // Crafted Name
            String craftedName = recipe.getCraftedName();
            if (craftedName != null && !craftedName.isEmpty()) {
                completeLore.add(Component.text("Crafted Name:", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(MessageUtil.colorize("  " + craftedName)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                completeLore.add(Component.text("Crafted Name: ", NamedTextColor.AQUA)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            completeLore.add(Component.empty());

            // Crafted Description
            List<String> craftedDesc = recipe.getCraftedDescription();
            if (craftedDesc != null && !craftedDesc.isEmpty()) {
                completeLore.add(Component.text("Crafted Description:", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                for (String line : craftedDesc) {
                    completeLore.add(MessageUtil.colorize("  " + line)
                            .decoration(TextDecoration.ITALIC, false));
                }
            } else {
                completeLore.add(Component.text("Crafted Description: ", NamedTextColor.AQUA)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            // Separator
            completeLore.add(Component.empty());
            completeLore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            completeLore.add(Component.empty());

            // GUI Name
            String guiName = recipe.getGuiName();
            if (guiName != null && !guiName.isEmpty()) {
                completeLore.add(Component.text("GUI Name:", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(MessageUtil.colorize("  " + guiName)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                completeLore.add(Component.text("GUI Name: ", NamedTextColor.YELLOW)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            completeLore.add(Component.empty());

            // GUI Description
            List<String> guiDesc = recipe.getGuiDescription();
            if (guiDesc != null && !guiDesc.isEmpty()) {
                completeLore.add(Component.text("GUI Description:", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                for (String line : guiDesc) {
                    completeLore.add(MessageUtil.colorize("  " + line)
                            .decoration(TextDecoration.ITALIC, false));
                }
            } else {
                completeLore.add(Component.text("GUI Description: ", NamedTextColor.YELLOW)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            // Additional info section
            ItemMeta resultMeta = recipe.getResultItem().getItemMeta();
            boolean hasAdditionalInfo = false;

            if (resultMeta != null) {
                hasAdditionalInfo = resultMeta.hasCustomModelData() ||
                        resultMeta.hasEnchants() ||
                        !resultMeta.getPersistentDataContainer().isEmpty();
            }

            if (hasAdditionalInfo) {
                completeLore.add(Component.empty());
                completeLore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(Component.empty());

                // CustomModelData
                if (resultMeta.hasCustomModelData()) {
                    completeLore.add(Component.text("Custom Model Data: ", NamedTextColor.LIGHT_PURPLE)
                            .append(Component.text(resultMeta.getCustomModelData(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                }

                // Enchantments
                if (resultMeta.hasEnchants()) {
                    completeLore.add(Component.text("Enchantments:", NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.ITALIC, false));
                    for (Map.Entry<Enchantment, Integer> entry : resultMeta.getEnchants().entrySet()) {
                        String enchName = entry.getKey().getKey().getKey();
                        completeLore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }

                // NBT
                PersistentDataContainer container = resultMeta.getPersistentDataContainer();
                if (!container.isEmpty()) {
                    completeLore.add(Component.text("NBT Data:", NamedTextColor.DARK_AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                    for (NamespacedKey key : container.getKeys()) {
                        if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                            String value = container.get(key, PersistentDataType.STRING);
                            completeLore.add(Component.text("  • " + key.getKey() + ": " + value, NamedTextColor.AQUA)
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                    }
                }
            }

            meta.lore(completeLore);

            // Apply visual effects from result item
            if (resultMeta != null) {
                if (resultMeta.hasCustomModelData()) {
                    meta.setCustomModelData(resultMeta.getCustomModelData());
                }
                if (resultMeta.hasEnchants()) {
                    for (Map.Entry<Enchantment, Integer> entry : resultMeta.getEnchants().entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                }
            }

            result.setItemMeta(meta);
        }

        inventory.setItem(25, result);
    }

    private void addInfoBook() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Key: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getKey(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().toString(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Result: ", NamedTextColor.GRAY)
                .append(Component.text(
                        MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                        NamedTextColor.WHITE
                ))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getResultAmount() + "x", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        // Show world restrictions if any
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        if (!disabledWorlds.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("World Restrictions:", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            for (String world : disabledWorlds) {
                lore.add(Component.text("  • " + world, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        if (recipe.getGuiName() != null && !recipe.getGuiName().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("GUI Name:", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(MessageUtil.colorize(recipe.getGuiName())
                    .decoration(TextDecoration.ITALIC, false));
        }

        if (recipe.getCraftedName() != null && !recipe.getCraftedName().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Crafted Name:", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(MessageUtil.colorize(recipe.getCraftedName())
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Show CustomModelData if present
        ItemMeta resultMeta = recipe.getResultItem().getItemMeta();
        if (resultMeta != null && resultMeta.hasCustomModelData()) {
            lore.add(Component.empty());
            lore.add(Component.text("Custom Model Data: ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(resultMeta.getCustomModelData(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Show Enchantments
        if (resultMeta != null && resultMeta.hasEnchants()) {
            lore.add(Component.empty());
            lore.add(Component.text("Enchantments:", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            for (Map.Entry<Enchantment, Integer> entry : resultMeta.getEnchants().entrySet()) {
                String enchName = entry.getKey().getKey().getKey();
                lore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        // Show NBT Data
        if (resultMeta != null) {
            PersistentDataContainer container = resultMeta.getPersistentDataContainer();
            if (!container.getKeys().isEmpty()) {
                lore.add(Component.empty());
                lore.add(Component.text("NBT Data:", NamedTextColor.DARK_AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                for (NamespacedKey key : container.getKeys()) {
                    if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                        String value = container.get(key, PersistentDataType.STRING);
                        lore.add(Component.text("  • " + key.getKey() + ": " + value, NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
            }
        }

        // Show GUI Description
        if (recipe.getGuiDescription() != null && !recipe.getGuiDescription().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("GUI Description:", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            for (String line : recipe.getGuiDescription()) {
                lore.add(MessageUtil.colorize("  " + line)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        // Show Crafted Description
        if (recipe.getCraftedDescription() != null && !recipe.getCraftedDescription().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Crafted Description:", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            for (String line : recipe.getCraftedDescription()) {
                lore.add(MessageUtil.colorize("  " + line)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(49, info);
    }

    private void addToggleButton() {
        boolean enabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;

        ItemStack toggleButton = new ItemStack(material);
        ItemMeta meta = toggleButton.getItemMeta();
        meta.displayName(Component.text(
                enabled ? "Enabled" : "Disabled",
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED
        ).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(
                        enabled ? "Active" : "Inactive",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED
                ))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text(
                enabled ? "Players can use this recipe" : "Recipe is disabled",
                NamedTextColor.GRAY
        ).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to " + (enabled ? "disable" : "enable"), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        toggleButton.setItemMeta(meta);
        inventory.setItem(48, toggleButton);
    }

    private void addWorldSettingsButton() {
        ItemStack button = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("World Settings", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        if (disabledWorlds.isEmpty()) {
            lore.add(Component.text("Enabled in all worlds", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Disabled in:", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            for (String world : disabledWorlds) {
                lore.add(Component.text("  • " + world, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        lore.add(Component.empty());
        lore.add(Component.text("Configure which worlds", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("this recipe works in", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to configure", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(46, button);
    }

    private void addEditItemButton() {
        ItemStack button = new ItemStack(Material.ANVIL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Result Item", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Customize the result", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("item's properties:", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Name & Lore", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Custom Model Data", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• NBT Tags", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Enchantments", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(47, button);
    }

    private void addDeleteButton() {
        ItemStack deleteButton;
        ItemMeta meta;

        if (deleteConfirmation) {
            deleteButton = new ItemStack(Material.LIME_WOOL);
            meta = deleteButton.getItemMeta();
            meta.displayName(Component.text("Confirm Deletion", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("⚠ ARE YOU SURE? ⚠", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("This will permanently delete:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Recipe from memory", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Recipe files (.yml, .json)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• All recipe data", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("» Click again to DELETE", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("» Click elsewhere to cancel", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, true));

            meta.lore(lore);
            deleteButton.setItemMeta(meta);
        } else {
            deleteButton = new ItemStack(Material.BARRIER);
            meta = deleteButton.getItemMeta();
            meta.displayName(Component.text("Delete Recipe", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("⚠ Warning!", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("This will permanently delete", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("this recipe and its files.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("» Click to confirm", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, true));

            meta.lore(lore);
            deleteButton.setItemMeta(meta);
        }

        inventory.setItem(50, deleteButton);
    }

    private void addHiddenToggleButton() {
        boolean hidden = recipe.isHidden();

        Material material = hidden ? Material.ENDER_EYE : Material.ENDER_PEARL;

        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text(hidden ? "Hidden Recipe" : "Visible Recipe",
                        hidden ? NamedTextColor.DARK_PURPLE : NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(hidden ? "Hidden" : "Visible",
                        hidden ? NamedTextColor.DARK_PURPLE : NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If hidden, players must craft", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("this recipe once to unlock it.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(45, button);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Recipe List", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(meta);
        inventory.setItem(53, back);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        if (!clicker.equals(player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();

        // Reset delete confirmation if clicking anywhere else
        if (slot != 50 && deleteConfirmation) {
            deleteConfirmation = false;
            updateInventory();
        }

        // Check permission for management actions
        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to manage recipes.");
            return;
        }

        // Toggle enable/disable (slot 48)
        if (slot == 48) {
            boolean currentlyEnabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());

            if (currentlyEnabled) {
                plugin.getRecipeManager().disableRecipe(recipe.getKey());
                plugin.getConfigManager().removeEnabledRecipe(recipe.getKey());
                MessageUtil.sendWarning(player, "Disabled recipe: <white>" + recipe.getKey());
            } else {
                plugin.getRecipeManager().enableRecipe(recipe.getKey());
                plugin.getConfigManager().addEnabledRecipe(recipe.getKey());
                MessageUtil.sendSuccess(player, "Enabled recipe: <white>" + recipe.getKey());
            }

            updateInventory();
            return;
        }

        // World settings button (slot 46)
        if (slot == 46) {
            new WorldSettingsGUI(plugin, player, recipe).open();
            return;
        }

        // Edit item button (slot 47)
        if (slot == 47) {
            ItemStack resultItem = recipe.getResultItem();

            // Pass existing GUI and Crafted fields to editor
            new ItemEditorGUI(plugin, player, resultItem,
                    recipe.getGuiName(),
                    recipe.getGuiDescription(),
                    recipe.getCraftedName(),
                    recipe.getCraftedDescription(),
                    (editedItem) -> {
                        if (editedItem != null) {
                            ItemEditorGUI editor = ItemEditorGUI.getLastEditor(player.getUniqueId());
                            String newGuiName = editor != null ? editor.getGuiName() : null;
                            List<String> newGuiDesc = editor != null ? editor.getGuiDescription() : null;
                            String newCraftedName = editor != null ? editor.getCustomName() : null;
                            List<String> newCraftedDesc = editor != null ? editor.getCustomLore() : null;

                            plugin.getRecipeManager().updateRecipeResult(recipe.getKey(), editedItem,
                                    newGuiName, newGuiDesc, newCraftedName, newCraftedDesc);
                            MessageUtil.sendSuccess(player, "Updated result item for recipe: " + recipe.getKey());
                        }

                        CustomRecipe updatedRecipe = plugin.getRecipeManager().getRecipe(recipe.getKey());
                        if (updatedRecipe != null) {
                            new RecipeEditorGUI(plugin, player, updatedRecipe).open();
                        }
                    }).open();
            return;
        }

        // Delete button (slot 50)
        if (slot == 50) {
            if (!deleteConfirmation) {
                deleteConfirmation = true;
                updateInventory();
                MessageUtil.sendWarning(player, "Click again to confirm deletion!");
                return;
            }

            String recipeKey = recipe.getKey();

            if (plugin.getRecipeManager().deleteRecipePermanently(recipeKey)) {
                plugin.getConfigManager().removeEnabledRecipe(recipeKey);
                MessageUtil.sendSuccess(player, "Permanently deleted recipe: <white>" + recipeKey);

                player.closeInventory();
                new RecipeListGUI(plugin, player).open();
            } else {
                MessageUtil.sendError(player, "Failed to delete recipe.");
                deleteConfirmation = false;
                updateInventory();
            }
            return;
        }

        // Hidden toggle (slot 45)
        if (slot == 45) {
            boolean newValue = !recipe.isHidden();

            String originalKey = null;
            for (String key : plugin.getConfig().getKeys(false)) {
                if (key.equalsIgnoreCase(recipe.getKey())) {
                    originalKey = key;
                    break;
                }
            }

            if (originalKey != null) {
                plugin.getConfig().set(originalKey + ".hidden", newValue);
                plugin.saveConfig();
                plugin.reloadConfig();
                plugin.loadConfiguration();

                MessageUtil.sendSuccess(player,
                        "Recipe is now " + (newValue ? "<dark_purple>hidden" : "<aqua>visible"));

                CustomRecipe updatedRecipe = plugin.getRecipeManager().getRecipe(recipe.getKey());
                if (updatedRecipe != null) {
                    new RecipeEditorGUI(plugin, player, updatedRecipe).open();
                }
            } else {
                MessageUtil.sendError(player, "Failed to find recipe in config.");
            }
            return;
        }

        // Back button (slot 53)
        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
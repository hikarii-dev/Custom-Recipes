package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for viewing and editing recipe patterns
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

    /**
     * Opens the GUI for the player
     */
    public void open() {
        player.openInventory(inventory);
    }

    /**
     * Updates the inventory contents
     */
    private void updateInventory() {
        inventory.clear();

        // Fill borders with light blue glass
        fillBorders();

        // Add crafting grid
        addCraftingGrid();

        // Add equals sign
        addEqualsSign();

        // Add result item
        addResultItem();

        // Add info book
        addInfoBook();

        // Add control buttons
        addHiddenToggleButton();
        addToggleButton();
        addDeleteButton();
        addBackButton();
    }

    /**
     * Fills borders with light blue stained glass panes
     */
    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);

        // Fill all slots first
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }

        // Clear grid slots (3x3)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int slot : gridSlots) {
            inventory.setItem(slot, null);
        }

        // Clear result slot
        inventory.setItem(25, null);
    }

    /**
     * Adds equals sign indicator
     */
    private void addEqualsSign() {
        ItemStack equals = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = equals.getItemMeta();
        meta.displayName(Component.text("=", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        equals.setItemMeta(meta);
        inventory.setItem(23, equals);
    }

    /**
     * Adds the crafting grid with recipe ingredients
     */
    private void addCraftingGrid() {
        if (recipe.getType() == RecipeType.SHAPED) {
            addShapedCraftingGrid();
        } else if (recipe.getType() == RecipeType.SHAPELESS) {
            addShapelessCraftingGrid();
        }
    }

    /**
     * Adds shaped recipe grid
     */
    private void addShapedCraftingGrid() {
        List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();

        // Slots 10-12, 19-21, 28-30 (3x3 grid)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        for (int i = 0; i < ingredients.size() && i < gridSlots.length; i++) {
            RecipeIngredient ingredient = ingredients.get(i);

            // Skip AIR
            if (ingredient.material() == Material.AIR) {
                continue;
            }

            ItemStack item = new ItemStack(ingredient.material());
            ItemMeta meta = item.getItemMeta();

            // Check if meta exists
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

    /**
     * Adds shapeless recipe grid
     */
    private void addShapelessCraftingGrid() {
        Map<Material, Integer> ingredients = recipe.getShapelessData().ingredients();

        // Slots 10-12, 19-21, 28-30 (3x3 grid)
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        int slotIndex = 0;
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            Material mat = entry.getKey();
            int count = entry.getValue();

            // Place multiple items of same type in grid
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

    /**
     * Adds the result item (GUI display version)
     */
    private void addResultItem() {
        ItemStack result = recipe.createGUIDisplay();
        ItemMeta meta = result.getItemMeta();

        List<Component> lore = new ArrayList<>(meta.hasLore() ? meta.lore() : List.of());
        lore.add(Component.empty());
        lore.add(Component.text("→ Recipe Result", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        result.setItemMeta(meta);

        inventory.setItem(25, result);
    }

    /**
     * Adds info book with recipe details
     */
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

        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(49, info);
    }

    /**
     * Adds toggle enable/disable button
     */
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

    /**
     * Adds delete button
     */
    private void addDeleteButton() {
        ItemStack deleteButton;
        ItemMeta meta;

        if (deleteConfirmation) {
            // Confirmation state - GREEN wool
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
            // Normal state - BARRIER
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

    /**
     * Adds hidden button
     */
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

    /**
     * Adds back button
     */
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

        // Delete button (slot 50)
        if (slot == 50) {
            if (!deleteConfirmation) {
                // First click - show confirmation
                deleteConfirmation = true;
                updateInventory();
                MessageUtil.sendWarning(player, "Click again to confirm deletion!");
                return;
            }

            // Second click - actually delete
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
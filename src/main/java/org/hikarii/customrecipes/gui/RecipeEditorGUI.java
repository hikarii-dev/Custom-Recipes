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
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing and editing recipe patterns
 */
public class RecipeEditorGUI implements Listener {

    private final CustomRecipes plugin;
    private final Player player;
    private final CustomRecipe recipe;
    private final Inventory inventory;

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
                Component.text("Recipe: " + recipe.getKey(), NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        // Register as listener
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

        // Fill borders with glass panes
        fillBorders();

        // Add crafting grid
        addCraftingGrid();

        // Add result item
        addResultItem();

        // Add info items
        addInfoItems();

        // Add back button
        addBackButton();
    }

    /**
     * Fills the borders with decorative glass panes
     */
    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);

        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderPane);
            inventory.setItem(45 + i, borderPane);
        }

        // Side columns
        for (int i = 1; i < 5; i++) {
            inventory.setItem(i * 9, borderPane);
            inventory.setItem(i * 9 + 8, borderPane);
        }
    }

    /**
     * Adds the crafting grid with recipe ingredients
     */
    private void addCraftingGrid() {
        List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();

        for (int i = 0; i < 9; i++) {
            RecipeIngredient ingredient = ingredients.get(i);

            if (!ingredient.isEmpty()) {
                ItemStack item = new ItemStack(ingredient.material());
                ItemMeta meta = item.getItemMeta();

                // Add lore showing position
                int row = i / 3 + 1;
                int col = i % 3 + 1;
                meta.lore(List.of(
                        Component.empty(),
                        Component.text("Position: Row " + row + ", Col " + col, NamedTextColor.DARK_GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                ));

                item.setItemMeta(meta);
                inventory.setItem(GRID_SLOTS[i], item);
            } else {
                // Empty slot - show as barrier
                ItemStack empty = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta meta = empty.getItemMeta();
                meta.displayName(Component.text("Empty", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                empty.setItemMeta(meta);
                inventory.setItem(GRID_SLOTS[i], empty);
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

        inventory.setItem(24, result);
    }

    /**
     * Adds information items and management buttons
     */
    private void addInfoItems() {
        // Recipe info (center top - slot 4)
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
        inventory.setItem(4, info);

        // Toggle enable/disable button (left of info - slot 3)
        addToggleButton();

        // Delete button (right of info - slot 5)
        addDeleteButton();
    }

    /**
     * Adds toggle enable/disable button
     */
    private void addToggleButton() {
        boolean enabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String action = enabled ? "Disable" : "Enable";
        NamedTextColor color = enabled ? NamedTextColor.RED : NamedTextColor.GREEN;

        ItemStack toggleButton = new ItemStack(material);
        ItemMeta meta = toggleButton.getItemMeta();
        meta.displayName(Component.text(action + " Recipe", color)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(enabled ? "Enabled" : "Disabled", enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to " + action.toLowerCase(), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        toggleButton.setItemMeta(meta);
        inventory.setItem(3, toggleButton);
    }

    /**
     * Adds delete button
     */
    private void addDeleteButton() {
        ItemStack deleteButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = deleteButton.getItemMeta();
        meta.displayName(Component.text("Delete Recipe", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("⚠ Warning!", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("This will remove the recipe", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("from memory.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to delete", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        deleteButton.setItemMeta(meta);
        inventory.setItem(5, deleteButton);
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
        inventory.setItem(49, back);
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

        // Back button
        if (slot == 49) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        // Check if player has manage permission for these actions
        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to manage recipes.");
            return;
        }

        // Toggle enable/disable button (slot 3)
        if (slot == 3) {
            boolean currentlyEnabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());

            if (currentlyEnabled) {
                // Disable recipe
                if (plugin.getRecipeManager().disableRecipe(recipe.getKey())) {
                    plugin.getConfigManager().removeEnabledRecipe(recipe.getKey());
                    MessageUtil.sendSuccess(player, "Disabled recipe: <white>" + recipe.getKey());
                    updateInventory(); // Refresh GUI
                } else {
                    MessageUtil.sendError(player, "Failed to disable recipe.");
                }
            } else {
                // Enable recipe
                if (plugin.getRecipeManager().enableRecipe(recipe.getKey())) {
                    plugin.getConfigManager().addEnabledRecipe(recipe.getKey());
                    MessageUtil.sendSuccess(player, "Enabled recipe: <white>" + recipe.getKey());
                    updateInventory(); // Refresh GUI
                } else {
                    MessageUtil.sendError(player, "Failed to enable recipe.");
                }
            }
            return;
        }

        // Delete button (slot 5)
        if (slot == 5) {
            if (plugin.getRecipeManager().deleteRecipe(recipe.getKey())) {
                plugin.getConfigManager().removeEnabledRecipe(recipe.getKey());
                MessageUtil.sendSuccess(player, "Deleted recipe: <white>" + recipe.getKey());
                MessageUtil.sendWarning(player, "Note: Recipe removed from memory. Edit config.yml to remove permanently.");

                // Go back to list
                player.closeInventory();
                new RecipeListGUI(plugin, player).open();
            } else {
                MessageUtil.sendError(player, "Failed to delete recipe.");
            }
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
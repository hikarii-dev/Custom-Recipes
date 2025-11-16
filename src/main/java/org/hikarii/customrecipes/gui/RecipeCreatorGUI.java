package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.config.JsonRecipeFileManager;
import org.hikarii.customrecipes.config.ValidationException;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.recipe.data.ShapelessRecipeData;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import org.hikarii.customrecipes.util.MessageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI for creating custom recipes
 */
public class RecipeCreatorGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private RecipeType currentType = RecipeType.SHAPED;
    private ItemStack[] savedGrid = new ItemStack[9];
    private ItemEditorGUI currentEditor = null;

    // Grid slots: 10-12, 19-21, 28-30 (3x3)
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 25;
    private static final int TYPE_TOGGLE = 45;
    private static final int CREATE_BUTTON = 48;
    private static final int CANCEL_BUTTON = 50;
    private static final int EDIT_RESULT_BUTTON = 53;

    public RecipeCreatorGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(
                null,
                54, // 6 rows
                Component.text("Create Custom Recipe", NamedTextColor.DARK_PURPLE)
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
        fillBorders();
        addEqualsSign();
        addTypeToggleButton();
        addCreateButton();
        addCancelButton();
        addEditResultButton();
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
        for (int slot : GRID_SLOTS) {
            inventory.setItem(slot, null);
        }

        // Clear result slot
        inventory.setItem(RESULT_SLOT, null);
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
        meta.lore(List.of(
                Component.empty(),
                Component.text("Place ingredients in the", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("3x3 grid on the left", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Place result item", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("on the right →", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        equals.setItemMeta(meta);
        inventory.setItem(23, equals);
    }

    /**
     * Adds recipe type toggle button
     */
    private void addTypeToggleButton() {
        boolean isShaped = currentType == RecipeType.SHAPED;

        ItemStack toggle = new ItemStack(isShaped ? Material.CRAFTING_TABLE : Material.CHEST);
        ItemMeta meta = toggle.getItemMeta();
        meta.displayName(Component.text(
                isShaped ? "Shaped Recipe" : "Shapeless Recipe",
                isShaped ? NamedTextColor.AQUA : NamedTextColor.LIGHT_PURPLE
        ).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (isShaped) {
            lore.add(Component.text("Position matters!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Items must be placed", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("in exact positions.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Position doesn't matter!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Items can be placed", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("in any order.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        toggle.setItemMeta(meta);
        inventory.setItem(TYPE_TOGGLE, toggle);
    }

    /**
     * Adds create button
     */
    private void addCreateButton() {
        ItemStack create = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = create.getItemMeta();
        meta.displayName(Component.text("Create Recipe", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Click to create this", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("custom recipe!", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("» Left Click", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        create.setItemMeta(meta);
        inventory.setItem(CREATE_BUTTON, create);
    }

    /**
     * Adds cancel button
     */
    private void addCancelButton() {
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = cancel.getItemMeta();
        meta.displayName(Component.text("Cancel", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Return to recipe list", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("» Click to cancel", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        cancel.setItemMeta(meta);
        inventory.setItem(CANCEL_BUTTON, cancel);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        if (!clicker.equals(player)) {
            return;
        }

        int slot = event.getSlot();
        ClickType clickType = event.getClick();

        // Check if clicked in grid or result slot
        boolean isGridSlot = false;
        for (int gridSlot : GRID_SLOTS) {
            if (slot == gridSlot) {
                isGridSlot = true;
                break;
            }
        }

        // Allow interaction with grid slots, result slot, and player inventory
        if (isGridSlot || slot == RESULT_SLOT) {
            // Allow placing/taking items in grid and result
            return;
        }

        // Allow clicks in player's own inventory (bottom slots)
        if (event.getClickedInventory() != inventory) {
            // Clicked in player inventory - allow normal interaction
            return;
        }

        // For all other slots in the GUI, cancel the click
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Type toggle button
        if (slot == TYPE_TOGGLE) {
            currentType = (currentType == RecipeType.SHAPED) ? RecipeType.SHAPELESS : RecipeType.SHAPED;
            updateInventory();
            return;
        }

        // Create button
        if (slot == CREATE_BUTTON) {
            handleCreateRecipe();
            return;
        }

        // Cancel button
        if (slot == CANCEL_BUTTON) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        // Edit result button (slot 53)
        if (slot == EDIT_RESULT_BUTTON) {
            ItemStack result = inventory.getItem(RESULT_SLOT);
            if (result == null || result.getType() == Material.AIR) {
                MessageUtil.sendError(player, "Please place a result item first!");
                return;
            }

            // Save current grid state
            for (int i = 0; i < 9; i++) {
                ItemStack gridItem = inventory.getItem(GRID_SLOTS[i]);
                savedGrid[i] = gridItem != null ? gridItem.clone() : null;
            }

            RecipeType savedType = currentType;

            // Create and save editor reference
            currentEditor = new ItemEditorGUI(plugin, player, result, () -> {
                // Create NEW GUI instead of reopening old one
                RecipeCreatorGUI newGUI = new RecipeCreatorGUI(plugin, player);
                newGUI.currentType = savedType;

                // Restore grid items in NEW GUI
                for (int i = 0; i < 9; i++) {
                    if (savedGrid[i] != null) {
                        newGUI.inventory.setItem(GRID_SLOTS[i], savedGrid[i]);
                    }
                }

                // Set edited result from editor
                if (currentEditor != null) {
                    newGUI.inventory.setItem(RESULT_SLOT, currentEditor.getEditedItem());
                }

                newGUI.open();
            });
            currentEditor.open();
            return;
        }
    }

    private void addEditResultButton() {
        ItemStack button = new ItemStack(Material.ANVIL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Result Item", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Customize the result", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("item's name and lore", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(EDIT_RESULT_BUTTON, button);
    }

    /**
     * Handles recipe creation
     */
    private void handleCreateRecipe() {
        // Validate grid has at least one ingredient
        boolean hasIngredient = false;
        for (int slot : GRID_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                hasIngredient = true;
                break;
            }
        }

        if (!hasIngredient) {
            MessageUtil.sendError(player, "Please place at least one ingredient in the grid!");
            return;
        }

        // Validate result exists
        ItemStack result = inventory.getItem(RESULT_SLOT);
        if (result == null || result.getType() == Material.AIR) {
            MessageUtil.sendError(player, "Please place a result item!");
            return;
        }

        // Build recipe pattern - get all items first
        Material[][] grid = new Material[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = GRID_SLOTS[row * 3 + col];
                ItemStack item = inventory.getItem(slot);
                grid[row][col] = (item == null || item.getType() == Material.AIR) ? null : item.getType();
            }
        }

        // Find bounds (remove empty edges)
        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (grid[row][col] != null) {
                    if (row < minRow) minRow = row;
                    if (row > maxRow) maxRow = row;
                    if (col < minCol) minCol = col;
                    if (col > maxCol) maxCol = col;
                }
            }
        }

        // Build optimized pattern
        int height = maxRow - minRow + 1;
        int width = maxCol - minCol + 1;
        String[] pattern = new String[height];

        for (int row = 0; row < height; row++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                Material mat = grid[minRow + row][minCol + col];
                if (mat == null) {
                    rowBuilder.append("AIR ");
                } else {
                    rowBuilder.append(mat.name()).append(" ");
                }
            }
            pattern[row] = rowBuilder.toString().trim();
        }

        // Generate unique recipe key
        String recipeKey = generateRecipeKey(result.getType());

        try {
            ShapedRecipeData recipeData = null;
            ShapelessRecipeData shapelessData = null;

            if (currentType == RecipeType.SHAPED) {
                // Create ShapedRecipeData
                recipeData = ShapedRecipeData.fromPattern(pattern);
            } else {
                // Create ShapelessRecipeData - count items in grid
                org.bukkit.inventory.ItemStack[] gridItems = new org.bukkit.inventory.ItemStack[9];
                for (int i = 0; i < 9; i++) {
                    gridItems[i] = inventory.getItem(GRID_SLOTS[i]);
                }
                shapelessData = ShapelessRecipeData.fromGridItems(gridItems);
            }

            // Create CustomRecipe
            CustomRecipe newRecipe = new CustomRecipe(
                    recipeKey,
                    null,
                    new ArrayList<>(),
                    null,
                    new ArrayList<>(),
                    currentType,
                    recipeData,
                    shapelessData,
                    result.clone(),
                    false
            );

            // Save to config
            saveRecipeToConfig(recipeKey, pattern, shapelessData, result);

            // Add to enabled list
            List<String> enabledRecipes = plugin.getConfig().getStringList("enabled-recipes");
            if (!enabledRecipes.contains(recipeKey)) {
                enabledRecipes.add(recipeKey);
                plugin.getConfig().set("enabled-recipes", enabledRecipes);
                plugin.saveConfig();
                plugin.reloadConfig();
            }

            // Register recipe
            plugin.getRecipeManager().registerSingleRecipe(newRecipe);
            MessageUtil.sendSuccess(player, "Created recipe: <white>" + recipeKey);

            // Open editor for new recipe
            player.closeInventory();
            new RecipeEditorGUI(plugin, player, newRecipe).open();


        } catch (ValidationException e) {
            MessageUtil.sendError(player, "Failed to create recipe: " + e.getMessage());
            plugin.getLogger().warning("Recipe creation failed: " + e.getMessage());
        }
    }

    /**
     * Generates a unique recipe key
     */
    private String generateRecipeKey(Material material) {
        String baseName = formatMaterialName(material.name());
        String key = baseName;
        int counter = 1;

        while (plugin.getRecipeManager().getRecipe(key) != null) {
            key = baseName + counter;
            counter++;
        }
        return key;
    }

    /**
     * Formats material name to PascalCase
     */
    private String formatMaterialName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return result.toString();
    }

    /**
     * Saves recipe to config.yml and JSON
     */
    private void saveRecipeToConfig(String recipeKey, String[] pattern, ShapelessRecipeData shapelessData, ItemStack result) {
        try {
            Map<String, Object> recipeData = new HashMap<>();

            // Recipe type
            if (currentType == RecipeType.SHAPELESS) {
                recipeData.put("type", currentType.name());
            }

            // Recipe pattern or ingredients
            if (currentType == RecipeType.SHAPED) {
                recipeData.put("recipe", List.of(pattern));
            } else {
                recipeData.put("ingredients", shapelessData.toConfigList());
            }

            // Save full ItemStack result (new format)
            Map<String, Object> resultData = ItemStackSerializer.toMap(result);
            recipeData.put("result", resultData);

            // Also save legacy fields for backwards compatibility
            recipeData.put("material", result.getType().name());
            recipeData.put("amount", result.getAmount());
            recipeData.put("hidden", false);

            // Extract names and lore from result item meta
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    String name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(meta.displayName());
                    if (name != null && !name.isEmpty()) {
                        recipeData.put("gui-name", name);
                        recipeData.put("crafted-name", name);
                    }
                }

                if (meta.hasLore() && meta.lore() != null) {
                    List<String> loreStrings = new ArrayList<>();
                    for (Component line : meta.lore()) {
                        String loreText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                                .serialize(line);
                        if (loreText != null && !loreText.isEmpty()) {
                            loreStrings.add(loreText);
                        }
                    }
                    if (!loreStrings.isEmpty()) {
                        recipeData.put("gui-description", loreStrings);
                        recipeData.put("crafted-description", loreStrings);
                    }
                }
            }

            // Save to YAML file with full ItemStack
            plugin.getConfigManager().getRecipeFileManager().saveRecipe(recipeKey, recipeData);

            // ALSO save to JSON file (optional, for human-readable format)
            JsonRecipeFileManager jsonManager = new JsonRecipeFileManager(plugin);
            jsonManager.saveRecipeJson(recipeKey, recipeData);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save recipe: " + e.getMessage());
            MessageUtil.sendError(player, "Failed to save recipe file!");
        }
    }

    /**
     * Extracts MiniMessage format from Component
     */
    private String extractMiniMessage(Component component) {
        if (component == null) return null;
        // Simple extraction - you may want more sophisticated parsing
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
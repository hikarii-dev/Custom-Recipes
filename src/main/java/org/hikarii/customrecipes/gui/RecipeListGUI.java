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
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing all custom recipes
 */
public class RecipeListGUI implements Listener {

    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private final List<CustomRecipe> recipes;
    private int page = 0;
    private static final int RECIPES_PER_PAGE = 45; // 5 rows for recipes

    public RecipeListGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.recipes = new ArrayList<>(plugin.getRecipeManager().getAllRecipes());
        this.inventory = Bukkit.createInventory(
                null,
                54, // 6 rows
                Component.text("Custom Recipes", NamedTextColor.DARK_PURPLE)
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

        if (recipes.isEmpty()) {
            // Show "no recipes" message
            ItemStack noRecipes = new ItemStack(Material.BARRIER);
            ItemMeta meta = noRecipes.getItemMeta();
            meta.displayName(Component.text("No Recipes Loaded", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Add recipes in config.yml", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            noRecipes.setItemMeta(meta);
            inventory.setItem(22, noRecipes);
            return;
        }

        // Calculate pagination
        int startIndex = page * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, recipes.size());

        // Add recipe items
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            CustomRecipe recipe = recipes.get(i);
            ItemStack item = createRecipeItem(recipe);
            inventory.setItem(slot++, item);
        }

        // Add navigation buttons (bottom row)
        addNavigationButtons();

        // Add info item
        addInfoItem();
    }

    /**
     * Creates an item representing a recipe
     */
    private ItemStack createRecipeItem(CustomRecipe recipe) {
        ItemStack item = new ItemStack(recipe.getResultMaterial());
        ItemMeta meta = item.getItemMeta();

        // Check if recipe is enabled
        boolean enabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());

        // Set display name with status indicator
        Component displayName;
        if (recipe.getGuiName() != null && !recipe.getGuiName().isEmpty()) {
            displayName = MessageUtil.colorize(recipe.getGuiName());
        } else {
            displayName = Component.text(
                    MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                    NamedTextColor.AQUA
            );
        }

        // Add status indicator
        Component statusIndicator = Component.text(
                enabled ? " ✓" : " ✗",
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED
        );
        displayName = displayName.append(statusIndicator);

        meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));

        // Set lore
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        // Status
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(
                        enabled ? "Enabled" : "Disabled",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED
                ))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Recipe Key: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getKey(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().toString(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getResultAmount() + "x", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        // Add GUI description if present
        if (recipe.getGuiDescription() != null && !recipe.getGuiDescription().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Description:", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            for (String line : recipe.getGuiDescription()) {
                lore.add(MessageUtil.colorize(line)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to manage recipe", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Adds navigation buttons and control buttons to the bottom row
     */
    private void addNavigationButtons() {
        int maxPages = (int) Math.ceil((double) recipes.size() / RECIPES_PER_PAGE);

        // Previous page button (left of info book - slot 48) - always visible
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        if (page > 0) {
            prevMeta.displayName(Component.text("◀ Previous Page", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            prevMeta.lore(List.of(
                    Component.text("Page " + page + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            prevMeta.displayName(Component.text("◀ Previous Page", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            prevMeta.lore(List.of(
                    Component.text("Already on first page", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }
        prevButton.setItemMeta(prevMeta);
        inventory.setItem(48, prevButton);

        // Next page button (right of info book - slot 50) - always visible
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        if (page < maxPages - 1) {
            nextMeta.displayName(Component.text("Next Page ▶", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            nextMeta.lore(List.of(
                    Component.text("Page " + (page + 2) + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            nextMeta.displayName(Component.text("Next Page ▶", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            nextMeta.lore(List.of(
                    Component.text("Already on last page", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }
        nextButton.setItemMeta(nextMeta);
        inventory.setItem(50, nextButton);

        // Create recipe button (slot 45)
        ItemStack createButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.displayName(Component.text("Create New Recipe", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        createMeta.lore(List.of(
                Component.empty(),
                Component.text("» Click to create recipe", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(45, createButton);

        // Settings button (slot 53)
        ItemStack settingsButton = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsButton.getItemMeta();
        settingsMeta.displayName(Component.text("Settings", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        settingsMeta.lore(List.of(
                Component.empty(),
                Component.text("» Click to open settings", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        settingsButton.setItemMeta(settingsMeta);
        inventory.setItem(53, settingsButton);
    }

    /**
     * Adds info item
     */
    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Total Recipes: " + recipes.size(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Click any recipe to view", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("its crafting pattern!", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        info.setItemMeta(meta);
        inventory.setItem(49, info);
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

        // Navigation buttons (left of book - slot 48, right of book - slot 50)
        if (slot == 48 && page > 0) {
            page--;
            updateInventory();
            return;
        }

        int maxPages = (int) Math.ceil((double) recipes.size() / RECIPES_PER_PAGE);
        if (slot == 50 && page < maxPages - 1) {
            page++;
            updateInventory();
            return;
        }

        // Create recipe button (slot 45)
        if (slot == 45) {
            if (!player.hasPermission("customrecipes.manage")) {
                MessageUtil.sendError(player, "You don't have permission to create recipes.");
                return;
            }
            // Open recipe creator GUI (placeholder for now)
            player.closeInventory();
            MessageUtil.sendInfo(player, "Recipe creator coming soon!");
            return;
        }

        // Settings button (slot 53)
        if (slot == 53) {
            new SettingsGUI(plugin, player).open();
            return;
        }

        // Recipe click - open recipe editor/viewer
        if (slot < 45) {
            int recipeIndex = (page * RECIPES_PER_PAGE) + slot;
            if (recipeIndex < recipes.size()) {
                CustomRecipe recipe = recipes.get(recipeIndex);
                new RecipeEditorGUI(plugin, player, recipe).open();
            }
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
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
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerRecipeListGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private List<CustomRecipe> recipes;
    private int page = 0;
    private static final int RECIPES_PER_PAGE = 45;
    private RecipeFilter currentFilter = RecipeFilter.ALL;

    public enum RecipeFilter {
        ALL("All Stations", Material.CHEST, NamedTextColor.WHITE),
        CRAFTING_TABLE("Crafting Table", Material.CRAFTING_TABLE, NamedTextColor.GREEN);

        private final String displayName;
        private final Material icon;
        private final NamedTextColor color;

        RecipeFilter(String displayName, Material icon, NamedTextColor color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public NamedTextColor getColor() {
            return color;
        }

        public RecipeFilter next() {
            RecipeFilter[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public PlayerRecipeListGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.recipes = getFilteredRecipes();
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Custom Recipes")
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private List<CustomRecipe> getFilteredRecipes() {
        List<CustomRecipe> allRecipes = new ArrayList<>(plugin.getRecipeManager().getAllRecipes());
        allRecipes = allRecipes.stream()
                .filter(recipe -> plugin.getRecipeManager().isRecipeEnabled(recipe.getKey()))
                .collect(Collectors.toList());
        if (currentFilter == RecipeFilter.ALL) {
            return allRecipes;
        }
        return allRecipes.stream()
                .filter(recipe -> recipe.getType() == RecipeType.SHAPED || recipe.getType() == RecipeType.SHAPELESS)
                .collect(Collectors.toList());
    }

    private void cycleFilter() {
        currentFilter = currentFilter.next();
        recipes = getFilteredRecipes();
        page = 0;
        updateInventory();
    }

    private void updateInventory() {
        inventory.clear();
        if (recipes.isEmpty()) {
            ItemStack noRecipes = new ItemStack(Material.BARRIER);
            ItemMeta meta = noRecipes.getItemMeta();
            meta.displayName(Component.text("No Custom Recipes Available", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Check back later!", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            noRecipes.setItemMeta(meta);
            inventory.setItem(22, noRecipes);
            addNavigationButtons();
            return;
        }
        ItemStack emptySlot = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = emptySlot.getItemMeta();
        emptyMeta.displayName(Component.empty());
        emptySlot.setItemMeta(emptyMeta);
        for (int i = 0; i < RECIPES_PER_PAGE; i++) {
            inventory.setItem(i, emptySlot);
        }
        int startIndex = page * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, recipes.size());
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            CustomRecipe recipe = recipes.get(i);
            ItemStack item = createRecipeItem(recipe);
            inventory.setItem(slot++, item);
        }
        addNavigationButtons();
        addInfoItem();
    }

    private ItemStack createRecipeItem(CustomRecipe recipe) {
        ItemStack item = new ItemStack(recipe.getResultMaterial());
        ItemMeta meta = item.getItemMeta();
        Component displayName;
        if (recipe.getName() != null && !recipe.getName().isEmpty()) {
            displayName = MessageUtil.colorize(recipe.getName())
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false);
        } else {
            displayName = Component.text(
                            MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                            NamedTextColor.AQUA
                    ).decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false);
        }
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("👁 ", NamedTextColor.AQUA)
                .append(Component.text("Click to view recipe", NamedTextColor.GRAY))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationButtons() {
        int maxPages = (int) Math.ceil((double) recipes.size() / RECIPES_PER_PAGE);
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
        ItemStack filterButton = new ItemStack(currentFilter.getIcon());
        ItemMeta filterMeta = filterButton.getItemMeta();
        filterMeta.displayName(Component.text("Filter: " + currentFilter.getDisplayName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> filterLore = new ArrayList<>();
        filterLore.add(Component.empty());
        filterLore.add(Component.text("Click to change filter", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        filterLore.add(Component.empty());
        for (RecipeFilter filter : RecipeFilter.values()) {
            NamedTextColor color = filter == currentFilter ? NamedTextColor.GREEN : NamedTextColor.YELLOW;
            filterLore.add(Component.text("  • " + filter.getDisplayName(), color)
                    .decoration(TextDecoration.ITALIC, false));
        }
        filterMeta.lore(filterLore);
        filterButton.setItemMeta(filterMeta);
        inventory.setItem(49, filterButton);
    }

    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.empty());
        int totalRecipes = plugin.getRecipeManager().getAllRecipes().stream()
                .filter(recipe -> plugin.getRecipeManager().isRecipeEnabled(recipe.getKey()))
                .collect(Collectors.toList()).size();
        int filteredRecipes = recipes.size();
        if (currentFilter == RecipeFilter.ALL) {
            infoLore.add(Component.text("Total Recipes: " + totalRecipes, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            infoLore.add(Component.text("Showing: " + filteredRecipes + "/" + totalRecipes, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            infoLore.add(Component.text("Filter: " + currentFilter.getDisplayName(), currentFilter.getColor())
                    .decoration(TextDecoration.ITALIC, false));
        }
        infoLore.add(Component.empty());
        infoLore.add(Component.text("Click recipe to view details", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(infoLore);
        info.setItemMeta(meta);
        inventory.setItem(53, info);
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

        if (slot == 49) {
            cycleFilter();
            return;
        }
        if (slot < 45) {
            int recipeIndex = (page * RECIPES_PER_PAGE) + slot;
            if (recipeIndex < recipes.size()) {
                CustomRecipe recipe = recipes.get(recipeIndex);
                InventoryClickEvent.getHandlerList().unregister(this);
                InventoryCloseEvent.getHandlerList().unregister(this);
                new RecipeViewerGUI(plugin, player, recipe, this).open();
            }
        }
    }

    public void reopen() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}

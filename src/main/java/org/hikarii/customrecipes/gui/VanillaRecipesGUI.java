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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeInfo;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeManager;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.*;
import java.util.stream.Collectors;

public class VanillaRecipesGUI implements Listener {
    private static final Map<UUID, VanillaRecipesGUI> waitingForSearch = new HashMap<>();
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private int page = 0;
    private static final int RECIPES_PER_PAGE = 45;

    private VanillaRecipeInfo.RecipeCategory currentCategory = null;
    private VanillaRecipeInfo.RecipeStation currentStation = VanillaRecipeInfo.RecipeStation.CRAFTING_TABLE;
    private List<VanillaRecipeInfo> displayedRecipes;
    private static final Map<UUID, VanillaRecipeInfo.RecipeCategory> savedCategories = new HashMap<>();
    private static final Map<UUID, VanillaRecipeInfo.RecipeStation> savedStations = new HashMap<>();

    public VanillaRecipesGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        VanillaRecipeInfo.RecipeCategory savedCat = savedCategories.get(player.getUniqueId());
        VanillaRecipeInfo.RecipeStation savedStat = savedStations.get(player.getUniqueId());

        if (savedCat != null) {
            this.currentCategory = savedCat;
        }
        if (savedStat != null) {
            this.currentStation = savedStat;
        }
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("Vanilla Recipes", NamedTextColor.DARK_GREEN)
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateDisplayedRecipes();
        updateInventory();
    }

    public VanillaRecipesGUI(CustomRecipes plugin, Player player, VanillaRecipeInfo.RecipeCategory category, VanillaRecipeInfo.RecipeStation station) {
        this.plugin = plugin;
        this.player = player;
        this.currentCategory = category;
        this.currentStation = station;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("Vanilla Recipes", NamedTextColor.DARK_GREEN)
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateDisplayedRecipes();
        updateInventory();
    }

    public void open() {
        savedCategories.put(player.getUniqueId(), currentCategory);
        savedStations.put(player.getUniqueId(), currentStation);
        player.openInventory(inventory);
    }

    private void updateDisplayedRecipes() {
        List<VanillaRecipeInfo> allRecipes = new ArrayList<>(
                plugin.getVanillaRecipeManager().getAllVanillaRecipes().values()
        );

        if (currentStation != null) {
            allRecipes = allRecipes.stream()
                    .filter(r -> r.getStation() == currentStation)
                    .collect(Collectors.toList());
        }

        if (currentCategory != null) {
            allRecipes = allRecipes.stream()
                    .filter(r -> r.getCategory() == currentCategory)
                    .collect(Collectors.toList());
        }
        this.displayedRecipes = allRecipes;
    }

    private void updateInventory() {
        inventory.clear();
        ItemStack emptySlot = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = emptySlot.getItemMeta();
        emptyMeta.displayName(Component.empty());
        emptySlot.setItemMeta(emptyMeta);
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, emptySlot);
        }
        addRecipeItems();
        addNavigationButtons();
        addFilterButtons();
        addInfoItem();
        addSearchButton();
        addBackButton();
    }

    private void addRecipeItems() {
        int startIndex = page * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, displayedRecipes.size());
        for (int i = startIndex; i < endIndex; i++) {
            VanillaRecipeInfo recipe = displayedRecipes.get(i);
            ItemStack item = createRecipeItem(recipe);
            inventory.setItem(i - startIndex, item);
        }
    }

    private ItemStack createRecipeItem(VanillaRecipeInfo recipe) {
        String recipeKey = recipe.getKey().replace("minecraft:", "");
        ItemStack item = new ItemStack(recipe.getResultMaterial());
        ItemMeta meta = item.getItemMeta();
        boolean disabled = plugin.getVanillaRecipeManager().isRecipeDisabled(recipeKey);
        boolean changed = plugin.getVanillaRecipeManager().isRecipeChanged(recipeKey);
        NamedTextColor nameColor;
        String statusText;
        if (disabled) {
            nameColor = NamedTextColor.RED;
            statusText = "✗ Disabled";
        } else if (changed) {
            nameColor = NamedTextColor.YELLOW;
            statusText = "⚡ Modified";
        } else {
            nameColor = NamedTextColor.GREEN;
            statusText = "✓ Enabled";
        }
        Component displayName = Component.text(recipe.getDisplayName(), nameColor)
                .decoration(TextDecoration.ITALIC, false);
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(statusText, nameColor))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getResultAmount() + "x", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Category: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getCategory().getDisplayName(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Left Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Right Click to " + (disabled ? "enable" : "disable"),
                        disabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addNavigationButtons() {
        int maxPages = (int) Math.ceil((double) displayedRecipes.size() / RECIPES_PER_PAGE);
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevButton.getItemMeta();
        prevMeta.displayName(Component.text("◀ Previous Page",
                        page > 0 ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        if (page > 0) {
            prevMeta.lore(List.of(
                    Component.text("Page " + page + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            prevMeta.lore(List.of(
                    Component.text("Already on first page", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }
        prevButton.setItemMeta(prevMeta);
        inventory.setItem(48, prevButton);
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.displayName(Component.text("Next Page ▶",
                        page < maxPages - 1 ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));

        if (page < maxPages - 1) {
            nextMeta.lore(List.of(
                    Component.text("Page " + (page + 2) + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        } else {
            nextMeta.lore(List.of(
                    Component.text("Already on last page", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        }
        nextButton.setItemMeta(nextMeta);
        inventory.setItem(50, nextButton);
    }

    private void addFilterButtons() {
        ItemStack stationButton = new ItemStack(currentStation.getIcon());
        ItemMeta stationMeta = stationButton.getItemMeta();
        stationMeta.displayName(Component.text("Station: " + currentStation.getDisplayName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> stationLore = new ArrayList<>();
        stationLore.add(Component.empty());
        stationLore.add(Component.text("Click to change station", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        stationLore.add(Component.empty());
        for (VanillaRecipeInfo.RecipeStation station : VanillaRecipeInfo.RecipeStation.values()) {
            NamedTextColor color = station == currentStation ? NamedTextColor.GREEN :
                    (station.isEnabled() ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY);
            stationLore.add(Component.text("  • " + station.getDisplayName(), color)
                    .decoration(TextDecoration.ITALIC, false));
        }
        stationMeta.lore(stationLore);
        stationButton.setItemMeta(stationMeta);
        inventory.setItem(46, stationButton);
        ItemStack categoryButton = new ItemStack( currentCategory != null ? currentCategory.getIcon() : Material.CHEST );
        ItemMeta categoryMeta = categoryButton.getItemMeta();
        categoryMeta.displayName(Component.text(
                        "Category: " + (currentCategory != null ? currentCategory.getDisplayName() : "All"),
                        NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> categoryLore = new ArrayList<>();
        categoryLore.add(Component.empty());
        categoryLore.add(Component.text("Click to change category", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        categoryLore.add(Component.empty());
        categoryLore.add(Component.text("  • All Categories",
                        currentCategory == null ? NamedTextColor.GREEN : NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        for (VanillaRecipeInfo.RecipeCategory category : VanillaRecipeInfo.RecipeCategory.values()) {
            NamedTextColor color = category == currentCategory ? NamedTextColor.GREEN : NamedTextColor.YELLOW;
            categoryLore.add(Component.text("  • " + category.getDisplayName(), color)
                    .decoration(TextDecoration.ITALIC, false));
        }
        categoryMeta.lore(categoryLore);
        categoryButton.setItemMeta(categoryMeta);
        inventory.setItem(47, categoryButton);
    }

    private void addSearchButton() {
        ItemStack search = new ItemStack(Material.COMPASS);
        ItemMeta meta = search.getItemMeta();
        meta.displayName(Component.text("Search Recipe", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Search recipes by name", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("» Click to search", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        search.setItemMeta(meta);
        inventory.setItem(45, search);
    }

    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Vanilla Recipes", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Total Recipes: " + displayedRecipes.size(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Left Click to edit recipe", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Right Click to toggle enable/disable", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(49, info);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Main Menu", NamedTextColor.YELLOW)
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
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }

        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        if (slot == 48 && page > 0) {
            page--;
            updateInventory();
            return;
        }

        int maxPages = (int) Math.ceil((double) displayedRecipes.size() / RECIPES_PER_PAGE);
        if (slot == 50 && page < maxPages - 1) {
            page++;
            updateInventory();
            return;
        }

        if (slot == 45) {
            waitingForSearch.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type the recipe name to search (partial names allowed)");
            MessageUtil.sendAdminInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        if (slot == 46) {
            cycleStation();
            return;
        }

        if (slot == 47) {
            cycleCategory();
            return;
        }

        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        if (slot < 0 || slot >= 54) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR ||
                clicked.getType() == Material.LIME_STAINED_GLASS_PANE) {
            return;
        }

        if (slot >= 0 && slot < 45) {
            int recipeIndex = (page * RECIPES_PER_PAGE) + slot;
            if (recipeIndex >= 0 && recipeIndex < displayedRecipes.size()) {
                VanillaRecipeInfo recipe = displayedRecipes.get(recipeIndex);
                String recipeKey = recipe.getKey().replace("minecraft:", "");

                if (clickType.isRightClick()) {
                    plugin.getVanillaRecipeManager().toggleRecipe(recipeKey);
                    boolean nowDisabled = plugin.getVanillaRecipeManager().isRecipeDisabled(recipeKey);
                    MessageUtil.send(player,
                            (nowDisabled ? "Disabled" : "Enabled") + " vanilla recipe: " + recipe.getDisplayName(),
                            nowDisabled ? NamedTextColor.RED : NamedTextColor.GREEN);
                    updateInventory();
                } else {
                    new VanillaRecipeEditorGUI(plugin, player, recipe, this).open();
                }
            }
        }
    }

    private void cycleStation() {
        VanillaRecipeInfo.RecipeStation[] stations = VanillaRecipeInfo.RecipeStation.values();
        int currentIndex = Arrays.asList(stations).indexOf(currentStation);
        int nextIndex = currentIndex;
        do {
            nextIndex = (nextIndex + 1) % stations.length;
            if (stations[nextIndex].isEnabled()) {
                currentStation = stations[nextIndex];
                page = 0;
                updateDisplayedRecipes();
                updateInventory();
                savedStations.put(player.getUniqueId(), currentStation);
                return;
            }
        } while (nextIndex != currentIndex);
        MessageUtil.sendAdminWarning(player, "Other stations coming soon!");
    }

    private void cycleCategory() {
        VanillaRecipeInfo.RecipeCategory[] categories = VanillaRecipeInfo.RecipeCategory.values();
        if (currentCategory == null) {
            currentCategory = categories[0];
        } else {
            int currentIndex = Arrays.asList(categories).indexOf(currentCategory);
            if (currentIndex == categories.length - 1) {
                currentCategory = null;
            } else {
                currentCategory = categories[currentIndex + 1];
            }
        }
        page = 0;
        updateDisplayedRecipes();
        updateInventory();
        savedCategories.put(player.getUniqueId(), currentCategory);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        VanillaRecipesGUI gui = waitingForSearch.get(chatPlayer.getUniqueId());
        if (gui == null || !chatPlayer.equals(player)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equalsIgnoreCase("cancel")) {
            waitingForSearch.remove(chatPlayer.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendAdminWarning(chatPlayer, "Search cancelled");
                gui.open();
            });
            return;
        }

        List<VanillaRecipeInfo> results = plugin.getVanillaRecipeManager().searchRecipes(message);
        waitingForSearch.remove(chatPlayer.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (results.isEmpty()) {
                MessageUtil.sendError(chatPlayer, "No recipes found matching: " + message);
                gui.open();
            } else {
                new VanillaRecipeSearchResultsGUI(plugin, chatPlayer, results, message, gui).open();
            }
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            if (!waitingForSearch.containsKey(player.getUniqueId())) {
                savedCategories.remove(player.getUniqueId());
                savedStations.remove(player.getUniqueId());
                InventoryClickEvent.getHandlerList().unregister(this);
                InventoryCloseEvent.getHandlerList().unregister(this);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
            }
        }
    }

    public void reopen() {
        updateDisplayedRecipes();
        updateInventory();
        open();
    }
    public VanillaRecipeInfo.RecipeCategory getCurrentCategory() {
        return currentCategory;
    }

    public VanillaRecipeInfo.RecipeStation getCurrentStation() {
        return currentStation;
    }
}
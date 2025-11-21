package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.Map;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeListGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private List<CustomRecipe> recipes;
    private int page = 0;
    private static final int RECIPES_PER_PAGE = 45;
    private RecipeFilter currentFilter = RecipeFilter.ALL;

    public enum RecipeFilter {
        ALL("All Stations", Material.CHEST, NamedTextColor.WHITE),
        CRAFTING_TABLE("Crafting Table", Material.CRAFTING_TABLE, NamedTextColor.GREEN),
        FURNACE("Furnace", Material.FURNACE, NamedTextColor.GOLD),
        BLAST_FURNACE("Blast Furnace", Material.BLAST_FURNACE, NamedTextColor.DARK_RED),
        SMOKER("Smoker", Material.SMOKER, NamedTextColor.GRAY),
        ANVIL("Anvil", Material.ANVIL, NamedTextColor.DARK_GRAY),
        SMITHING_TABLE("Smithing Table", Material.SMITHING_TABLE, NamedTextColor.BLUE),
        BREWING_STAND("Brewing Stand", Material.BREWING_STAND, NamedTextColor.LIGHT_PURPLE),
        STONECUTTER("Stonecutter", Material.STONECUTTER, NamedTextColor.GRAY);

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

    public RecipeListGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.recipes = getFilteredRecipes();
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Custom Recipes Menu")
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private List<CustomRecipe> getFilteredRecipes() {
        List<CustomRecipe> allRecipes = new ArrayList<>(plugin.getRecipeManager().getAllRecipes());
        if (currentFilter == RecipeFilter.ALL) {
            return allRecipes;
        }
        return allRecipes.stream()
                .filter(recipe -> {
                    switch (currentFilter) {
                        case CRAFTING_TABLE:
                            return recipe.getType() == RecipeType.SHAPED ||
                                   recipe.getType() == RecipeType.SHAPELESS;
                        case FURNACE:
                        case BLAST_FURNACE:
                        case SMOKER:
                        case ANVIL:
                        case SMITHING_TABLE:
                        case BREWING_STAND:
                        case STONECUTTER:
                            return false;
                        default:
                            return true;
                    }
                })
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
            meta.displayName(Component.text("Coming soon", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Please wait a few days", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("while I update the plugin", NamedTextColor.GRAY)
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
        boolean enabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());
        Component displayName;
        if (recipe.getName() != null && !recipe.getName().isEmpty()) {
            displayName = MessageUtil.colorize(recipe.getName())
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false);
        } else {
            displayName = Component.text(
                            MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                            NamedTextColor.AQUA)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false);
        }
        meta.displayName(displayName);
        List<Component> lore = new ArrayList<>();
        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            for (String line : recipe.getDescription()) {
                lore.add(MessageUtil.colorize(line)
                        .decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.empty());
        }
        Component statusLine = Component.text(enabled ? "✓ " : "✗ ",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text("Status: ", NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.text(enabled ? "Enabled" : "Disabled",
                                enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD, false))
                .decoration(TextDecoration.ITALIC, false);
        lore.add(statusLine);
        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().toString(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));

        ItemMeta resultMeta = recipe.getResultItem().getItemMeta();
        if (resultMeta != null) {
            boolean hasSpecialProps = false;
            if (resultMeta.hasCustomModelData()) {
                if (!hasSpecialProps) {
                    lore.add(Component.empty());
                    hasSpecialProps = true;
                }
                lore.add(Component.text("Custom Model Data: ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(resultMeta.getCustomModelData(), NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
            }
            if (resultMeta.hasEnchants()) {
                if (!hasSpecialProps) {
                    lore.add(Component.empty());
                    hasSpecialProps = true;
                }
                lore.add(Component.text("Enchantments: ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(resultMeta.getEnchants().size() + "x", NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                int shown = 0;
                for (Map.Entry<Enchantment, Integer> entry : resultMeta.getEnchants().entrySet()) {
                    if (shown++ >= 2) break;
                    String enchName = entry.getKey().getKey().getKey();
                    lore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                }
                if (resultMeta.getEnchants().size() > 2) {
                    lore.add(Component.text("  ... and " + (resultMeta.getEnchants().size() - 2) + " more",
                                    NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
                }
            }
            PersistentDataContainer container = resultMeta.getPersistentDataContainer();
            if (!container.isEmpty()) {
                if (!hasSpecialProps) {
                    lore.add(Component.empty());
                }
                lore.add(Component.text("NBT Data: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(container.getKeys().size() + " tags", NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                int shown = 0;
                for (NamespacedKey key : container.getKeys()) {
                    if (shown++ >= 2) break;
                    if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                        String value = container.get(key, PersistentDataType.STRING);
                        lore.add(Component.text("  • " + key.getKey() + ": " + value, NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
                if (container.getKeys().size() > 2) {
                    lore.add(Component.text("  ... and " + (container.getKeys().size() - 2) + " more",
                                    NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
                }
            }
        }
        lore.add(Component.empty());
        lore.add(Component.text("⚙ ", NamedTextColor.GOLD)
                .append(Component.text("Left-click to manage", NamedTextColor.GRAY))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("✎ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("Right-click to edit item", NamedTextColor.GRAY))
                .decoration(TextDecoration.ITALIC, false));
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        if (!disabledWorlds.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("⚠ World Restrictions:", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Disabled in: " + String.join(", ", disabledWorlds),
                            NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
        }
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

        ItemStack vanillaButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta vanillaMeta = vanillaButton.getItemMeta();
        vanillaMeta.displayName(Component.text("Vanilla Recipes", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        vanillaMeta.lore(List.of(
                Component.empty(),
                Component.text("» Click to browse", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        vanillaButton.setItemMeta(vanillaMeta);
        inventory.setItem(52, vanillaButton);

        ItemStack filterButton = new ItemStack(currentFilter.getIcon());
        ItemMeta filterMeta = filterButton.getItemMeta();
        filterMeta.displayName(Component.text("Station: " + currentFilter.getDisplayName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> filterLore = new ArrayList<>();
        filterLore.add(Component.empty());
        filterLore.add(Component.text("Click to change station", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        filterLore.add(Component.empty());
        for (RecipeFilter filter : RecipeFilter.values()) {
            NamedTextColor color = filter == currentFilter ? NamedTextColor.GREEN : NamedTextColor.YELLOW;
            filterLore.add(Component.text("  • " + filter.getDisplayName(), color)
                    .decoration(TextDecoration.ITALIC, false));
        }
        filterMeta.lore(filterLore);
        filterButton.setItemMeta(filterMeta);
        inventory.setItem(47, filterButton);

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

    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.empty());

        int totalRecipes = plugin.getRecipeManager().getAllRecipes().size();
        int filteredRecipes = recipes.size();
        if (currentFilter == RecipeFilter.ALL) {
            infoLore.add(Component.text("Total Recipes: " + totalRecipes, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            infoLore.add(Component.text("Showing: " + filteredRecipes + "/" + totalRecipes, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            infoLore.add(Component.text("Station: " + currentFilter.getDisplayName(), currentFilter.getColor())
                    .decoration(TextDecoration.ITALIC, false));
        }
        infoLore.add(Component.empty());
        infoLore.add(Component.text("Left Click recipe to manage", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        infoLore.add(Component.text("Right Click recipe to edit item", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(infoLore);
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
        ClickType clickType = event.getClick();
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

        if (slot == 45) {
            if (!player.hasPermission("customrecipes.manage")) {
                MessageUtil.sendError(player, "You don't have permission to create recipes.");
                return;
            }
            new StationSelectorGUI(plugin, player).open();
            return;
        }

        if (slot == 47) {
            
            cycleFilter();
            MessageUtil.sendAdminInfo(player, "Station filter: <white>" + currentFilter.getDisplayName());
            return;
        }

        if (slot == 52) {
            new VanillaRecipesGUI(plugin, player).open();
            return;
        }

        if (slot == 53) {
            new SettingsGUI(plugin, player).open();
            return;
        }

        if (slot < 45) {
            int recipeIndex = (page * RECIPES_PER_PAGE) + slot;
            if (recipeIndex < recipes.size()) {
                CustomRecipe recipe = recipes.get(recipeIndex);
                if (clickType == ClickType.RIGHT) {
                    if (!player.hasPermission("customrecipes.manage")) {
                        MessageUtil.sendError(player, "You don't have permission to edit recipes.");
                        return;
                    }
                    ItemStack resultItem = recipe.getResultItem();
                    new ItemEditorGUI(plugin, player, resultItem, (editedItem) -> {
                        if (editedItem != null) {
                            ItemEditorGUI editor = ItemEditorGUI.getLastEditor(player.getUniqueId());
                            String newName = editor != null ? editor.getCustomName() : null;
                            List<String> newDesc = editor != null ? editor.getCustomLore() : null;
                            plugin.getRecipeManager().updateRecipeResult(recipe.getKey(), editedItem,
                                    newName, newDesc);
                            MessageUtil.sendAdminSuccess(player, "Updated result item for recipe: " + recipe.getKey());
                        }
                        new RecipeListGUI(plugin, player).open();
                    }).open();
                } else {
                    new RecipeEditorGUI(plugin, player, recipe).open();
                }
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
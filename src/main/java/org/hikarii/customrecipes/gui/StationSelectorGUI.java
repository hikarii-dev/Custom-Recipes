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
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;

public class StationSelectorGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;

    public enum RecipeStation {
        CRAFTING_TABLE("Crafting Table", Material.CRAFTING_TABLE, true, 9,
                "Create shaped or shapeless recipes", "Standard 3x3 crafting grid"),

        FURNACE("Furnace", Material.FURNACE, false, 2,
                "Smelt items with fuel", "Coming soon!"),

        BLAST_FURNACE("Blast Furnace", Material.BLAST_FURNACE, false, 2,
                "Faster ore smelting", "Coming soon!"),

        SMOKER("Smoker", Material.SMOKER, false, 2,
                "Faster food cooking", "Coming soon!"),

        STONECUTTER("Stonecutter", Material.STONECUTTER, false, 1,
                "Cut stone blocks", "Coming soon!"),

        BREWING_STAND("Brewing Stand", Material.BREWING_STAND, false, 4,
                "Brew potions", "Coming soon!"),

        SMITHING_TABLE("Smithing Table", Material.SMITHING_TABLE, false, 3,
                "Upgrade equipment", "Coming soon!"),

        ANVIL("Anvil", Material.ANVIL, false, 2,
                "Combine and rename items", "Coming soon!"),

        LOOM("Loom", Material.LOOM, false, 3,
                "Create banners", "Coming soon!");

        private final String displayName;
        private final Material icon;
        private final boolean enabled;
        private final int gridSize;
        private final String description;
        private final String statusMessage;

        RecipeStation(String displayName, Material icon, boolean enabled, int gridSize,
                      String description, String statusMessage) {
            this.displayName = displayName;
            this.icon = icon;
            this.enabled = enabled;
            this.gridSize = gridSize;
            this.description = description;
            this.statusMessage = statusMessage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getGridSize() {
            return gridSize;
        }

        public String getDescription() {
            return description;
        }

        public String getStatusMessage() {
            return statusMessage;
        }
    }

    public StationSelectorGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Select Crafting Station")
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
        addStationButtons();
        addInfoItem();
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
    }
    private void addStationButtons() {
        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25, 28};
        RecipeStation[] stations = RecipeStation.values();
        for (int i = 0; i < Math.min(stations.length, slots.length); i++) {
            RecipeStation station = stations[i];
            ItemStack button = createStationButton(station);
            inventory.setItem(slots[i], button);
        }
    }

    private ItemStack createStationButton(RecipeStation station) {
        ItemStack item = new ItemStack(station.getIcon());
        ItemMeta meta = item.getItemMeta();

        NamedTextColor nameColor = station.isEnabled() ? NamedTextColor.AQUA : NamedTextColor.DARK_GRAY;
        meta.displayName(Component.text(station.getDisplayName(), nameColor)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text(station.getDescription(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Grid Size: ", NamedTextColor.GRAY)
                .append(Component.text(station.getGridSize() + " slot" + (station.getGridSize() > 1 ? "s" : ""),
                        NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        if (station.isEnabled()) {
            lore.add(Component.text("✓ ", NamedTextColor.GREEN)
                    .append(Component.text("Available", NamedTextColor.GREEN))
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("» Click to create recipe", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, true));
        } else {
            lore.add(Component.text("✗ ", NamedTextColor.RED)
                    .append(Component.text(station.getStatusMessage(), NamedTextColor.RED))
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        if (station.isEnabled()) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Crafting Stations", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Select a crafting station", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("to create a custom recipe", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        int enabledCount = 0;
        int totalCount = RecipeStation.values().length;
        for (RecipeStation station : RecipeStation.values()) {
            if (station.isEnabled()) enabledCount++;
        }
        lore.add(Component.text("Available: ", NamedTextColor.GRAY)
                .append(Component.text(enabledCount + "/" + totalCount, NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("More stations coming soon!", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(49, info);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Recipe List", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Return to main menu", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
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

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        int slot = event.getSlot();
        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to create recipes.");
            return;
        }

        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        int[] stationSlots = {10, 12, 14, 16, 19, 21, 23, 25, 28};
        for (int i = 0; i < stationSlots.length; i++) {
            if (slot == stationSlots[i] && i < RecipeStation.values().length) {
                RecipeStation station = RecipeStation.values()[i];
                handleStationClick(station);
                return;
            }
        }
    }

    private void handleStationClick(RecipeStation station) {
        if (!station.isEnabled()) {
            MessageUtil.sendAdminWarning(player, station.getDisplayName() + " is not yet available!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        switch (station) {
            case CRAFTING_TABLE -> {
                new RecipeCreatorGUI(plugin, player).open();
                MessageUtil.sendAdminSuccess(player, "Opening Crafting Table recipe creator...");
            }
            case FURNACE -> {
                
                MessageUtil.sendAdminWarning(player, "Furnace recipes are coming soon!");
            }
            case BLAST_FURNACE -> {
                
                MessageUtil.sendAdminWarning(player, "Blast Furnace recipes are coming soon!");
            }
            case SMOKER -> {
                
                MessageUtil.sendAdminWarning(player, "Smoker recipes are coming soon!");
            }
            case STONECUTTER -> {
                
                MessageUtil.sendAdminWarning(player, "Stonecutter recipes are coming soon!");
            }
            case BREWING_STAND -> {
                
                MessageUtil.sendAdminWarning(player, "Brewing Stand recipes are coming soon!");
            }
            case SMITHING_TABLE -> {
                
                MessageUtil.sendAdminWarning(player, "Smithing Table recipes are coming soon!");
            }
            case ANVIL -> {
                
                MessageUtil.sendAdminWarning(player, "Anvil recipes are coming soon!");
            }
            case LOOM -> {
                
                MessageUtil.sendAdminWarning(player, "Loom recipes are coming soon!");
            }
            default -> {
                MessageUtil.sendError(player, "Unknown station type!");
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
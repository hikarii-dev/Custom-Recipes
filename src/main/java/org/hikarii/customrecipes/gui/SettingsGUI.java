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

/**
 * GUI for plugin settings
 */
public class SettingsGUI implements Listener {

    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;

    public SettingsGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                Component.text("Settings", NamedTextColor.DARK_PURPLE)
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
        addSpawnEggNameSetting();
        addCraftedNamesSetting();
        addBackButton();
    }

    /**
     * Fills the borders with decorative glass panes
     */
    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);

        // Fill all slots
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }
    }

    /**
     * Adds spawn egg name setting toggle
     */
    private void addSpawnEggNameSetting() {
        boolean enabled = plugin.isKeepSpawnEggNames();

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "Enabled" : "Disabled";
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Spawn Egg Custom Names", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, color))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If enabled, mobs spawned from", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("custom spawn eggs will keep", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("the custom name.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(21, item);
    }

    /**
     * Adds crafted names setting toggle
     */
    private void addCraftedNamesSetting() {
        boolean enabled = plugin.isUseCraftedCustomNames();

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "Enabled" : "Disabled";
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Crafted Item Custom Names", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, color))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If enabled, crafted items will", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("have custom names and lore", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("from the recipe config.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(23, item);
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

        // Back button (slot 49)
        if (slot == 49) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        // Check permission
        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to change settings.");
            return;
        }

        // Spawn egg name toggle (slot 21)
        if (slot == 21) {
            boolean newValue = !plugin.isKeepSpawnEggNames();
            plugin.getConfig().set("spawn-egg-keep-custom-name", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();

            MessageUtil.sendSuccess(player,
                    (newValue ? "Enabled" : "Disabled") + " spawn egg custom names");
            updateInventory();
            return;
        }

        // Crafted names toggle (slot 23)
        if (slot == 23) {
            boolean newValue = !plugin.isUseCraftedCustomNames();
            plugin.getConfig().set("use-crafted-custom-names", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();

            MessageUtil.sendSuccess(player,
                    (newValue ? "Enabled" : "Disabled") + " crafted item custom names");
            updateInventory();
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
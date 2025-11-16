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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for editing item properties (name, lore, etc.)
 */
public class ItemEditorGUI implements Listener {

    private static final Map<UUID, ItemEditorGUI> waitingForInput = new HashMap<>();

    private enum EditMode {
        NONE,
        NAME,
        LORE
    }

    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private final ItemStack item;
    private final Runnable onComplete;

    private String customName;
    private List<String> customLore;
    private EditMode currentMode = EditMode.NONE;

    public ItemEditorGUI(CustomRecipes plugin, Player player, ItemStack item, Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.item = item.clone(); // Clone to avoid modifying original
        this.onComplete = onComplete;
        this.customName = null;
        this.customLore = new ArrayList<>();

        this.inventory = Bukkit.createInventory(
                null,
                54,
                Component.text("Item Editor", NamedTextColor.DARK_GREEN)
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
        addPreviewItem();
        addNameButton();
        addLoreButton();
        addClearButton();
        addSaveButton();
        addCancelButton();
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }
    }

    private void addPreviewItem() {
        ItemStack preview = item.clone();
        ItemMeta meta = preview.getItemMeta();

        if (meta != null) {
            if (customName != null && !customName.isEmpty()) {
                meta.displayName(MessageUtil.colorize(customName)
                        .decoration(TextDecoration.ITALIC, false));
            }

            if (!customLore.isEmpty()) {
                List<Component> loreComponents = customLore.stream()
                        .map(line -> MessageUtil.colorize(line)
                                .decoration(TextDecoration.ITALIC, false))
                        .toList();
                meta.lore(loreComponents);
            }

            preview.setItemMeta(meta);
        }

        inventory.setItem(22, preview);
    }

    private void addNameButton() {
        ItemStack button = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Name", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (customName != null && !customName.isEmpty()) {
            lore.add(Component.text("Current:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(MessageUtil.colorize(customName)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("No custom name set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit in chat", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(20, button);
    }

    private void addLoreButton() {
        ItemStack button = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Description", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (!customLore.isEmpty()) {
            lore.add(Component.text("Current lines: " + customLore.size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            for (int i = 0; i < Math.min(3, customLore.size()); i++) {
                lore.add(MessageUtil.colorize(customLore.get(i))
                        .decoration(TextDecoration.ITALIC, false));
            }
            if (customLore.size() > 3) {
                lore.add(Component.text("... and " + (customLore.size() - 3) + " more", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("No description set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to add line in chat", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(24, button);
    }

    private void addClearButton() {
        ItemStack button = new ItemStack(Material.BARRIER);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Clear All", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Remove custom name", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("and description", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to clear", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(31, button);
    }

    private void addSaveButton() {
        ItemStack button = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Save & Return", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Apply changes and", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("return to creator", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to save", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(48, button);
    }

    private void addCancelButton() {
        ItemStack button = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Cancel", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Discard changes and", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("return to creator", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to cancel", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(50, button);
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

        // Name button
        if (slot == 20) {
            currentMode = EditMode.NAME;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the item name in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        // Lore button
        if (slot == 24) {
            currentMode = EditMode.LORE;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type a description line in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>done</red> when finished, or <red>cancel</red> to cancel");
            return;
        }

        // Clear button
        if (slot == 31) {
            customName = null;
            customLore.clear();
            MessageUtil.sendSuccess(player, "Cleared all customization");
            updateInventory();
            return;
        }

        // Save button
        if (slot == 48) {
            applyToItem();
            MessageUtil.sendSuccess(player, "Saved item customization");
            player.closeInventory();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
            return;
        }

        // Cancel button
        if (slot == 50) {
            player.closeInventory();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, onComplete);
            }
            return;
        }
    }

    private void applyToItem() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (customName != null && !customName.isEmpty()) {
                meta.displayName(MessageUtil.colorize(customName)
                        .decoration(TextDecoration.ITALIC, false));
            }

            if (!customLore.isEmpty()) {
                List<Component> loreComponents = customLore.stream()
                        .map(line -> MessageUtil.colorize(line)
                                .decoration(TextDecoration.ITALIC, false))
                        .toList();
                meta.lore(loreComponents);
            }

            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        ItemEditorGUI editor = waitingForInput.get(chatPlayer.getUniqueId());

        if (editor == null || !chatPlayer.equals(player)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            waitingForInput.remove(chatPlayer.getUniqueId());
            currentMode = EditMode.NONE;
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendWarning(chatPlayer, "Cancelled");
                editor.open();
            });
            return;
        }

        if (message.equalsIgnoreCase("done") && currentMode == EditMode.LORE) {
            waitingForInput.remove(chatPlayer.getUniqueId());
            currentMode = EditMode.NONE;
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendSuccess(chatPlayer, "Finished editing description");
                editor.updateInventory();
                editor.open();
            });
            return;
        }

        // Handle input based on mode
        if (currentMode == EditMode.NAME) {
            editor.customName = message;
            waitingForInput.remove(chatPlayer.getUniqueId());
            currentMode = EditMode.NONE;
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendSuccess(chatPlayer, "Set item name");
                editor.updateInventory();
                editor.open();
            });
        } else if (currentMode == EditMode.LORE) {
            editor.customLore.add(message);
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendSuccess(chatPlayer, "Added description line");
                MessageUtil.sendInfo(chatPlayer, "Type another line, or type <gold>done</gold> to finish");
            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            // Only unregister if not waiting for input
            if (!waitingForInput.containsKey(player.getUniqueId())) {
                InventoryClickEvent.getHandlerList().unregister(this);
                InventoryCloseEvent.getHandlerList().unregister(this);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
            }
        }
    }

    public String getCustomName() {
        return customName;
    }

    public List<String> getCustomLore() {
        return customLore;
    }

    public ItemStack getEditedItem() {
        return item;
    }
}
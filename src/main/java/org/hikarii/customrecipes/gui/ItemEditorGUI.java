package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.util.MessageUtil;

import java.util.*;
import java.util.function.Consumer;

public class ItemEditorGUI implements Listener {
    private static final Map<UUID, ItemEditorGUI> waitingForInput = new HashMap<>();
    private static final Map<UUID, ItemEditorGUI> lastEditors = new HashMap<>();
    private enum EditMode {
        NONE,
        NAME,
        LORE,
        CUSTOM_MODEL_DATA,
        NBT_KEY,
        NBT_VALUE
    }

    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private final ItemStack item;
    private final Consumer<ItemStack> onComplete;
    private String customName;
    private List<String> customLore;
    private Integer customModelData;
    private Map<String, String> customNBT;
    public Map<Enchantment, Integer> customEnchantments;
    private EditMode currentMode = EditMode.NONE;
    private String tempNBTKey;
    public boolean hideEnchantments = false;

    public static ItemEditorGUI getLastEditor(UUID playerUuid) {
        return lastEditors.get(playerUuid);
    }

    public ItemEditorGUI(CustomRecipes plugin, Player player, ItemStack item, Consumer<ItemStack> onComplete) {
        this.plugin = plugin;
        lastEditors.put(player.getUniqueId(), this);
        this.player = player;
        this.item = item.clone();
        this.onComplete = onComplete;
        this.customName = null;
        this.customLore = new ArrayList<>();
        this.customModelData = null;
        this.customNBT = new HashMap<>();
        this.customEnchantments = new HashMap<>();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                customName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(meta.displayName());
            }
            if (meta.hasLore() && meta.lore() != null) {
                for (Component line : meta.lore()) {
                    String loreText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(line);
                    customLore.add(loreText);
                }
            }
            if (meta.hasCustomModelData()) {
                customModelData = meta.getCustomModelData();
            }
            if (meta.hasEnchants()) {
                customEnchantments.putAll(meta.getEnchants());
            }
            if (meta.hasItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)) {
                hideEnchantments = true;
            }
            PersistentDataContainer container = meta.getPersistentDataContainer();
            for (NamespacedKey key : container.getKeys()) {
                if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                    String value = container.get(key, PersistentDataType.STRING);
                    if (value != null) {
                        customNBT.put(key.getKey(), value);
                    }
                }
            }
        }
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("Advanced Item Editor", NamedTextColor.DARK_GREEN)
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void updateInventory() {
        inventory.clear();
        fillBorders();
        addPreviewItem();
        addNameButton();
        addLoreButton();
        addCustomModelDataButton();
        addNBTButton();
        addEnchantmentButton();
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

            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            if (hideEnchantments) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            if (!customNBT.isEmpty()) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                for (Map.Entry<String, String> entry : customNBT.entrySet()) {
                    NamespacedKey key = new NamespacedKey(plugin, entry.getKey());
                    container.set(key, PersistentDataType.STRING, entry.getValue());
                }
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
            lore.add(Component.text("No name set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(11, button);
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
        lore.add(Component.text("» Left Click to add line", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Right Click to clear & restart", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(20, button);
    }

    private void addCustomModelDataButton() {
        ItemStack button = new ItemStack(Material.PAINTING);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Custom Model Data", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (customModelData != null) {
            lore.add(Component.text("Current: ", NamedTextColor.GRAY)
                    .append(Component.text(customModelData, NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("No custom model data", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("Used for texture packs", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to set", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(15, button);
    }

    private void addNBTButton() {
        ItemStack button = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("NBT Data", NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (!customNBT.isEmpty()) {
            lore.add(Component.text("Current NBT tags: " + customNBT.size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            int shown = 0;
            for (Map.Entry<String, String> entry : customNBT.entrySet()) {
                if (shown++ >= 3) {
                    lore.add(Component.text("... and " + (customNBT.size() - 3) + " more", NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
                    break;
                }
                lore.add(Component.text(entry.getKey() + ": " + entry.getValue(), NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("No NBT data set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to add NBT", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(24, button);
    }

    private void addEnchantmentButton() {
        ItemStack button = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Enchantments", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (!customEnchantments.isEmpty()) {
            lore.add(Component.text("Current enchantments: " + customEnchantments.size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            int shown = 0;
            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                if (shown++ >= 3) {
                    lore.add(Component.text("  ... and " + (customEnchantments.size() - 3) + " more",
                                    NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false));
                    break;
                }
                String enchName = entry.getKey().getKey().getKey();
                lore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("No enchantments", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to open selector", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(29, button);
    }

    private void addClearButton() {
        ItemStack button = new ItemStack(Material.BARRIER);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Clear All", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Clear all customization", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to clear", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(53, button);
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
        lore.add(Component.text("Discard changes", NamedTextColor.GRAY)
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
        if (slot == 11) {
            currentMode = EditMode.NAME;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type the item name in chat (supports MiniMessage colors)");
            MessageUtil.sendAdminInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        if (slot == 20) {
            if (event.getClick().isRightClick()) {
                customLore.clear();
                MessageUtil.sendAdminSuccess(player, "Cleared crafted description");
                updateInventory();
                return;
            }

            currentMode = EditMode.LORE;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type a description line in chat (supports MiniMessage colors)");
            MessageUtil.sendAdminInfo(player, "Type <red>done</red> when finished, or <red>cancel</red> to cancel");
            return;
        }

        if (slot == 15) {
            currentMode = EditMode.CUSTOM_MODEL_DATA;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type the custom model data number in chat");
            MessageUtil.sendAdminInfo(player, "Type <red>cancel</red> to cancel or <red>clear</red> to remove");
            return;
        }

        if (slot == 24) {
            currentMode = EditMode.NBT_KEY;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type the NBT tag key in chat");
            MessageUtil.sendAdminInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        if (slot == 29) {
            player.closeInventory();
            String savedName = customName;
            List<String> savedLore = new ArrayList<>(customLore);
            Integer savedModelData = customModelData;
            Map<String, String> savedNBT = new HashMap<>(customNBT);
            Map<Enchantment, Integer> savedEnchants = new HashMap<>(customEnchantments);
            boolean savedHide = hideEnchantments;
            Bukkit.getScheduler().runTask(plugin, () -> {
                new EnchantmentSelectorGUI(plugin, player, item,
                        savedEnchants,
                        savedHide,
                        (v) -> {
                            ItemEditorGUI editor = ItemEditorGUI.getLastEditor(player.getUniqueId());
                            ItemEditorGUI newEditor = new ItemEditorGUI(plugin, player, item, onComplete);
                            newEditor.customName = savedName;
                            newEditor.customLore = savedLore;
                            newEditor.customModelData = savedModelData;
                            newEditor.customNBT = savedNBT;

                            if (editor != null) {
                                newEditor.customEnchantments.clear();
                                newEditor.customEnchantments.putAll(editor.customEnchantments);
                                newEditor.hideEnchantments = editor.hideEnchantments;
                            }
                            newEditor.updateInventory();
                            newEditor.open();
                        }).open();
            });
            return;
        }
        if (slot == 53) {
            customName = null;
            customLore.clear();
            customModelData = null;
            customNBT.clear();
            customEnchantments.clear();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(null);
                meta.lore(null);
                meta.setCustomModelData(null);
                for (Enchantment ench : meta.getEnchants().keySet()) {
                    meta.removeEnchant(ench);
                }
                PersistentDataContainer container = meta.getPersistentDataContainer();
                for (NamespacedKey key : new HashSet<>(container.getKeys())) {
                    if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                        container.remove(key);
                    }
                }
                item.setItemMeta(meta);
            }
            MessageUtil.sendAdminSuccess(player, "Cleared all customization");
            updateInventory();
            return;
        }
        if (slot == 48) {
            applyToItem();
            MessageUtil.sendAdminSuccess(player, "Saved item customization");
            player.closeInventory();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(getEditedItem()));
            }
            return;
        }
        if (slot == 50) {
            player.closeInventory();
            if (onComplete != null) {
                Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(null));
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

            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            if (hideEnchantments) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            if (!customNBT.isEmpty()) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                for (Map.Entry<String, String> entry : customNBT.entrySet()) {
                    NamespacedKey key = new NamespacedKey(plugin, entry.getKey());
                    container.set(key, PersistentDataType.STRING, entry.getValue());
                }
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
                MessageUtil.sendAdminWarning(chatPlayer, "Cancelled");
                editor.open();
            });
            return;
        }
        switch (currentMode) {
            case NAME -> {
                editor.customName = message;
                waitingForInput.remove(chatPlayer.getUniqueId());
                currentMode = EditMode.NONE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendAdminSuccess(chatPlayer, "Set item name");
                    editor.updateInventory();
                    editor.open();
                });
            }
            case LORE -> {
                if (message.equalsIgnoreCase("done")) {
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendAdminSuccess(chatPlayer, "Finished editing description");
                        editor.updateInventory();
                        editor.open();
                    });
                } else {
                    editor.customLore.add(message);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendAdminSuccess(chatPlayer, "Added description line");
                        MessageUtil.sendAdminInfo(chatPlayer, "Type another line, or type <gold>done</gold> to finish");
                    });
                }
            }
            case CUSTOM_MODEL_DATA -> {
                if (message.equalsIgnoreCase("clear")) {
                    editor.customModelData = null;
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendAdminSuccess(chatPlayer, "Cleared custom model data");
                        editor.updateInventory();
                        editor.open();
                    });
                } else {
                    try {
                        int modelData = Integer.parseInt(message);
                        editor.customModelData = modelData;
                        waitingForInput.remove(chatPlayer.getUniqueId());
                        currentMode = EditMode.NONE;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            MessageUtil.sendAdminSuccess(chatPlayer, "Set custom model data to " + modelData);
                            editor.updateInventory();
                            editor.open();
                        });
                    } catch (NumberFormatException e) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            MessageUtil.sendError(chatPlayer, "Invalid number! Please enter a valid integer.");
                        });
                    }
                }
            }
            case NBT_KEY -> {
                editor.tempNBTKey = message;
                currentMode = EditMode.NBT_VALUE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendAdminInfo(chatPlayer, "Now type the value for NBT tag '" + message + "'");
                });
            }
            case NBT_VALUE -> {
                editor.customNBT.put(editor.tempNBTKey, message);
                waitingForInput.remove(chatPlayer.getUniqueId());
                currentMode = EditMode.NONE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendAdminSuccess(chatPlayer, "Added NBT: " + editor.tempNBTKey + " = " + message);
                    editor.updateInventory();
                    editor.open();
                });
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
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

    public Integer getCustomModelData() {
        return customModelData;
    }

    public Map<String, String> getCustomNBT() {
        return customNBT;
    }

    public ItemStack getEditedItem() {
        return item.clone();
    }

    public void forceUpdatePreview() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Enchantment ench : new HashSet<>(meta.getEnchants().keySet())) {
                meta.removeEnchant(ench);
            }
            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            if (hideEnchantments) {
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        updateInventory();
    }
}
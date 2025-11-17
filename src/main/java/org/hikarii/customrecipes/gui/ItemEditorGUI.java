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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Enhanced GUI for editing item properties (name, lore, NBT, CustomModelData, etc.)
 */
public class ItemEditorGUI implements Listener {
    private static final Map<UUID, ItemEditorGUI> waitingForInput = new HashMap<>();
    private static final Map<UUID, ItemEditorGUI> lastEditors = new HashMap<>();

    private enum EditMode {
        NONE,
        NAME,
        LORE,
        GUI_NAME,
        GUI_DESCRIPTION,
        CUSTOM_MODEL_DATA,
        NBT_KEY,
        NBT_VALUE,
        ENCHANTMENT_NAME,
        ENCHANTMENT_LEVEL
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
    private Map<Enchantment, Integer> customEnchantments;
    private EditMode currentMode = EditMode.NONE;
    private String guiName;
    private List<String> guiDescription;
    private String craftedName;
    private List<String> craftedDescription;

    // Temporary storage for multi-step input
    private String tempNBTKey;
    private Enchantment tempEnchantment;

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
        this.guiName = null;
        this.guiDescription = new ArrayList<>();
        this.craftedName = null;
        this.craftedDescription = new ArrayList<>();

        // Load existing data from item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Load display name
            if (meta.hasDisplayName()) {
                customName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(meta.displayName());
            }

            // Load lore
            if (meta.hasLore() && meta.lore() != null) {
                for (Component line : meta.lore()) {
                    String loreText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(line);
                    customLore.add(loreText);
                }
            }

            // Load CustomModelData
            if (meta.hasCustomModelData()) {
                customModelData = meta.getCustomModelData();
            }

            // Load enchantments
            if (meta.hasEnchants()) {
                customEnchantments.putAll(meta.getEnchants());
            }

            // Load NBT data
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
                Component.text("Advanced Item Editor", NamedTextColor.DARK_GREEN)
                        .decoration(TextDecoration.ITALIC, false)
        );

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    /**
     * Constructor with pre-filled GUI and Crafted fields
     */
    public ItemEditorGUI(CustomRecipes plugin, Player player, ItemStack item,
                         String existingGuiName, List<String> existingGuiDescription,
                         String existingCraftedName, List<String> existingCraftedDescription,
                         Consumer<ItemStack> onComplete) {
        this(plugin, player, item, onComplete);

        // Override with existing GUI fields
        if (existingGuiName != null && !existingGuiName.isEmpty()) {
            this.guiName = existingGuiName;
        }
        if (existingGuiDescription != null && !existingGuiDescription.isEmpty()) {
            this.guiDescription = new ArrayList<>(existingGuiDescription);
        }

        // Override with existing Crafted fields (they override customName/customLore from ItemStack)
        if (existingCraftedName != null && !existingCraftedName.isEmpty()) {
            this.customName = existingCraftedName;
        }
        if (existingCraftedDescription != null && !existingCraftedDescription.isEmpty()) {
            this.customLore = new ArrayList<>(existingCraftedDescription);
        }

        updateInventory(); // Refresh to show loaded data
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
        addGuiNameButton();
        addGuiDescriptionButton();
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
            // Start with clean slate - remove existing name/lore
            meta.displayName(Component.empty());
            meta.lore(null);

            // Build complete lore from scratch
            List<Component> completeLore = new ArrayList<>();

            // === CRAFTED SECTION ===
            completeLore.add(Component.text("Preview of Result Item:", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            completeLore.add(Component.empty());

            // Crafted Name
            if (customName != null && !customName.isEmpty()) {
                completeLore.add(Component.text("Crafted Name:", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(MessageUtil.colorize("  " + customName)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                completeLore.add(Component.text("Crafted Name: ", NamedTextColor.AQUA)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            completeLore.add(Component.empty());

            // Crafted Description
            if (!customLore.isEmpty()) {
                completeLore.add(Component.text("Crafted Description:", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                for (String line : customLore) {
                    completeLore.add(MessageUtil.colorize("  " + line)
                            .decoration(TextDecoration.ITALIC, false));
                }
            } else {
                completeLore.add(Component.text("Crafted Description: ", NamedTextColor.AQUA)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            // Separator
            completeLore.add(Component.empty());
            completeLore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            completeLore.add(Component.empty());

            // === GUI SECTION ===

            // GUI Name
            if (guiName != null && !guiName.isEmpty()) {
                completeLore.add(Component.text("GUI Name:", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(MessageUtil.colorize("  " + guiName)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                completeLore.add(Component.text("GUI Name: ", NamedTextColor.YELLOW)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            completeLore.add(Component.empty());

            // GUI Description
            if (!guiDescription.isEmpty()) {
                completeLore.add(Component.text("GUI Description:", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                for (String line : guiDescription) {
                    completeLore.add(MessageUtil.colorize("  " + line)
                            .decoration(TextDecoration.ITALIC, false));
                }
            } else {
                completeLore.add(Component.text("GUI Description: ", NamedTextColor.YELLOW)
                        .append(Component.text("(none)", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }

            // === ADDITIONAL INFO ===
            if (customModelData != null || !customEnchantments.isEmpty() || !customNBT.isEmpty()) {
                completeLore.add(Component.empty());
                completeLore.add(Component.text("━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                completeLore.add(Component.empty());

                // CustomModelData
                if (customModelData != null) {
                    completeLore.add(Component.text("Custom Model Data: ", NamedTextColor.LIGHT_PURPLE)
                            .append(Component.text(customModelData, NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                }

                // Enchantments
                if (!customEnchantments.isEmpty()) {
                    completeLore.add(Component.text("Enchantments:", NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.ITALIC, false));
                    for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                        String enchName = entry.getKey().getKey().getKey();
                        completeLore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }

                // NBT
                if (!customNBT.isEmpty()) {
                    completeLore.add(Component.text("NBT Data:", NamedTextColor.DARK_AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                    for (Map.Entry<String, String> entry : customNBT.entrySet()) {
                        completeLore.add(Component.text("  • " + entry.getKey() + ": " + entry.getValue(), NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
            }

            meta.lore(completeLore);

            // Apply CustomModelData to preview
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            // Apply enchantments to preview (with glowing effect)
            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            // Apply NBT data to preview
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
        meta.displayName(Component.text("Edit Crafted Name", NamedTextColor.GOLD)
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
            lore.add(Component.text("No crafted name set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Used on crafted items", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit in chat", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(11, button);
    }

    private void addLoreButton() {
        ItemStack button = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Crafted Description", NamedTextColor.AQUA)
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
            lore.add(Component.text("No crafted description set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Used on crafted items", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Left Click to add line", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Right Click to clear & restart", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(13, button);
    }

    private void addGuiNameButton() {
        ItemStack button = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit GUI Name", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (guiName != null && !guiName.isEmpty()) {
            lore.add(Component.text("Current:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(MessageUtil.colorize(guiName)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("No GUI name set", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Used in recipe browser", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(20, button);
    }

    private void addGuiDescriptionButton() {
        ItemStack button = new ItemStack(Material.MAP);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit GUI Description", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (!guiDescription.isEmpty()) {
            lore.add(Component.text("Current lines: " + guiDescription.size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            for (int i = 0; i < Math.min(3, guiDescription.size()); i++) {
                lore.add(MessageUtil.colorize(guiDescription.get(i))
                        .decoration(TextDecoration.ITALIC, false));
            }
            if (guiDescription.size() > 3) {
                lore.add(Component.text("... and " + (guiDescription.size() - 3) + " more", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("No GUI description", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("Used in recipe browser", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Left Click to add line", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Right Click to clear & restart", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(24, button);
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
        inventory.setItem(29, button);
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
            lore.add(Component.text("Current enchantments:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                String enchName = entry.getKey().getKey().getKey();
                lore.add(Component.text("• " + enchName + " " + entry.getValue(), NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("No enchantments", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to add", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(33, button);
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
        inventory.setItem(40, button);
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

        // Name button
        if (slot == 11) {
            currentMode = EditMode.NAME;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the item name in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        // Crafted Description button
        if (slot == 13) {
            if (event.getClick().isRightClick()) {
                // Right click - clear and restart
                customLore.clear();
                MessageUtil.sendSuccess(player, "Cleared crafted description");
                updateInventory();
                return;
            }

            // Left click - add line
            currentMode = EditMode.LORE;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type a description line in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>done</red> when finished, or <red>cancel</red> to cancel");
            return;
        }

        // GUI Name button (slot 20)
        if (slot == 20) {
            currentMode = EditMode.GUI_NAME;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the GUI name in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        // GUI Description button (slot 24)
        if (slot == 24) {
            if (event.getClick().isRightClick()) {
                // Right click - clear and restart
                guiDescription.clear();
                MessageUtil.sendSuccess(player, "Cleared GUI description");
                updateInventory();
                return;
            }

            // Left click - add line
            currentMode = EditMode.GUI_DESCRIPTION;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type a GUI description line in chat (supports MiniMessage colors)");
            MessageUtil.sendInfo(player, "Type <red>done</red> when finished, or <red>cancel</red> to cancel");
            return;
        }

        // Custom Model Data button
        if (slot == 15) {
            currentMode = EditMode.CUSTOM_MODEL_DATA;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the custom model data number in chat");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel or <red>clear</red> to remove");
            return;
        }

        // NBT button
        if (slot == 29) {
            currentMode = EditMode.NBT_KEY;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the NBT tag key in chat");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        // Enchantment button
        if (slot == 33) {
            currentMode = EditMode.ENCHANTMENT_NAME;
            waitingForInput.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendInfo(player, "Type the enchantment name (e.g., sharpness, protection)");
            MessageUtil.sendInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        // Clear button
        if (slot == 40) {
            customName = null;
            customLore.clear();
            customModelData = null;
            customNBT.clear();
            customEnchantments.clear();
            guiName = null;
            guiDescription.clear();
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
                Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(getEditedItem()));
            }
            return;
        }

        // Cancel button
        if (slot == 50) {
            player.closeInventory();
            if (onComplete != null) {
                // Return original item without changes
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

            // Apply enchantments
            for (Map.Entry<Enchantment, Integer> entry : customEnchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }

            // Apply NBT data
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
                MessageUtil.sendWarning(chatPlayer, "Cancelled");
                editor.open();
            });
            return;
        }

        // Handle different input modes
        switch (currentMode) {
            case NAME -> {
                editor.customName = message;
                waitingForInput.remove(chatPlayer.getUniqueId());
                currentMode = EditMode.NONE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendSuccess(chatPlayer, "Set item name");
                    editor.updateInventory();
                    editor.open();
                });
            }
            case LORE -> {
                if (message.equalsIgnoreCase("done")) {
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Finished editing description");
                        editor.updateInventory();
                        editor.open();
                    });
                } else {
                    editor.customLore.add(message);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Added description line");
                        MessageUtil.sendInfo(chatPlayer, "Type another line, or type <gold>done</gold> to finish");
                    });
                }
            }
            case GUI_NAME -> {
                editor.guiName = message;
                waitingForInput.remove(chatPlayer.getUniqueId());
                currentMode = EditMode.NONE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendSuccess(chatPlayer, "Set GUI name");
                    editor.updateInventory();
                    editor.open();
                });
            }
            case GUI_DESCRIPTION -> {
                if (message.equalsIgnoreCase("done")) {
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Finished editing GUI description");
                        editor.updateInventory();
                        editor.open();
                    });
                } else {
                    editor.guiDescription.add(message);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Added GUI description line");
                        MessageUtil.sendInfo(chatPlayer, "Type another line, or type <gold>done</gold> to finish");
                    });
                }
            }
            case CUSTOM_MODEL_DATA -> {
                if (message.equalsIgnoreCase("clear")) {
                    editor.customModelData = null;
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Cleared custom model data");
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
                            MessageUtil.sendSuccess(chatPlayer, "Set custom model data to " + modelData);
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
                    MessageUtil.sendInfo(chatPlayer, "Now type the value for NBT tag '" + message + "'");
                });
            }
            case NBT_VALUE -> {
                editor.customNBT.put(editor.tempNBTKey, message);
                waitingForInput.remove(chatPlayer.getUniqueId());
                currentMode = EditMode.NONE;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtil.sendSuccess(chatPlayer, "Added NBT: " + editor.tempNBTKey + " = " + message);
                    editor.updateInventory();
                    editor.open();
                });
            }
            case ENCHANTMENT_NAME -> {
                Enchantment enchant = Enchantment.getByName(message.toUpperCase());
                if (enchant == null) {
                    // Try by key
                    enchant = Enchantment.getByKey(NamespacedKey.minecraft(message.toLowerCase()));
                }

                if (enchant != null) {
                    editor.tempEnchantment = enchant;
                    currentMode = EditMode.ENCHANTMENT_LEVEL;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendInfo(chatPlayer, "Now type the enchantment level (1-255)");
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendError(chatPlayer, "Unknown enchantment! Try: sharpness, protection, efficiency, etc.");
                    });
                }
            }
            case ENCHANTMENT_LEVEL -> {
                try {
                    int level = Integer.parseInt(message);
                    if (level < 1 || level > 255) {
                        throw new NumberFormatException();
                    }
                    editor.customEnchantments.put(editor.tempEnchantment, level);
                    waitingForInput.remove(chatPlayer.getUniqueId());
                    currentMode = EditMode.NONE;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendSuccess(chatPlayer, "Added enchantment: " +
                                editor.tempEnchantment.getKey().getKey() + " level " + level);
                        editor.updateInventory();
                        editor.open();
                    });
                } catch (NumberFormatException e) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtil.sendError(chatPlayer, "Invalid level! Please enter a number between 1 and 255.");
                    });
                }
            }
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

    public Integer getCustomModelData() {
        return customModelData;
    }

    public Map<String, String> getCustomNBT() {
        return customNBT;
    }

    public ItemStack getEditedItem() {
        return item.clone();
    }

    public String getGuiName() {
        return guiName;
    }

    public List<String> getGuiDescription() {
        return guiDescription;
    }
}
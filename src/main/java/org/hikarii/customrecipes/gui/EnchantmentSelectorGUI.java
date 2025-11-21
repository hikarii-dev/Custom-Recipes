package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EnchantmentSelectorGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private final ItemStack targetItem;
    private final Map<Enchantment, Integer> selectedEnchantments;
    private final Consumer<Map<Enchantment, Integer>> onComplete;
    private final Consumer<Boolean> onHideToggle;
    private final Consumer<Void> onReturn;
    private boolean showAllEnchantments = false;
    private boolean hideEnchantments = false;
    private int page = 0;
    private static final int ENCHANTS_PER_PAGE = 45;

    public EnchantmentSelectorGUI(CustomRecipes plugin, Player player, ItemStack targetItem,
                                  Map<Enchantment, Integer> currentEnchantments,
                                  boolean hideEnchants,
                                  Consumer<Void> onReturn) {
        this.plugin = plugin;
        this.player = player;
        this.targetItem = targetItem;
        this.selectedEnchantments = new HashMap<>(currentEnchantments);
        this.hideEnchantments = hideEnchants;
        this.onReturn = onReturn;
        this.onComplete = null;
        this.onHideToggle = null;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("Select Enchantments", NamedTextColor.LIGHT_PURPLE)
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
        addEnchantmentBooks();
        addNavigationButtons();
        addFilterButton();
        addHideEnchantsButton();
        addBackButton();
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }
    }

    private List<Enchantment> getAvailableEnchantments() {
        if (showAllEnchantments) {
            return Arrays.stream(Enchantment.values())
                    .sorted(Comparator.comparing(e -> e.getKey().getKey()))
                    .collect(Collectors.toList());
        } else {
            return Arrays.stream(Enchantment.values())
                    .filter(e -> e.canEnchantItem(targetItem))
                    .sorted(Comparator.comparing(e -> e.getKey().getKey()))
                    .collect(Collectors.toList());
        }
    }

    private void addEnchantmentBooks() {
        List<Enchantment> enchantments = getAvailableEnchantments();
        int startIndex = page * ENCHANTS_PER_PAGE;
        int endIndex = Math.min(startIndex + ENCHANTS_PER_PAGE, enchantments.size());
        for (int i = startIndex; i < endIndex; i++) {
            Enchantment enchantment = enchantments.get(i);
            ItemStack book = createEnchantmentBook(enchantment);
            inventory.setItem(i - startIndex, book);
        }
    }

    private ItemStack createEnchantmentBook(Enchantment enchantment) {
        int currentLevel = selectedEnchantments.getOrDefault(enchantment, 0);
        Material bookType = currentLevel > 0 ? Material.KNOWLEDGE_BOOK : Material.BOOK;
        ItemStack book = new ItemStack(bookType);
        ItemMeta meta = book.getItemMeta();
        String enchantName = formatEnchantmentName(enchantment.getKey().getKey());
        NamedTextColor nameColor = currentLevel > 0 ? NamedTextColor.AQUA : NamedTextColor.GRAY;
        meta.displayName(Component.text(enchantName, nameColor)
                .decoration(TextDecoration.BOLD, currentLevel > 0)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Max Level: " + enchantment.getMaxLevel(), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Current: Level " + currentLevel,
                        currentLevel > 0 ? NamedTextColor.GREEN : NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        String description = getEnchantmentDescription(enchantment);
        if (description != null) {
            lore.add(Component.text(description, NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
        }

        if (!enchantment.canEnchantItem(targetItem)) {
            lore.add(Component.text("⚠ Not compatible with item", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
        }
        lore.add(Component.text("» Left Click: +1 Level", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Right Click: -1 Level", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Shift Click: Set Max/Reset", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        book.setItemMeta(meta);
        return book;
    }

    private String formatEnchantmentName(String key) {
        String[] parts = key.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private String getEnchantmentDescription(Enchantment enchantment) {
        return switch (enchantment.getKey().getKey()) {
            case "protection" -> "Reduces damage from all sources";
            case "fire_protection" -> "Reduces fire damage";
            case "feather_falling" -> "Reduces fall damage";
            case "blast_protection" -> "Reduces explosion damage";
            case "projectile_protection" -> "Reduces projectile damage";
            case "respiration" -> "Extends underwater breathing";
            case "aqua_affinity" -> "Faster mining underwater";
            case "thorns" -> "Damages attackers";
            case "depth_strider" -> "Faster underwater movement";
            case "frost_walker" -> "Freezes water beneath you";
            case "soul_speed" -> "Faster on soul blocks";
            case "swift_sneak" -> "Faster sneaking";
            case "sharpness" -> "Increases melee damage";
            case "smite" -> "Increases damage to undead";
            case "bane_of_arthropods" -> "Increases damage to arthropods";
            case "knockback" -> "Increases knockback";
            case "fire_aspect" -> "Sets target on fire";
            case "looting" -> "Increases mob loot";
            case "sweeping_edge" -> "Increases sweep attack damage";
            case "efficiency" -> "Faster mining";
            case "silk_touch" -> "Mines blocks intact";
            case "unbreaking" -> "Increases durability";
            case "fortune" -> "Increases block drops";
            case "power" -> "Increases arrow damage";
            case "punch" -> "Increases arrow knockback";
            case "flame" -> "Arrows set target on fire";
            case "infinity" -> "Infinite arrows (needs 1)";
            case "luck_of_the_sea" -> "Increases fishing luck";
            case "lure" -> "Faster fishing";
            case "loyalty" -> "Trident returns";
            case "impaling" -> "Increases trident damage";
            case "riptide" -> "Trident launches player";
            case "channeling" -> "Summons lightning";
            case "multishot" -> "Shoots 3 arrows";
            case "quick_charge" -> "Faster crossbow reload";
            case "piercing" -> "Arrows pierce entities";
            case "mending" -> "Repairs with XP";
            case "vanishing_curse" -> "Item vanishes on death";
            case "binding_curse" -> "Cannot remove armor";
            case "density" -> "Increases mace damage based on fall distance";
            case "breach" -> "Reduces enemy armor effectiveness";
            case "wind_burst" -> "Launches entities with wind burst";
            default -> null;
        };
    }

    private void addNavigationButtons() {
        List<Enchantment> enchantments = getAvailableEnchantments();
        int maxPages = (int) Math.ceil((double) enchantments.size() / ENCHANTS_PER_PAGE);
        if (maxPages > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.displayName(Component.text("◀ Previous Page",
                            page > 0 ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            prevMeta.lore(List.of(
                    Component.text("Page " + (page + 1) + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(48, prevButton);
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.displayName(Component.text("Next Page ▶",
                            page < maxPages - 1 ? NamedTextColor.YELLOW : NamedTextColor.DARK_GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            nextMeta.lore(List.of(
                    Component.text("Page " + (page + 1) + "/" + maxPages, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(50, nextButton);
        }
    }

    private void addFilterButton() {
        ItemStack button = new ItemStack(showAllEnchantments ? Material.ENDER_EYE : Material.ENDER_PEARL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text(showAllEnchantments ? "All Enchantments" : "Compatible Only",
                        NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (showAllEnchantments) {
            lore.add(Component.text("Showing all enchantments", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("including incompatible ones", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Showing only enchantments", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("compatible with this item", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(45, button);
    }

    private void addHideEnchantsButton() {
        ItemStack button = new ItemStack(hideEnchantments ? Material.ENDER_EYE : Material.ENDER_PEARL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Enchantment Visibility", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: " + (hideEnchantments ? "Hidden" : "Visible"),
                        hideEnchantments ? NamedTextColor.RED : NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("When hidden, enchantments", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("won't show on the item", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(46, button);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Item Editor", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Save and return", NamedTextColor.GRAY)
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

        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        if (slot == 45) {
            showAllEnchantments = !showAllEnchantments;
            page = 0;
            updateInventory();
            return;
        }

        if (slot == 46) {
            hideEnchantments = !hideEnchantments;
            updateInventory();
            return;
        }

        if (slot == 48) {
            if (page > 0) {
                page--;
                updateInventory();
            }
            return;
        }

        if (slot == 50) {
            List<Enchantment> enchantments = getAvailableEnchantments();
            int maxPages = (int) Math.ceil((double) enchantments.size() / ENCHANTS_PER_PAGE);
            if (page < maxPages - 1) {
                page++;
                updateInventory();
            }
            return;
        }

        if (slot == 53) {
            ItemEditorGUI editor = ItemEditorGUI.getLastEditor(player.getUniqueId());
            if (editor != null) {
                editor.customEnchantments.clear();
                editor.customEnchantments.putAll(selectedEnchantments);
                editor.hideEnchantments = hideEnchantments;
                editor.forceUpdatePreview();
            }
            player.closeInventory();

            if (onReturn != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onReturn.accept(null);
                });
            }
            return;
        }
        if (slot < 45) {
            List<Enchantment> enchantments = getAvailableEnchantments();
            int enchantIndex = (page * ENCHANTS_PER_PAGE) + slot;
            if (enchantIndex < enchantments.size()) {
                Enchantment enchantment = enchantments.get(enchantIndex);
                int currentLevel = selectedEnchantments.getOrDefault(enchantment, 0);
                int maxLevel = enchantment.getMaxLevel();
                if (clickType.isShiftClick()) {
                    if (currentLevel > 0) {
                        selectedEnchantments.remove(enchantment);
                    } else {
                        selectedEnchantments.put(enchantment, maxLevel);
                    }
                } else if (clickType.isLeftClick()) {
                    if (currentLevel < maxLevel) {
                        selectedEnchantments.put(enchantment, currentLevel + 1);
                    }
                } else if (clickType.isRightClick()) {
                    if (currentLevel > 0) {
                        if (currentLevel == 1) {
                            selectedEnchantments.remove(enchantment);
                        } else {
                            selectedEnchantments.put(enchantment, currentLevel - 1);
                        }
                    }
                }
                updateInventory();
            }
        }
    }

    public Map<Enchantment, Integer> getSelectedEnchantments() {
        return new HashMap<>(selectedEnchantments);
    }

    public boolean isHideEnchantments() {
        return hideEnchantments;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
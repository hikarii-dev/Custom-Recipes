package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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
 * Enhanced GUI for plugin settings
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

    public void open() {
        player.openInventory(inventory);
    }

    private void updateInventory() {
        inventory.clear();
        fillBorders();

        // Row 1: Basic settings (slots 10, 12, 14)
        addSpawnEggNameSetting();    // Slot 10
        addCraftedNamesSetting();    // Slot 12
        addIgnoreDataSetting();      // Slot 14

        // Row 3: World restrictions (slots 28, 37, 38, 39)
        addWorldRestrictionSetting(); // Slot 28
        addWorldSettingsButtons();    // Slots 37, 38, 39

        // Navigation
        addBackButton(); // Slot 53
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
        inventory.setItem(10, item);
    }

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
        inventory.setItem(12, item);
    }

    private void addIgnoreDataSetting() {
        boolean enabled = plugin.getConfig().getBoolean("ignore-metadata", false);

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "Enabled" : "Disabled";
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Ignore Metadata", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, color))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If enabled, recipe ingredients", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("will ignore NBT/metadata", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("(e.g., damaged items work)", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(14, item);
    }

    private void addWorldRestrictionSetting() {
        boolean enabled = plugin.getRecipeWorldManager().isGlobalWorldRestrictionEnabled();

        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "Enabled" : "Disabled";
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("World Restrictions", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, color))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If enabled, recipes can be", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("restricted per world", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        List<String> disabledWorlds = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
        if (enabled && !disabledWorlds.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Globally disabled in:", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            for (String world : disabledWorlds) {
                lore.add(Component.text("  • " + world, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(28, item);
    }

    private void addWorldSettingsButtons() {
        // Overworld button - slot 37
        addWorldButton(37, World.Environment.NORMAL, "Overworld", Material.GRASS_BLOCK);

        // Nether button - slot 38
        addWorldButton(38, World.Environment.NETHER, "Nether", Material.NETHERRACK);

        // End button - slot 39
        addWorldButton(39, World.Environment.THE_END, "The End", Material.END_STONE);
    }

    private void addWorldButton(int slot, World.Environment environment, String displayName, Material icon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        // Check if this world type is globally disabled
        List<String> globallyDisabled = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
        boolean isDisabled = false;

        // Find worlds of this type
        List<String> worldsOfType = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == environment) {
                worldsOfType.add(world.getName());
                if (globallyDisabled.contains(world.getName())) {
                    isDisabled = true;
                }
            }
        }

        NamedTextColor titleColor = isDisabled ? NamedTextColor.RED : NamedTextColor.GREEN;
        meta.displayName(Component.text(displayName + " Settings", titleColor)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(isDisabled ? "Recipes Disabled" : "Recipes Enabled",
                        isDisabled ? NamedTextColor.RED : NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false));

        if (!worldsOfType.isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Worlds:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            for (String worldName : worldsOfType) {
                boolean worldDisabled = globallyDisabled.contains(worldName);
                lore.add(Component.text("  • " + worldName,
                                worldDisabled ? NamedTextColor.RED : NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.text("» Affects all " + displayName.toLowerCase() + " worlds", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));

        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Recipe List", NamedTextColor.YELLOW)
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

        // Back button
        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        // Check permission
        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to change settings.");
            return;
        }

        // Spawn egg name toggle (slot 10)
        if (slot == 10) {
            boolean newValue = !plugin.isKeepSpawnEggNames();
            plugin.getConfig().set("spawn-egg-keep-custom-name", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();
            MessageUtil.sendSuccess(player, (newValue ? "Enabled" : "Disabled") + " spawn egg custom names");
            updateInventory();
            return;
        }

        // Crafted names toggle (slot 12)
        if (slot == 12) {
            boolean newValue = !plugin.isUseCraftedCustomNames();
            plugin.getConfig().set("use-crafted-custom-names", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();
            MessageUtil.sendSuccess(player, (newValue ? "Enabled" : "Disabled") + " crafted item custom names");
            updateInventory();
            return;
        }

        // Ignore metadata toggle (slot 14)
        if (slot == 14) {
            boolean newValue = !plugin.getConfig().getBoolean("ignore-metadata", false);
            plugin.getConfig().set("ignore-metadata", newValue);
            plugin.saveConfig();
            MessageUtil.sendSuccess(player, (newValue ? "Enabled" : "Disabled") + " ignore metadata");
            updateInventory();
            return;
        }

        // World restrictions toggle (slot 28)
        if (slot == 28) {
            boolean newValue = !plugin.getRecipeWorldManager().isGlobalWorldRestrictionEnabled();
            List<String> currentDisabled = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
            plugin.getRecipeWorldManager().setGlobalWorldRestrictions(newValue, currentDisabled);
            MessageUtil.sendSuccess(player, (newValue ? "Enabled" : "Disabled") + " world restrictions");
            updateInventory();
            return;
        }

        // World buttons (37, 38, 39)
        if (slot == 37 || slot == 38 || slot == 39) {
            World.Environment env = switch (slot) {
                case 37 -> World.Environment.NORMAL;
                case 38 -> World.Environment.NETHER;
                case 39 -> World.Environment.THE_END;
                default -> null;
            };

            if (env != null) {
                toggleWorldEnvironment(env);
            }
            return;
        }
    }

    private void toggleWorldEnvironment(World.Environment environment) {
        List<String> globallyDisabled = new ArrayList<>(plugin.getRecipeWorldManager().getGlobalDisabledWorlds());
        List<String> worldsOfType = new ArrayList<>();

        // Find all worlds of this type
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == environment) {
                worldsOfType.add(world.getName());
            }
        }

        if (worldsOfType.isEmpty()) {
            MessageUtil.sendWarning(player, "No worlds of this type found!");
            return;
        }

        // Check if any world of this type is disabled
        boolean anyDisabled = worldsOfType.stream().anyMatch(globallyDisabled::contains);

        if (anyDisabled) {
            // Enable all worlds of this type
            globallyDisabled.removeAll(worldsOfType);
            MessageUtil.sendSuccess(player, "Enabled recipes in all " +
                    environment.name().toLowerCase().replace("_", " ") + " worlds");
        } else {
            // Disable all worlds of this type
            globallyDisabled.addAll(worldsOfType);
            MessageUtil.sendWarning(player, "Disabled recipes in all " +
                    environment.name().toLowerCase().replace("_", " ") + " worlds");
        }

        plugin.getRecipeWorldManager().setGlobalWorldRestrictions(true, globallyDisabled);
        updateInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
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
                MessageUtil.createGradientMenuTitle("Settings")
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
        addSpawnEggNameSetting();
        addCraftedNamesSetting();
        addIgnoreDataSetting();
        addAdminNotificationsSetting();
        addDisableAllCustomButton();
        addDisableAllVanillaButton();
        addWorldRestrictionSetting();
        addWorldSettingsButtons();
        addEnableAllCustomButton();
        addEnableAllVanillaButton();
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

    private void addAdminNotificationsSetting() {
        boolean enabled = plugin.getConfig().getBoolean("admin-notifications", true);
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "Enabled" : "Disabled";
        NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Admin Notifications", NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(status, color))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If enabled, shows chat", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("notifications when you", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("perform admin actions", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(16, item);
    }

    private void addWorldRestrictionSetting() {
        boolean enabled = plugin.getRecipeWorldManager().isGlobalWorldRestrictionEnabled();
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
        boolean hasRestrictions = !disabledWorlds.isEmpty();
        Material material = hasRestrictions ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = hasRestrictions ? "Enabled" : "Disabled";
        NamedTextColor color = hasRestrictions ? NamedTextColor.GREEN : NamedTextColor.RED;

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
        if (hasRestrictions) {
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
        addWorldButton(37, World.Environment.NORMAL, "Overworld", Material.GRASS_BLOCK);
        addWorldButton(38, World.Environment.NETHER, "Nether", Material.NETHERRACK);
        addWorldButton(39, World.Environment.THE_END, "The End", Material.END_STONE);
    }

    private void addWorldButton(int slot, World.Environment environment, String displayName, Material icon) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        List<String> globallyDisabled = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
        boolean isDisabled = false;
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
        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to change settings.");
            return;
        }

        if (slot == 10) {
            boolean newValue = !plugin.isKeepSpawnEggNames();
            plugin.getConfig().set("spawn-egg-keep-custom-name", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();
            MessageUtil.sendAdminSuccess(player, (newValue ? "Enabled" : "Disabled") + " spawn egg custom names");
            updateInventory();
            return;
        }

        if (slot == 12) {
            boolean newValue = !plugin.isUseCraftedCustomNames();
            plugin.getConfig().set("use-crafted-custom-names", newValue);
            plugin.saveConfig();
            plugin.loadConfiguration();
            MessageUtil.sendAdminSuccess(player, (newValue ? "Enabled" : "Disabled") + " crafted item custom names");
            updateInventory();
            return;
        }

        if (slot == 14) {
            boolean newValue = !plugin.getConfig().getBoolean("ignore-metadata", false);
            plugin.getConfig().set("ignore-metadata", newValue);
            plugin.saveConfig();
            MessageUtil.sendAdminSuccess(player, (newValue ? "Enabled" : "Disabled") + " ignore metadata");
            updateInventory();
            return;
        }

        if (slot == 16) {
            boolean newValue = !plugin.getConfig().getBoolean("admin-notifications", true);
            plugin.getConfig().set("admin-notifications", newValue);
            plugin.saveConfig();
            MessageUtil.sendAdminSuccess(player, (newValue ? "Enabled" : "Disabled") + " admin notifications");
            updateInventory();
            return;
        }

        if (slot == 41) {
            for (org.hikarii.customrecipes.recipe.CustomRecipe recipe : plugin.getRecipeManager().getAllRecipes()) {
                if (plugin.getRecipeManager().isRecipeEnabled(recipe.getKey())) {
                    plugin.getRecipeManager().disableRecipe(recipe.getKey());
                    plugin.getConfigManager().removeEnabledRecipe(recipe.getKey());
                }
            }
            MessageUtil.sendAdminWarning(player, "Disabled all custom recipes");
            updateInventory();
            return;
        }

        if (slot == 32) {
            for (org.hikarii.customrecipes.recipe.CustomRecipe recipe : plugin.getRecipeManager().getAllRecipes()) {
                String key = recipe.getKey();
                if (!plugin.getRecipeManager().isRecipeEnabled(key)) {
                    plugin.getRecipeManager().registerSingleRecipe(recipe);
                    plugin.getConfigManager().addEnabledRecipe(key);
                }
            }
            MessageUtil.sendAdminSuccess(player, "Enabled all custom recipes");
            updateInventory();
            return;
        }

        if (slot == 34) {
            int enabled = 0;
            for (String recipeKey : plugin.getVanillaRecipeManager().getAllVanillaRecipes().keySet()) {
                if (plugin.getVanillaRecipeManager().isRecipeDisabled(recipeKey)) {
                    plugin.getVanillaRecipeManager().toggleRecipe(recipeKey);
                    enabled++;
                }
            }
            MessageUtil.sendAdminSuccess(player, "Enabled all " + enabled + " vanilla recipes");
            updateInventory();
            return;
        }

        if (slot == 43) {
            int disabled = 0;
            for (String recipeKey : plugin.getVanillaRecipeManager().getAllVanillaRecipes().keySet()) {
                if (!plugin.getVanillaRecipeManager().isRecipeDisabled(recipeKey)) {
                    plugin.getVanillaRecipeManager().toggleRecipe(recipeKey);
                    disabled++;
                }
            }
            MessageUtil.sendAdminWarning(player, "Disabled all " + disabled + " vanilla recipes");
            updateInventory();
            return;
        }

        if (slot == 28) {
            boolean newValue = !plugin.getRecipeWorldManager().isGlobalWorldRestrictionEnabled();
            List<String> currentDisabled = plugin.getRecipeWorldManager().getGlobalDisabledWorlds();
            plugin.getRecipeWorldManager().setGlobalWorldRestrictions(newValue, currentDisabled);
            MessageUtil.sendAdminSuccess(player, (newValue ? "Enabled" : "Disabled") + " world restrictions");
            updateInventory();
            return;
        }

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
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == environment) {
                worldsOfType.add(world.getName());
            }
        }

        if (worldsOfType.isEmpty()) {
            MessageUtil.sendAdminWarning(player, "No worlds of this type found!");
            return;
        }

        boolean anyDisabled = worldsOfType.stream().anyMatch(globallyDisabled::contains);
        if (anyDisabled) {
            globallyDisabled.removeAll(worldsOfType);
            MessageUtil.sendAdminSuccess(player, "Enabled recipes in all " +
                    environment.name().toLowerCase().replace("_", " ") + " worlds");
        } else {
            globallyDisabled.addAll(worldsOfType);
            MessageUtil.sendAdminWarning(player, "Disabled recipes in all " +
                    environment.name().toLowerCase().replace("_", " ") + " worlds");
        }
        plugin.getRecipeWorldManager().setGlobalWorldRestrictions(true, globallyDisabled);
        updateInventory();
    }

    private void addDisableAllCustomButton() {
        int disabledCount = (int) plugin.getRecipeManager().getAllRecipes().stream()
                .filter(recipe -> !plugin.getRecipeManager().isRecipeEnabled(recipe.getKey()))
                .count();
        int totalCount = plugin.getRecipeManager().getRecipeCount();
        boolean allDisabled = disabledCount == totalCount && totalCount > 0;

        ItemStack button = new ItemStack(allDisabled ? Material.RED_DYE : Material.BARRIER);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Disable All Custom Recipes", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Total Recipes: " + totalCount, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Currently Disabled: " + disabledCount, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Disable all custom recipes", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("from working on the server", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to disable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(41, button);
    }

    private void addEnableAllCustomButton() {
        int enabledCount = (int) plugin.getRecipeManager().getAllRecipes().stream()
                .filter(recipe -> plugin.getRecipeManager().isRecipeEnabled(recipe.getKey()))
                .count();
        int totalCount = plugin.getRecipeManager().getRecipeCount();

        ItemStack button = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Enable All Custom Recipes", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Total Recipes: " + totalCount, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Currently Enabled: " + enabledCount, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Enable all custom recipes", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("on the server", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to enable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(32, button);
    }

    private void addEnableAllVanillaButton() {
        int totalVanilla = plugin.getVanillaRecipeManager().getAllVanillaRecipes().size();
        int disabledVanilla = (int) plugin.getVanillaRecipeManager().getAllVanillaRecipes().keySet().stream()
                .filter(key -> plugin.getVanillaRecipeManager().isRecipeDisabled(key))
                .count();
        int enabledVanilla = totalVanilla - disabledVanilla;

        ItemStack button = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Enable All Vanilla Recipes", NamedTextColor.DARK_GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Total Recipes: " + totalVanilla, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Currently Enabled: " + enabledVanilla, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Enable all vanilla recipes", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("on the crafting table", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to enable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(34, button);
    }

    private void addDisableAllVanillaButton() {
        int totalVanilla = plugin.getVanillaRecipeManager().getAllVanillaRecipes().size();
        int disabledVanilla = (int) plugin.getVanillaRecipeManager().getAllVanillaRecipes().keySet().stream()
                .filter(key -> plugin.getVanillaRecipeManager().isRecipeDisabled(key))
                .count();
        boolean allDisabled = disabledVanilla == totalVanilla && totalVanilla > 0;

        ItemStack button = new ItemStack(allDisabled ? Material.RED_DYE : Material.BARRIER);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Disable All Vanilla Recipes", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Total Recipes: " + totalVanilla, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Currently Disabled: " + disabledVanilla, NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Disable all vanilla recipes", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("from the crafting table", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("⚠ Warning: This affects", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("all 801 vanilla recipes!", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to disable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(43, button);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
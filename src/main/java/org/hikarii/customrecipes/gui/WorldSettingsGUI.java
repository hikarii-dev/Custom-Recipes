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
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeWorldManager;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;

public class WorldSettingsGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final CustomRecipe recipe;
    private final Inventory inventory;
    private final List<World> worlds;

    public WorldSettingsGUI(CustomRecipes plugin, Player player, CustomRecipe recipe) {
        this.plugin = plugin;
        this.player = player;
        this.recipe = recipe;
        this.worlds = new ArrayList<>(Bukkit.getWorlds());
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("World Settings: " + recipe.getKey(), NamedTextColor.DARK_AQUA)
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
        addInfoItem();
        addWorldButtons();
        addEnableAllButton();
        addDisableAllButton();
        addBackButton();
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderPane);
            inventory.setItem(45 + i, borderPane);
        }

        for (int i = 1; i < 5; i++) {
            inventory.setItem(i * 9, borderPane);
            inventory.setItem((i * 9) + 8, borderPane);
        }
    }

    private void addInfoItem() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("World Restrictions", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Configure which worlds", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("this recipe works in", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Green = Recipe Enabled", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Red = Recipe Disabled", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Click a world to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(4, info);
    }

    private void addWorldButtons() {
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        int slot = 10;
        for (World world : worlds) {
            if (slot == 17 || slot == 26 || slot == 35) {
                slot += 2;
            }

            if (slot > 43) {
                break;
            }
            boolean isEnabled = !disabledWorlds.contains(world.getName());
            addWorldButton(slot, world, isEnabled);
            slot++;
        }
    }

    private void addWorldButton(int slot, World world, boolean enabled) {
        Material icon = switch (world.getEnvironment()) {
            case NORMAL -> Material.GRASS_BLOCK;
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.COMMAND_BLOCK;
        };
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        NamedTextColor nameColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        meta.displayName(Component.text(world.getName(), nameColor)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        String worldType = switch (world.getEnvironment()) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "The End";
            default -> "Custom";
        };
        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(worldType, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        int playerCount = world.getPlayers().size();
        lore.add(Component.text("Players: ", NamedTextColor.GRAY)
                .append(Component.text(playerCount + " online", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Recipe Status:", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(enabled ? "✓ Enabled" : "✗ Disabled",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to " + (enabled ? "disable" : "enable"), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        if (enabled) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private void addEnableAllButton() {
        ItemStack button = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Enable in All Worlds", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Allow this recipe to", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("work in all worlds", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to enable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(47, button);
    }

    private void addDisableAllButton() {
        ItemStack button = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Disable in All Worlds", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Prevent this recipe from", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("working in all worlds", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to disable all", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(51, button);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Recipe Editor", NamedTextColor.YELLOW)
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
        if (slot == 49) {
            new RecipeEditorGUI(plugin, player, recipe).open();
            return;
        }

        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to manage world settings.");
            return;
        }

        if (slot == 47) {
            plugin.getRecipeWorldManager().setRecipeWorldRestrictions(recipe.getKey(), new ArrayList<>());
            MessageUtil.sendAdminSuccess(player, "Enabled recipe in all worlds");
            updateInventory();
            return;
        }

        if (slot == 51) {
            List<String> allWorlds = worlds.stream().map(World::getName).toList();
            plugin.getRecipeWorldManager().setRecipeWorldRestrictions(recipe.getKey(), allWorlds);
            MessageUtil.sendAdminWarning(player, "Disabled recipe in all worlds");
            updateInventory();
            return;
        }

        if (slot >= 10 && slot <= 43 &&
                slot % 9 != 0 && slot % 9 != 8) {
            int index = calculateWorldIndex(slot);
            if (index < 0 || index >= worlds.size()) {
                return;
            }

            World world = worlds.get(index);
            plugin.getRecipeWorldManager().toggleWorldRestriction(recipe.getKey(), world.getName());
            List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
            boolean nowEnabled = !disabledWorlds.contains(world.getName());

            if (nowEnabled) {
                MessageUtil.sendAdminSuccess(player, "Enabled recipe in world: " + world.getName());
            } else {
                MessageUtil.sendAdminWarning(player, "Disabled recipe in world: " + world.getName());
            }
            updateInventory();
        }
    }

    private int calculateWorldIndex(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        if (row < 1 || row > 4 || col < 1 || col > 7) {
            return -1;
        }
        int baseIndex = (row - 1) * 7;
        int index = baseIndex + (col - 1);
        return index;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
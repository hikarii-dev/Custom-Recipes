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
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.ArrayList;
import java.util.List;

public class RecipeViewerGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final CustomRecipe recipe;
    private final PlayerRecipeListGUI parentGUI;
    private final Inventory inventory;

    private static final int RESULT_SLOT = 25;
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int BACK_BUTTON_SLOT = 53;
    private static final int INFO_SLOT = 44;

    public RecipeViewerGUI(CustomRecipes plugin, Player player, CustomRecipe recipe, PlayerRecipeListGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.recipe = recipe;
        this.parentGUI = parentGUI;

        String recipeName = recipe.getName() != null && !recipe.getName().isEmpty()
                ? recipe.getName()
                : MessageUtil.formatMaterialName(recipe.getResultMaterial().name());

        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Recipe: " + recipeName)
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
        addCraftingGrid();
        addEqualsSign();
        addResultItem();
        addInfoBook();
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

        for (int slot : GRID_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(RESULT_SLOT, null);
    }

    private void addCraftingGrid() {
        if (recipe.getType() == RecipeType.SHAPED || recipe.getType() == RecipeType.SHAPELESS) {
            List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();
            for (int i = 0; i < Math.min(GRID_SLOTS.length, ingredients.size()); i++) {
                RecipeIngredient ingredient = ingredients.get(i);
                if (ingredient.material() != Material.AIR) {
                    ItemStack displayItem;
                    if (ingredient.hasExactItem()) {
                        displayItem = ingredient.getExactItem().clone();
                    } else {
                        displayItem = new ItemStack(ingredient.material(), ingredient.amount());
                    }
                    ItemMeta meta = displayItem.getItemMeta();
                    if (meta != null) {
                        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                        lore.add(Component.empty());
                        lore.add(Component.text("Amount: " + ingredient.amount(), NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false));
                        if (ingredient.hasExactItem()) {
                            lore.add(Component.text("⚠ Exact item required", NamedTextColor.YELLOW)
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(lore);
                        displayItem.setItemMeta(meta);
                    }
                    inventory.setItem(GRID_SLOTS[i], displayItem);
                }
            }
        }
    }

    private void addEqualsSign() {
        ItemStack equals = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = equals.getItemMeta();
        meta.displayName(Component.text("=", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );
        equals.setItemMeta(meta);
        inventory.setItem(23, equals);
    }

    private void addResultItem() {
        ItemStack resultDisplay = recipe.getResultItem().clone();
        ItemMeta resultMeta = resultDisplay.getItemMeta();
        if (resultMeta != null) {
            List<Component> lore = resultMeta.hasLore() ? new ArrayList<>(resultMeta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("✔ Recipe Result", NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            resultMeta.lore(lore);
            resultDisplay.setItemMeta(resultMeta);
        }
        inventory.setItem(RESULT_SLOT, resultDisplay);
    }

    private void addInfoBook() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (recipe.getName() != null && !recipe.getName().isEmpty()) {
            lore.add(Component.text("Name: ", NamedTextColor.GRAY)
                    .append(MessageUtil.colorize(recipe.getName()))
                    .decoration(TextDecoration.ITALIC, false));
        }

        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().toString(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            lore.add(Component.empty());
            lore.add(Component.text("Description:", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            for (String line : recipe.getDescription()) {
                lore.add(Component.text("  " + line, NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(INFO_SLOT, info);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Recipe List", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(meta);
        inventory.setItem(BACK_BUTTON_SLOT, back);
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

        int slot = event.getSlot();
        if (slot == BACK_BUTTON_SLOT) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
            parentGUI.reopen();
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

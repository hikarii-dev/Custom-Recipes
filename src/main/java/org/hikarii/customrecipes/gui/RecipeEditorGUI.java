package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.enchantments.Enchantment;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.RecipeWorldManager;
import org.hikarii.customrecipes.recipe.data.RecipeIngredient;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import org.hikarii.customrecipes.util.MessageUtil;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class RecipeEditorGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final CustomRecipe recipe;
    private final Inventory inventory;
    private boolean deleteConfirmation = false;
    private static final int EDIT_RECIPE_BUTTON = 46;
    private static final int EDIT_RESULT_SLOT = 25;
    private static final int GIVE_ITEM_BUTTON = 52;
    private boolean editMode = false;
    private ItemStack[] editGridItems = new ItemStack[9];
    private ItemStack editResultItem = null;

    private static final int[] GRID_SLOTS = {
            10, 11, 12,
            19, 20, 21,
            28, 29, 30
    };

    public RecipeEditorGUI(CustomRecipes plugin, Player player, CustomRecipe recipe) {
        this.plugin = plugin;
        this.player = player;
        this.recipe = recipe;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Recipe Editor")
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

        if (editMode) {
            addEditableGrid();
            addEqualsSign();
            addEditableResultItem();
            addSaveEditButton();
            addCancelEditButton();
        } else {
            addCraftingGrid();
            addEqualsSign();
            addResultItem();
            addInfoBook();
            addHiddenToggleButton();
            addToggleButton();
            addWorldSettingsButton();
            addEditRecipeButton();
            addEditItemButton();
            addDeleteButton();
            addBackButton();
            addGiveItemButton();
        }
    }

    private void addEditableResultItem() {
        if (editResultItem != null && editResultItem.getType() != Material.AIR) {
            ItemStack display = editResultItem.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() && meta.lore() != null ?
                        new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Amount: " + display.getAmount(), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("» Left Click to add", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, true));
                lore.add(Component.text("» Right Click to remove", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, true));
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(EDIT_RESULT_SLOT, display);
        } else {
            inventory.setItem(EDIT_RESULT_SLOT, null);
        }
    }

    private void addEditableGrid() {
        for (int i = 0; i < 9; i++) {
            if (editGridItems[i] != null && editGridItems[i].getType() != Material.AIR) {
                ItemStack display = editGridItems[i].clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    List<Component> originalLore = meta.hasLore() && meta.lore() != null ?
                            new ArrayList<>(meta.lore()) : new ArrayList<>();
                    List<Component> displayLore = new ArrayList<>(originalLore);
                    displayLore.add(Component.empty());
                    displayLore.add(Component.text("Amount: " + display.getAmount(), NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false));

                    if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                        if (damageable.hasDamage()) {
                            int maxDurability = display.getType().getMaxDurability();
                            int currentDurability = maxDurability - damageable.getDamage();
                            displayLore.add(Component.text("Durability: " + currentDurability + "/" + maxDurability,
                                            NamedTextColor.AQUA)
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                    }
                    displayLore.add(Component.empty());
                    displayLore.add(Component.text("» Left Click to add", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, true));
                    displayLore.add(Component.text("» Right Click to remove", NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, true));
                    meta.lore(displayLore);
                    display.setItemMeta(meta);
                }
                inventory.setItem(GRID_SLOTS[i], display);
            } else {
                inventory.setItem(GRID_SLOTS[i], null);
            }
        }
    }

    private void addEditRecipeButton() {
        ItemStack button = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Recipe Pattern", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Modify the crafting", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("pattern of this recipe", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(EDIT_RECIPE_BUTTON, button);
    }

    private void addSaveEditButton() {
        ItemStack button = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Save Recipe Changes", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Apply changes to recipe", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to save", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(45, button);
    }

    private void addCancelEditButton() {
        ItemStack button = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Cancel Editing", NamedTextColor.RED)
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
        inventory.setItem(46, button);
    }

    private void initializeEditGrid() {
        Arrays.fill(editGridItems, null);
        editResultItem = recipe.getResultItem().clone();
        if (recipe.getType() == RecipeType.SHAPED) {
            List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();
            for (int i = 0; i < Math.min(ingredients.size(), 9); i++) {
                RecipeIngredient ingredient = ingredients.get(i);
                if (ingredient.material() != Material.AIR) {
                    if (ingredient.hasExactItem()) {
                        editGridItems[i] = ingredient.getExactItem().clone();
                    } else {
                        editGridItems[i] = new ItemStack(ingredient.material(), ingredient.amount());
                    }
                }
            }
        } else if (recipe.getType() == RecipeType.SHAPELESS) {
            Map<Material, Integer> ingredients = recipe.getShapelessData().ingredients();
            int index = 0;
            for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
                if (index >= 9) break;
                editGridItems[index++] = new ItemStack(entry.getKey(), entry.getValue());
            }
        }
    }

    private void saveEditedRecipe() {
        if (editResultItem == null || editResultItem.getType() == Material.AIR) {
            MessageUtil.sendError(player, "Cannot save recipe without a result item!");
            return;
        }

        try {
            Map<String, Object> recipeData = new HashMap<>();
            recipeData.put("name", recipe.getName());
            recipeData.put("description", recipe.getDescription());
            recipeData.put("type", recipe.getType().name());
            recipeData.put("hidden", recipe.isHidden());
            if (recipe.getType() == RecipeType.SHAPED) {
                List<String> pattern = new ArrayList<>();
                List<ItemStack> exactItems = new ArrayList<>();
                for (int row = 0; row < 3; row++) {
                    StringBuilder rowBuilder = new StringBuilder();
                    for (int col = 0; col < 3; col++) {
                        int index = row * 3 + col;
                        ItemStack item = editGridItems[index];
                        if (item == null || item.getType() == Material.AIR) {
                            rowBuilder.append("AIR ");
                            exactItems.add(null);
                        } else {
                            rowBuilder.append(item.getType().name());
                            if (item.getAmount() > 1) {
                                rowBuilder.append(":").append(item.getAmount());
                            }
                            rowBuilder.append(" ");
                            boolean shouldSaveExact = false;
                            if (item.hasItemMeta()) {
                                ItemMeta meta = item.getItemMeta();
                                if (item.getType() == Material.ENCHANTED_BOOK) {
                                    if (meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                                        org.bukkit.inventory.meta.EnchantmentStorageMeta bookMeta =
                                                (org.bukkit.inventory.meta.EnchantmentStorageMeta) meta;
                                        if (bookMeta.hasStoredEnchants()) {
                                            shouldSaveExact = true;
                                        }
                                    }
                                }
                                if (meta.hasEnchants()) {
                                    shouldSaveExact = true;
                                }
                                if (!meta.getPersistentDataContainer().getKeys().isEmpty()) {
                                    shouldSaveExact = true;
                                }
                                if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                                    org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                                    if (damageable.hasDamage() && damageable.getDamage() > 0) {
                                        shouldSaveExact = true;
                                    }
                                }
                            }
                            if (shouldSaveExact) {
                                exactItems.add(item.clone());
                            } else {
                                exactItems.add(null);
                            }
                        }
                    }
                    pattern.add(rowBuilder.toString().trim());
                }
                recipeData.put("recipe", pattern);
                List<String> exactItemsData = new ArrayList<>();
                for (ItemStack exactItem : exactItems) {
                    if (exactItem != null) {
                        exactItemsData.add(ItemStackSerializer.toBase64(exactItem));
                    } else {
                        exactItemsData.add(null);
                    }
                }
                if (exactItemsData.stream().anyMatch(Objects::nonNull)) {
                    recipeData.put("exact-ingredients", exactItemsData);
                }
            } else {
                List<String> ingredients = new ArrayList<>();
                Map<Material, Integer> counts = new HashMap<>();
                for (ItemStack item : editGridItems) {
                    if (item != null && item.getType() != Material.AIR) {
                        counts.merge(item.getType(), item.getAmount(), Integer::sum);
                    }
                }
                for (Map.Entry<Material, Integer> entry : counts.entrySet()) {
                    ingredients.add(entry.getKey().name() + ":" + entry.getValue());
                }
                recipeData.put("ingredients", ingredients);
            }

            Map<String, Object> resultData = ItemStackSerializer.toMap(editResultItem);
            recipeData.put("result", resultData);
            recipeData.put("material", editResultItem.getType().name());
            recipeData.put("amount", editResultItem.getAmount());
            if (editResultItem.hasItemMeta()) {
                ItemMeta resultMeta = editResultItem.getItemMeta();
                if (resultMeta.hasDisplayName() || resultMeta.hasLore() || resultMeta.hasEnchants() ||
                        !resultMeta.getPersistentDataContainer().getKeys().isEmpty()) {
                    recipeData.put("result-full", ItemStackSerializer.toMap(editResultItem));
                }
            }

            plugin.getConfigManager().getRecipeFileManager().saveRecipe(recipe.getKey(), recipeData);
            plugin.loadConfiguration();
            MessageUtil.sendAdminSuccess(player, "Recipe pattern updated successfully!");
            editMode = false;
            editResultItem = null;
            CustomRecipe updatedRecipe = plugin.getRecipeManager().getRecipe(recipe.getKey());
            if (updatedRecipe != null) {
                new RecipeEditorGUI(plugin, player, updatedRecipe).open();
            }
        } catch (Exception e) {
            MessageUtil.sendError(player, "Failed to save recipe: " + e.getMessage());
            plugin.getLogger().severe("Failed to save edited recipe: " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = borderPane.getItemMeta();
        meta.displayName(Component.empty());
        borderPane.setItemMeta(meta);
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, borderPane);
        }

        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int slot : gridSlots) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(25, null);
    }

    private void addEqualsSign() {
        ItemStack equals = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = equals.getItemMeta();
        meta.displayName(Component.text("=", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Place ingredients in the", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("3x3 grid on the left", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Place result item", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("on the right →", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        equals.setItemMeta(meta);
        inventory.setItem(23, equals);
    }

    private void addCraftingGrid() {
        if (recipe.getType() == RecipeType.SHAPED) {
            addShapedCraftingGrid();
        } else if (recipe.getType() == RecipeType.SHAPELESS) {
            addShapelessCraftingGrid();
        }
    }

    private void addShapedCraftingGrid() {
        List<RecipeIngredient> ingredients = recipe.getRecipeData().ingredients();
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < ingredients.size() && i < gridSlots.length; i++) {
            RecipeIngredient ingredient = ingredients.get(i);
            if (ingredient.material() == Material.AIR) {
                continue;
            }

            ItemStack item;
            if (ingredient.hasExactItem()) {
                item = ingredient.getExactItem().clone();
            } else {
                item = new ItemStack(ingredient.material(), ingredient.amount());
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof org.bukkit.inventory.meta.Damageable) {
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                if (damageable.hasDamage()) {
                    int maxDurability = item.getType().getMaxDurability();
                    int currentDurability = maxDurability - damageable.getDamage();
                    List<Component> lore = meta.hasLore() && meta.lore() != null ?
                            new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("Durability: " + currentDurability + "/" + maxDurability,
                                    NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
            }
            inventory.setItem(gridSlots[i], item);
        }
    }

    private void addShapelessCraftingGrid() {
        Map<Material, Integer> ingredients = recipe.getShapelessData().ingredients();
        int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        int slotIndex = 0;
        for (Map.Entry<Material, Integer> entry : ingredients.entrySet()) {
            Material mat = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count && slotIndex < gridSlots.length; i++) {
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text(
                        MessageUtil.formatMaterialName(mat.name()),
                        NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Shapeless ingredient", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position doesn't matter", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
                item.setItemMeta(meta);
                inventory.setItem(gridSlots[slotIndex], item);
                slotIndex++;
            }
        }
    }

    private void addResultItem() {
        ItemStack result = recipe.getResultItem().clone();
        inventory.setItem(25, result);
    }

    private void addInfoBook() {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Recipe Information", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Key: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getKey(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().toString(), NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Result: ", NamedTextColor.GRAY)
                .append(Component.text(
                        MessageUtil.formatMaterialName(recipe.getResultMaterial().name()),
                        NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getResultAmount() + "x", NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        if (recipe.getName() != null && !recipe.getName().isEmpty()) {
            lore.add(Component.text("Name:", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(MessageUtil.colorize("  " + recipe.getName())
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
        }

        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            lore.add(Component.text("Description:", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            for (String line : recipe.getDescription()) {
                lore.add(MessageUtil.colorize("  " + line)
                        .decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.empty());
        }

        ItemMeta resultMeta = recipe.getResultItem().getItemMeta();
        if (resultMeta != null && resultMeta.hasCustomModelData()) {
            lore.add(Component.text("Custom Model Data: ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text(resultMeta.getCustomModelData(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
        }

        if (resultMeta != null) {
            PersistentDataContainer container = resultMeta.getPersistentDataContainer();
            if (!container.getKeys().isEmpty()) {
                lore.add(Component.text("NBT Data:", NamedTextColor.DARK_AQUA)
                        .decoration(TextDecoration.ITALIC, false));
                for (NamespacedKey key : container.getKeys()) {
                    if (key.getNamespace().equals(plugin.getName().toLowerCase())) {
                        String value = container.get(key, PersistentDataType.STRING);
                        lore.add(Component.text("  • " + key.getKey() + ": " + value, NamedTextColor.AQUA)
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
                lore.add(Component.empty());
            }
        }
        if (resultMeta != null && resultMeta.hasEnchants()) {
            lore.add(Component.text("Enchantments:", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            for (Map.Entry<Enchantment, Integer> entry : resultMeta.getEnchants().entrySet()) {
                String enchName = entry.getKey().getKey().getKey();
                lore.add(Component.text("  • " + enchName + " " + entry.getValue(), NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.empty());
        }
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        if (!disabledWorlds.isEmpty()) {
            lore.add(Component.text("World Restrictions:", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            for (String world : disabledWorlds) {
                lore.add(Component.text("  • " + world, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(44, info);
    }

    private void addToggleButton() {
        boolean enabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack toggleButton = new ItemStack(material);
        ItemMeta meta = toggleButton.getItemMeta();
        meta.displayName(Component.text(
                enabled ? "Enabled" : "Disabled",
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(
                        enabled ? "Active" : "Inactive",
                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text(
                enabled ? "Players can use this recipe" : "Recipe is disabled",
                NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to " + (enabled ? "disable" : "enable"), NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        toggleButton.setItemMeta(meta);
        inventory.setItem(48, toggleButton);
    }

    private void addWorldSettingsButton() {
        ItemStack button = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("World Settings", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        List<String> disabledWorlds = plugin.getRecipeWorldManager().getDisabledWorlds(recipe.getKey());
        if (disabledWorlds.isEmpty()) {
            lore.add(Component.text("Enabled in all worlds", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Disabled in:", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            for (String world : disabledWorlds) {
                lore.add(Component.text("  • " + world, NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        lore.add(Component.empty());
        lore.add(Component.text("Configure which worlds", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("this recipe works in", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to configure", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(49, button);
    }

    private void addEditItemButton() {
        ItemStack button = new ItemStack(Material.ANVIL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Result Item", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Customize the result", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("item's properties:", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Name & Lore", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Custom Model Data", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• NBT Tags", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("• Enchantments", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(47, button);
    }

    private void addDeleteButton() {
        ItemStack deleteButton;
        ItemMeta meta;
        if (deleteConfirmation) {
            deleteButton = new ItemStack(Material.LIME_WOOL);
            meta = deleteButton.getItemMeta();
            meta.displayName(Component.text("Confirm Deletion", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("⚠ ARE YOU SURE? ⚠", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("This will permanently delete:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Recipe from memory", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• Recipe files (.yml, .json)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("• All recipe data", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("» Click again to DELETE", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("» Click elsewhere to cancel", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, true));
            meta.lore(lore);
            deleteButton.setItemMeta(meta);
        } else {
            deleteButton = new ItemStack(Material.BARRIER);
            meta = deleteButton.getItemMeta();
            meta.displayName(Component.text("Delete Recipe", NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("⚠ Warning!", NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("This will permanently delete", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("this recipe and its files.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("» Click to confirm", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, true));
            meta.lore(lore);
            deleteButton.setItemMeta(meta);
        }
        inventory.setItem(50, deleteButton);
    }

    private void addHiddenToggleButton() {
        boolean hidden = recipe.isHidden();
        Material material = hidden ? Material.ENDER_EYE : Material.ENDER_PEARL;
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text(hidden ? "Hidden Recipe" : "Visible Recipe",
                        hidden ? NamedTextColor.DARK_PURPLE : NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                .append(Component.text(hidden ? "Hidden" : "Visible",
                        hidden ? NamedTextColor.DARK_PURPLE : NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("If hidden, players must craft", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("this recipe once to unlock it.", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(45, button);
    }

    private void addBackButton() {
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.displayName(Component.text("« Back to Main Menu", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(meta);
        inventory.setItem(53, back);
    }

    private void addGiveItemButton() {
        ItemStack button = new ItemStack(Material.CHEST);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Get Result Item", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Receive this recipe's", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("result item with all", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("configured properties", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to receive", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(GIVE_ITEM_BUTTON, button);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player dragPlayer) || !dragPlayer.equals(player)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < inventory.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player clicker)) {
            return;
        }

        if (!clicker.equals(player)) {
            return;
        }

        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        if (editMode) {
            if (slot == EDIT_RESULT_SLOT) {
                if (event.getClickedInventory() != inventory) {
                    return;
                }
                event.setCancelled(true);
                if (clickType.isLeftClick()) {
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        if (editResultItem == null || editResultItem.getType() == Material.AIR) {
                            editResultItem = cursor.clone();
                            editResultItem.setAmount(1);
                        } else if (editResultItem.getType() == cursor.getType()) {
                            int newAmount = Math.min(editResultItem.getAmount() + 1, cursor.getType().getMaxStackSize());
                            editResultItem.setAmount(newAmount);
                        } else {
                            editResultItem = cursor.clone();
                            editResultItem.setAmount(1);
                        }
                    } else if (editResultItem != null && editResultItem.getType() != Material.AIR) {
                        int newAmount = Math.min(editResultItem.getAmount() + 1, editResultItem.getType().getMaxStackSize());
                        editResultItem.setAmount(newAmount);
                    }
                    updateInventory();
                } else if (clickType.isRightClick()) {
                    if (editResultItem != null && editResultItem.getType() != Material.AIR) {
                        if (editResultItem.getAmount() > 1) {
                            editResultItem.setAmount(editResultItem.getAmount() - 1);
                        } else {
                            editResultItem = null;
                        }
                        updateInventory();
                    }
                }
                return;
            }
            boolean isGridSlot = false;
            int gridIndex = -1;
            for (int i = 0; i < GRID_SLOTS.length; i++) {
                if (slot == GRID_SLOTS[i]) {
                    isGridSlot = true;
                    gridIndex = i;
                    break;
                }
            }
            if (isGridSlot) {
                if (event.getClickedInventory() != inventory) {
                    return;
                }
                event.setCancelled(true);
                if (clickType.isLeftClick()) {
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        ItemStack existing = editGridItems[gridIndex];
                        int maxStack = cursor.getType().getMaxStackSize();

                        if (existing == null || existing.getType() == Material.AIR) {
                            editGridItems[gridIndex] = cursor.clone();
                            editGridItems[gridIndex].setAmount(1);
                        } else if (existing.isSimilar(cursor)) {
                            existing.setAmount(Math.min(existing.getAmount() + 1, maxStack));
                        } else {
                            editGridItems[gridIndex] = cursor.clone();
                            editGridItems[gridIndex].setAmount(1);
                        }
                    } else {
                        ItemStack existing = editGridItems[gridIndex];
                        if (existing != null && existing.getType() != Material.AIR) {
                            int maxStack = existing.getType().getMaxStackSize();
                            existing.setAmount(Math.min(existing.getAmount() + 1, maxStack));
                        }
                    }
                    updateInventory();
                } else if (clickType.isRightClick()) {
                    ItemStack existing = editGridItems[gridIndex];
                    if (existing != null && existing.getType() != Material.AIR) {
                        if (existing.getAmount() > 1) {
                            existing.setAmount(existing.getAmount() - 1);
                        } else {
                            editGridItems[gridIndex] = null;
                        }
                        updateInventory();
                    }
                }
                return;
            }
            if (event.getClickedInventory() == inventory) {
                event.setCancelled(true);
                if (slot == 45) {
                    saveEditedRecipe();
                    return;
                }
                if (slot == 46) {
                    editMode = false;
                    editResultItem = null;
                    Arrays.fill(editGridItems, null);
                    updateInventory();
                    return;
                }
            }
            return;
        }

        if (event.getClickedInventory() == inventory) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() != inventory) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (slot != 50 && deleteConfirmation) {
            deleteConfirmation = false;
            updateInventory();
        }

        if (!player.hasPermission("customrecipes.manage")) {
            MessageUtil.sendError(player, "You don't have permission to manage recipes.");
            return;
        }

        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (slot == EDIT_RECIPE_BUTTON) {
            editMode = true;
            initializeEditGrid();
            updateInventory();
            return;
        }

        if (slot == 48) {
            boolean currentlyEnabled = plugin.getRecipeManager().isRecipeEnabled(recipe.getKey());
            if (currentlyEnabled) {
                plugin.getRecipeManager().disableRecipe(recipe.getKey());
                plugin.getRecipeStateTracker().markRecipeDisabled(recipe.getKey());
                MessageUtil.sendAdminWarning(player, "Disabled recipe: <white>" + recipe.getKey());
            } else {
                plugin.getRecipeManager().enableRecipe(recipe.getKey());
                plugin.getRecipeStateTracker().markRecipeEnabled(recipe.getKey());
                MessageUtil.sendAdminSuccess(player, "Enabled recipe: <white>" + recipe.getKey());
            }
            updateInventory();
            return;
        }

        if (slot == 49) {
            new WorldSettingsGUI(plugin, player, recipe).open();
            return;
        }

        if (slot == 47) {
            ItemStack resultItem = recipe.getResultItem();
            new ItemEditorGUI(plugin, player, resultItem, (editedItem) -> {
                if (editedItem != null) {
                    ItemEditorGUI editor = ItemEditorGUI.getLastEditor(player.getUniqueId());
                    String newName = editor != null ? editor.getCustomName() : null;
                    List<String> newDesc = editor != null ? editor.getCustomLore() : null;
                    plugin.getRecipeManager().updateRecipeResult(recipe.getKey(), editedItem,
                            newName, newDesc);
                    MessageUtil.sendAdminSuccess(player, "Updated result item for recipe: " + recipe.getKey());
                }
                CustomRecipe updatedRecipe = plugin.getRecipeManager().getRecipe(recipe.getKey());
                if (updatedRecipe != null) {
                    new RecipeEditorGUI(plugin, player, updatedRecipe).open();
                }
            }).open();
            return;
        }

        if (slot == 50) {
            if (!deleteConfirmation) {
                deleteConfirmation = true;
                updateInventory();
                MessageUtil.sendAdminWarning(player, "Click again to confirm deletion!");
                return;
            }
            String recipeKey = recipe.getKey();
            if (plugin.getRecipeManager().deleteRecipePermanently(recipeKey)) {
                plugin.getConfigManager().removeEnabledRecipe(recipeKey);
                MessageUtil.sendAdminSuccess(player, "Permanently deleted recipe: <white>" + recipeKey);
                player.closeInventory();
                new RecipeListGUI(plugin, player).open();
            } else {
                MessageUtil.sendError(player, "Failed to delete recipe.");
                deleteConfirmation = false;
                updateInventory();
            }
            return;
        }

        if (slot == 45) {
            boolean newValue = !recipe.isHidden();
            String recipeKey = recipe.getKey();
            try {
                File recipeFile = new File(plugin.getConfigManager().getRecipeFileManager().getRecipesFolder(), recipeKey + ".yml");
                if (recipeFile.exists()) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(recipeFile);
                    config.set("hidden", newValue);
                    config.save(recipeFile);

                    plugin.getRecipeManager().unregisterAll();
                    plugin.getConfigManager().loadRecipes();
                    plugin.getRecipeManager().registerAllRecipes();

                    MessageUtil.sendAdminSuccess(player,
                            "Recipe is now " + (newValue ? "<dark_purple>hidden" : "<aqua>visible"));
                    CustomRecipe updatedRecipe = plugin.getRecipeManager().getRecipe(recipeKey);
                    if (updatedRecipe != null) {
                        new RecipeEditorGUI(plugin, player, updatedRecipe).open();
                    } else {
                        new RecipeListGUI(plugin, player).open();
                    }
                } else {
                    MessageUtil.sendError(player, "Recipe file not found.");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to toggle hidden state: " + e.getMessage());
                MessageUtil.sendError(player, "Failed to update recipe.");
            }
            return;
        }

        if (slot == 53) {
            new RecipeListGUI(plugin, player).open();
            return;
        }

        if (slot == GIVE_ITEM_BUTTON) {
            ItemStack resultItem = recipe.getResultItem().clone();
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(resultItem);
                MessageUtil.sendAdminSuccess(player, "Received result item: " + recipe.getKey());
                try {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                } catch (Exception ignored) {}
            } else {
                MessageUtil.sendError(player, "Your inventory is full!");
            }
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
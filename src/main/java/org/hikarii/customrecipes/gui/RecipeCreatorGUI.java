package org.hikarii.customrecipes.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.config.JsonRecipeFileManager;
import org.hikarii.customrecipes.config.ValidationException;
import org.hikarii.customrecipes.recipe.CustomRecipe;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.data.ShapedRecipeData;
import org.hikarii.customrecipes.recipe.data.ShapelessRecipeData;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.function.Consumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeCreatorGUI implements Listener {
    private final CustomRecipes plugin;
    private final Player player;
    private final Inventory inventory;
    private RecipeType currentType = RecipeType.SHAPED;
    private ItemStack[] savedGrid = new ItemStack[9];
    private ItemStack[] gridItems = new ItemStack[9];
    private ItemEditorGUI currentEditor = null;
    private ItemStack resultItem = null; 

    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 25;
    private static final int TYPE_TOGGLE = 45;
    private static final int CREATE_BUTTON = 48;
    private static final int CANCEL_BUTTON = 50;
    private static final int EDIT_RESULT_BUTTON = 53;

    public RecipeCreatorGUI(CustomRecipes plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createGradientMenuTitle("Create Custom Recipe")
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
        addGridItems();
        addEqualsSign();
        addTypeToggleButton();
        addCreateButton();
        addCancelButton();
        addEditResultButton();
        updateResultDisplay();
    }

    private void updateResultDisplay() {
        if (resultItem != null && resultItem.getType() != Material.AIR) {
            ItemStack display = resultItem.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<Component> originalLore = resultItem.hasItemMeta() && resultItem.getItemMeta().hasLore() && resultItem.getItemMeta().lore() != null ?
                        new ArrayList<>(resultItem.getItemMeta().lore()) : new ArrayList<>();
                List<Component> displayLore = new ArrayList<>(originalLore);
                displayLore.add(Component.empty());
                displayLore.add(Component.text("Amount: " + display.getAmount(), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                displayLore.add(Component.empty());
                displayLore.add(Component.text("» Left Click to add", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, true));
                displayLore.add(Component.text("» Right Click to remove", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, true));
                meta.lore(displayLore);
                display.setItemMeta(meta);
            }
            inventory.setItem(RESULT_SLOT, display);
        } else {
            inventory.setItem(RESULT_SLOT, null);
        }
    }

    private void addGridItems() {
        for (int i = 0; i < 9; i++) {
            if (gridItems[i] != null && gridItems[i].getType() != Material.AIR) {
                ItemStack display = gridItems[i].clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    List<Component> lore = meta.hasLore() && meta.lore() != null ?
                            new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("Amount: " + display.getAmount(), NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false));
                    if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                        if (damageable.hasDamage()) {
                            int maxDurability = display.getType().getMaxDurability();
                            int currentDurability = maxDurability - damageable.getDamage();
                            lore.add(Component.text("Durability: " + currentDurability + "/" + maxDurability,
                                            NamedTextColor.AQUA)
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                    }
                    lore.add(Component.empty());
                    lore.add(Component.text("» Left Click to add", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, true));
                    lore.add(Component.text("» Right Click to remove", NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, true));
                    meta.lore(lore);
                    display.setItemMeta(meta);
                }
                inventory.setItem(GRID_SLOTS[i], display);
            }
        }
    }

    private void handleCreateRecipe() {
        boolean hasIngredient = false;
        for (int i = 0; i < 9; i++) {
            if (gridItems[i] != null && gridItems[i].getType() != Material.AIR) {
                hasIngredient = true;
                break;
            }
        }

        if (!hasIngredient) {
            MessageUtil.sendError(player, "Please place at least one ingredient in the grid!");
            return;
        }

        if (resultItem == null || resultItem.getType() == Material.AIR) {
            MessageUtil.sendError(player, "Please place a result item!");
            return;
        }
        ItemStack result = resultItem.clone();
        String recipeKey = generateRecipeKey(result.getType());
        try {
            ShapedRecipeData recipeData = null;
            ShapelessRecipeData shapelessData = null;
            if (currentType == RecipeType.SHAPED) {
                List<ItemStack> exactItems = new ArrayList<>();
                List<String> patternList = new ArrayList<>();
                for (int row = 0; row < 3; row++) {
                    StringBuilder rowBuilder = new StringBuilder();
                    for (int col = 0; col < 3; col++) {
                        int index = row * 3 + col;
                        ItemStack item = gridItems[index];
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
                                            plugin.getLogger().info("DEBUG handleCreateRecipe[" + index + "]: ENCHANTED_BOOK with StoredEnchants: " +
                                                    bookMeta.getStoredEnchants());
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
                                plugin.getLogger().info("DEBUG handleCreateRecipe[" + index + "]: Saving exactItem: " +
                                        item.getType());
                            } else {
                                exactItems.add(null);
                            }
                        }
                    }
                    patternList.add(rowBuilder.toString().trim());
                }
                recipeData = ShapedRecipeData.fromConfigList(patternList, exactItems);
            } else {
                shapelessData = ShapelessRecipeData.fromGridItems(gridItems);
            }

            CustomRecipe newRecipe = new CustomRecipe(
                    recipeKey,
                    null,
                    new ArrayList<>(),
                    currentType,
                    recipeData,
                    shapelessData,
                    result.clone(),
                    false,
                    new ArrayList<>()
            );
            saveRecipeToConfig(recipeKey, recipeData, shapelessData, result);
            List<String> enabledRecipes = plugin.getConfig().getStringList("enabled-recipes");
            if (!enabledRecipes.contains(recipeKey)) {
                enabledRecipes.add(recipeKey);
                plugin.getConfig().set("enabled-recipes", enabledRecipes);
                plugin.saveConfig();
                plugin.reloadConfig();
            }

            plugin.getRecipeManager().registerSingleRecipe(newRecipe);
            MessageUtil.sendAdminSuccess(player, "Created recipe: <white>" + recipeKey);
            player.closeInventory();
            new RecipeEditorGUI(plugin, player, newRecipe).open();
        } catch (Exception e) {
            MessageUtil.sendError(player, "Failed to create recipe: " + e.getMessage());
            plugin.getLogger().warning("Recipe creation failed: " + e.getMessage());
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    private void saveRecipeToConfig(String recipeKey, ShapedRecipeData shapedData, ShapelessRecipeData shapelessData, ItemStack result) {
        try {
            Map<String, Object> recipeData = new HashMap<>();
            if (currentType == RecipeType.SHAPELESS) {
                recipeData.put("type", currentType.name());
            }

            if (currentType == RecipeType.SHAPED) {
                List<String> patternList = new ArrayList<>();
                for (int row = 0; row < 3; row++) {
                    StringBuilder rowBuilder = new StringBuilder();
                    for (int col = 0; col < 3; col++) {
                        int index = row * 3 + col;
                        ItemStack item = gridItems[index];
                        if (item == null || item.getType() == Material.AIR) {
                            rowBuilder.append("AIR ");
                        } else {
                            rowBuilder.append(item.getType().name());
                            if (item.getAmount() > 1) {
                                rowBuilder.append(":").append(item.getAmount());
                            }
                            rowBuilder.append(" ");
                        }
                    }
                    patternList.add(rowBuilder.toString().trim());
                }
                recipeData.put("recipe", patternList);
                List<ItemStack> exactItemsToSave = new ArrayList<>();
                boolean hasExactItems = false;
                for (int i = 0; i < 9; i++) {
                    ItemStack item = gridItems[i];
                    boolean shouldSaveExact = false;
                    if (item != null && item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
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

                        if (item.getType() == Material.ENCHANTED_BOOK &&
                                meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta) {
                            org.bukkit.inventory.meta.EnchantmentStorageMeta bookMeta =
                                    (org.bukkit.inventory.meta.EnchantmentStorageMeta) meta;
                            if (bookMeta.hasStoredEnchants()) {
                                shouldSaveExact = true;
                            }
                        }
                    }
                    if (shouldSaveExact) {
                        exactItemsToSave.add(item.clone());
                        hasExactItems = true;
                    } else {
                        exactItemsToSave.add(null);
                    }
                }
                if (hasExactItems) {
                    List<String> serializedItems = new ArrayList<>();
                    for (ItemStack exactItem : exactItemsToSave) {
                        if (exactItem != null) {
                            serializedItems.add(ItemStackSerializer.toBase64(exactItem));
                        } else {
                            serializedItems.add(null);
                        }
                    }
                    recipeData.put("exact-ingredients", serializedItems);
                }
            } else {
                recipeData.put("ingredients", shapelessData.toConfigList());
            }
            Map<String, Object> resultData = ItemStackSerializer.toMap(result);
            recipeData.put("result", resultData);
            recipeData.put("material", result.getType().name());
            recipeData.put("amount", result.getAmount());
            if (result.hasItemMeta()) {
                ItemMeta resultMeta = result.getItemMeta();
                if (resultMeta.hasDisplayName() || resultMeta.hasLore() || resultMeta.hasEnchants() ||
                        !resultMeta.getPersistentDataContainer().getKeys().isEmpty()) {
                    recipeData.put("result-full", ItemStackSerializer.toMap(result));
                }
            }
            recipeData.put("hidden", false);
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    String name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(meta.displayName());
                    if (name != null && !name.isEmpty()) {
                        recipeData.put("name", name);
                    }
                }
                if (meta.hasLore() && meta.lore() != null) {
                    List<String> loreStrings = new ArrayList<>();
                    for (Component line : meta.lore()) {
                        String loreText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                                .serialize(line);
                        if (loreText != null && !loreText.isEmpty()) {
                            loreStrings.add(loreText);
                        }
                    }
                    if (!loreStrings.isEmpty()) {
                        recipeData.put("description", loreStrings);
                    }
                }
            }
            plugin.getConfigManager().getRecipeFileManager().saveRecipe(recipeKey, recipeData);
            JsonRecipeFileManager jsonManager = new JsonRecipeFileManager(plugin);
            jsonManager.saveRecipeJson(recipeKey, recipeData);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save recipe: " + e.getMessage());
            MessageUtil.sendError(player, "Failed to save recipe file!");
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

        for (int slot : GRID_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(RESULT_SLOT, null);
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

    private void addTypeToggleButton() {
        boolean isShaped = currentType == RecipeType.SHAPED;
        ItemStack toggle = new ItemStack(isShaped ? Material.CRAFTING_TABLE : Material.CHEST);
        ItemMeta meta = toggle.getItemMeta();
        meta.displayName(Component.text(
                isShaped ? "Shaped Recipe" : "Shapeless Recipe",
                isShaped ? NamedTextColor.AQUA : NamedTextColor.LIGHT_PURPLE
        ).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (isShaped) {
            lore.add(Component.text("Position matters!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Items must be placed", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("in exact positions.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("Position doesn't matter!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Items can be placed", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("in any order.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        toggle.setItemMeta(meta);
        inventory.setItem(TYPE_TOGGLE, toggle);
    }

    private void addCreateButton() {
        ItemStack create = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = create.getItemMeta();
        meta.displayName(Component.text("Create Recipe", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Click to create this", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("custom recipe!", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("» Left Click", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        create.setItemMeta(meta);
        inventory.setItem(CREATE_BUTTON, create);
    }

    private void addCancelButton() {
        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = cancel.getItemMeta();
        meta.displayName(Component.text("Cancel", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Return to main menu", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("» Click to cancel", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, true)
        ));
        cancel.setItemMeta(meta);
        inventory.setItem(CANCEL_BUTTON, cancel);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player dragPlayer) || !dragPlayer.equals(player)) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot < inventory.getSize()) {
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
                    ItemStack existing = gridItems[gridIndex];
                    int maxStack = cursor.getType().getMaxStackSize();

                    if (existing == null || existing.getType() == Material.AIR) {
                        gridItems[gridIndex] = cursor.clone();
                        gridItems[gridIndex].setAmount(1);
                    } else if (existing.getType() == cursor.getType()) {
                        existing.setAmount(Math.min(existing.getAmount() + 1, maxStack));
                    } else {
                        gridItems[gridIndex] = cursor.clone();
                        gridItems[gridIndex].setAmount(1);
                    }
                } else {
                    ItemStack existing = gridItems[gridIndex];
                    if (existing != null && existing.getType() != Material.AIR) {
                        int maxStack = existing.getType().getMaxStackSize();
                        existing.setAmount(Math.min(existing.getAmount() + 1, maxStack));
                    }
                }
                updateInventory();
            } else if (clickType.isRightClick()) {
                ItemStack existing = gridItems[gridIndex];
                if (existing != null && existing.getType() != Material.AIR) {
                    if (existing.getAmount() > 1) {
                        existing.setAmount(existing.getAmount() - 1);
                    } else {
                        gridItems[gridIndex] = null;
                    }
                    updateInventory();
                }
            }
            return;
        }
        if (slot == RESULT_SLOT) {
            if (event.getClickedInventory() != inventory) {
                return;
            }
            event.setCancelled(true);
            if (clickType.isLeftClick()) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (resultItem == null || resultItem.getType() == Material.AIR) {
                        resultItem = cursor.clone();
                        resultItem.setAmount(1);
                    } else if (resultItem.isSimilar(cursor)) {
                        int newAmount = Math.min(resultItem.getAmount() + 1, cursor.getType().getMaxStackSize());
                        resultItem.setAmount(newAmount);
                    } else {
                        resultItem = cursor.clone();
                        resultItem.setAmount(1);
                    }
                } else if (resultItem != null && resultItem.getType() != Material.AIR) {
                    int newAmount = Math.min(resultItem.getAmount() + 1, resultItem.getType().getMaxStackSize());
                    resultItem.setAmount(newAmount);
                }
            } else if (clickType.isRightClick()) {
                if (resultItem != null && resultItem.getType() != Material.AIR) {
                    if (resultItem.getAmount() > 1) {
                        resultItem.setAmount(resultItem.getAmount() - 1);
                    } else {
                        resultItem = null;
                    }
                }
            }
            updateResultDisplay();
            return;
        }

        if (event.getClickedInventory() != inventory) {
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (slot == TYPE_TOGGLE) {
            currentType = (currentType == RecipeType.SHAPED) ? RecipeType.SHAPELESS : RecipeType.SHAPED;
            updateInventory();
            return;
        }

        if (slot == CREATE_BUTTON) {
            handleCreateRecipe();
            return;
        }

        if (slot == CANCEL_BUTTON) {
            new StationSelectorGUI(plugin, player).open();
            return;
        }

        if (slot == EDIT_RESULT_BUTTON) {
            if (resultItem == null || resultItem.getType() == Material.AIR) {
                MessageUtil.sendError(player, "Please place a result item first!");
                return;
            }

            ItemStack[] tempSavedGrid = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                tempSavedGrid[i] = gridItems[i] != null ? gridItems[i].clone() : null;
            }
            RecipeType tempSavedType = currentType;
            ItemStack tempResult = resultItem.clone();
            currentEditor = new ItemEditorGUI(plugin, player, tempResult, (editedItem) -> {
                RecipeCreatorGUI newGUI = new RecipeCreatorGUI(plugin, player);
                newGUI.currentType = tempSavedType;
                for (int i = 0; i < 9; i++) {
                    newGUI.gridItems[i] = tempSavedGrid[i] != null ? tempSavedGrid[i].clone() : null;
                }

                if (editedItem != null) {
                    newGUI.resultItem = editedItem.clone();
                } else {
                    newGUI.resultItem = tempResult.clone();
                }
                newGUI.updateInventory();
                newGUI.open();
            });
            currentEditor.open();
            return;
        }
    }

    private void addEditResultButton() {
        ItemStack button = new ItemStack(Material.ANVIL);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(Component.text("Edit Result Item", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Customize the result", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("item's name and lore", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to edit", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        button.setItemMeta(meta);
        inventory.setItem(EDIT_RESULT_BUTTON, button);
    }

    private String generateRecipeKey(Material material) {
        String baseName = formatMaterialName(material.name());
        String key = baseName;
        int counter = 1;
        while (plugin.getRecipeManager().getRecipe(key) != null) {
            key = baseName + counter;
            counter++;
        }
        return key;
    }

    private String formatMaterialName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return result.toString();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}
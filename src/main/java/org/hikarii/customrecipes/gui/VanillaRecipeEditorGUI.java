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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.recipe.RecipeType;
import org.hikarii.customrecipes.recipe.vanilla.IngredientChoice;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeInfo;
import org.hikarii.customrecipes.recipe.vanilla.VanillaRecipeManager;
import org.hikarii.customrecipes.util.MessageUtil;
import java.util.*;

public class VanillaRecipeEditorGUI implements Listener {
    private static final Map<UUID, VanillaRecipeEditorGUI> waitingForSearch = new HashMap<>();
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 25;
    private static final int INGREDIENT_CYCLE_BUTTON = 52;
    private int currentVariantIndex = 0;
    private int maxVariants = 1;
    private long lastVariantSwitch = 0;
    private static final long SWITCH_COOLDOWN = 200;

    private final CustomRecipes plugin;
    private final Player player;
    private final VanillaRecipeInfo originalRecipe;
    private final VanillaRecipesGUI parentGUI;
    private final Inventory inventory;
    private final ItemStack[] gridItems = new ItemStack[9];
    private RecipeType currentType;

    public VanillaRecipeEditorGUI(CustomRecipes plugin, Player player, VanillaRecipeInfo recipe, VanillaRecipesGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.originalRecipe = recipe;
        this.parentGUI = parentGUI;
        this.currentType = recipe.getType();
        this.inventory = Bukkit.createInventory(
                null,
                54,
                MessageUtil.createMenuTitle("Edit: " + recipe.getDisplayName(), NamedTextColor.DARK_AQUA)
        );
        String recipeKey = recipe.getKey().replace("minecraft:", "");
        this.maxVariants = plugin.getVanillaRecipeManager().getMaxVariantsForRecipe(recipeKey);
        VanillaRecipeManager.VanillaRecipeState state = plugin.getVanillaRecipeManager().getRecipeState(recipeKey);

        if (state != null) {
            this.currentVariantIndex = state.getCurrentVariantIndex();
            loadVariantPattern(currentVariantIndex);
        } else {
            this.currentVariantIndex = 0;
            loadOriginalPattern();
        }
        plugin.getVanillaRecipeManager().setCurrentVariant(recipeKey, currentVariantIndex);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    private void loadOriginalPattern() {
        List<String> pattern = originalRecipe.getPattern();
        for (int i = 0; i < Math.min(pattern.size(), 3); i++) {
            String[] row = pattern.get(i).split(" ");
            for (int j = 0; j < Math.min(row.length, 3); j++) {
                String materialName = row[j];
                if (!materialName.equals("AIR") && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName);
                    if (material != null) {
                        gridItems[i * 3 + j] = new ItemStack(material);
                    }
                }
            }
        }
    }

    private void loadChangedPattern(List<String> pattern) {
        for (int i = 0; i < Math.min(pattern.size(), 3); i++) {
            String[] row = pattern.get(i).split(" ");
            for (int j = 0; j < Math.min(row.length, 3); j++) {
                String materialName = row[j];
                if (!materialName.equals("AIR") && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName);
                    if (material != null) {
                        gridItems[i * 3 + j] = new ItemStack(material);
                    }
                }
            }
        }
    }

    private void loadVariantPattern(int variantIndex) {
        Arrays.fill(gridItems, null);
        String recipeKey = originalRecipe.getKey().replace("minecraft:", "");
        VanillaRecipeManager.VanillaRecipeState state = plugin.getVanillaRecipeManager().getRecipeState(recipeKey);
        if (state != null && state.getPatternForVariant(variantIndex) != null) {
            List<String> pattern = state.getPatternForVariant(variantIndex);
            this.currentType = state.getTypeForVariant(variantIndex);
            for (int i = 0; i < Math.min(pattern.size(), 3); i++) {
                String[] row = pattern.get(i).split(" ");
                for (int j = 0; j < Math.min(row.length, 3); j++) {
                    String materialName = row[j];
                    if (!materialName.equals("AIR") && !materialName.isEmpty()) {
                        Material material = Material.getMaterial(materialName);
                        if (material != null) {
                            gridItems[i * 3 + j] = new ItemStack(material);
                        } else {
                            gridItems[i * 3 + j] = null;
                        }
                    } else {
                        gridItems[i * 3 + j] = null;
                    }
                }
            }
        } else {
            loadOriginalPattern();
        }
    }

    private void loadPatternToGrid(List<String> pattern) {
        for (int i = 0; i < Math.min(pattern.size(), 3); i++) {
            String[] row = pattern.get(i).split(" ");
            for (int j = 0; j < Math.min(row.length, 3); j++) {
                String materialName = row[j];
                if (!materialName.equals("AIR") && !materialName.isEmpty()) {
                    Material material = Material.getMaterial(materialName);
                    if (material != null) {
                        gridItems[i * 3 + j] = new ItemStack(material);
                    }
                } else {
                    gridItems[i * 3 + j] = null;
                }
            }
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void updateInventory() {
        inventory.clear();
        fillBorders();
        addGridItems();
        addResultItem();
        addEqualsSign();
        addTypeToggle();
        addButtons();
    }

    private void fillBorders() {
        ItemStack borderPane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
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

    private void addGridItems() {
        for (int i = 0; i < 9; i++) {
            if (gridItems[i] != null) {
                ItemStack display = gridItems[i].clone();
                ItemMeta meta = display.getItemMeta();
                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Click to remove", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, true));
                meta.lore(lore);
                display.setItemMeta(meta);
                inventory.setItem(GRID_SLOTS[i], display);
            }
        }
    }

    private void addResultItem() {
        ItemStack result = originalRecipe.hasVariantResults() ?
                originalRecipe.getResultForVariant(currentVariantIndex) :
                new ItemStack(originalRecipe.getResultMaterial(), originalRecipe.getResultAmount());
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.hasLore() && meta.lore() != null ?
                    new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("» Left Click to add +1", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, true));
            lore.add(Component.text("» Right Click to remove -1", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, true));
            meta.lore(lore);
            result.setItemMeta(meta);
        }
        inventory.setItem(RESULT_SLOT, result);
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

    private void addTypeToggle() {
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
        } else {
            lore.add(Component.text("Position doesn't matter!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("» Click to toggle", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        toggle.setItemMeta(meta);
        inventory.setItem(45, toggle);
    }

    private void addButtons() {
        if (maxVariants > 1) {
            addVariantSwitcher();
        }

        ItemStack save = new ItemStack(Material.LIME_WOOL);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.displayName(Component.text("Save", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        saveMeta.lore(List.of(
                Component.empty(),
                Component.text("Save changes and stay", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        save.setItemMeta(saveMeta);
        inventory.setItem(48, save);

        ItemStack close = new ItemStack(Material.RED_WOOL);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        closeMeta.lore(List.of(
                Component.empty(),
                Component.text("Return to recipe list", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        close.setItemMeta(closeMeta);
        inventory.setItem(50, close);

        ItemStack search = new ItemStack(Material.COMPASS);
        ItemMeta searchMeta = search.getItemMeta();
        searchMeta.displayName(Component.text("New Search", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        searchMeta.lore(List.of(
                Component.empty(),
                Component.text("Search for another recipe", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        search.setItemMeta(searchMeta);
        inventory.setItem(51, search);

        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.displayName(Component.text("Reset", NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        resetMeta.lore(List.of(
                Component.empty(),
                Component.text("Restore original recipe", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("⚠ This cannot be undone", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        reset.setItemMeta(resetMeta);
        inventory.setItem(53, reset);
    }

    private void addVariantSwitcher() {
        String variantName = getVariantName(currentVariantIndex);
        ItemStack switcher = new ItemStack(Material.ARROW);
        ItemMeta meta = switcher.getItemMeta();
        meta.displayName(Component.text("Ingredient Variant", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Current: ", NamedTextColor.GRAY)
                .append(Component.text(variantName, NamedTextColor.GREEN)
                        .decoration(TextDecoration.BOLD, true))
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Available variants:", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        for (int i = 0; i < maxVariants; i++) {
            String name = getVariantName(i);
            boolean isCurrent = i == currentVariantIndex;
            lore.add(Component.text(isCurrent ? "➤ " : "  ",
                            isCurrent ? NamedTextColor.GREEN : NamedTextColor.GRAY)
                    .append(Component.text(name, isCurrent ? NamedTextColor.GREEN : NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("Each variant has its", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("own recipe pattern", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("» Click to switch variant", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, true));
        meta.lore(lore);
        switcher.setItemMeta(meta);
        inventory.setItem(8, switcher);
    }

    private String getVariantName(int index) {
        IngredientChoice firstChoice = null;
        for (List<IngredientChoice> row : originalRecipe.getIngredientGrid()) {
            for (IngredientChoice choice : row) {
                if (choice.hasMultipleOptions()) {
                    firstChoice = choice;
                    break;
                }
            }
            if (firstChoice != null) break;
        }
        if (firstChoice != null && index < firstChoice.getOptions().size()) {
            Material mat = firstChoice.getOptions().get(index);
            return MessageUtil.formatMaterialName(mat.name());
        }
        return "Variant " + (index + 1);
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

        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }

        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        if (slot == 8 && maxVariants > 1) {
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            if (now - lastVariantSwitch < SWITCH_COOLDOWN) {
                return;
            }
            lastVariantSwitch = now;
            saveCurrentVariant();
            currentVariantIndex = (currentVariantIndex + 1) % maxVariants;
            String recipeKey = originalRecipe.getKey().replace("minecraft:", "");
            plugin.getVanillaRecipeManager().setCurrentVariant(recipeKey, currentVariantIndex);
            loadVariantPattern(currentVariantIndex);
            updateInventoryFast();
            return;
        }

        if (slot < 0 || slot >= 54) {
            return;
        }

        if (event.getClickedInventory() != inventory) {
            return;
        }

        if (slot == RESULT_SLOT) {
            event.setCancelled(true);
            ItemStack result = inventory.getItem(RESULT_SLOT);

            if (clickType.isLeftClick()) {
                if (result != null && result.getType() != Material.AIR) {
                    int newAmount = Math.min(result.getAmount() + 1, result.getType().getMaxStackSize());
                    result.setAmount(newAmount);
                    inventory.setItem(RESULT_SLOT, result);
                }
            } else if (clickType.isRightClick()) {
                if (result != null && result.getType() != Material.AIR && result.getAmount() > 1) {
                    result.setAmount(result.getAmount() - 1);
                    inventory.setItem(RESULT_SLOT, result);
                }
            }
            return;
        }

        for (int i = 0; i < GRID_SLOTS.length; i++) {
            if (slot == GRID_SLOTS[i]) {
                event.setCancelled(true);

                if (clickType.isLeftClick() && gridItems[i] != null) {
                    gridItems[i] = null;
                    updateInventory();
                } else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                    gridItems[i] = new ItemStack(event.getCursor().getType(), 1);
                    updateInventory();
                }
                return;
            }
        }

        event.setCancelled(true);
        if (slot == 45) {
            currentType = currentType == RecipeType.SHAPED ? RecipeType.SHAPELESS : RecipeType.SHAPED;
            updateInventory();
            return;
        }

        if (slot == 48) {
            saveRecipe();
            MessageUtil.sendAdminSuccess(player, "Saved recipe changes!");
            return;
        }

        if (slot == 50) {
            VanillaRecipesGUI gui = new VanillaRecipesGUI(plugin, player,
                    parentGUI.getCurrentCategory(),
                    parentGUI.getCurrentStation());
            gui.open();
            return;
        }

        if (slot == 51) {
            waitingForSearch.put(player.getUniqueId(), this);
            player.closeInventory();
            MessageUtil.sendAdminInfo(player, "Type the recipe name to search");
            MessageUtil.sendAdminInfo(player, "Type <red>cancel</red> to cancel");
            return;
        }

        if (slot == 53) {
            String recipeKey = originalRecipe.getKey().replace("minecraft:", "");
            plugin.getVanillaRecipeManager().resetRecipe(recipeKey);
            MessageUtil.sendAdminSuccess(player, "Reset recipe to original!");
            loadOriginalPattern();
            currentType = originalRecipe.getType();
            updateInventory();
            return;
        }
    }

    private void saveRecipe() {
        saveCurrentVariant();
        ItemStack resultItem = inventory.getItem(RESULT_SLOT);
        if (resultItem != null && resultItem.getAmount() != originalRecipe.getResultAmount()) {
            String recipeKey = originalRecipe.getKey().replace("minecraft:", "");
            VanillaRecipeManager.VanillaRecipeState state = plugin.getVanillaRecipeManager().getRecipeState(recipeKey);
            if (state != null) {
                state.setResultAmount(resultItem.getAmount());
            }
        }
        MessageUtil.sendAdminSuccess(player, "Saved variant: " + getVariantName(currentVariantIndex));
    }

    private void saveCurrentVariant() {
        List<String> pattern = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            StringBuilder rowPattern = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                ItemStack item = gridItems[row * 3 + col];
                if (item == null) {
                    rowPattern.append("AIR ");
                } else {
                    rowPattern.append(item.getType().name()).append(" ");
                }
            }
            pattern.add(rowPattern.toString().trim());
        }
        String recipeKey = originalRecipe.getKey().replace("minecraft:", "");
        plugin.getVanillaRecipeManager().updateRecipeVariant(recipeKey, currentVariantIndex, pattern, currentType);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();
        VanillaRecipeEditorGUI editor = waitingForSearch.get(chatPlayer.getUniqueId());
        if (editor == null || !chatPlayer.equals(player)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        if (message.equalsIgnoreCase("cancel")) {
            waitingForSearch.remove(chatPlayer.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                MessageUtil.sendAdminWarning(chatPlayer, "Search cancelled");
                editor.open();
            });
            return;
        }

        List<VanillaRecipeInfo> results = plugin.getVanillaRecipeManager().searchRecipes(message);
        waitingForSearch.remove(chatPlayer.getUniqueId());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (results.isEmpty()) {
                MessageUtil.sendError(chatPlayer, "No recipes found matching: " + message);
                editor.open();
            } else {
                new VanillaRecipeSearchResultsGUI(plugin, chatPlayer, results, message, parentGUI).open();
            }
        });
    }

    private void updateInventoryFast() {
        for (int slot : GRID_SLOTS) {
            inventory.setItem(slot, null);
        }
        if (maxVariants > 1) {
            addVariantSwitcher();
        }
        addResultItem();
        addGridItems();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            if (!waitingForSearch.containsKey(player.getUniqueId())) {
                InventoryClickEvent.getHandlerList().unregister(this);
                InventoryCloseEvent.getHandlerList().unregister(this);
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
            }
        }
    }
}
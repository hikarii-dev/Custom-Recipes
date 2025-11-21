package org.hikarii.customrecipes.recipe.vanilla;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.hikarii.customrecipes.recipe.RecipeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanillaRecipeInfo {
    private final String key;
    private final String displayName;
    private final Material resultMaterial;
    private final Map<Integer, ItemStack> variantResults;
    private final int resultAmount;
    private final RecipeType type;
    private final List<List<IngredientChoice>> ingredientGrid; 
    private final RecipeCategory category;
    private final RecipeStation station;

    public VanillaRecipeInfo(String key, String displayName, Material resultMaterial,
                             int resultAmount, RecipeType type, List<List<IngredientChoice>> ingredientGrid,
                             RecipeCategory category, RecipeStation station,
                             Map<Integer, ItemStack> variantResults) {
        this.key = key;
        this.displayName = displayName;
        this.resultMaterial = resultMaterial;
        this.resultAmount = resultAmount;
        this.type = type;
        this.ingredientGrid = ingredientGrid;
        this.category = category;
        this.station = station;
        this.variantResults = variantResults != null ? variantResults : new HashMap<>();
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getResultMaterial() {
        return resultMaterial;
    }

    public int getResultAmount() {
        return resultAmount;
    }

    public RecipeType getType() {
        return type;
    }

    public List<List<IngredientChoice>> getIngredientGrid() {
        return ingredientGrid;
    }

    public List<String> getPattern() {
        List<String> pattern = new ArrayList<>();
        for (List<IngredientChoice> row : ingredientGrid) {
            StringBuilder rowBuilder = new StringBuilder();
            for (IngredientChoice choice : row) {
                if (rowBuilder.length() > 0) rowBuilder.append(" ");
                rowBuilder.append(choice.getSelected().name());
            }
            pattern.add(rowBuilder.toString());
        }
        return pattern;
    }

    public IngredientChoice getIngredientChoice(int row, int col) {
        if (row >= 0 && row < ingredientGrid.size()) {
            List<IngredientChoice> rowList = ingredientGrid.get(row);
            if (col >= 0 && col < rowList.size()) {
                return rowList.get(col);
            }
        }
        return new IngredientChoice(Material.AIR);
    }

    public RecipeCategory getCategory() {
        return category;
    }

    public RecipeStation getStation() {
        return station;
    }

    public enum RecipeCategory {
        BUILDING("Building Blocks", Material.STONE),
        DECORATIONS("Decorations", Material.PAINTING),
        REDSTONE("Redstone", Material.REDSTONE),
        TRANSPORTATION("Transportation", Material.MINECART),
        FOOD("Food & Drinks", Material.APPLE),
        TOOLS("Tools", Material.IRON_PICKAXE),
        COMBAT("Combat", Material.IRON_SWORD),
        BREWING("Brewing", Material.BREWING_STAND),
        MISC("Miscellaneous", Material.LAVA_BUCKET);

        private final String displayName;
        private final Material icon;
        RecipeCategory(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }
    }

    public enum RecipeStation {
        CRAFTING_TABLE("Crafting Table", Material.CRAFTING_TABLE, true, 9),
        FURNACE("Furnace (Coming soon)", Material.FURNACE, false, 2),
        BLAST_FURNACE("Blast Furnace (Coming soon)", Material.BLAST_FURNACE, false, 2),
        SMOKER("Smoker (Coming soon)", Material.SMOKER, false, 2),
        STONECUTTER("Stonecutter (Coming soon)", Material.STONECUTTER, false, 1),
        LOOM("Loom (Coming soon)", Material.LOOM, false, 3),
        BREWING_STAND("Brewing Stand (Coming soon)", Material.BREWING_STAND, false, 4),
        CARTOGRAPHY_TABLE("Cartography Table (Coming soon)", Material.CARTOGRAPHY_TABLE, false, 2),
        SMITHING_TABLE("Smithing Table (Coming soon)", Material.SMITHING_TABLE, false, 3);

        private final String displayName;
        private final Material icon;
        private final boolean enabled;
        private final int gridSize;
        RecipeStation(String displayName, Material icon, boolean enabled, int gridSize) {
            this.displayName = displayName;
            this.icon = icon;
            this.enabled = enabled;
            this.gridSize = gridSize;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getGridSize() {
            return gridSize;
        }
    }

    public ItemStack getResultForVariant(int variantIndex) {
        if (variantResults.containsKey(variantIndex)) {
            return variantResults.get(variantIndex).clone();
        }
        return new ItemStack(resultMaterial, resultAmount);
    }

    public boolean hasVariantResults() {
        return !variantResults.isEmpty();
    }

    public boolean hasCustomVariantResults() {
        return !variantResults.isEmpty();
    }

    public Map<Integer, ItemStack> getVariantResults() {
        return variantResults;
    }
}
package org.hikarii.customrecipes.recipe.vanilla;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientChoice {
    private final List<Material> options;
    private int selectedIndex;

    public IngredientChoice(List<Material> options) {
        this.options = new ArrayList<>(options);
        this.selectedIndex = 0;
    }

    public IngredientChoice(Material singleOption) {
        this.options = new ArrayList<>();
        this.options.add(singleOption);
        this.selectedIndex = 0;
    }

    public Material getSelected() {
        if (options.isEmpty()) return Material.AIR;
        return options.get(selectedIndex);
    }

    public List<Material> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.size()) {
            this.selectedIndex = index;
        }
    }

    public boolean hasMultipleOptions() {
        return options.size() > 1;
    }

    public void cycleNext() {
        selectedIndex = (selectedIndex + 1) % options.size();
    }

    public void cyclePrevious() {
        selectedIndex = (selectedIndex - 1 + options.size()) % options.size();
    }

    public static IngredientChoice fromString(String data) {
        if (data.contains("|")) {
            String[] parts = data.split("\\|");
            List<Material> materials = new ArrayList<>();
            for (String part : parts) {
                Material mat = Material.getMaterial(part.trim());
                if (mat != null) {
                    materials.add(mat);
                }
            }
            return new IngredientChoice(materials);
        } else {
            Material mat = Material.getMaterial(data);
            return new IngredientChoice(mat != null ? mat : Material.AIR);
        }
    }

    public String toString() {
        if (options.size() == 1) {
            return options.get(0).name();
        }
        return String.join("|", options.stream().map(Material::name).toList());
    }
}
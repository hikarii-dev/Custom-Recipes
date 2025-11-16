package org.hikarii.customrecipes.recipe;

import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;

import java.io.*;
import java.util.*;

public class RecipeDataManager {

    private final CustomRecipes plugin;
    private final Map<UUID, Set<String>> discoveredRecipes;
    private final File dataFile;

    public RecipeDataManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.discoveredRecipes = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "discovered-recipes.dat");

        loadData();
    }

    public boolean hasDiscovered(Player player, String recipeKey) {
        return discoveredRecipes.getOrDefault(player.getUniqueId(), Collections.emptySet())
                .contains(recipeKey.toLowerCase());
    }

    public void markDiscovered(Player player, String recipeKey) {
        UUID uuid = player.getUniqueId();
        discoveredRecipes.computeIfAbsent(uuid, k -> new HashSet<>()).add(recipeKey.toLowerCase());
        saveData();
        plugin.debug("Player " + player.getName() + " discovered recipe: " + recipeKey);
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            @SuppressWarnings("unchecked")
            Map<UUID, Set<String>> loaded = (Map<UUID, Set<String>>) ois.readObject();
            discoveredRecipes.putAll(loaded);
            plugin.debug("Loaded discovered recipes data for " + discoveredRecipes.size() + " players");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load discovered recipes data: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                oos.writeObject(discoveredRecipes);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save discovered recipes data: " + e.getMessage());
        }
    }
}
package org.hikarii.customrecipes.recipe;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;
import java.util.*;

public class RecipeWorldManager {
    private final CustomRecipes plugin;
    private final Map<String, Set<String>> recipeWorldRestrictions;
    private boolean globalWorldRestrictionEnabled;
    private Set<String> globalDisabledWorlds;

    public RecipeWorldManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipeWorldRestrictions = new HashMap<>();
        this.globalDisabledWorlds = new HashSet<>();
        loadWorldRestrictions();
    }

    public void loadWorldRestrictions() {
        recipeWorldRestrictions.clear();
        globalDisabledWorlds.clear();
        globalWorldRestrictionEnabled = plugin.getConfig().getBoolean("world-restrictions.enabled", false);
        if (globalWorldRestrictionEnabled) {
            List<String> disabledWorldsList = plugin.getConfig().getStringList("world-restrictions.disabled-worlds");
            globalDisabledWorlds.addAll(disabledWorldsList);
            plugin.debug("Global world restrictions enabled. Disabled worlds: " + globalDisabledWorlds);
        }

        ConfigurationSection recipesSection = plugin.getConfig().getConfigurationSection("recipe-world-settings");
        if (recipesSection != null) {
            for (String recipeKey : recipesSection.getKeys(false)) {
                List<String> disabledWorlds = recipesSection.getStringList(recipeKey + ".disabled-worlds");
                if (!disabledWorlds.isEmpty()) {
                    recipeWorldRestrictions.put(recipeKey.toLowerCase(), new HashSet<>(disabledWorlds));
                    plugin.debug("Recipe '" + recipeKey + "' disabled in worlds: " + disabledWorlds);
                }
            }
        }
    }

    public boolean isRecipeAllowedInWorld(String recipeKey, World world) {
        if (world == null) return true;
        return isRecipeAllowedInWorld(recipeKey, world.getName());
    }

    public boolean isRecipeAllowedInWorld(String recipeKey, String worldName) {
        Set<String> disabledWorlds = recipeWorldRestrictions.get(recipeKey.toLowerCase());
        if (disabledWorlds != null && disabledWorlds.contains(worldName)) {
            return false;
        }

        if (globalWorldRestrictionEnabled && globalDisabledWorlds.contains(worldName)) {
            ConfigurationSection overrides = plugin.getConfig().getConfigurationSection("recipe-world-settings." + recipeKey);
            if (overrides != null && overrides.contains("override-global")) {
                return overrides.getBoolean("override-global");
            }
            return false;
        }
        return true;
    }

    public boolean isRecipeAllowedForPlayer(String recipeKey, Player player) {
        return isRecipeAllowedInWorld(recipeKey, player.getWorld());
    }

    public void setRecipeWorldRestrictions(String recipeKey, List<String> disabledWorlds) {
        if (disabledWorlds == null || disabledWorlds.isEmpty()) {
            recipeWorldRestrictions.remove(recipeKey.toLowerCase());
            plugin.getConfig().set("recipe-world-settings." + recipeKey, null);
        } else {
            recipeWorldRestrictions.put(recipeKey.toLowerCase(), new HashSet<>(disabledWorlds));
            plugin.getConfig().set("recipe-world-settings." + recipeKey + ".disabled-worlds", disabledWorlds);
        }
        plugin.saveConfig();
    }

    public void toggleWorldRestriction(String recipeKey, String worldName) {
        Set<String> disabledWorlds = recipeWorldRestrictions.computeIfAbsent(
                recipeKey.toLowerCase(),
                k -> new HashSet<>()
        );
        if (disabledWorlds.contains(worldName)) {
            disabledWorlds.remove(worldName);
        } else {
            disabledWorlds.add(worldName);
        }
        setRecipeWorldRestrictions(recipeKey, new ArrayList<>(disabledWorlds));
    }

    public List<String> getDisabledWorlds(String recipeKey) {
        Set<String> disabled = recipeWorldRestrictions.get(recipeKey.toLowerCase());
        if (disabled == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(disabled);
    }

    public void setGlobalWorldRestrictions(boolean enabled, List<String> disabledWorlds) {
        this.globalWorldRestrictionEnabled = enabled;
        this.globalDisabledWorlds.clear();
        if (disabledWorlds != null) {
            this.globalDisabledWorlds.addAll(disabledWorlds);
        }
        plugin.getConfig().set("world-restrictions.enabled", enabled);
        plugin.getConfig().set("world-restrictions.disabled-worlds", disabledWorlds);
        plugin.saveConfig();
    }

    public List<String> getGlobalDisabledWorlds() {
        return new ArrayList<>(globalDisabledWorlds);
    }

    public boolean isGlobalWorldRestrictionEnabled() {
        return globalWorldRestrictionEnabled;
    }

    public static WorldType getWorldType(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> WorldType.OVERWORLD;
            case NETHER -> WorldType.NETHER;
            case THE_END -> WorldType.END;
            default -> WorldType.CUSTOM;
        };
    }

    public enum WorldType {
        OVERWORLD("Overworld", Material.GRASS_BLOCK),
        NETHER("Nether", Material.NETHERRACK),
        END("End", Material.END_STONE),
        CUSTOM("Custom", Material.COMMAND_BLOCK);

        private final String displayName;
        private final Material icon;
        WorldType(String displayName, Material icon) {
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
}
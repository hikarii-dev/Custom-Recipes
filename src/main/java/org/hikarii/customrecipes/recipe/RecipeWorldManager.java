package org.hikarii.customrecipes.recipe;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;

import java.util.*;

/**
 * Manages per-world recipe restrictions
 */
public class RecipeWorldManager {

    private final CustomRecipes plugin;
    private final Map<String, Set<String>> recipeWorldRestrictions; // recipe -> disabled worlds
    private boolean globalWorldRestrictionEnabled;
    private Set<String> globalDisabledWorlds;

    public RecipeWorldManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipeWorldRestrictions = new HashMap<>();
        this.globalDisabledWorlds = new HashSet<>();
        loadWorldRestrictions();
    }

    /**
     * Loads world restrictions from configuration
     */
    public void loadWorldRestrictions() {
        recipeWorldRestrictions.clear();
        globalDisabledWorlds.clear();

        // Load global world restrictions
        globalWorldRestrictionEnabled = plugin.getConfig().getBoolean("world-restrictions.enabled", false);
        if (globalWorldRestrictionEnabled) {
            List<String> disabledWorldsList = plugin.getConfig().getStringList("world-restrictions.disabled-worlds");
            globalDisabledWorlds.addAll(disabledWorldsList);
            plugin.debug("Global world restrictions enabled. Disabled worlds: " + globalDisabledWorlds);
        }

        // Load per-recipe world restrictions
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

    /**
     * Checks if a recipe is allowed in a specific world
     *
     * @param recipeKey the recipe key
     * @param world the world to check
     * @return true if allowed, false if disabled
     */
    public boolean isRecipeAllowedInWorld(String recipeKey, World world) {
        if (world == null) return true;
        return isRecipeAllowedInWorld(recipeKey, world.getName());
    }

    /**
     * Checks if a recipe is allowed in a specific world by name
     *
     * @param recipeKey the recipe key
     * @param worldName the world name
     * @return true if allowed, false if disabled
     */
    public boolean isRecipeAllowedInWorld(String recipeKey, String worldName) {
        // Check per-recipe restrictions first
        Set<String> disabledWorlds = recipeWorldRestrictions.get(recipeKey.toLowerCase());
        if (disabledWorlds != null && disabledWorlds.contains(worldName)) {
            return false;
        }

        // Check global restrictions
        if (globalWorldRestrictionEnabled && globalDisabledWorlds.contains(worldName)) {
            // Check if this recipe has an override
            ConfigurationSection overrides = plugin.getConfig().getConfigurationSection("recipe-world-settings." + recipeKey);
            if (overrides != null && overrides.contains("override-global")) {
                return overrides.getBoolean("override-global");
            }
            return false;
        }

        return true;
    }

    /**
     * Checks if a recipe is allowed for a specific player (based on their world)
     *
     * @param recipeKey the recipe key
     * @param player the player
     * @return true if allowed, false if disabled
     */
    public boolean isRecipeAllowedForPlayer(String recipeKey, Player player) {
        return isRecipeAllowedInWorld(recipeKey, player.getWorld());
    }

    /**
     * Sets world restrictions for a recipe
     *
     * @param recipeKey the recipe key
     * @param disabledWorlds list of world names where recipe is disabled
     */
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

    /**
     * Toggles world restriction for a recipe
     *
     * @param recipeKey the recipe key
     * @param worldName the world name
     */
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

    /**
     * Gets list of disabled worlds for a recipe
     *
     * @param recipeKey the recipe key
     * @return list of world names where recipe is disabled
     */
    public List<String> getDisabledWorlds(String recipeKey) {
        Set<String> disabled = recipeWorldRestrictions.get(recipeKey.toLowerCase());
        if (disabled == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(disabled);
    }

    /**
     * Sets global world restrictions
     *
     * @param enabled whether global restrictions are enabled
     * @param disabledWorlds list of globally disabled worlds
     */
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

    /**
     * Gets global disabled worlds
     *
     * @return list of globally disabled worlds
     */
    public List<String> getGlobalDisabledWorlds() {
        return new ArrayList<>(globalDisabledWorlds);
    }

    /**
     * Checks if global world restrictions are enabled
     *
     * @return true if enabled
     */
    public boolean isGlobalWorldRestrictionEnabled() {
        return globalWorldRestrictionEnabled;
    }

    /**
     * Gets world type enum from world environment
     */
    public static WorldType getWorldType(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> WorldType.OVERWORLD;
            case NETHER -> WorldType.NETHER;
            case THE_END -> WorldType.END;
            default -> WorldType.CUSTOM;
        };
    }

    /**
     * World type enumeration
     */
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
package org.hikarii.customrecipes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.inventory.ItemStack;
import org.hikarii.customrecipes.CustomRecipes;
import org.hikarii.customrecipes.util.ItemStackSerializer;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRecipeFileManager {
    private final CustomRecipes plugin;
    private final File recipesFolder;
    private final Gson gson;

    public JsonRecipeFileManager(CustomRecipes plugin) {
        this.plugin = plugin;
        this.recipesFolder = new File(plugin.getDataFolder(), "recipes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
        }
    }

    public void saveRecipeJson(String recipeKey, Map<String, Object> recipeData) throws IOException {
        File jsonFile = new File(recipesFolder, recipeKey + ".json");
        try (Writer writer = new FileWriter(jsonFile)) {
            gson.toJson(recipeData, writer);
        }
        plugin.debug("Saved recipe to JSON: " + jsonFile.getName());
    }

    public Map<String, Object> loadRecipeJson(String recipeKey) throws IOException {
        File jsonFile = new File(recipesFolder, recipeKey + ".json");
        if (!jsonFile.exists()) {
            return null;
        }
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonToMap(json);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonToMap(JsonObject json) {
        Map<String, Object> map = new HashMap<>();
        json.entrySet().forEach(entry -> {
            String key = entry.getKey();
            var value = entry.getValue();
            if (value.isJsonObject()) {
                map.put(key, jsonToMap(value.getAsJsonObject()));
            } else if (value.isJsonArray()) {
                map.put(key, gson.fromJson(value, List.class));
            } else if (value.isJsonPrimitive()) {
                var primitive = value.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    map.put(key, primitive.getAsNumber());
                } else if (primitive.isBoolean()) {
                    map.put(key, primitive.getAsBoolean());
                } else {
                    map.put(key, primitive.getAsString());
                }
            }
        });
        return map;
    }

    public boolean deleteRecipe(String recipeKey) {
        File jsonFile = new File(recipesFolder, recipeKey + ".json");
        if (jsonFile.exists()) {
            boolean deleted = jsonFile.delete();
            if (deleted) {
                plugin.debug("Deleted JSON file: " + jsonFile.getName());
            }
            return deleted;
        }
        return false;
    }
    public boolean jsonFileExists(String recipeKey) {
        return new File(recipesFolder, recipeKey + ".json").exists();
    }
}
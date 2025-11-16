package org.hikarii.customrecipes.update;

import org.hikarii.customrecipes.CustomRecipes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final CustomRecipes plugin;
    private final UpdateSource source;
    private final String resourceId; // For Spigot
    private final String githubRepo; // For GitHub (format: "username/repo")
    private String latestVersion = null;
    private boolean updateAvailable = false;

    public UpdateChecker(CustomRecipes plugin, UpdateSource source, String resourceId, String githubRepo) {
        this.plugin = plugin;
        this.source = source;
        this.resourceId = resourceId;
        this.githubRepo = githubRepo;
    }

    public void checkForUpdates() {
        if (source == UpdateSource.DISABLED) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String fetchedVersion = null;
                if (source == UpdateSource.SPIGOT) {
                    fetchedVersion = checkSpigot();
                } else if (source == UpdateSource.GITHUB) {
                    fetchedVersion = checkGitHub();
                }

                if (fetchedVersion != null) {
                    latestVersion = fetchedVersion;
                    String currentVersion = plugin.getDescription().getVersion();

                    if (!currentVersion.equals(latestVersion)) {
                        updateAvailable = true;
                        plugin.getLogger().info("New version available: " + latestVersion + " (Current: " + currentVersion + ")");
                    } else {
                        plugin.getLogger().info("You are running the latest version!");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
                if (plugin.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String checkSpigot() throws Exception {
        URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }

    private String checkGitHub() throws Exception {
        URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String json = response.toString();
            int tagIndex = json.indexOf("\"tag_name\":");
            if (tagIndex != -1) {
                int start = json.indexOf("\"", tagIndex + 11) + 1;
                int end = json.indexOf("\"", start);
                String tag = json.substring(start, end);
                // Remove 'v' prefix if present
                return tag.startsWith("v") ? tag.substring(1) : tag;
            }
        }
        return null;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getDownloadUrl() {
        if (source == UpdateSource.SPIGOT) {
            return "https://www.spigotmc.org/resources/" + resourceId + "/";
        } else if (source == UpdateSource.GITHUB) {
            return "https://github.com/" + githubRepo + "/releases/latest";
        }
        return null;
    }
}
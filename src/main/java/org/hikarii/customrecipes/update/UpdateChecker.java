package org.hikarii.customrecipes.update;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Enhanced Update Checker with live notification support
 */
public class UpdateChecker {
    private final CustomRecipes plugin;
    private final UpdateSource source;
    private final String resourceId;
    private final String githubRepo;
    private String latestVersion = null;
    private String currentVersion;
    private boolean updateAvailable = false;
    private boolean hasNotifiedThisSession = false;
    private int checkTaskId = -1;

    public UpdateChecker(CustomRecipes plugin, UpdateSource source, String resourceId, String githubRepo) {
        this.plugin = plugin;
        this.source = source;
        this.resourceId = resourceId;
        this.githubRepo = githubRepo;
        this.currentVersion = plugin.getDescription().getVersion();
    }

    /**
     * Starts periodic update checking
     */
    public void startPeriodicCheck() {
        if (source == UpdateSource.DISABLED) {
            return;
        }

        // Initial check
        checkForUpdates();

        // Schedule periodic checks every 30 minutes
        checkTaskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            checkForUpdatesLive();
        }, 36000L, 36000L).getTaskId(); // 30 minutes = 36000 ticks
    }

    /**
     * Stops periodic update checking
     */
    public void stopPeriodicCheck() {
        if (checkTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(checkTaskId);
            checkTaskId = -1;
        }
    }

    /**
     * Checks for updates (initial check)
     */
    public void checkForUpdates() {
        if (source == UpdateSource.DISABLED) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String fetchedVersion = fetchVersion();

                if (fetchedVersion != null) {
                    latestVersion = fetchedVersion;

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

    /**
     * Checks for updates and notifies online admins if a new version is found
     */
    private void checkForUpdatesLive() {
        if (source == UpdateSource.DISABLED) {
            return;
        }

        try {
            String fetchedVersion = fetchVersion();

            if (fetchedVersion != null) {
                String previousLatest = latestVersion;
                latestVersion = fetchedVersion;

                if (!currentVersion.equals(latestVersion)) {
                    updateAvailable = true;

                    // Check if this is a newly discovered update
                    if (previousLatest == null || !previousLatest.equals(latestVersion)) {
                        hasNotifiedThisSession = false;

                        // Notify all online admins
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            notifyOnlineAdmins();
                        });

                        plugin.getLogger().info("New update discovered: " + latestVersion);
                    }
                }
            }
        } catch (Exception e) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().warning("Update check failed: " + e.getMessage());
            }
        }
    }

    /**
     * Fetches the latest version from the configured source
     */
    private String fetchVersion() throws Exception {
        if (source == UpdateSource.SPIGOT) {
            return checkSpigot();
        } else if (source == UpdateSource.GITHUB) {
            return checkGitHub();
        }
        return null;
    }

    /**
     * Checks for updates from SpigotMC
     */
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

    /**
     * Checks for updates from GitHub
     */
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
                return tag.startsWith("v") ? tag.substring(1) : tag;
            }
        }
        return null;
    }

    /**
     * Notifies all online administrators about the available update
     */
    public void notifyOnlineAdmins() {
        if (!updateAvailable || hasNotifiedThisSession) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("customrecipes.update.notify")) {
                sendUpdateNotification(player);
            }
        }

        hasNotifiedThisSession = true;
    }

    /**
     * Sends update notification to a specific player
     */
    public void sendUpdateNotification(Player player) {
        String downloadUrl = getDownloadUrl();

        // Animated notification with fancy formatting
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage(Component.empty());
            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));

            // Flash effect
            Component title = Component.text("  ✨ ", NamedTextColor.YELLOW)
                    .append(Component.text("CustomRecipes Update Available!", NamedTextColor.GOLD)
                            .decoration(TextDecoration.BOLD, true))
                    .append(Component.text(" ✨", NamedTextColor.YELLOW));

            player.sendMessage(title);
            player.sendMessage(Component.empty());

            player.sendMessage(Component.text("  Current Version: ", NamedTextColor.GRAY)
                    .append(Component.text(currentVersion, NamedTextColor.RED)));

            player.sendMessage(Component.text("  Latest Version: ", NamedTextColor.GRAY)
                    .append(Component.text(latestVersion, NamedTextColor.GREEN)
                            .decoration(TextDecoration.BOLD, true)));

            player.sendMessage(Component.empty());

            Component downloadButton = Component.text("  [", NamedTextColor.DARK_GRAY)
                    .append(Component.text("Download Update", NamedTextColor.AQUA)
                            .decoration(TextDecoration.BOLD, true))
                    .append(Component.text("]", NamedTextColor.DARK_GRAY))
                    .clickEvent(ClickEvent.openUrl(downloadUrl))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Click to open download page", NamedTextColor.YELLOW)
                    ));

            Component changelogButton = Component.text("  [", NamedTextColor.DARK_GRAY)
                    .append(Component.text("View Changelog", NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.BOLD, true))
                    .append(Component.text("]", NamedTextColor.DARK_GRAY))
                    .clickEvent(ClickEvent.openUrl(getChangelogUrl()))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Click to view what's new", NamedTextColor.YELLOW)
                    ));

            player.sendMessage(downloadButton);
            player.sendMessage(changelogButton);

            player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
            player.sendMessage(Component.empty());

            // Play a sound notification if available
            try {
                player.playSound(player.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                        0.5f, 1.0f);
            } catch (Exception ignored) {
                // Sound might not be available in all versions
            }
        }, 10L);
    }

    /**
     * Gets the download URL for the latest version
     */
    public String getDownloadUrl() {
        if (source == UpdateSource.SPIGOT) {
            return "https://www.spigotmc.org/resources/" + resourceId + "/";
        } else if (source == UpdateSource.GITHUB) {
            return "https://github.com/" + githubRepo + "/releases/latest";
        }
        return null;
    }

    /**
     * Gets the changelog URL
     */
    public String getChangelogUrl() {
        if (source == UpdateSource.GITHUB) {
            return "https://github.com/" + githubRepo + "/releases/tag/v" + latestVersion;
        }
        return getDownloadUrl();
    }

    // Getters
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
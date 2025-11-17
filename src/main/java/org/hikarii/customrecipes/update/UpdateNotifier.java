package org.hikarii.customrecipes.update;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.hikarii.customrecipes.CustomRecipes;

public class UpdateNotifier implements Listener {
    private final CustomRecipes plugin;
    private final UpdateChecker updateChecker;

    public UpdateNotifier(CustomRecipes plugin, UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("customrecipes.update.notify")) {
            return;
        }

        if (updateChecker.isUpdateAvailable()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updateChecker.sendUpdateNotification(player);
            }, 40L);
        }
    }

    private void sendUpdateNotification(Player player) {
        String currentVersion = plugin.getDescription().getVersion();
        String latestVersion = updateChecker.getLatestVersion();
        String downloadUrl = updateChecker.getDownloadUrl();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));

        player.sendMessage(Component.text("  CustomRecipes Update Available!", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));

        player.sendMessage(Component.empty());

        player.sendMessage(Component.text("  Current Version: ", NamedTextColor.GRAY)
                .append(Component.text(currentVersion, NamedTextColor.RED)));

        player.sendMessage(Component.text("  Latest Version: ", NamedTextColor.GRAY)
                .append(Component.text(latestVersion, NamedTextColor.GREEN)));

        player.sendMessage(Component.empty());

        Component downloadButton = Component.text("  [Download Update]", NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.openUrl(downloadUrl))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open download page", NamedTextColor.YELLOW)));

        player.sendMessage(downloadButton);

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
    }
}
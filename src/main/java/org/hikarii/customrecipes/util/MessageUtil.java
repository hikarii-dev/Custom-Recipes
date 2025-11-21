package org.hikarii.customrecipes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hikarii.customrecipes.CustomRecipes;

public class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#00D4FF:#7B2CBF>[CustomRecipes]</gradient> ";
    private static CustomRecipes plugin;

    public static void setPlugin(CustomRecipes pluginInstance) {
        plugin = pluginInstance;
    }

    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<gray>" + message));
    }

    public static void sendAdminInfo(CommandSender sender, String message) {
        if (plugin != null && sender instanceof Player) {
            boolean notificationsEnabled = plugin.getConfig().getBoolean("admin-notifications", true);
            if (!notificationsEnabled) {
                return;
            }
        }
        sender.sendMessage(colorize(PREFIX + "<gray>" + message));
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<green>" + message));
    }

    public static void sendAdminSuccess(CommandSender sender, String message) {
        if (plugin != null && sender instanceof Player) {
            boolean notificationsEnabled = plugin.getConfig().getBoolean("admin-notifications", true);
            if (!notificationsEnabled) {
                return;
            }
        }
        sender.sendMessage(colorize(PREFIX + "<green>" + message));
    }

    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<yellow>" + message));
    }

    public static void sendAdminWarning(CommandSender sender, String message) {
        if (plugin != null && sender instanceof Player) {
            boolean notificationsEnabled = plugin.getConfig().getBoolean("admin-notifications", true);
            if (!notificationsEnabled) {
                return;
            }
        }
        sender.sendMessage(colorize(PREFIX + "<yellow>" + message));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<red>" + message));
    }

    public static void send(CommandSender sender, String message, TextColor color) {
        Component component = Component.text(message, color);
        sender.sendMessage(colorize(PREFIX).append(component));
    }

    public static String formatMaterialName(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return "";
        }

        String[] words = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static Component createMenuTitle(String text, NamedTextColor color) {
        return Component.text(text, color)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static Component createGradientMenuTitle(String text) {
        return colorize("<gradient:#00D4FF:#7B2CBF>" + text + "</gradient>")
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false);
    }
}
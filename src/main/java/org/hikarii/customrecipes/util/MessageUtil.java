package org.hikarii.customrecipes.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Utility class for handling messages and text formatting
 */
public class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#00D4FF:#7B2CBF>[CustomRecipes]</gradient> ";

    /**
     * Colorizes text using MiniMessage format
     *
     * @param text the text to colorize
     * @return the colorized component
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Sends an info message to a sender
     *
     * @param sender the command sender
     * @param message the message
     */
    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<gray>" + message));
    }

    /**
     * Sends a success message to a sender
     *
     * @param sender the command sender
     * @param message the message
     */
    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<green>" + message));
    }

    /**
     * Sends a warning message to a sender
     *
     * @param sender the command sender
     * @param message the message
     */
    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<yellow>" + message));
    }

    /**
     * Sends an error message to a sender
     *
     * @param sender the command sender
     * @param message the message
     */
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(colorize(PREFIX + "<red>" + message));
    }

    /**
     * Sends a message with custom color to a sender
     *
     * @param sender the command sender
     * @param message the message
     * @param color the color
     */
    public static void send(CommandSender sender, String message, TextColor color) {
        Component component = Component.text(message, color);
        sender.sendMessage(colorize(PREFIX).append(component));
    }

    /**
     * Creates a clickable command component
     *
     * @param text the text to display
     * @param command the command to run
     * @param hoverText the hover text
     * @return the component
     */
    public static Component clickableCommand(String text, String command, String hoverText) {
        return Component.text(text, NamedTextColor.AQUA)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(Component.text(hoverText)));
    }

    /**
     * Formats a material name to be more readable
     *
     * @param materialName the material name (e.g., "DIAMOND_SWORD")
     * @return formatted name (e.g., "Diamond Sword")
     */
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
}
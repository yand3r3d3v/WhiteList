package me.thealeksbl4ckr.aWL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Utils {

    public static void log(String text) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static String msg(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}

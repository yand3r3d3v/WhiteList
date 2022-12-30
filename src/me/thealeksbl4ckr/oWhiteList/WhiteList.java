package me.thealeksbl4ckr.oWhiteList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WhiteList implements CommandExecutor {

    public static Main plugin;

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args){
        if(commandLabel.equalsIgnoreCase("whitelist")) {
            if (args.length <= 1) return false;
            if (!sender.isOp() || !sender.hasPermission("whitelist.*") || !sender.hasPermission("*")) return false;

            String command = args[0];
            switch (command) {
                case "add":
                    if (args.length != 2) sender.sendMessage(getString("add"));

                    if (sender.getServer().getPlayer(args[1]) != null) {
                        DataBase.addWhitelistOnline(sender.getServer().getPlayer(args[1]), sender);
                        DataBase.users.add(args[1]);
                    } else {
                        DataBase.users.add(args[1]);
                        DataBase.addWhitelistOffline(args[1], sender);
                    }
                case "remove":
                case "del":
                    if (args.length != 2) sender.sendMessage(getString("msg.del"));
                    if (sender.getServer().getPlayer(args[1]) != null) {
                        DataBase.removePlayerWhitelistOnline(sender.getServer().getPlayer(args[1]), sender);
                        DataBase.users.remove(args[1]);
                    } else {
                        DataBase.users.remove(args[1]);
                        DataBase.removePlayerWhitelistOffline(args[1], sender);
                    }
                case "reload":
                    plugin.reloadConfig();
                    sender.sendMessage(getString("msg.reload"));
                case "enable":
                case "on":
                    plugin.getConfig().set("enabled", true);
                    plugin.reloadConfig();
                    sender.sendMessage(getString("msg.enable"));
                case "disable":
                case "off":
                    plugin.getConfig().set("enabled", false);
                    plugin.reloadConfig();
                    sender.sendMessage(getString("msg.disable"));


                default:
                    String status = plugin.getConfig().getString("enabled").equals("true") ? "&aВключен" : "&cВыключен";
                    sender.sendMessage(Utils.msg(String.format("&caWL &8| &fСтатус белого списка: %s", status)));
                    sender.sendMessage(Utils.msg(String.format("&caWL &8| &fИгроки &a%s &6%s", DataBase.users.size(), DataBase.users)));
            }
        }
        return false;
    }

    private String getString(String name) {
        return Utils.msg(plugin.getConfig().getString(name));
    }
}

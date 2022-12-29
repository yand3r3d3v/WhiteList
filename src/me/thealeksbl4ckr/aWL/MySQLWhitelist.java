package me.thealeksbl4ckr.aWL;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class MySQLWhitelist extends JavaPlugin implements Listener {

    public static MySQLWhitelist plugin;
    public static Connection connection;


    @Override
    public void onEnable(){
        Utils.log("");
        Utils.log("&9▄▀▄&f █░░ ▄▀▄ ▄▀▄ █▀▄ █▀▀ █▀▀▄");
        Utils.log("&9█░█&f █░▄ █░█ █▀█ █░█ █▀▀ █▐█▀ &9" + this.getName() + " &fv" + this.getDescription().getVersion());
        Utils.log("&9░▀░&f ▀▀▀ ░▀░ ▀░▀ ▀▀░ ▀▀▀ ▀░▀▀ &fRunning on &a" + this.getServer().getBukkitVersion() + " &8- &e" + this.getServer().getName());
        Utils.log("");
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

        DataBase.openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + getConfig().getString("table") + "` (`UUID` varchar(100), `user` varchar(100)) ;");
            sql.execute();

            DataBase.getUsers();
            Utils.log("&8| &fИгроков загружено &a" + DataBase.users.size());
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            DataBase.closeConnection();
        }
    }

    @Override
    public void onDisable(){
        try {
            if(connection == null && !connection.isClosed()){
                connection.close();
                DataBase.users.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args){
        if(commandLabel.equalsIgnoreCase("awl") || commandLabel.equalsIgnoreCase("whitelist")) {
            if (args.length <= 1) return false;
            if (!sender.isOp() || !sender.hasPermission("whitelist.*")) return false;

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
                                        DataBase.delWhitelistOnline(sender.getServer().getPlayer(args[1]), sender);
                                        DataBase.users.remove(args[1]);
                                    } else {
                                        DataBase.users.remove(args[1]);
                                        DataBase.delWhitelistOffline(args[1], sender);
                                    }
                        case "reload":
                            this.reloadConfig();
                            sender.sendMessage(getString("msg.reload"));
                        case "enable":
                        case "on":
                                this.getConfig().set("enabled", true);
                                this.reloadConfig();
                                sender.sendMessage(getString("msg.enable"));
                        case "disable":
                        case "off":
                                this.getConfig().set("enabled", false);
                                this.reloadConfig();
                                sender.sendMessage(getString("msg.disable"));


                        default:
                            String status = this.getConfig().getString("enabled").equals("true") ? "&aВключен" : "&cВыключен";
                            sender.sendMessage(Utils.msg(String.format("&caWL &8| &fСтатус белого списка: %s", status)));
                            sender.sendMessage(Utils.msg(String.format("&caWL &8| &fИгроки &a%s &6%s", DataBase.users.size(), DataBase.users)));
                    }
        }
                return false;
    }

    private String getString(String name) {
        return Utils.msg(this.getConfig().getString(name));
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        if(!DataBase.isWhitelisted(player)){
            if(this.getConfig().getBoolean("enabled")){
                event.setKickMessage(this.getConfig().getString("msg.kick").replace("&", "§"));
                event.setResult(Result.KICK_WHITELIST);
            }
        }

    }
}
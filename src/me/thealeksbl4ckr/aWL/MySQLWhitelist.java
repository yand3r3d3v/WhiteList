package me.thealeksbl4ckr.aWL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.leonhard.storage.Json;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class MySQLWhitelist extends JavaPlugin implements Listener {

    public static MySQLWhitelist plugin;
    public static Connection connection;
    List<String> users = new ArrayList<>();


    @Override
    public void onEnable(){
        Utils.log("");
        Utils.log("&9▄▀▄&f █░░ ▄▀▄ ▄▀▄ █▀▄ █▀▀ █▀▀▄");
        Utils.log("&9█░█&f █░▄ █░█ █▀█ █░█ █▀▀ █▐█▀ &9" + this.getName() + " &fv" + this.getDescription().getVersion());
        Utils.log("&9░▀░&f ▀▀▀ ░▀░ ▀░▀ ▀▀░ ▀▀▀ ▀░▀▀ &fRunning on &a" + this.getServer().getBukkitVersion() + " &8- &e" + this.getServer().getName());
        Utils.log("");
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + getConfig().getString("table") + "` (`UUID` varchar(100), `user` varchar(100)) ;");
            sql.execute();

            getUsers();
            Utils.log("&8| &fИгроков загружено &a" + users.size());
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            closeConnection();
        }
    }

    @Override
    public void onDisable(){
        try {
            if(connection == null && !connection.isClosed()){
                connection.close();
                users.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getUsers() {
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + getConfig().getString("table"));
            ResultSet rs = sql.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("user"));
            }
            rs.close();
            sql.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args){
        if(commandLabel.equalsIgnoreCase("awl") || commandLabel.equalsIgnoreCase("whitelist")){
            if(args.length >= 1){
                if(args[0].equalsIgnoreCase("list")) {
                    if(sender.hasPermission("awl.list") || sender.isOp() || sender.hasPermission("aWL.*")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&caWL &8| &fИгроки &a" + users.size() +  " &6" + users));
                    }
                }
                else if(args[0].equalsIgnoreCase("add")){
                    if(sender.hasPermission("aWL.del") || sender.isOp() || sender.hasPermission("aWL.*")){
                        if(args.length == 2){
                            if(sender.getServer().getPlayer(args[1]) != null){
                                addWhitelistOnline(sender.getServer().getPlayer(args[1]), sender);
                                users.add(args[1]);
                            }else{
                                users.add(args[1]);
                                addWhitelistOffline(args[1], sender);
                            }
                        }else{
                            sender.sendMessage(this.getConfig().getString("msg.add").replace("&", "§"));
                        }
                    }else{
                        sender.sendMessage(this.getConfig().getString("msg.perm").replace("&", "§"));
                    }
                }else if(args[0].equalsIgnoreCase("del") || commandLabel.equalsIgnoreCase("remove")){
                    if(sender.hasPermission("aWL.del") || sender.isOp() || sender.hasPermission("aWL.*")){
                        if(args.length == 2){
                            if(sender.getServer().getPlayer(args[1]) != null){
                                delWhitelistOnline(sender.getServer().getPlayer(args[1]), sender);
                                users.remove(args[1]);
                            }else{
                                users.remove(args[1]);
                                delWhitelistOffline(args[1], sender);
                            }
                        }else{
                            sender.sendMessage(this.getConfig().getString("msg.del").replace("&", "§"));
                        }
                    }else{
                        sender.sendMessage(this.getConfig().getString("msg.perm").replace("&", "§"));
                    }
                }else if(args[0].equalsIgnoreCase("reload")){
                    if(sender.hasPermission("aWL.reload") || sender.isOp() || sender.hasPermission("aWL.*")){
                        this.reloadConfig();
                        sender.sendMessage(this.getConfig().getString("msg.reloadwl").replace("&", "§"));
                    }else{
                        sender.sendMessage(this.getConfig().getString("msg.perm").replace("&", "§"));
                    }
                }else if(args[0].equalsIgnoreCase("on")){
                    if(sender.hasPermission("aWL.enable") || sender.isOp() || sender.hasPermission("aWL.*")){
                        this.getConfig().set("enabled", true);
                        sender.sendMessage(this.getConfig().getString("msg.onwl").replace("&", "§"));
                    }else{
                        sender.sendMessage(this.getConfig().getString("msg.perm").replace("&", "§"));
                    }
                }else if(args[0].equalsIgnoreCase("off")){
                    if(sender.hasPermission("aWL.disable") || sender.isOp() || sender.hasPermission("aWL.*")){
                        this.getConfig().set("enabled", false);
                        sender.sendMessage(this.getConfig().getString("msg.offwl").replace("&", "§"));
                    }else{
                        sender.sendMessage(this.getConfig().getString("msg.perm").replace("&", "§"));
                    }
                }else{
                    sender.sendMessage(this.getConfig().getString("msg.usage").replace("&", "§"));
                }
            }else{
                sender.sendMessage(this.getConfig().getString("msg.usage").replace("&", "§"));
            }
            return true;
        }
        return false;
    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        if(!isWhitelisted(player)){
            if(this.getConfig().getBoolean("enabled")){
                event.setKickMessage(this.getConfig().getString("msg.kick").replace("&", "§"));
                event.setResult(Result.KICK_WHITELIST);
            }
        }

    }


    public synchronized void openConnection(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://"+ getConfig().getString("host") +":" + getConfig().getString("port") + "/"+ getConfig().getString("database") +"", ""+ getConfig().getString("user") +"", ""+ getConfig().getString("password") +"");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized static void closeConnection(){
        try{
            connection.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isWhitelisted(Player player){
        if(users.contains(player.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public void addWhitelistOnline(Player player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + getConfig().getString("table") + "` WHERE `UUID`=?;");
            UUID uuid = player.getUniqueId();
            sql.setString(1, uuid.toString());
            ResultSet rs = sql.executeQuery();
            if(!rs.next()){
                PreparedStatement sql1 = connection.prepareStatement("INSERT INTO `" + getConfig().getString("table") + "` (`UUID`, `user`) VALUES (?,?);");
                sql1.setString(1, uuid.toString());
                sql1.setString(2, player.getName());
                sql1.execute();
                sql1.close();
            }rs.close();
            sql.close();
            sender.sendMessage(this.getConfig().getString("msg.addwl").replace("%player%", player.getDisplayName()).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public void addWhitelistOffline(String player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + getConfig().getString("table") + "` WHERE `user`=?;");
            sql.setString(1, player);
            ResultSet rs = sql.executeQuery();
            if(!rs.next()){
                PreparedStatement sql1 = connection.prepareStatement("INSERT INTO `" + getConfig().getString("table") + "` (`user`) VALUES (?);");
                sql1.setString(1, player);
                sql1.execute();
                sql1.close();
            }rs.close();
            sql.close();
            sender.sendMessage(this.getConfig().getString("msg.addwl").replace("%player%", player).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public void delWhitelistOnline(Player player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("DELETE FROM `" + getConfig().getString("table") + "` WHERE `UUID`=?;");
            UUID uuid = player.getUniqueId();
            sql.setString(1, uuid.toString());
            sql.execute();
            sql.close();
            sender.sendMessage(this.getConfig().getString("msg.delwl").replace("%player%", player.getDisplayName()).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public void delWhitelistOffline(String player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("DELETE FROM `" + getConfig().getString("table") + "` WHERE `user`=?;");
            sql.setString(1, player);
            sql.execute();
            sql.close();
            sender.sendMessage(this.getConfig().getString("msg.delwl").replace("%player%", player).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
}
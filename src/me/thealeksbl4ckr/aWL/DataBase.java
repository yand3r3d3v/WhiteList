package me.thealeksbl4ckr.aWL;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataBase {

    public static MySQLWhitelist plugin;
    public static Connection connection;
    public static List<String> users = new ArrayList<>();

    public synchronized static void openConnection(){
        try{
            connection = DriverManager.getConnection("jdbc:mysql://"+ plugin.getConfig().getString("host") +":" + plugin.getConfig().getString("port") + "/"+ plugin.getConfig().getString("database") +"", ""+ plugin.getConfig().getString("user") +"", ""+ plugin.getConfig().getString("password") +"");
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

    public static boolean isWhitelisted(Player player){
        if(users.contains(player.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public static void addWhitelistOnline(Player player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + plugin.getConfig().getString("table") + "` WHERE `UUID`=?;");
            UUID uuid = player.getUniqueId();
            sql.setString(1, uuid.toString());
            ResultSet rs = sql.executeQuery();
            if(!rs.next()){
                PreparedStatement sql1 = connection.prepareStatement("INSERT INTO `" + plugin.getConfig().getString("table") + "` (`UUID`, `user`) VALUES (?,?);");
                sql1.setString(1, uuid.toString());
                sql1.setString(2, player.getName());
                sql1.execute();
                sql1.close();
            }rs.close();
            sql.close();
            sender.sendMessage(plugin.getConfig().getString("msg.addtowhitelist").replace("%player%", player.getDisplayName()).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public static void addWhitelistOffline(String player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + plugin.getConfig().getString("table") + "` WHERE `user`=?;");
            sql.setString(1, player);
            ResultSet rs = sql.executeQuery();
            if(!rs.next()){
                PreparedStatement sql1 = connection.prepareStatement("INSERT INTO `" + plugin.getConfig().getString("table") + "` (`user`) VALUES (?);");
                sql1.setString(1, player);
                sql1.execute();
                sql1.close();
            }rs.close();
            sql.close();
            sender.sendMessage(plugin.getConfig().getString("msg.addwl").replace("%player%", player).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public static void delWhitelistOnline(Player player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("DELETE FROM `" + plugin.getConfig().getString("table") + "` WHERE `UUID`=?;");
            UUID uuid = player.getUniqueId();
            sql.setString(1, uuid.toString());
            sql.execute();
            sql.close();
            sender.sendMessage(plugin.getConfig().getString("msg.delwl").replace("%player%", player.getDisplayName()).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }
    public static void delWhitelistOffline(String player, CommandSender sender){
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("DELETE FROM `" + plugin.getConfig().getString("table") + "` WHERE `user`=?;");
            sql.setString(1, player);
            sql.execute();
            sql.close();
            sender.sendMessage(plugin.getConfig().getString("msg.delwl").replace("%player%", player).replace("&", "§"));
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            closeConnection();
        }
    }


    public static List<String> getUsers() {
        openConnection();
        try{
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM `" + plugin.getConfig().getString("table"));
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
}

package me.thealeksbl4ckr.oWhiteList;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable(){
        Utils.log("");
        Utils.log("&9▄▀▄&f █░░ ▄▀▄ ▄▀▄ █▀▄ █▀▀ █▀▀▄");
        Utils.log("&9█░█&f █░▄ █░█ █▀█ █░█ █▀▀ █▐█▀ &9" + this.getName() + " &fv" + this.getDescription().getVersion());
        Utils.log("&9░▀░&f ▀▀▀ ░▀░ ▀░▀ ▀▀░ ▀▀▀ ▀░▀▀ &fRunning on &a" + this.getServer().getBukkitVersion() + " &8- &e" + this.getServer().getName());
        Utils.log("");


        if(!this.getConfig().getBoolean("enabled")) {
            this.getPluginLoader().disablePlugin(this);
            Utils.log("&6WHITELIST &8| &fПлагин был &7отключен&f, белый список не включен");
            Utils.log("&6WHITELIST &8| &fВключить белый список, вы можете в конфигурации плагина.");
        }
        getServer().getPluginCommand("whitelist").setExecutor(new WhiteList());
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

        DataBase.openConnection();
        DataBase.createDatabase();
    }

    @Override
    public void onDisable() {
       DataBase.closeDatabase();
    }
}
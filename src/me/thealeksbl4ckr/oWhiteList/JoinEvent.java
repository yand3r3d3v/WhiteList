package me.thealeksbl4ckr.oWhiteList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinEvent implements Listener {

    public static Main plugin;

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        if(plugin.getConfig().getBoolean("enabled")){
            if(!DataBase.isWhitelisted(player)){
                event.setKickMessage(Utils.msg(plugin.getConfig().getString("msg.kick")));
                event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
            }
        }

    }

}

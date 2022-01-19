package com.factionkingdom.factionwars.Util;

import com.factionkingdom.factionwars.FactionWars;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    public SoundUtil(FactionWars fw){

    }
    public static void countdown(){
        //need to adjust for players that are in the arena / recently warped to spectator?
        //or maybe not
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers())
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_LEVER_CLICK, 2.0F, 1.0F);
    }
    public static void start(){

    }
    public static void end(){

    }
}

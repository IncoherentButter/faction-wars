package com.factionkingdom.factionwars.Managers;

import com.factionkingdom.factionwars.FactionWars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private static FileConfiguration config;

    public static void setupConfig(FactionWars fw){
        ConfigManager.config = fw.getConfig();
        //fw.saveDefaultConfig(); //saves default config if no other changes have been made
    }

    public int getCountdownSeconds(){return config.getInt("countdown-seconds");}
    public int getRequiredFactions(){return config.getInt("required-factions");}
    public int getPlayersPerFaction(){return config.getInt("players-per-faction");}
    public boolean getDefaultKit(){return config.getBoolean("default-kit");}
    public boolean getInTheRing(){return config.getBoolean("in-the-ring");}
//    public static Location getSpectatorSpawn(String arenaName){
//        return new Location(
//                Bukkit.getWorld(config.getString("arenas." + arenaName + ".world")),
//                config.getDouble("arenas." + arenaName + ".x"),
//                config.getDouble("arenas." + arenaName + ".y"),
//                config.getDouble("arenas." + arenaName + ".z"),
//                (float)config.getDouble("arenas." + arenaName + ".yaw"),
//                (float)config.getDouble("arenas." + arenaName + ".pitch"));
//    }
//    public static int getRequiredTeams(String arenaName){
//        return config.getInt("arena." + arenaName + ".required-factions");
//    }
//
//    public void setRequiredFactions(String arenaName, int numFacs){
//        config.set("arenas." + arenaName + ".required-factions", numFacs);
//    }
//    public void setSpectatorSpawn(String arenaName, Location loc){
//        World world = loc.getWorld();
//        double x = loc.getX();
//        double y = loc.getY();
//        double z = loc.getZ();
//        float yaw = loc.getYaw();
//        float pitch = loc.getPitch();
//        config.set("arenas." + arenaName + ".world", world);
//        config.set("arenas." + arenaName + ".x", x);
//        config.set("arenas." + arenaName + ".y", y);
//        config.set("arenas." + arenaName + ".z", z);
//        config.set("arenas." + arenaName + ".yaw", yaw);
//        config.set("arenas." + arenaName + ".pitch", pitch);
//    }


}

package com.factionkingdom.factionwars.Managers;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Instances.Arena;
import com.factionkingdom.factionwars.Util.MessageUtil;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
//import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaManager {

    private FactionWars fw;
    private List<Arena> arenas = new ArrayList<>();
    private List<String> arenaNames = new ArrayList<>();

    private File arenaData;
    private FileConfiguration arenaConfig;

    private List<Arena> liveArenas = new ArrayList<>();
    private List<UUID> playersInArenas = new ArrayList<>();


    private MessageUtil msgUtil;



    public ArenaManager(FactionWars fw){
        /*
        This class manages the list of arenas
        - registers new arenas
        - gets arenas based on name and players

         */
        this.fw = fw;
        FileConfiguration config = fw.getConfig();


        arenaData = fw.getArenaDataFile();
        arenaConfig = fw.getArenaConfig();

        msgUtil = fw.getMessageUtil();


        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("ArenaManager: config list of names in ArenaManager:");
        for (String name: arenaConfig.getStringList("arena_names")){
            stringBuilder.append("&3" + name + "\n");
            System.out.println("$$$" + name + "$$$");
        }
        String list = stringBuilder.toString();
        System.out.println("Arena Mananger list: " + list);

        for (String str: config.getConfigurationSection("arenas.").getKeys(false)){
            arenas.add(new Arena(fw, str));
        }

        System.out.println("ArenaManager.constructor: ran for (String str: config.getConfigurationSection(\"arenas.\").getKeys(false)){\n" +
                "            arenas.add(new Arena(fw, str));\n" +
                "        }");
    }

    public void define(Player p, String name){
        /** Defines the parameters for the arena.
         * Necessary parameters:
         *   - worldguard region
         *   - kit or not?
         *   - do players lose if they leave the region?
         *   - if not, should fighters be allowed to leave the region?
         */
        //loads the arenaConfig from the arenaData.yml
        arenaConfig = YamlConfiguration.loadConfiguration(arenaData);

            //the below section handles the creation of the WG region
//            ProtectedRegion region = new ProtectedRegion(name, false); //transientRegion: if true, the region should only be kept in memory, should not be saved
//
//            BukkitWorld bukkitWorld = new BukkitWorld(p.getWorld());
//            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//            RegionManager regions = container.get(bukkitWorld);
//
//            regions.addRegion(region);
//        }

//        if (!this.dataFile.contains("arenas." + name) && !PlayerHandler.creationMode().contains(player.getUniqueId())) {
//            this.dataFile.save(this.fileData);
//        } else if (PlayerHandler.creationMode().contains(player.getUniqueId())) {
//            this.messageUtil.messageLanguage((CommandSender)player, "creation_already");
//        } else {
//            this.messageUtil.messageLanguage((CommandSender)player, "arena_already_exists");
//        }
    }

    public void undefine(Player p, String name){
        arenaConfig = YamlConfiguration.loadConfiguration(this.arenaData);
        arenaNames = arenaConfig.getStringList("arena_names");

        //make sure the input is 1) actually an arena, and 2) not currently in progress
        if (!liveArenas.contains(name) && arenaNames.contains(name)){
            arenaNames.remove(name);

            arenaConfig.set("arenas." + name, null);
            arenaConfig.set("arena_names", arenaNames);
            try {
                arenaConfig.save(arenaData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            msgUtil.messageLanguageArena(p, "arena_delete", name);

            //handles the deletion of the region from WG if it exists
//            BukkitWorld bukkitWorld = new BukkitWorld(p.getWorld());
//            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//            RegionManager regions = container.get(bukkitWorld);
//            if (regions.hasRegion(name))
//                regions.removeRegion(name);
        } else if (liveArenas.contains(name)){
            msgUtil.messageLanguage(p, "arena_delete_live");
        } else{
            msgUtil.messageLanguage(p, "arena_delete_absent");
        }
    }

    public List<Arena> getArenas(){return arenas;}
    public List<String> getArenaNames(){return arenaNames;}

    public void addArena(Player p, String arenaName) {
        /**
         * Checks if an arena is already in the config. If not, adds it
         */
        System.out.println("ArenaManager.addArena(): ADDED ARENA " + arenaName + " to arenas and arenaNames");
        arenaData = fw.getArenaDataFile();
        arenaConfig = YamlConfiguration.loadConfiguration(arenaData);
        if (arenaConfig.contains("arena_names." + arenaName)){
            msgUtil.messageLanguage(p, "created_already");
        } else{
            System.out.println("ArenaManager.addArena(): input arena not in config, adding to config");

            Arena newArena = new Arena(fw, arenaName);
            System.out.println("ArenaManager.addArena(): initialized new arena");
            arenas.add(newArena);
            System.out.println("ArenaManager.addArena(): added new arena to ArenaManager.arenas, List<Arena> = " + arenas);

            //If arenaName hasn't been added yet, add it to list
            arenaNames = arenaConfig.getStringList("arena_names");
            System.out.println("ArenaManager.addArena(): got arenaConfig list of arenas: = " + arenaNames);
            arenaNames.add(arenaName);
            System.out.println("ArenaManager.addArena(): added new arena to arenaNames: " + arenaNames);
//            fw.setArenaConfig(arenaConfig, arenaData);
            //overwrite the config arena name list
            arenaConfig.set("arena_names", arenaNames);

            fw.saveArenaConfig(arenaConfig, arenaData);

            System.out.println("ArenaManager.addArena(): looping through all the arena names currently in config");
            for (String name : arenaConfig.getStringList("arena_names")){
                System.out.println("***" + name + "***");
            }

            //create the new arena and add it to Arena List
        }

    }


    public Arena getArena(Player p){
        for (Arena arena : getArenas()){
            if (arena.getFighters() != null && arena.getFighters().contains(p.getUniqueId())){
                return arena;
            }
        }
        return null;
    }
    public Arena getArena(String arenaName){
        System.out.println("arena.getArena(): ARENAS IN ARENA MANAGER: " + arenaNames);
        for (Arena arena : getArenas()){
            if (arena.getArenaName() != null && arena.getArenaName().equalsIgnoreCase(arenaName)){
                System.out.println("arena.getArena(): returned arena: > " + arenaName);
                return arena;
            }
        }
        return null;
    }
    public Arena getArena(Faction faction){
        for (Arena arena : getArenas()){
            if (arena.getFactions() != null && arena.getFactions().contains(faction)){
                return arena;
            }
        }
        return null;
    }

    public File getArenaData(){return arenaData;}



//    @Override
//    public boolean isPhysicalArea() {
//        return false;
//    }
//
//    @Override
//    public List<BlockVector2> getPoints() {
//        return null;
//    }
//
//    @Override
//    public int volume() {
//        return 0;
//    }
//
//    @Override
//    public boolean contains(BlockVector3 pt) {
//        return false;
//    }
//
//    @Override
//    public RegionType getType() {
//        return null;
//    }
}

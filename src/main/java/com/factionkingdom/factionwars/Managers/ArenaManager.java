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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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


        fw.saveArenaConfig(arenaConfig, arenaData);
        loadConfig();

    }

    public void loadConfig(){

        if (arenaConfig.isConfigurationSection("arena_names")){
            Set<String> arenaNamesInConfig = arenaConfig.getConfigurationSection("arena_names").getKeys(false);

            for (String arenaName: arenaNamesInConfig){
                Set<String> arenaInfo = arenaConfig.getConfigurationSection("arena_names." + arenaName).getKeys(false);
                for (String name : arenaInfo){
                    System.out.println("###" + name + "###");
                }
                arenaNames.add(arenaName);
                Arena arena = new Arena(fw, arenaName, false);
                arenas.add(arena);
            }
        }
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
        arenaData = fw.getArenaDataFile();
        arenaConfig = fw.getArenaConfig();
        if (arenaConfig.contains("arena_names." + arenaName)){
            /*
            If this arena already exists, don't make it.
             */
            msgUtil.messageLanguage(p, "created_already");
        }
        else{
            /*
            Otherwise, make it
             */
            Arena newArena = new Arena(fw, arenaName, true);

            arenas.add(newArena);
            arenaNames.add(arenaName);

            fw.saveArenaConfig(arenaConfig, arenaData);

        }

    }
    public void removeArena(Player p, Arena arena){
        String arenaName = arena.getArenaName();
        arenaData = fw.getArenaDataFile();
        arenaConfig = fw.getArenaConfig();
        System.out.println("Checking for " + arenaConfig.getConfigurationSection("arena_names").getKeys(false));
        if(!arenaConfig.getConfigurationSection("arena_names").getKeys(false).contains(arenaName)){
            /*
            If this arena isn't in the arena list, there is nothing to remove
             */
            msgUtil.messageLanguageArena(p, "correct_arena_not_exist", arenaName);
        }
        else{
            /*
            If this arena isn't being used, then delete it
             */
            String cur_state = arenaConfig.getString("arena_names." + arenaName +".game-state");
            if (!cur_state.equalsIgnoreCase(GameState.DORMANT.getDisplay())){
                msgUtil.messageLanguageArena(p, "&cYou must wait until &7%arena%&c isn't in use!", arenaName);
            }
            else{
                //Handle removing from the config itself
                arenaConfig.getConfigurationSection("arena_names").set(arenaName, null);
                fw.saveArenaConfig(arenaConfig, arenaData);

                //Remove from the ArenaManager
                arenas.remove(arena);
                arenaNames.remove(arenaName);

                //Send message notification
                msgUtil.messageLanguageArena(p, "arena_delete", arenaName);

            }
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

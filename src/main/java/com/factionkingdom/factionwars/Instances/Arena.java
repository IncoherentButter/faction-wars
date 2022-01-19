package com.factionkingdom.factionwars.Instances;

import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Managers.ConfigManager;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Arena {
    private FactionWars fw;

    private File arenaData;
    private FileConfiguration arenaConfig;

    private Countdown countdown;
    private Game game;

    //Name, main spawn
    private String arenaName;
    private Location spectatorSpawn;

    //Is kit same for all?
    //How many factions required?
    //Players per faction?
    private boolean defaultKit;
    private boolean inTheRing;
    private int requiredFactions;
    private int playersPerFaction;
    private int countdownSeconds;

    private GameState state;
    private HashMap<Faction, List<UUID>> roster = new HashMap<>();
    private List<Faction> factions;
    private List<UUID> fighters;
    private HashMap<Faction, Integer> facNumbers = new HashMap<>();
    private HashMap<Integer, Location> factionSpawns = new HashMap<>();


    //Kit
    private ItemStack[] kit_items;
    private ItemStack[] kit_armor;
    private HashMap<UUID, ItemStack[]> itemLocker;
    private HashMap<UUID, ItemStack[]> armorLocker;


    //Combat spawnpoints, fighters, factions,
    //Gamestate

    private List<Integer> numTeams = new ArrayList<>();
    private Iterator<Integer> iter = numTeams.iterator();


    public Arena(FactionWars fw, String arenaName){
        this.fw = fw;

        arenaData = fw.getArenaDataFile();

        this.arenaName = arenaName;
        setRequiredFactions(2);
        setPlayersPerFaction(2);
        setCountdownSeconds(180);

        this.countdown = new Countdown(fw, this);

        createIterator();

        /*
        Default settings
         */
        setState(GameState.DORMANT); //default to DORMANT
        setDefaultKit(true);
        setInTheRing(false);

        setupConfig();
    }

    public void setupConfig(){
        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".countdown-seconds", this.countdownSeconds);
        arenaConfig.set("arena_names." + arenaName + ".game-state", this.state.getDisplay());
        arenaConfig.set("arena_names." + arenaName + ".required-factions", this.requiredFactions);
        arenaConfig.set("arena_names." + arenaName + ".players-per-faction", this.playersPerFaction);
        arenaConfig.set("arena_names." + arenaName + ".is-defualt-kit", this.defaultKit);
        arenaConfig.set("arena_names." + arenaName + ".is-in-the-ring", this.inTheRing);
        arenaConfig.set("arena_names." + arenaName + ".spectator-spawn.world", "");
        arenaConfig.set("arena_names." + arenaName + ".spectator-spawn.x", "");
        arenaConfig.set("arena_names." + arenaName + ".spectator-spawn.y", "");
        arenaConfig.set("arena_names." + arenaName + ".spectator-spawn.z", "");
        arenaConfig.set("arena_names." + arenaName + ".spawns", "");
        System.out.println("Arena.setupConfig(): set up config for arena " + arenaName);
        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void createIterator(){
        for (int i = 1; i < requiredFactions + 1; i ++) numTeams.add(i);
    }

    public void start(){
        //disable the player movement disability
        //toggle so that if a fighter dies, they get sent to the spectator spawn
        game.start();
    }
    public void stop(){
        game.stop();
    }

    public void open(){
        setState(GameState.RECRUITING);
        System.out.println("SET GAMESTATE OF " + arenaName + " TO " + state.getDisplay());
    }
    public void reset(boolean kickPlayers){
        if (kickPlayers){
            Location spectator_spawn = ConfigManager.getSpectatorSpawn(arenaName);
            for (UUID uuid : fighters){
                Bukkit.getPlayer(uuid).teleport(spectator_spawn);
            }
            fighters.clear();
        }
        sendTitle("", "");

        setState(GameState.DORMANT);
        countdown.cancel(); // if countdown isn't running, this doesnt change anything

        /*
        Resetting things
         */
        countdown = new Countdown(fw, this);
        game = new Game(this);
    }

    public void sendMessage(String message){
        for (UUID uuid : fighters){
            Bukkit.getPlayer(uuid).sendMessage(message);
        }
    }
    public void sendTitle(String title, String subtitle){
        for (UUID uuid : fighters){
            Bukkit.getPlayer(uuid).sendTitle(title, subtitle);
        }
    }
    /*
    Getters
     */
    public Game getGame(){return game;}
    public String getArenaName(){return arenaName;}
    public Location getSpectatorSpawn(){return spectatorSpawn;}
    public ItemStack[] getKitItems(){return kit_items;}
    public ItemStack[] getKitArmor(){return kit_armor;}

    public boolean isDefaultKit(){return defaultKit;}
    public boolean isInTheRing(){return inTheRing;}
    public int getCountdownSeconds(){return countdownSeconds;}

    public GameState getState(){return state;}

    public int getRequiredFactions(){return requiredFactions;}
    public List<UUID> getFighters(){return fighters;}
    public List<Faction> getFactions(){return factions;}
    public List<UUID> getRoster(Faction fac){
        /*
        If a faction is in the arena's roster, return that faction's fighters.
         */
        if (roster.containsKey(fac)){
            return roster.get(fac);
        }
        return null;
    }
    public HashMap<Faction, Integer> getFacNumbers(){return facNumbers;}
    public HashMap<Integer, Location> getfactionSpawns(){return getfactionSpawns();}
    public Faction getFaction(Player p){
        /*
        Returns the player's faction IF they are a fighter in the arena
         */
        UUID p_id = p.getUniqueId();
        if (!fighters.contains(p_id)){
            return null;
        }
        //get the playerdata from Factions API to find player faction
        MPlayer f_p = MPlayer.get(p_id);
        Faction p_fac = f_p.getFaction();
        return p_fac;
    }
    //public ProtectedRegion getRegion(){return region;}

    /*
    Setters
     */

    public void setCountdownSeconds(int sec){
        this.countdownSeconds = sec;
        System.out.println("Arena.setCdSeconds:" + arenaName + " WILL COUNTDOWN FOR " + String.valueOf(sec) + " SECONDS");

        arenaConfig = fw.getArenaConfig();
        fw.saveArenaConfig(arenaConfig, arenaData);

//        //arenaConfig.set("arena_names." + arenaName + ".countdown-seconds", countdownSeconds);
//        try {
//            arenaConfig.save(arenaData);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(e);
//        }

    }
    public void setState(GameState state){
        this.state = state;
        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".game-state", state);
        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setSpectatorSpawn(Location loc){
        this.spectatorSpawn = loc;
        arenaConfig = YamlConfiguration.loadConfiguration(this.arenaData);

        arenaConfig.set("arena_names." + arenaName + ".spectator-spawn", loc);

        try {
            arenaConfig.save(arenaData);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    public void setKit(Player p){
        /**
         * Defines this arena's default kit
         * @param kit The kit that the fighting players will use
         */
        ItemStack[] kit_items = p.getInventory().getContents();
        ItemStack[] kit_armor = p.getInventory().getArmorContents();

        this.kit_items = kit_items;
        this.kit_armor = kit_armor;
    }
    public void setPlayersPerFaction(int playersPerFaction){
        this.playersPerFaction = playersPerFaction;

        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".players-per-faction", playersPerFaction);

        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setFactionSpawn(int factionId, Location factionSpawn){
        arenaConfig = YamlConfiguration.loadConfiguration(this.arenaData);
        if (factionSpawns.size() < requiredFactions){
            factionSpawns.put(factionId, factionSpawn);
            arenaConfig.set("arena_names." + arenaName + ".spawns." + factionId, factionSpawn);

            try {
                arenaConfig.save(arenaData);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
    }
    public void setRequiredFactions(int num){
        requiredFactions = num;

        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".required-factions", num);

        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setDefaultKit(boolean isKit){
        this.defaultKit = isKit;

        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".is-default-kit", isKit);
        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setInTheRing(boolean inTheRing){
        this.inTheRing = inTheRing;

        arenaConfig = fw.getArenaConfig();

        arenaConfig.set("arena_names." + arenaName + ".is-in-the-ring", inTheRing);

        fw.saveArenaConfig(arenaConfig, arenaData);
    }


    /*
    Roster Management
     */

    public void addFaction(Faction fac){
        /*
        Adds a faction to the roster if there is still space
         */
        int nextNumber = iter.next();
        if (roster.size() < requiredFactions){
            List<UUID> fac_roster = new ArrayList<>();
            roster.put(fac, fac_roster);
            addFacNumber(fac);
        }
        if(checkIsReady()){
            countdown.start();
        }
    }
    public void addFighter(Player p){
        UUID p_id = p.getUniqueId();
        MPlayer f_p = MPlayer.get(p_id);
        Faction p_fac = f_p.getFaction();
        if (p_fac == null){
            /*
            If the player isn't in a faction, they can't join
             */
            fw.getMessageUtil().message(p, "factionless");
            return;
        }
        //if the faction is already in the roster, add the player as normal
        if (roster.containsKey(p_fac)){
            //if this player is already in the faction OR
            //this faction has the max number of players already,
            //then dont add the player
            if (roster.get(p_fac).contains(p_id) || roster.get(p_fac).size() >= playersPerFaction){
                return;
            }
            roster.get(p_fac).add(p_id);
            if (defaultKit){
                storeKit(p);
            }
        }
        //if this is the first player for this faction
        // AND we aren't at faction limit, add the faction with the player
        else{
            addFaction(p_fac);
            addFighter(p);
            storeKit(p);
        }
        if (checkIsReady()){
            countdown.start();
        }

    }
    public void removeFighter(Player p){
        UUID p_id = p.getUniqueId();
        MPlayer f_p = MPlayer.get(p_id);
        Faction p_fac = f_p.getFaction();
        if (roster.get(p_fac).contains(p_id)){
            roster.get(p_fac).remove(p_id);
        }
        if(fighters.contains(p_id)){
            fighters.remove(p_id);
        }

        p.teleport(spectatorSpawn);
        //if there was a default kit given to all players, we want to give the removed player their inventory back
        if (defaultKit){
            restoreKit(p);
        }

        sendMessage(ChatColor.LIGHT_PURPLE.toString() + p.getName() + " of the faction " + p_fac + " has been eliminated!");

        //if this removal puts the faction's members to 0, remove the faction from the list
        if (roster.get(p_fac).size() == 0){
            removeFaction(p_fac);
            sendMessage(ChatColor.LIGHT_PURPLE.toString() + p_fac + " has been eliminated! " + roster.size() + " teams remain!");
            p.sendTitle("GG", "Your faction has been eliminated");
        }
        else{
            p.sendTitle("GG", "Your faction has " + roster.get(p_fac).size() + " fighters remaining");
        }

        if (state == GameState.COUNTDOWN && roster.get(p_fac).size() < playersPerFaction){
            sendMessage(ChatColor.RED + "There aren't enough players. Countdown stopped!");
            reset(false);
        }
        if (state == GameState.LIVE && factions.size() == 1){
            sendMessage(ChatColor.GOLD + "Game Over! " + roster.keySet() + " wins!");
            reset(true);
        }
    }
    public void removeFaction(Faction fac){
        roster.remove(fac);
    }
    public void addArenaSpawn(Integer n, Location loc){
        factionSpawns.put(n, loc);
    }
    public void addFacNumber(Faction fac){facNumbers.put(fac, iter.next());}

    public void guiPrompt(Player p){
        /**
         * This method gives the player a GUI that lets them set whether an arena uses default kits, has inTheRing
         * set to true, and any other boolean methods
         * @param p The player who is inputting settings for the arena
         */

        //note: certain settings, like requiredFactions and playersPerFaction kind of require players to
        //type their values. We can include a block in the GUI whose name tells them what to type into
        //chat to set these values
        //Menu arenaMenu = new ArenaMenu();
        //arenaMenu.open(p);
        //still need to configure the GUI itself
    }


    public void storeKit(Player p){
        /**
         * Store the kit of a player who is entering an arena wtih a default kit
         * @param p The player getting their kit stored
         */
        ItemStack[] p_items = p.getInventory().getContents();
        ItemStack[] p_armor = p.getInventory().getArmorContents();
        itemLocker.put(p.getUniqueId(), p_items);
        armorLocker.put(p.getUniqueId(), p_armor);

        p.getInventory().clear();
        p.updateInventory();
    }
    public void restoreKit(Player p){
        /**
         *
         * @param p The player who just died/left and is getting their kit restored
         */

        UUID p_id = p.getUniqueId();
        if (itemLocker.containsKey(p_id)){
            p.getInventory().setContents(itemLocker.get(p_id));
        }
        if (armorLocker.containsKey(p_id)){
            p.getInventory().setArmorContents(armorLocker.get(p_id));
        }
        p.updateInventory();

    }
    public void equip(Player p){
        /**
         * Equips a plyer with the default kit that was set up in the arena
         * @param p The player getting equipped
         */
        p.getInventory().setContents(kit_items);
        p.getInventory().setArmorContents(kit_armor);
        p.updateInventory();
    }

    public boolean checkIsReady(){
        /**
         * Checks if, based on current number of factions
         * and players fighting for each faction, the game can start yet
         */
        boolean isReady = true;
        if (roster.size() == requiredFactions){
            for (Faction fac : roster.keySet()){
                int fac_team_size = roster.get(fac).size();
                if (fac_team_size != playersPerFaction){
                    isReady = false;
                }
            }
        }
        return isReady;
    }




}

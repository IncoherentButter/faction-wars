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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.piston.config.Config;

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

    private boolean brandNew;


    public Arena(FactionWars fw, String arenaName, boolean brandNew){
        this.fw = fw;

        arenaData = fw.getArenaDataFile();
        arenaConfig = fw.getArenaConfig();

        this.brandNew = brandNew;

        this.arenaName = arenaName;

        setupConfig(brandNew);

        setRequiredFactions(fw.getConfigManager().getRequiredFactions());

        setPlayersPerFaction(fw.getConfigManager().getPlayersPerFaction());

        setCountdownSeconds(fw.getConfigManager().getCountdownSeconds());




        this.countdown = new Countdown(fw, this);


        createIterator();

        /*
        Default settings
         */
//        setState(GameState.DORMANT); //default to DORMANT


//        setDefaultKit(true);
//
//        setInTheRing(false);

    }

    public void setupConfig(boolean brandNew){
        arenaConfig = fw.getArenaConfig();
        arenaData = fw.getArenaDataFile();
//        ConfigurationSection subSection = arenaConfig.createSection("arena_names. " + arenaName);
//        subSection.set("", "");
        if (brandNew){
            ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
            section.createSection("countdown-seconds");
            section.createSection("game-state");
            section.createSection("required-factions");
            section.createSection("players-per-faction");
            section.createSection("default-kit");
            section.createSection("in-the-ring");
            section.createSection("spectator-spawn.world");
            section.createSection("spectator-spawn.x");
            section.createSection("spectator-spawn.y");
            section.createSection("spectator-spawn.z");
            section.createSection("spectator-spawn.pitch");
            section.createSection("spectator-spawn.yaw");
            section.createSection("spawns");

            section.set("countdown-seconds", fw.getConfigManager().getCountdownSeconds());
            section.set("game-state", "Dormant");
            section.set("required-factions", fw.getConfigManager().getRequiredFactions());
            section.set("players-per-faction", fw.getConfigManager().getPlayersPerFaction());
            section.set("default-kit", fw.getConfigManager().getDefaultKit());
            section.set("in-the-ring", fw.getConfigManager().getInTheRing());
            section.set("spectator-spawn.world", "unset");
            section.set("spectator-spawn.x", "unset");
            section.set("spectator-spawn.y", "unset");
            section.set("spectator-spawn.z", "unset");
            section.set("spectator-spawn.pitch", "unset");
            section.set("spectator-spawn.yaw", "unset");
            for (int i = 1; i < requiredFactions + 1; i ++){
                section.createSection("spawns." + String.valueOf(i) + ".world");
                section.createSection("spawns." + String.valueOf(i) + ".x");
                section.createSection("spawns." + String.valueOf(i) + ".y");
                section.createSection("spawns." + String.valueOf(i) + ".z");
                section.createSection("spawns." + String.valueOf(i) + ".pitch");
                section.createSection("spawns." + String.valueOf(i) + ".yaw");

                section.set("spawns." + String.valueOf(i) + ".world", "world");
                section.set("spawns." + String.valueOf(i) + ".x", "unset");
                section.set("spawns." + String.valueOf(i) + ".y", "unset");
                section.set("spawns." + String.valueOf(i) + ".z", "unset");
                section.set("spawns." + String.valueOf(i) + ".pitch", "unset");
                section.set("spawns." + String.valueOf(i) + ".yaw", "unset");
            }

            System.out.println("Arena.setupConfig(): set up config for arena " + arenaName);
        }
        else{
            ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);

            int cds = section.getInt("countdown-seconds");
            String stateString = section.getString("game-state");
            int rf = section.getInt("required-factions");
            int ppf = section.getInt("players-per-faction");
            boolean dk = Boolean.parseBoolean(section.getString("default-kit"));
            boolean itr = Boolean.parseBoolean(section.getString("in-the-ring"));
            if (section.get("spectator-spawn.x") instanceof Double){
                Location ss = new Location(Bukkit.getWorld(String.valueOf(section.getString("spectator-spawn.world"))),
                        (Double.parseDouble(section.getString("spectator-spawn.x"))),
                        (Double.parseDouble(section.getString("spectator-spawn.y"))),
                        (Double.parseDouble(section.getString("spectator-spawn.z"))),
                        (Float.parseFloat(section.getString("spectator-spawn.pitch"))),
                        (Float.parseFloat(section.getString("spectator-spawn.yaw"))));

                ConfigurationSection specSpawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spectator-spawn");
                specSpawnSection.createSection("world");
                specSpawnSection.set("world", ss.getWorld());

                specSpawnSection.createSection("x");
                specSpawnSection.set("x", ss.getX());

                specSpawnSection.createSection("y");
                specSpawnSection.set("y", ss.getY());

                specSpawnSection.createSection("z");
                specSpawnSection.set("z", ss.getZ());

                specSpawnSection.createSection("pitch");
                specSpawnSection.set("pitch", ss.getPitch());

                specSpawnSection.createSection("yaw");
                specSpawnSection.set("yaw", ss.getYaw());
                this.spectatorSpawn = ss;
            } else{
                ConfigurationSection specSpawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spectator-spawn");
                specSpawnSection.set("world", "world");
                specSpawnSection.set("x", "unset");
                specSpawnSection.set("y", "unset");
                specSpawnSection.set("z", "unset");
                specSpawnSection.set("pitch", "unset");
                specSpawnSection.set("yaw", "unset");
            }
            for (int i = 1; i < requiredFactions + 1; i ++){
                if (section.get("spawns." + String.valueOf(i) + ".x") instanceof Double){
                    /*
                    This means that this number spawn has been set
                     */
                    Location fs = new Location(Bukkit.getWorld(String.valueOf(section.getString("spawns." + String.valueOf(i) + ".world"))),
                            (Double.parseDouble(section.getString("spawns." + String.valueOf(i) + ".x"))),
                            (Double.parseDouble(section.getString("spawns." + String.valueOf(i) + ".y"))),
                            (Double.parseDouble(section.getString("spawns." + String.valueOf(i) + ".z"))),
                            (Float.parseFloat(section.getString("spawns." + String.valueOf(i) + ".pitch"))),
                            (Float.parseFloat(section.getString("spawns." + String.valueOf(i) + ".yaw"))));
                    ConfigurationSection spawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + String.valueOf(i));
                    spawnSection.createSection("world");
                    spawnSection.set("world", fs.getWorld());

                    spawnSection.createSection("x");
                    spawnSection.set("x", fs.getX());

                    spawnSection.createSection("y");
                    spawnSection.set("y", fs.getY());

                    spawnSection.createSection("z");
                    spawnSection.set("z", fs.getZ());

                    spawnSection.createSection("pitch");
                    spawnSection.set("pitch", fs.getPitch());

                    spawnSection.createSection("yaw");
                    spawnSection.set("yaw", fs.getYaw());

                    factionSpawns.put(i, fs);
                } else{
                    ConfigurationSection spawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + String.valueOf(i));
                    spawnSection.set("world", "world");
                    spawnSection.set("x", "unset");
                    spawnSection.set("y", "unset");
                    spawnSection.set("z", "unset");
                    spawnSection.set("pitch", "unset");
                    spawnSection.set("yaw", "unset");
                }

            }


            ConfigurationSection spawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns");
//            Set<String> spawn_ids = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns").getKeys(false);
//            HashMap<Integer, Location> fspawns = new HashMap<>();
//            for (String spawn_id_string : spawn_ids){
//                Location loc = new Location(Bukkit.getWorld(String.valueOf(arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + spawn_id_string + ".world"))),
//                        Integer.parseInt(String.valueOf(arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + spawn_id_string + ".x"))),
//                                Integer.parseInt(String.valueOf(arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + spawn_id_string + ".y"))),
//                                        Integer.parseInt(String.valueOf(arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns." + spawn_id_string + ".z"))));
//                fspawns.put(Integer.parseInt(spawn_id_string), loc);
//            }
//            setCountdownSeconds(cds);

//
//            section.createSection("game-state");
//            section.createSection("required-factions");
//            section.createSection("players-per-faction");
//            section.createSection("default-kit");
//            section.createSection("in-the-ring");
//            section.createSection("spectator-spawn.world");
//            section.createSection("spectator-spawn.x");
//            section.createSection("spectator-spawn.y");
//            section.createSection("spectator-spawn.z");
//            section.createSection("spawns");
//            ConfigurationSection cdSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".countdown-seconds");
//            cdSection.set(null, section.getInt("countdown-seconds"));
//
//            ConfigurationSection gsSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".game-state");
//            gsSection.set(null, section.getString("game-state"));
////
            section.createSection("countdown-seconds");
            section.set("countdown-seconds", countdownSeconds);

            section.createSection("game-state");
            section.set("game-state", stateString);

            section.createSection("required-factions");
            section.set("required-factions", rf);

            section.createSection("players-per-faction");
            section.set("players-per-faction", ppf);

            section.createSection("default-kit");
            section.set("default-kit", dk);

            section.createSection("in-the-ring");
            section.set("in-the-ring", itr);




//            section.createSection("spawns");
//            fspawns.forEach((Integer id, Location fsp) -> section.set("spawns." + String.valueOf(id)));


//            setCountdownSeconds(section.getInt("countdown-seconds"));
//            setState(section.getString("game-state"));
//            setRequiredFactions(section.getInt("required-factions"));
//            setPlayersPerFaction(section.getInt("players-per-faction"));
//            setDefaultKit(Boolean.parseBoolean(section.getString("default-kit")));
//            setInTheRing(Boolean.parseBoolean(section.getString("in-the-ring")));
//            setSpectatorSpawn(section.getLocation("spectator-spawn"));
//            ConfigurationSection spawnSection = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns");
////            if spawnSection.
////            if (!arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns"))
//            Set<String> spawn_ids = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns").getKeys(false);
//            for (String spawn_id_string : spawn_ids){
//                Integer spawn_id = Integer.parseInt(spawn_id_string);
//                setFactionSpawn(spawn_id, section.getLocation("arena_names." + arenaName + ".spawns." + spawn_id));
//            }

        }

        fw.saveArenaConfig(arenaConfig, arenaData);
        this.brandNew = false;
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
            Location spectator_spawn = spectatorSpawn;
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
//        ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
//        section.set("countdown-seconds", sec);
//        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setState(GameState state){
        this.state = state;
//        ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
//        section.set("game-state", state.getDisplay());
//        fw.saveArenaConfig(arenaConfig, arenaData);

    }
    public void setState(String stateDisplay){
        if (stateDisplay.equalsIgnoreCase("Dormant")){
            state = GameState.DORMANT;
        }
        else if (stateDisplay.equalsIgnoreCase("Recruiting")){
            state = GameState.RECRUITING;
        } else if (stateDisplay.equalsIgnoreCase("Countdown")){
            state = GameState.COUNTDOWN;
        } else{
            state = GameState.LIVE;
        }
        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".game-state");
        section.set("", state.getDisplay());
        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setSpectatorSpawn(Location loc){
        this.spectatorSpawn = loc;

        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
        section.set("spectator-spawn", loc);
        fw.saveArenaConfig(arenaConfig, arenaData);
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
    public void setRequiredFactions(int num){
        requiredFactions = num;

        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
        section.set("required-factions", requiredFactions);
        fw.saveArenaConfig(arenaConfig, arenaData);
//        ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
//        section.set("required-factions", num);
//        fw.saveArenaConfig(arenaConfig, arenaData);

    }
    public void setPlayersPerFaction(int playersPerFaction){
        this.playersPerFaction = playersPerFaction;
        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
        section.set("players-per-faction", playersPerFaction);
        fw.saveArenaConfig(arenaConfig, arenaData);
//        ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
//        section.set("players-per-faction", playersPerFaction);
//        fw.saveArenaConfig(arenaConfig, arenaData);
    }
    public void setFactionSpawn(int factionId, Location factionSpawn){
        arenaConfig = YamlConfiguration.loadConfiguration(this.arenaData);

        //get ConfigSection "spawns"

        if (factionSpawns.size() < requiredFactions){
            //Set spawn in config
            ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName + ".spawns");
            for (int i = 1; i < requiredFactions + 1; i++){
                if (i == factionId){
                    section.createSection(String.valueOf(factionId));
                    section.set(String.valueOf(factionId), factionSpawn);
                }
                else{
                    section.createSection(String.valueOf(i));
                    section.set(String.valueOf(i), factionSpawns.get(i));
                }
            }
//            section.createSection("spawns");
//            section.set(String.valueOf(factionId), factionSpawn);
            fw.saveArenaConfig(arenaConfig, arenaData);

            //Record spawn in Arena.class
            factionSpawns.put(factionId, factionSpawn);
        }
    }
    public void setDefaultKit(boolean isKit){
        this.defaultKit = isKit;

        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
        section.set("default-kit", isKit);
        fw.saveArenaConfig(arenaConfig, arenaData);
//        if (!brandNew){
//            ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
//            section.set("default-kit", isKit);
//            fw.saveArenaConfig(arenaConfig, arenaData);
//        }

    }
    public void setInTheRing(boolean inTheRing){
        this.inTheRing = inTheRing;

        ConfigurationSection section = arenaConfig.getConfigurationSection("arena_names." + arenaName);
        section.set("in-the-ring", inTheRing);
        fw.saveArenaConfig(arenaConfig, arenaData);
//        ConfigurationSection section = arenaConfig.createSection("arena_names." + arenaName);
//        section.set("in-the-ring", inTheRing);
//        fw.saveArenaConfig(arenaConfig, arenaData);

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

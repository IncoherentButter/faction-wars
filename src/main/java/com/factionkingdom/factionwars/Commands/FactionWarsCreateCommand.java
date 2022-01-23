package com.factionkingdom.factionwars.Commands;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Instances.Arena;
import com.factionkingdom.factionwars.Managers.ArenaManager;
import com.factionkingdom.factionwars.Menus.ArenaMenu;
import com.factionkingdom.factionwars.Menus.Menu;
import com.factionkingdom.factionwars.Util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;

public class FactionWarsCreateCommand implements CommandExecutor {

    private MessageUtil messageUtil;
    private ArenaManager arenaManager;
    private FactionWars fw;

    public FactionWarsCreateCommand(FactionWars fw){
        //some notes, to be deleted later:
        // Make the arena creation separate from the Event creation.
        // So, we should have /fwc arena <name>, and this creates the region for combat (at which point u can do wg and all that)
        // and THEN have a separate command, like /fwc prepare, where you get prompted with
        // a list of arenas you've pre-defined, and you choose from that list which arena you'd like the players
        // to fight in. THEN, after that, you get prompted with several booleans to set, like below

        //flow should be:
        //  /fwc arena <name> --> /fwc prepare --> choose arena --> get prompted with several booleans to set, such as
        // kit? (whether or not you want to use a predefined kit), inTheRing? (whether or not players are eliminated once
        // they are knocked out of the ring)

        this.messageUtil = fw.getMessageUtil();
        this.arenaManager = fw.getArenaManager();
        this.fw = fw;



    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.isOp()) {
                p.sendMessage(ChatColor.RED + "Only operators can create Faction Wars!");
                return false;
            }

            if (args.length == 0) {
                messageUtil.fwcHelp(p);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("setkit")) {
                //if default kits are set to true, then this command sets the current inventory to the default kit
                //else, send the player a message that tells them they need to change the settings for the arena to
                //do that
                //the set kit is just the player's current inventory
                Arena arena = safelyGetArena(args[1], p);
                if (arena != null){
                    if (!arena.isDefaultKit()){
                        messageUtil.messageLanguageArena(p, "arena_default_kits_off", arena.getArenaName());
                    }else {
                        arena.setKit(p);
                        fw.getMessageUtil().messageLanguageArena(p, "arena_set_kit", args[1]);
                    }
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
                //If they do "/fwc create", tell them they need more
                messageUtil.messageLanguage(p, "correct_arena_creation");
            }else if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
                /*
                Creates an arena with a given name
                 */
                if (args.length > 2) {
                    //tell them arena names must be one word
                    messageUtil.messageLanguage(p, "correct_arena_name_length");
                } else {
                    //Create the arena
//                    Arena newArena = new Arena(fw, args[1]);
                    //Define it as a WG region
                    //arenaManager.define(p, args[1]);
                    //Add to the arena list
                    arenaManager.addArena(p, args[1]);
                    messageUtil.messageLanguageArena(p, "arena_create", args[1]);
                    messageUtil.messageUsageHelp(p, "creation", args[1]);
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("prepare")) {
                /*
                Prompts players with a setup GUI after they do /fwc prepare
                 */
                String arenaName = args[1];
                Arena arena = safelyGetArena(arenaName, p);
                if (arena != null) {
                    Menu settingsMenu = new ArenaMenu(fw, arenaName, p);
                    settingsMenu.open(p);
                }
                //arenaManager.getArena(args[1]);
            } else if (args.length == 3 && args[0].equalsIgnoreCase("requiredFactions")) {
                /*
                Sets the number of required factions for this arena
                 */
                try {
                    Integer numRequiredFactions = Integer.parseInt(args[2]);
                    Arena arena = safelyGetArena(args[1], p);
                    if (arena != null) {
                        arena.setRequiredFactions(numRequiredFactions);
                        messageUtil.messageLanguageArenaInt(p,"arena_required_factions",arena.getArenaName(), numRequiredFactions);
                    }
                } catch (NumberFormatException x) {
                    messageUtil.messageLanguage(p, "correct_required_factions");
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("playersPerFaction")){
                /*
                Sets the number of players that each faction must have
                 */
                try {
                    Integer playersPerFaction = Integer.parseInt(args[2]);
                    Arena arena = safelyGetArena(args[1], p);
                    if (arena != null) {
                        arena.setPlayersPerFaction(playersPerFaction);
                        messageUtil.messageLanguageArenaInt(p,"arena_players_per",arena.getArenaName(), playersPerFaction);

                    }
                } catch (NumberFormatException x) {
                    messageUtil.messageLanguage(p, "correct_players_per_faction");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")){
                String arenaName = args[1];
                Arena arena = safelyGetArena(arenaName, p);
                if (arena != null) {
                    arenaManager.removeArena(p, arena);
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("setspawn")){
                /**
                 * Sets the faction spawn
                 */
                try {
                    Integer spawnNumber = Integer.parseInt(args[2]);
                    Arena arena = safelyGetArena(args[1], p);
                    if (arena != null) {
                        Location spawn = p.getLocation();
                        int numFactions = arena.getRequiredFactions();

                        if (spawnNumber > numFactions || spawnNumber <= 0){
                            messageUtil.messageLanguageArenaInt(p,"correct_arena_spawnnumber" , arena.getArenaName(), numFactions);
                        }
                        else{
                            arena.setFactionSpawn(spawnNumber, spawn);
                            messageUtil.messageLanguageArenaInt(p,"arena_set_spawn",arena.getArenaName(), spawnNumber);
                        }
                    }
                } catch (NumberFormatException x) {
                    messageUtil.messageLanguage(p, "correct_players_per_faction");
                }
            }
//            } else if (args.length >= 2) {
//                if (args[0].equalsIgnoreCase("arena")) {
//                    //if the player input /fwc arena <arena name>, then we create the arena named <arena name>
//                    StringBuilder stringBuilder = new StringBuilder();
//                    for (int i = 1; i < args.length; i++) {
//                        stringBuilder.append(args[i]);
//                    }
//                    String arenaName = stringBuilder.toString();
//                    ArenaManager arenaManager = new ArenaManager(fw);
//                }
//            }
        }
        return false;
    }

    public Arena safelyGetArena(String arenaName, Player p){
        /**
         * Since our commands rely heavily on players inputting valid arena names,
         * we create this method to check if a given arena name is valid. If it is,
         * we return the associated arena.
         *
         * FUTURE: return a list of valid arena names when the input is incorrect
         * @param arenaName The arena name input by the player.
         */
        if (arenaManager.getArenaNames().contains(arenaName)){
            Arena arena = arenaManager.getArena(arenaName);
            return arena;
        } else{
            messageUtil.messageLanguageArena(p, "correct_arena_not_exist", arenaName);
            return null;
        }
    }
}

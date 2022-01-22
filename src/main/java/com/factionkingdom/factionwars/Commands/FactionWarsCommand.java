package com.factionkingdom.factionwars.Commands;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Instances.Arena;
import com.factionkingdom.factionwars.Managers.ArenaManager;
import com.factionkingdom.factionwars.Util.MessageUtil;
import com.factionkingdom.factionwars.Util.SoundUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class FactionWarsCommand implements CommandExecutor {

    private MessageUtil messageUtil;
    private ArenaManager arenaManager;
    private FactionWars fw;

    private File arenaData;
    private FileConfiguration arenaConfig;

    public FactionWarsCommand(FactionWars fw){
        this.messageUtil = fw.getMessageUtil();
        this.arenaManager = fw.getArenaManager();
        this.fw = fw;

        arenaData = fw.getArenaDataFile();
        arenaConfig = fw.getArenaConfig();

        StringBuilder stringBuilder = new StringBuilder();
        System.out.println("FW: config list of names in FWCommand:");
        for (String name: arenaConfig.getStringList("arena_names")){
            stringBuilder.append("&3" + name + "\n");
            System.out.println("$$$" + name + "$$$");
        }
        String list = stringBuilder.toString();
        System.out.println("FW: " + list);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length == 0) {
                //blank /fw returns help
                messageUtil.fwHelp(p);
            } else if (args.length == 1 && args[0].equalsIgnoreCase("list")){
                messageUtil.messageArenaList(p, fw);

//                StringBuilder stringBuilder = new StringBuilder();
//                System.out.println("FW.list(): config list of names in FWCommand:");
//                for (String name: arenaConfig.getStringList("arena_names")){
//                    stringBuilder.append("&3" + name + "\n");
//                    System.out.println("$$$" + name + "$$$");
//                }
//                String list = stringBuilder.toString();
//                messageUtil.message(p, list);

            } else if (args.length == 3 && args[0].equalsIgnoreCase("start")){
                String arenaName = args[1];
                Integer startTime = Integer.parseInt(args[2]);

                System.out.println("fwCommand: tried to start " + arenaName + " with a countdown lasting " + startTime.toString() + " seconds.");
                Arena arena = safelyGetArena(arenaName, p);
                if (arena != null && arena.getState() == GameState.DORMANT){
                    arena.setCountdownSeconds(startTime);
                    arena.open();

                    //arenaManager.addArena(arena);
                    messageUtil.messageLanguageArena(p, "arena_start_recruiting", arenaName);
                }
            }else if (args.length == 2 && (args[0].equalsIgnoreCase("spectate") || args[0].equalsIgnoreCase("spec"))){
                /*
                Let players spectate an arena. Teleports them to the spectator spawn
                 */
                String arenaName = args[1];
                if(safelyGetArena(arenaName, p) != null){
                    p.teleport(arenaManager.getArena(arenaName).getSpectatorSpawn());
                    messageUtil.messageLanguageArena(p, "arena_spectate", arenaName);
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("join")){
                /*
                Lets a player join an arena. Currently, any player in a faction can join and represent their faction
                on a first-come, first-serve basis.
                 */
                String arenaName = args[1];
                if (isRecruitingArena(arenaName, p)){
                    arenaManager.getArena(arenaName).addFighter(p);
                    messageUtil.messageLanguageArena(p, "arena_join", arenaName);
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("leave")){
                String arenaName = args[1];
                if (safelyGetArena(arenaName, p) != null){
                    Arena arena = arenaManager.getArena(arenaName);
                    arena.removeFighter(p);
                }

            } else if (args.length == 2 && args[0].equalsIgnoreCase("stop")){
                String arenaName = args[1];
                if (safelyGetArena(arenaName, p) != null && safelyGetArena(arenaName, p).getState() != GameState.DORMANT){
                    Arena arena = arenaManager.getArena(arenaName);
                    arena.stop();
                }
            }
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
    public boolean isRecruitingArena(String arenaName, Player p){
        /**
         * Checks if the arena someone is trying to join is currently recruiting
         *
         * @param arenaName The arena name input by the player.
         */
        Arena arena = arenaManager.getArena(arenaName);
        if (arena.getState() == GameState.RECRUITING){
            return true;
        } else if (arena == null){
            messageUtil.messageLanguageArena(p, "correct_arena_not_exist", arenaName);
        }
        else{
            messageUtil.messageLanguageArena(p, "arena_not_recruiting", arenaName);
        }
        return false;
    }
}

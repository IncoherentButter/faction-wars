package com.factionkingdom.factionwars.Util;

import com.factionkingdom.factionwars.FactionWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;
import java.util.Set;

public class MessageUtil {

    private FactionWars fw;

    private File languageFile;
    private File arenaDataFile;
    private File configFile;

    private FileConfiguration languageConfig;
    private FileConfiguration arenaConfig;
    private FileConfiguration config;

    public MessageUtil(FactionWars fw){
        languageFile = fw.getLanguageFile();
        arenaDataFile = fw.getArenaDataFile();
        configFile = new File(fw.getDataFolder(), "config.yml");

        languageConfig = fw.getLanguageConfig();
        arenaConfig = fw.getArenaConfig();
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void message(CommandSender p, String msg){
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public void messageArenaList(CommandSender p, FactionWars fw){
        arenaConfig = fw.getArenaConfig();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&e&lFactionWars Arenas\n");
        stringBuilder.append("&7----------------------\n");
        System.out.println("MessageUtil.messageArenaList(): list of arenas in config");
        Set<String> arenaNamesInConfig = arenaConfig.getConfigurationSection("arena_names").getKeys(false);
        if (!arenaNamesInConfig.isEmpty()){
            for (String name : arenaNamesInConfig){
                stringBuilder.append(name).append("\n");
                System.out.println("^^^" + name + "^^^");
            }
        }
        message(p, languageConfig.getString("arena_list") + stringBuilder);
    }

    public void messageLanguage(CommandSender p, String key){
        //Building message from language.yml based on the input key
        this.languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("prefix")));
        stringBuilder.append( ChatColor.translateAlternateColorCodes('&', languageConfig.getString(key)));
        String msg = stringBuilder.toString();

        p.sendMessage(msg);
    }

    public void messageLanguageArena(CommandSender p, String key, String arenaName){
        /**
         * For message's we want to send that are specific to a given arena
         */
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);

        /*
        This was for debugging; it prints out every line of the language.yml
         */
//        try (BufferedReader br = new BufferedReader(new FileReader(languageFile))) {
//            String line = null;
//            while (true) {
//                try {
//                    if (!((line = br.readLine()) != null)) break;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(line);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (languageConfig.contains(key)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("prefix")));
            stringBuilder.append( ChatColor.translateAlternateColorCodes('&', languageConfig.getString(key)).replaceAll("%arena%", arenaName));
            String msg = stringBuilder.toString();
            p.sendMessage(msg);
        }
        else{
            p.sendMessage("There is no registered key titled " + key);
        }


    }

    public void messageLanguageArenaInt(CommandSender p, String key, String arenaName, Integer n){
        if (languageConfig.contains(key)){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("prefix")));
            stringBuilder.append( ChatColor.translateAlternateColorCodes('&', languageConfig.getString(key)).replaceAll("%arena%", arenaName).replaceAll("%num%", n.toString()));
            String msg = stringBuilder.toString();
            p.sendMessage(msg);
        }
        else{
            p.sendMessage("There is no registered key titled " + key);
        }
    }

    public void broadcastCountdown(String key, int i){
        StringBuilder stringBuilder = new StringBuilder();
        //if i isn't 0, then we want to broadcast a countdown with a number + play a countdown sound
        if (i != 0){
            if (i > 60 && i % 30 == 0){
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("countdown")
                        .replaceAll("%time%", String.format("%02d:%02d", new Object[] {i/60, Integer.valueOf(i % 60)}))));
                String msg = stringBuilder.toString();
                Bukkit.broadcastMessage(msg);
                SoundUtil.countdown();
            }
            else if (i <= 60 && i % 15 ==0){
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("countdown_soon")
                        .replaceAll("%time%", String.valueOf(i))));
                String msg = stringBuilder.toString();
                Bukkit.broadcastMessage(msg);
                SoundUtil.countdown();
            }
            else if (i <= 10){
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString("countdown_very_soon")
                        .replaceAll("%time%", String.valueOf(i))));
                String msg = stringBuilder.toString();
                Bukkit.broadcastMessage(msg);
                SoundUtil.countdown();
            }
        }
        else{
            stringBuilder.append(ChatColor.translateAlternateColorCodes('&', languageConfig.getString(key)));
            //handles countdown (start, stop)
        }

    }

    public void fwHelp(CommandSender p) {
        /**
         *
         * @param player The command sender
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&e&lFactionWars Commands\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&7----------------------\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw start <name> <seconds> &8| &7Starts arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw stop <name> &8| &7Stops arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw list &8| &7Lists all arenas\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw join <name> &8| &7Join an active arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw spectate <name> &8| &7Spectate an arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&3/fw leave &8| &7Leave an active arena\n"));

        String message = stringBuilder.toString();
        p.sendMessage(message);
    }

    public void fwcHelp(CommandSender p){
        /**
         * Sends a player help for the /factionwarscreate (/fwc) command
         * @param p The player who is getting the help
         */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&e&lFactionWars Arena Creation\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&7----------------------\n"));

        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc create arena <name> &8| &7Initialize an arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc delete arena <name> &8| &7Delete an arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc prepare <name> &8| &7Manage arena settings\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc requiredFactions <name> <number> &8| &7Set the number of required factions for this arena\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc playersPerFaction <name> <number> &8| &7Set the number of players required for each faction\n"));
        stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&b/fwc setkit <name> &8| &7Set the kit for the arena\n"));


        String message = stringBuilder.toString();
        p.sendMessage(message);
    }

    public void messageUsageHelp(CommandSender p, String cmd, String arenaName){
        StringBuilder stringBuilder = new StringBuilder();
        switch(cmd){
            case "creation":
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&e&lFactionWars Arena Configuration\n"));
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', "&7----------------------------\n"));
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&c! &3You should define a WorldGuard Region &c!\n"));
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&',"&c! &3with /region define " + arenaName + "&c!"));
        }
        String msg = stringBuilder.toString();
        p.sendMessage(msg);
    }

}

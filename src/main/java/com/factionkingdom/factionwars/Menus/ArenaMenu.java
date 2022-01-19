package com.factionkingdom.factionwars.Menus;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.Instances.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArenaMenu extends Menu{
    public HashMap<Integer, Material> terracotta_map;


    public ArenaMenu(FactionWars fw, String title, Player p){
        /*
        Future:
        the /arena list command will return a GUI with blocks for each arena. On click, teleport to that arena's
        spectator spawn.

        /arena prepare list will let players click into the preparation GUI for any arena if they have the permission to do so
         */
        super(fw, "§6§lFactionWars§7 — §3" + title, 1);

        setInvOpened(opened -> opened.sendMessage("§aYou opened the inventory!"));
        setInvClosed(closed -> closed.sendMessage("§cYou closed the inventory!"));

        //Register an info button (does nothing)
        ItemStack requiredFactionsInfo = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta rfMeta = requiredFactionsInfo.getItemMeta();
        rfMeta.setDisplayName(ChatColor.GRAY + "Required Factions");
        List<String> rfLore = new ArrayList<>();
        rfLore.add(ChatColor.RED + "/fwc requiredFactions <name> <num>");
        rfLore.add(ChatColor.GRAY + "Sets the number of factions");
        rfLore.add(ChatColor.GRAY + "that fight in this arena!");
        rfMeta.setLore(rfLore);
        rfMeta.setLocalizedName("rf");
        requiredFactionsInfo.setItemMeta(rfMeta);
        registerButton(new MenuButton(requiredFactionsInfo), 2);

        ItemStack playersPerFactionInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta ppfMeta = playersPerFactionInfo.getItemMeta();
        ppfMeta.setDisplayName(ChatColor.GRAY + "Players Per Faction");
        List<String> ppfLore = new ArrayList<>();
        ppfLore.add(ChatColor.RED + "/fwc playersPerFaction <name> <num>");
        ppfLore.add(ChatColor.GRAY + "Sets the number of fighters");
        ppfLore.add(ChatColor.GRAY + "that each faction must bring!");
        ppfMeta.setLore(ppfLore);
        ppfMeta.setLocalizedName("ppf");
        playersPerFactionInfo.setItemMeta(ppfMeta);
        registerButton(new MenuButton(playersPerFactionInfo), 6);

        ItemStack spectatorSpawnInfo = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta ssMeta = spectatorSpawnInfo.getItemMeta();
        ssMeta.setDisplayName(ChatColor.GRAY + "Setting Spectator Spawn");
        List<String> ssLore = new ArrayList<>();
        ssLore.add(ChatColor.RED + "/fwc setSpecSpawn <name>");
        ssLore.add(ChatColor.GRAY + "Sets where non-fighters warp");
        ssLore.add(ChatColor.GRAY + "to watch the combat!");
        ssLore.add(ChatColor.GRAY + "Fighters warp here upon death.");
        ssMeta.setLore(ssLore);
        ssMeta.setLocalizedName("ss");
        spectatorSpawnInfo.setItemMeta(ssMeta);
        registerButton(new MenuButton(spectatorSpawnInfo), 3);

        ItemStack factionSpawnInfo = new ItemStack(Material.SCAFFOLDING);
        ItemMeta fsMeta = factionSpawnInfo.getItemMeta();
        fsMeta.setDisplayName(ChatColor.GRAY + "Setting Faction Spawns");
        List<String> fsLore = new ArrayList<>();
        fsLore.add(ChatColor.RED + "/fwc setspawn <name> <num>");
        fsLore.add(ChatColor.GRAY + "Sets a faction spawn!");
        fsLore.add(ChatColor.GRAY + "Numbers should be between");
        fsLore.add(ChatColor.GRAY + "1 and the number of required factions");
        fsMeta.setLore(fsLore);
        fsMeta.setLocalizedName("fs");
        factionSpawnInfo.setItemMeta(fsMeta);
        registerButton(new MenuButton(factionSpawnInfo), 5);

        ItemStack countdownSecondsInfo = new ItemStack(Material.NETHER_STAR);
        ItemMeta cdsMeta = countdownSecondsInfo.getItemMeta();
        cdsMeta.setDisplayName(ChatColor.DARK_RED + "Starting Your Arena");
        List<String> cdsLore = new ArrayList<>();
        cdsLore.add(ChatColor.RED + "/fw start <name> <seconds>");
        cdsLore.add(ChatColor.GRAY + "Opens the arena to players!");
        cdsLore.add(ChatColor.GRAY + "When the arena is ready, it");
        cdsLore.add(ChatColor.GRAY + "will count down for <seconds>");
        cdsLore.add(ChatColor.GRAY + "seconds before starting!");
        cdsMeta.setLore(cdsLore);
        cdsMeta.setLocalizedName("cds");
        countdownSecondsInfo.setItemMeta(cdsMeta);
        registerButton(new MenuButton(countdownSecondsInfo), 4);

        ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blank.getItemMeta();
        blankMeta.setDisplayName(ChatColor.MAGIC + "1");
        blank.setItemMeta(blankMeta);
        registerButton(new MenuButton(blank), 1);
        registerButton(new MenuButton(blank), 7);

        //Register kit button
        ItemStack kitSetter = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemMeta kitSetterMeta = kitSetter.getItemMeta();
        kitSetterMeta.setDisplayName(ChatColor.GOLD + "Preset Kit");
        kitSetterMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); //removes the +5 armor indicator from the item
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "When enabled, you must");
        lore.add(ChatColor.AQUA + "set a kit for all fighters");
        lore.add(ChatColor.AQUA +"in this arena to share.");
        kitSetterMeta.setLore(lore);
        kitSetterMeta.setLocalizedName("defaultKit");
        kitSetter.setItemMeta(kitSetterMeta);

        Arena arena = fw.getArenaManager().getArena(title);
//        boolean isKit;
//        boolean isInTheRing;
//        if (fw.getArenaManager().getArenaNames().contains(title)){
//            isKit = arena.isDefaultKit();
//            isInTheRing = arena.isInTheRing();
//        }
//        else{
//            isKit = true;
//            isInTheRing = false;
//        }

//        registerButton(new MenuButton(kitSetter).setWhenClicked(
//                        clicked -> clicked.sendMessage("You toggled Preset Kits"), p),
//                1);

        registerButton(new MenuButton(kitSetter).setWhenClicked(
                        clicked -> {
                            //if statement meant to prevent a click from being registered on open
                            fw.getMessageUtil().messageLanguageArena(clicked, "arena_default_kits_" + !arena.isDefaultKit(), arena.getArenaName());
                            toggleKit(arena, arena.isDefaultKit());
                            }),
                0);


        //Register inTheRing button
        ItemStack ringSetter = new ItemStack(Material.TARGET);
        ItemMeta ringSetterMeta = ringSetter.getItemMeta();
        ringSetterMeta.setDisplayName(ChatColor.GOLD + "In The Ring");
        List<String> ringLore = new ArrayList<>();
        ringLore.add(ChatColor.AQUA + "When enabled, fighters lose");
        ringLore.add(ChatColor.AQUA + "when they step outside");
        ringLore.add(ChatColor.AQUA + "the arena.");
        ringSetterMeta.setLore(ringLore);
        ringSetterMeta.setLocalizedName("inTheRing");
        ringSetter.setItemMeta(ringSetterMeta);

        registerButton(new MenuButton(ringSetter).setWhenClicked(
                        clicked -> {
                            fw.getMessageUtil().messageLanguageArena(p, "arena_inthering_" + !arena.isInTheRing(), arena.getArenaName());
                            toggleRing(arena, arena.isInTheRing());
                            }),
                8);

    }

    public void setTerracotta_map(){
        terracotta_map.put(0, Material.BLACK_GLAZED_TERRACOTTA);
        terracotta_map.put(1, Material.BROWN_GLAZED_TERRACOTTA);
        terracotta_map.put(2, Material.PURPLE_GLAZED_TERRACOTTA);
        terracotta_map.put(3, Material.GRAY_GLAZED_TERRACOTTA);
        terracotta_map.put(4, Material.YELLOW_GLAZED_TERRACOTTA);
        terracotta_map.put(5, Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
        terracotta_map.put(6, Material.BLUE_GLAZED_TERRACOTTA);
        terracotta_map.put(7, Material.CYAN_GLAZED_TERRACOTTA);
        terracotta_map.put(8, Material.RED_GLAZED_TERRACOTTA);
        terracotta_map.put(9, Material.WHITE_GLAZED_TERRACOTTA);
        terracotta_map.put(10, Material.ORANGE_GLAZED_TERRACOTTA);
        terracotta_map.put(11, Material.LIME_GLAZED_TERRACOTTA);
        terracotta_map.put(12, Material.GREEN_GLAZED_TERRACOTTA);
        terracotta_map.put(13, Material.MAGENTA_GLAZED_TERRACOTTA);
        terracotta_map.put(14, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
        terracotta_map.put(15, Material.PINK_GLAZED_TERRACOTTA);
    }
    public void toggleKit(Arena arena, boolean isKit){
        arena.setDefaultKit(!isKit);
    }
    public void toggleRing(Arena arena, boolean isInRing){
        arena.setInTheRing(!isInRing);
    }
}

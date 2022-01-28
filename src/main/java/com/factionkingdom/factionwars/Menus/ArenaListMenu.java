package com.factionkingdom.factionwars.Menus;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.Instances.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class ArenaListMenu extends Menu{

    private HashMap<Integer, Material> terracotta_map = new HashMap<>();

    public ArenaListMenu(FactionWars fw, String title, Player p, Integer numRows){
        super(fw, "§6§lFactionWars§7 — §3" + title, numRows);
        setTerracotta_map();

        int counter = 0;
        int terra_index = 0;
        for (Arena arena : fw.getArenaManager().getArenas()){
            ItemStack arenaBlock = new ItemStack(terracotta_map.get(terra_index % terracotta_map.size()));
            ItemMeta aBlockMeta = arenaBlock.getItemMeta();

            aBlockMeta.setDisplayName(ChatColor.AQUA + arena.getArenaName());
            aBlockMeta.setLocalizedName("gui" + arena.getArenaName());
            arenaBlock.setItemMeta(aBlockMeta);

            registerButton(new MenuButton(arenaBlock), counter);
            counter = counter + 2;
            terra_index += 1;
        }

        setInvOpened(opened -> opened.sendMessage("§aYou opened the inventory!"));
        setInvClosed(closed -> closed.sendMessage("§cYou closed the inventory!"));

    }


    public void setTerracotta_map(){
        this.terracotta_map.put(0, Material.BLACK_GLAZED_TERRACOTTA);
        this.terracotta_map.put(2, Material.PURPLE_GLAZED_TERRACOTTA);
        this.terracotta_map.put(4, Material.YELLOW_GLAZED_TERRACOTTA);
        this.terracotta_map.put(6, Material.BLUE_GLAZED_TERRACOTTA);
        this.terracotta_map.put(7, Material.CYAN_GLAZED_TERRACOTTA);
        this.terracotta_map.put(8, Material.RED_GLAZED_TERRACOTTA);
        this.terracotta_map.put(9, Material.WHITE_GLAZED_TERRACOTTA);
        this.terracotta_map.put(10, Material.ORANGE_GLAZED_TERRACOTTA);
        this.terracotta_map.put(11, Material.LIME_GLAZED_TERRACOTTA);
        this.terracotta_map.put(12, Material.GREEN_GLAZED_TERRACOTTA);
        this.terracotta_map.put(13, Material.MAGENTA_GLAZED_TERRACOTTA);
        this.terracotta_map.put(5, Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
        this.terracotta_map.put(14, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
        this.terracotta_map.put(15, Material.PINK_GLAZED_TERRACOTTA);
        this.terracotta_map.put(1, Material.BROWN_GLAZED_TERRACOTTA);
        this.terracotta_map.put(3, Material.GRAY_GLAZED_TERRACOTTA);
    }
}

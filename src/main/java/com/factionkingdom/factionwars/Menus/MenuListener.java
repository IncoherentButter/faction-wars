package com.factionkingdom.factionwars.Menus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        /**
         * Checks if a player clicked in a registered event
         */
        Player p = (Player) e.getWhoClicked();
        Menu matchedMenu = MenuManager.getInstance().matchMenu(p.getUniqueId());
        if (matchedMenu != null){
            matchedMenu.handleClick(e, p);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        Menu matchedMenu = MenuManager.getInstance().matchMenu(p.getUniqueId());
        if (matchedMenu != null){
            matchedMenu.handleClose(p);
        }
        MenuManager.getInstance().unRegisterMenu(p.getUniqueId()); //unregister menu
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        Menu matchedMenu = MenuManager.getInstance().matchMenu(p.getUniqueId());
        if (matchedMenu != null){
            matchedMenu.handleClose(p);
        }
        MenuManager.getInstance().unRegisterMenu(p.getUniqueId());
    }
}
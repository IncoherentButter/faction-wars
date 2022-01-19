package com.factionkingdom.factionwars.Menus;

import com.factionkingdom.factionwars.FactionWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.HashMap;
import java.util.Map;

public class Menu {
    private Inventory inv;
    private Map<Integer, MenuButton> buttonMap;

    private Consumer<Player> invClosed;
    private Consumer<Player> invOpened;

    protected FactionWars fw;



    public Menu(FactionWars fw, String title, int rows){
        if (rows > 6 || rows < 1 || title.length() > 32){
            throw new IllegalArgumentException("Invalid menu arguments.");
        }
        this.inv = Bukkit.createInventory(null, rows * 9, title);
        this.buttonMap = new HashMap<>();
        this.fw = fw;
    }
    public void registerButton(MenuButton button, int slot){
        buttonMap.put(slot, button);
        System.out.println("REGISTERED BUTTON IN SLOT " + slot);
    }

    public void setInvClosed(Consumer<Player> invClosed){
        this.invClosed = invClosed;
    }
    public void setInvOpened(Consumer<Player> invOpened){
        this.invOpened = invOpened;
        System.out.println("SET INV OPENED TO " + invOpened);
    }

    public void handleClose(Player p){
        if (invClosed != null){
            invClosed.accept(p);
        }
    }
    public void handleOpen(Player p){
        if (invOpened != null){
            invOpened.accept(p);
        }
        System.out.println("JUST HANDLED OPENING INVENTORY FOR PLAYER " + p);
    }

    public void handleClick(InventoryClickEvent e, Player p){
        /**
         *
         * @param p Player who clicked
         */
        System.out.println("JUST HANDLED A CLICK");
        e.setCancelled(true);
        Inventory clicked_inv = e.getClickedInventory();
        Menu relevantArenaMenu = MenuManager.getInstance().matchMenu(p.getUniqueId());

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null){
            return;
        }
        if (buttonMap.containsKey(e.getRawSlot())){
            //Gets the clicked button's associated action
            Consumer<Player> consumer = buttonMap.get(e.getRawSlot()).getWhenClicked();
            if (consumer != null){
                //if there is an assoc. action, perform it
                consumer.accept((Player) e.getWhoClicked());
            }
        }
    }

    public void open(Player p){
        MenuManager manager = MenuManager.getInstance(); // menumanager instance
        //sets ALL buttons in the inventory
        buttonMap.forEach((slot, button) -> {
            inv.setItem(slot, button.getIs());
        });
        //Open this newly populated inventory +
        p.openInventory(inv);
        manager.registerMenu(p.getUniqueId(), this);
        handleOpen(p);
    }

}

package com.factionkingdom.factionwars.Menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

public class MenuButton {
    private ItemStack is;
    private Consumer<Player> whenClicked;

    public MenuButton(ItemStack is){
        this.is = is;
    }

    public ItemStack getIs(){return is;}
    public Consumer<Player> getWhenClicked(){
        return whenClicked;
    }

    public MenuButton setWhenClicked(Consumer<Player> cp){
        this.whenClicked = cp;
        return this;
    }


}
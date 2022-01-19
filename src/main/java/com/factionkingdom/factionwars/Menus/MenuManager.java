package com.factionkingdom.factionwars.Menus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {
    private static MenuManager instance;
    private final Map<UUID, Menu> openMenus;

    public MenuManager(){
        this.openMenus = new HashMap<>();
    }

    public static MenuManager getInstance(){
        if (instance == null){
            instance = new MenuManager();
        }
        return instance;
    }

    public void registerMenu(UUID uuid, Menu menu){
        /**
         * Registers menu to user
         */
        openMenus.put(uuid, menu);
    }

    public void unRegisterMenu(UUID uuid){
        openMenus.remove(uuid);
    }

    public Menu matchMenu(UUID uuid){
        return openMenus.get(uuid);
    }

}

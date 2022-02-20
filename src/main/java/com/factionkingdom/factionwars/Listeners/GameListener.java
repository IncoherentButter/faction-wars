package com.factionkingdom.factionwars.Listeners;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Instances.Arena;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener{
    /*
    Listens to all the events related to the actual combat section itself
     */

    /*
    - Player death
      - Check if their whole faction is gone
    - Player move
      - Check if they left the boundaries (if this is disallowed; that fact is set in (Arena? or Game?
     */

    //figure if Arena or Game holds all the boolean settings (kit, inTheRing, num of factions and players etc.)

    private FactionWars fw;

    public GameListener(FactionWars fw){
        this.fw = fw;
    }

    @EventHandler
    public void fighterDeathEvent(PlayerDeathEvent e){
        Player deadFighter = e.getEntity().getPlayer();
        Arena fighterArena = fw.getArenaManager().getArena(deadFighter);
        //if this is true, then the dead player was in an arena
        if (fighterArena != null){
            //check if they were last player fighting for their faction
            //if they were, check if there is only one faction remaining
            Faction fac = fighterArena.getFaction(deadFighter);
            if (fac != null){
                fighterArena.removeFighter(deadFighter);
            }
            //send them straight to the spectator spawn. dont let them go back to normal spawn
            //^^^ teleportation is handled by the Arena.removeFighter() method
            //  - if their kits were replaced for this fight, automatically return their kits
            //^^^ kits now handled in Arena class
        }
    }


    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent e){
        Player p = e.getPlayer();
        Arena p_arena = fw.getArenaManager().getArena(p);
        if (p_arena == null){
            return;
        }
        //If the player is a fighter in a countdown arena, block their
        //movement
        if (p_arena!= null && p_arena.getState() == GameState.COUNTDOWN){
            e.setCancelled(true);
            return;
        }

        //if the player is in an arena and that arena is set to require players to stay in the ring, then check if they left the ring
//        if (p_arena != null && p_arena.isInTheRing()){
//            //this checks if the arena's region contains the player who moved. If not, it removes them
//            if (! p_arena.getRegion().getMembers().getUniqueIds().contains(p.getUniqueId())){
//                p_arena.removeFighter(p);
//            }
//        }
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e){
        Player p = e.getPlayer();
        Arena p_arena = fw.getArenaManager().getArena(p);
        //if the player is in an arena when they quit, remove them from it
        if (p_arena != null){
            p_arena.removeFighter(p);
        }
    }
}

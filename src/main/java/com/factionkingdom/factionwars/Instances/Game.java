package com.factionkingdom.factionwars.Instances;

import com.factionkingdom.factionwars.GameState;
import org.bukkit.ChatColor;

public class Game {
    private Arena arena;

    public Game(Arena arena){
        this.arena = arena;
    }


    public void start(){
        arena.setState(GameState.LIVE);
        arena.sendMessage(ChatColor.GOLD + "Let the games begin!");

        //msgUtil.timerBroadcast(0);
        //msgUtil.broadcastLanguageTime("round_first", this.configFile.getInt("delay"));
        //SoundUtil.start();

        //add delay and start when it's done
        //            (new BukkitRunnable() {
        //                public void run() {
        //                    ArenaHandler.this.round();
        //                }
        //            }).runTaskLater(this.plugin, (20 * this.configFile.getInt("delay")));
    }
    public void stop(){
        arena.reset(true);
        arena.sendMessage(ChatColor.RED + "The Faction War has been stopped!");
    }

    public void reset(){

    }
}

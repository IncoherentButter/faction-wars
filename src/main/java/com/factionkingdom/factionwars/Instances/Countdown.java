package com.factionkingdom.factionwars.Instances;

import com.factionkingdom.factionwars.FactionWars;
import com.factionkingdom.factionwars.GameState;
import com.factionkingdom.factionwars.Listeners.GameListener;
import com.factionkingdom.factionwars.Managers.ConfigManager;
import com.factionkingdom.factionwars.Util.SoundUtil;
import com.massivecraft.factions.entity.Faction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

//import static com.massivecraft.factions.shade.me.lucko.helper.event.filter.EventHandlers.cancel;

public class Countdown extends BukkitRunnable {
    private FactionWars fw;
    private Arena arena;
    private int countdownSeconds;

    public Countdown(FactionWars fw, Arena arena){
        this.fw = fw;
        this.arena = arena;
        this.countdownSeconds = arena.getCountdownSeconds();
    }

    public void start(){
        arena.setState(GameState.COUNTDOWN);
        runTaskTimer(fw, 0, 20); //calls run() every 20 ticks, 1 sec

        fw.getMessageUtil().broadcastCountdown("countdown_begin", 0);
        SoundUtil.start();

        //teleport players to their spawns
        for (Faction fac : arena.getFactions()){
            Integer fac_num = arena.getFacNumbers().get(fac);
            Location fac_spawn = arena.getfactionSpawns().get(fac_num);
            List<UUID> fac_fighters = arena.getRoster(fac);
            for (UUID uuid : fac_fighters){
                Player fighter = Bukkit.getPlayer(uuid);
                fighter.teleport(fac_spawn);
            }
        }

        //doing this to enable the listening for player movement
        Bukkit.getPluginManager().registerEvents(new GameListener(fw), fw);


        //open the spectator warp for the public, announce its creation as /warp spec_<arena name>


        //set the kits for players if need be
        if (arena.isDefaultKit()){
            for (UUID uuid : arena.getFighters()){
                Player p = Bukkit.getPlayer(uuid);
                arena.storeKit(p);
                arena.equip(p);
            }
        }

    }

    @Override
    public void run() {
        if (countdownSeconds == 0){
            cancel(); //stops runnable
            arena.start();
            //code here to "unregister" the @EventHandler that is listening for player movement
            //IF there are no other arenas in countdown

            return;
        }
        if (countdownSeconds > 60){
            fw.getMessageUtil().broadcastCountdown("countdown", countdownSeconds);
        }
        else if (countdownSeconds <= 60 && countdownSeconds > 10){
            fw.getMessageUtil().broadcastCountdown("countdown_soon", countdownSeconds);
        } else if (countdownSeconds <= 10 && countdownSeconds >= 0){
            fw.getMessageUtil().broadcastCountdown("countdown_very_soon", countdownSeconds);
        }

        countdownSeconds--;
    }
}

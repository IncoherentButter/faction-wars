package com.factionkingdom.factionwars;

import com.factionkingdom.factionwars.Commands.FactionWarsCommand;
import com.factionkingdom.factionwars.Commands.FactionWarsCreateCommand;
import com.factionkingdom.factionwars.Commands.FactionWarsTournamentCommand;
import com.factionkingdom.factionwars.Commands.FactionWarsTournamentCreateCommand;
import com.factionkingdom.factionwars.Listeners.GameListener;
import com.factionkingdom.factionwars.Managers.ArenaManager;
import com.factionkingdom.factionwars.Managers.ConfigManager;
import com.factionkingdom.factionwars.Menus.MenuListener;
import com.factionkingdom.factionwars.Util.MessageUtil;
import com.factionkingdom.factionwars.Util.SoundUtil;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

public final class FactionWars extends JavaPlugin {

    public static StateFlag ARENA_FLAG;
    private ArenaManager arenaManager;
    private ConfigManager configManager;

    private File languageFile;
    private File arenaDataFile;

    private FileConfiguration languageConfig;
    private FileConfiguration arenaConfig;

    private MessageUtil messageUtil;
    private SoundUtil soundUtil;


    //initial plans: the defined arenas remain as WG regions at all times
    //future: store the WG regions for arenas, have all functionality work as normal,
    //BUT the wg regions get taken away when the fw is inactive for a given arena
    //this lets everyone move freely thru the region

    //futre: make configurable if a FW requires money to join


    @Override
    public void onEnable() {
        //saveDefaultConfig();
        this.configManager = new ConfigManager();
        ConfigManager.setupConfig(this);

        arenaDataFile = new File(getDataFolder(), "arenaData.yml");
        languageFile = new File(getDataFolder(), "language.yml");

        if(!arenaDataFile.exists()){
            System.out.println("FILE arenaData.yml DIDN'T EXIST, MAKING IT ANEW");
            File parent = arenaDataFile.getParentFile();
            parent.mkdirs();
            try {
                arenaDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (parent.exists() && !parent.mkdirs()) {throw new IllegalStateException("Couldn't create arenaData.yml dir: " + parent);}

            saveResource("arenaData.yml", false);
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaDataFile);

        if(!languageFile.exists()){
            System.out.println("FILE language.yml DIDN'T EXIST, MAKING IT ANEW");
            File parent = languageFile.getParentFile();
            parent.mkdirs();
            try {
                languageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (parent.exists() && !parent.mkdirs()) {throw new IllegalStateException("Couldn't create lanugage.yml dir: " + parent);}

            saveResource("language.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);


        generateFile("config");
        generateFile("language");
        generateFile("arenaData");

        messageUtil = new MessageUtil(this);
        arenaManager = new ArenaManager(this);
        soundUtil = new SoundUtil(this);

//        /**
//         * FactionsBridge stuff
//         */
//        FactionsBridge bridge = new FactionsBridge();
//        bridge.connect(this);
//        try {
//            Objects.requireNonNull(getCommand("factionsbridge")).setExecutor(this);
//        } catch (Exception e) {
//            exception(e, "Callum is an idiot.");
//        }
//        if (!FactionsBridge.get().connected()) {
//            log("FactionsBridge didn't connect.");
//            return;
//        }
//
//        // API test.
//        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
//            warn("FastAsyncWorldEdit changes the load-order of the Server. Delaying the API test by 5 seconds.");
//            Bukkit.getScheduler().runTaskLater(this, () -> {
//                int loaded = FactionsBridge.getFactionsAPI().getFactions().size();
//                warn(loaded + " factions have been loaded.");
//            }, 100L);
//        } else {
//            int loaded = FactionsBridge.getFactionsAPI().getFactions().size();
//            warn(loaded + " factions have been loaded.");
//        }
//
//        // Check for updates.
//        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
//            log("Checking for Updates.");
//            try {
//                Updater updater = new Updater(getDescription().getVersion());
//                switch (updater.getStatus()) {
//                    case BETA:
//                        log("You're using a Beta version of FactionsBridge.");
//                        break;
//                    case UP_TO_DATE:
//                        log("You're all up to date. No issues here!");
//                        break;
//                    case OUT_OF_DATE_MAJOR:
//                        error("The version of FactionsBridge you're currently using is majorly out of date.");
//                        break;
//                    case OUT_OF_DATE_MINOR:
//                        warn("The version of FactionsBridge you're currently using is out of date.");
//                        break;
//                    case OUT_OF_DATE_MINI:
//                        warn("The version of FactionsBridge you're currently using is slightly out of date.");
//                        break;
//                    default:
//                        throw new IllegalStateException("Unexpected value: " + updater.getStatus());
//                }
//            } catch (Exception e) {
//                warn("Couldn't check for updates.");
//            }
//        });




        // Plugin startup logic
        getCommand("factionwars").setExecutor(new FactionWarsCommand(this));
        getCommand("factionwarscreate").setExecutor(new FactionWarsCreateCommand(this));
        getCommand("factionwarstournament").setExecutor(new FactionWarsTournamentCommand());
        getCommand("factionwarstournamentcreate").setExecutor(new FactionWarsTournamentCreateCommand());
        //Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);




        //defining our custom WG flag
//        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
//        try{
//            StateFlag flag = new StateFlag("arena-flag", true);
//            //here we set the special WG properties for the arena flag
//            registry.register(flag);
//            ARENA_FLAG = flag;
//        } catch (FlagConflictException e){
//            //this should get caught if e.g. another plugin made a flag with the same name already
//            Flag<?> existing = registry.get("arena-flag");
//            if (existing instanceof StateFlag){
//                ARENA_FLAG = (StateFlag) existing;
//            } else{
//                System.out.println("Flag types don't match! Another plugin is using the same" +
//                        "flag name as you!);
//            }
//        }

    }

    public ArenaManager getArenaManager(){return arenaManager;}
    public MessageUtil getMessageUtil(){return messageUtil;}
    public ConfigManager getConfigManager(){return configManager;}
    public SoundUtil getSoundUtil(){return soundUtil;}

    public File getLanguageFile(){return languageFile;}
    public File getArenaDataFile(){return arenaDataFile;}

    public FileConfiguration getLanguageConfig(){return languageConfig;}
    public FileConfiguration getArenaConfig(){return arenaConfig;}


    public void saveArenaConfig(FileConfiguration arenaConfig, File arenaDataFile) {
        try {
            arenaConfig.save(arenaDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateFile(String name){
        File file = new File(getDataFolder(), name + ".yml");

        if (!file.exists()){
            System.out.println("FILE " + name + ".yml DIDN'T EXIST, MAKING IT ANEW");
            File parent = file.getParentFile();
            parent.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (parent != null && parent.exists() && !parent.mkdirs()) {throw new IllegalStateException("Couldn't create " + name + ".yml dir: " + parent);}

            saveResource(name + ".yml", false);
        }
        System.out.println("FILE " + name + ".yml ALREADY EXISTED, LOADING IT");
        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        try{
            yamlConfiguration.load(file);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

//    /**
//     * Command handler to redirect commands to their command-handling {@link ACommand} implementation.
//     *
//     * @param sender who sent the command.
//     * @param command which was called.
//     * @param label of the command.
//     * @param args to control the functionality of the command.
//     * @return {@code true} upon success.
//     */
//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
//                             @NotNull String label, @NotNull String[] args) {
//        if (args.length != 0) {
//            for (ACommand aCommand : commands) {
//                if (aCommand.isCommand(args[0])) {
//                    String[] arguments = new String[args.length - 1];
//                    System.arraycopy(args, 1, arguments, 0, args.length - 1); // Remove one entry from the beginning.
//                    aCommand.execute(sender, arguments);
//                    return true;
//                }
//            }
//        }
//        Arrays.stream(commands)
//                .map(aCommand -> translate("&b" + aCommand.getName() + " &7- &f" + aCommand.getDescription()))
//                .forEach(sender::sendMessage);
//        return false;
//    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
//        File arenaData = new File(this.getDataFolder(), "arenaData.yml");
//        FileConfiguration arenaConfig = YamlConfiguration.loadConfiguration(arenaData);
//        System.out.println("ARENA CONFIG NAMES: " + arenaConfig.getStringList("arena_names"));
    }
}

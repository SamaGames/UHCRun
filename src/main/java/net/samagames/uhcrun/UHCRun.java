package net.samagames.uhcrun;

import net.minecraft.server.v1_8_R1.BiomeBase;
import net.minecraft.server.v1_8_R1.BiomeForest;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.commands.CommandNextEvent;
import net.samagames.uhcrun.game.IGame;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.generator.FortressPopulator;
import net.samagames.uhcrun.generator.LobbyPopulator;
import net.samagames.uhcrun.generator.OrePopulator;
import net.samagames.uhcrun.generator.WorldLoader;
import net.samagames.uhcrun.listener.*;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class UHCRun extends JavaPlugin implements Listener
{

    private static UHCRun instance;
    private Location spawnLocation;
    private FileConfiguration config;
    private Logger logger;
    private BukkitTask startTimer;
    private OrePopulator populator;
    private IGame game;
    private boolean worldLoaded;
    private LobbyPopulator loobyPopulator;
    private PluginManager pluginManager;
    private WorldLoader worldLoader;
    private SamaGamesAPI samaGamesAPI;

    public static UHCRun getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        pluginManager = Bukkit.getPluginManager();
        config = this.getConfig();
        logger = this.getLogger();
        samaGamesAPI = SamaGamesAPI.get();


        this.saveResource("lobby.schematic", false);
        this.saveResource("nether.schematic", false);


        File conf = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile(), "world");
        logger.info("Checking wether world exists at : " + conf.getAbsolutePath());
        if (!conf.exists())
        {
            logger.info("No world exists. Will be generated.");
        } else
        {
            logger.info("World found!");
        }

        int playersPerTeam = getConfig().getInt("playersPerTeam", 1);


        this.game = new SoloGame((short) 20, (short) 4, (short) 10);
        pluginManager.registerEvents(this, this);

        /*if (playersPerTeam <= 1)
            game = new SoloGame();
        else
            game = new TeamGame(playersPerTeam);*/
        pluginManager.registerEvents(new ChunkListener(), this);
        pluginManager.registerEvents(new SpectatorListener(game), this);
        pluginManager.registerEvents(new LoginListener(game), this);
        pluginManager.registerEvents(new GameListener(game), this);
        pluginManager.registerEvents(new CompassTargeter(this), this);

        this.startTimer = Bukkit.getScheduler().runTaskTimer(this, this::postInit, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(final WorldInitEvent event)
    {
        World world = event.getWorld();
        if (world.getEnvironment() == World.Environment.NORMAL)
        {
            this.setupNormalWorld(world);
        }
    }

    @Override
    public void onDisable()
    {
        if (loobyPopulator != null)
            loobyPopulator.remove();
    }

    private void postInit()
    {
        samaGamesAPI.getGameManager().registerGame(game);
        getCommand("nextevent").setExecutor(new CommandNextEvent(game));
        game.setStatus(Status.STARTING);
        this.startTimer.cancel();

        this.worldLoaded = true;

        // Add the lobby
        loobyPopulator = new LobbyPopulator(this);
        loobyPopulator.generate();
        pluginManager.registerEvents(new CraftListener(), this);
        pluginManager.registerEvents(new BlockListener(), this);

        game.postInit();

        worldLoader = new WorldLoader();

        worldLoader.begin(Bukkit.getWorld("world"));

    }

    private void setupNormalWorld(World world)
    {
        try
        {
            this.patchBiomes();
        } catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }
        // Init custom ore populator
        populator = new OrePopulator();
        populator.addRule(new OrePopulator.Rule(Material.DIAMOND_ORE, 0, 4, 0, 64, 5));
        populator.addRule(new OrePopulator.Rule(Material.IRON_ORE, 0, 2, 0, 64, 15));
        populator.addRule(new OrePopulator.Rule(Material.GOLD_ORE, 0, 2, 0, 64, 8));
        populator.addRule(new OrePopulator.Rule(Material.LAPIS_ORE, 0, 3, 0, 64, 4));
        populator.addRule(new OrePopulator.Rule(Material.OBSIDIAN, 0, 4, 0, 32, 6));

        spawnLocation = new Location(world, 0.6, 152, 0.6);
        world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
        WorldBorder border = world.getWorldBorder();

        // Overworld settings
        border.setCenter(0D, 0D);
        border.setSize(1000);
        border.setWarningDistance(20);
        border.setWarningTime(0);
        border.setDamageBuffer(3D);
        border.setDamageAmount(2D);
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("randomTickSpeed", "3");
        world.setFullTime(6000);

        world.getPopulators().add(populator);
        world.getPopulators().add(new FortressPopulator(this));
    }

    public boolean isWorldLoaded()
    {
        return worldLoaded;
    }

    @EventHandler
    public void onPreJoin(PlayerJoinEvent event)
    {
        if (game == null || game.getStatus() == Status.WAITING_FOR_PLAYERS)
            event.getPlayer().teleport(spawnLocation);
    }

    public Location getSpawnLocation()
    {
        return spawnLocation;
    }

    public void removeSpawn()
    {
        loobyPopulator.remove();
    }

    public IGame getGame()
    {
        return game;
    }


    private void patchBiomes() throws ReflectiveOperationException
    {
        BiomeBase[] biomes = BiomeBase.getBiomes();
        Map<String, BiomeBase> biomesMap = BiomeBase.o;
        BiomeForest defaultBiome = new BiomeForest(0, 0);

        Field defaultBiomeField = BiomeBase.class.getDeclaredField("ad");
        this.setFinalStatic(defaultBiomeField, defaultBiome);

        biomesMap.remove("Ocean");
        biomesMap.remove("FrozenOcean");
        biomesMap.remove("FrozenRiver");
        biomesMap.remove("TaigaHills");
        biomesMap.remove("Deep Ocean");
        biomesMap.remove("Cold Beach");
        biomesMap.remove("Cold Taiga");
        biomesMap.remove("Cold Taiga Hills");
        biomesMap.remove("Mega Taiga");
        biomesMap.remove("Mega Taiga Hills");
        biomesMap.remove("Extreme Hills+");
        biomesMap.remove("Mesa");
        biomesMap.remove("Mesa Plateau F");
        biomesMap.remove("Mesa Plateau");

        for (int i = 0; i < biomes.length; i++)
        {
            if (biomes[i] != null && !biomesMap.containsKey(biomes[i].ah))
            {
                biomes[i] = null;
            }
        }

        this.setFinalStatic(BiomeBase.class.getDeclaredField("biomes"), biomes);
    }


    public void setFinalStatic(Field field, Object obj) throws ReflectiveOperationException
    {
        field.setAccessible(true);

        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, obj);
    }

    public SamaGamesAPI getAPI()
    {
        return samaGamesAPI;
    }
}

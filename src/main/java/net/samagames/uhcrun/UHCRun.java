package net.samagames.uhcrun;

import com.google.gson.Gson;
import net.samagames.uhcrun.compatibility.GameAdaptator;
import net.samagames.uhcrun.compatibility.GameProperties;
import net.samagames.uhcrun.generator.FortressPopulator;
import net.samagames.uhcrun.generator.OrePopulator;
import net.samagames.uhcrun.generator.WorldLoader;
import net.samagames.uhcrun.hook.NMSPatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
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

    private boolean worldLoaded;
    private PluginManager pluginManager;
    private WorldLoader worldLoader;
    private GameAdaptator adaptator;


    public static UHCRun getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {

        // Define the instance
        instance = this;

        pluginManager = getServer().getPluginManager();
        config = this.getConfig();
        logger = this.getLogger();

        // World Loader
        pluginManager.registerEvents(this, this);


        // Copy schematics
        this.saveResource("lobby.schematic", false);
        this.saveResource("nether.schematic", false);


        File conf = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile(), "world");
        logger.info("Checking wether world exists at : " + conf.getAbsolutePath());
        if (!conf.exists())
        {
            logger.warning("No world exists. Will be generated.");
        } else
        {
            logger.info("World found!");
        }

        if (pluginManager.isPluginEnabled("SamaGamesAPI"))
        {
            this.adaptator = new GameAdaptator(this);
            this.adaptator.onEnable();
        }

        this.startTimer = getServer().getScheduler().runTaskTimer(this, this::postInit, 20L, 20L);
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
        if (adaptator != null)
        {
            adaptator.removeSpawn();
        }
    }

    private void postInit()
    {
        World world = getServer().getWorld("world");
        this.startTimer.cancel();

        this.worldLoaded = true;

        if (pluginManager.isPluginEnabled("SamaGamesAPI"))
        {
            this.adaptator.postInit(world);
        }

        worldLoader = new WorldLoader();
        worldLoader.begin(world);

    }

    private void setupNormalWorld(World world)
    {

        GameProperties properties = new GameProperties();

        File gameJson = new File(UHCRun.getInstance().getDataFolder().getParentFile().getParentFile(), "game.json");

        if (gameJson.exists())
        {
            try
            {
                properties = new Gson().fromJson(new FileReader(gameJson), GameProperties.class);
            } catch (FileNotFoundException e)
            {
                logger.severe("game.json does not exist! THIS SHOULD BE IMPOSSIBLE!");
            }
        } else
        {
            logger.severe("game.json does not exist! THIS SHOULD BE IMPOSSIBLE!");
        }

        try
        {
            NMSPatcher patcher = new NMSPatcher(properties);
            patcher.patchBiomes();
            patcher.patchPotions();
        } catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }

        // Init custom ore populator
        populator = new OrePopulator();

        if (properties.getOptions().containsKey("ores"))
        {
            List<Map> ores = (List<Map>) properties.getOptions().get("ores");
            for (Map ore : ores)
            {
                populator.addRule(new OrePopulator.Rule((String) ore.get("id"), ((Double) ore.get("round")).intValue(), ((Double) ore.get("minY")).intValue(), ((Double) ore.get("maxY")).intValue(), ((Double) ore.get("size")).intValue()));
            }
        }

        final WorldBorder border = world.getWorldBorder();

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

        // Register Ore Populator
        world.getPopulators().add(populator);

        // Register Fortress Populator
        world.getPopulators().add(new FortressPopulator(this));
    }

    /*    private void patchBlocks() throws ReflectiveOperationException {
            this.overrideBlock(17, "log", "LOG", new BlockOldLog().c("log"));
            this.overrideBlock(162, "log2", "LOG2", new BlockNewLog().c("log"));
        }
    */
    public boolean isWorldLoaded()
    {
        return worldLoaded;
    }


    public Location getSpawnLocation()
    {
        return spawnLocation;
    }

    public void finishGeneration(World world, long time)
    {
        logger.info("Ready in " + time + "ms");
        if (adaptator != null)
        {
            worldLoader.computeTop(world);
            adaptator.loadEnd();
        } else
        {
            Bukkit.shutdown();
        }
    }

    public GameAdaptator getAdaptator()
    {
        return adaptator;
    }

    public void setSpawnLocation(Location spawnLocation)
    {
        this.spawnLocation = spawnLocation;
    }
}

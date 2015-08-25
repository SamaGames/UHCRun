package net.samagames.uhcrun;

import net.samagames.uhcrun.generator.FortressPopulator;
import net.samagames.uhcrun.generator.OrePopulator;
import net.samagames.uhcrun.generator.WorldLoader;
import net.samagames.uhcrun.hook.NMSPatcher;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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

        /*try {
            this.patchBlocks();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }*/

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
        NMSPatcher patcher = new NMSPatcher();
        try
        {
            patcher.patchBiomes();
            patcher.patchPotions();
        } catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }
        // Init custom ore populator
        populator = new OrePopulator();

        // FIXME: more modular system
        populator.addRule(new OrePopulator.Rule(Material.DIAMOND_ORE, 4, 0, 64, 5));
        populator.addRule(new OrePopulator.Rule(Material.IRON_ORE, 2, 0, 64, 15));
        populator.addRule(new OrePopulator.Rule(Material.GOLD_ORE, 2, 0, 64, 8));
        populator.addRule(new OrePopulator.Rule(Material.LAPIS_ORE, 3, 0, 64, 4));
        populator.addRule(new OrePopulator.Rule(Material.OBSIDIAN, 4, 0, 32, 6));


        // TODO: Use game.json
        List<Double> spawnPos = (List<Double>) config.getList("spawnLocation", Arrays.asList(0.6, 152D, 0.6));
        spawnLocation = new Location(world, spawnPos.get(0), spawnPos.get(1), spawnPos.get(2));

        world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());
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

    public void finishGeneration(long time)
    {
        logger.info("Ready in " + time + "ms");
        if (adaptator != null)
        {
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
}

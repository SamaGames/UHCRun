package net.samagames.uhcrun;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IManagedGame;
import net.samagames.uhcrun.generator.FortressPopulator;
import net.samagames.uhcrun.generator.LobbyPopulator;
import net.samagames.uhcrun.generator.OrePopulator;
import net.samagames.uhcrun.listener.CraftListener;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
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

    private Location spawnLocation;
    private static UHCRun instance;
    private FileConfiguration config;
    private Logger logger;
    private boolean needGen;
    private BukkitTask startTimer;
    private OrePopulator populator;
    private IManagedGame game;
    private boolean worldLoaded;
    private SamaGamesAPI api;
    private LobbyPopulator loobyPopulator;
    private PluginManager pluginManager;

    @Override
    public void onEnable()
    {
        instance = this;
        api = SamaGamesAPI.get();
        pluginManager = Bukkit.getPluginManager();
        config = this.getConfig();
        logger = this.getLogger();

        pluginManager.registerEvents(this, this);
        this.saveResource("lobby.schematic", false);
        this.saveResource("nether.schematic", false);


        File conf = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile(), "world");
        logger.info("Checking wether world exists at : " + conf.getAbsolutePath());
        if (!conf.exists())
        {
            needGen = true;
            logger.info("No world exists. Will be generated.");
        } else
        {
            logger.info("World found!");
        }

        this.startTimer = Bukkit.getScheduler().runTaskTimer(this, () -> postInit(), 20L, 20L);
    }

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event)
    {
        if (game == null)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(final WorldInitEvent event)
    {
        if (event.getWorld().getEnvironment() == World.Environment.NORMAL)
        {
            this.setupWorlds();
            event.getWorld().getPopulators().add(populator);
            event.getWorld().getPopulators().add(new FortressPopulator(this));
        }
    }

    @Override
    public void onDisable()
    {
        if(loobyPopulator != null)
            loobyPopulator.remove();
    }


    private void postInit()
    {
        this.startTimer.cancel();
        this.worldLoaded = true;

        // Add the looby
        loobyPopulator = new LobbyPopulator(this);
        loobyPopulator.generate();
        pluginManager.registerEvents(new CraftListener(), this);
    }


    public void setupWorlds()
    {
        // Init custom ore populator
        populator = new OrePopulator();
        populator.addRule(new OrePopulator.Rule(Material.DIAMOND_ORE, 0, 4, 0, 64, 5));
        populator.addRule(new OrePopulator.Rule(Material.IRON_ORE, 0, 2, 0, 64, 15));
        populator.addRule(new OrePopulator.Rule(Material.GOLD_ORE, 0, 2, 0, 64, 8));
        populator.addRule(new OrePopulator.Rule(Material.LAPIS_ORE, 0, 3, 0, 64, 4));
        populator.addRule(new OrePopulator.Rule(Material.OBSIDIAN, 0, 4, 0, 32, 6));


        World world = Bukkit.getWorlds().get(0);
        spawnLocation  = new Location(world, 0.6, 152, 0.6);
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

        System.out.println("WORLD INIT");
    }

    public boolean isWorldLoaded()
    {
        return worldLoaded;
    }

    @EventHandler
    public void onPreJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(spawnLocation);
    }
}

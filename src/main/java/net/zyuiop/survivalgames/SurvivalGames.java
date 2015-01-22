package net.zyuiop.survivalgames;

import net.minecraft.server.v1_8_R1.*;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.zyuiop.survivalgames.commands.CommandStart;
import net.zyuiop.survivalgames.game.Game;
import net.zyuiop.survivalgames.listeners.CompassTargetter;
import net.zyuiop.survivalgames.listeners.NetworkListener;
import net.zyuiop.survivalgames.generator.BlocksRule;
import net.zyuiop.survivalgames.generator.SurvivalGamesPopulator;
import net.zyuiop.survivalgames.generator.WorldGenerator;
import net.zyuiop.survivalgames.listeners.PlayerListener;
import net.zyuiop.survivalgames.listeners.SpectatorListener;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SurvivalGames extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private YamlConfiguration arenaConfig;
    public static SurvivalGames instance;
    public Game game;
    public SurvivalGamesPopulator populator;
    private BukkitTask startTimer;
    private SpawnBlock spawnBlock;
    public static boolean ready;

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(final WorldInitEvent event) {
        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {
            event.getWorld().getPopulators().add(populator);
        }
    }

    public void removeSpawn() {
        spawnBlock.remove();
    }

    public void setFinalStatic(Field field, Object obj) throws Exception {
        field.setAccessible(true);

        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~ Modifier.FINAL);

        field.set(null, obj);
    }

    @EventHandler
    public void onPreJoin(PlayerLoginEvent event) {
        if (!ready)
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Map en génération.");
    }

    public void onEnable() {
        instance = this;
        config = this.getConfig();

        File arenaFile = new File(this.getDataFolder(), "arena.yml");

        if (!arenaFile.exists())
            Bukkit.shutdown();

        populator = new SurvivalGamesPopulator();
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

        for (Object rule : arenaConfig.getList("rules")) {
            if (rule instanceof String) {
                if (rule.equals("nodiamond")) {
                    populator.replaceBlock(Material.DIAMOND_ORE, Material.STONE);
                }
                if (rule.equals("morediamond")) {
                    populator.registerRule(new BlocksRule(Material.DIAMOND_ORE, 0, 0.02, 2, 0, 16, 8));
                }

                if (rule.equals("moreiron")) {
                    populator.registerRule(new BlocksRule(Material.IRON_ORE, 0, 0.15, 2, 0, 45, 15));
                }

                if (rule.equals("moregold")) {
                    populator.registerRule(new BlocksRule(Material.GOLD_ORE, 0, 0.09, 2, 0, 35, 10));
                }

                if (rule.equals("morelapis")) {
                    populator.registerRule(new BlocksRule(Material.LAPIS_ORE, 0, 0.09, 2, 0, 30, 4));
                }

                if (rule.equals("cheat")) {
                    populator.replaceBlock(Material.LEAVES, Material.SLIME_BLOCK);
                    populator.replaceBlock(Material.GRAVEL, Material.DIAMOND_ORE);
                    populator.replaceBlock(Material.DIRT, Material.IRON_ORE);
                }
            }
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        this.saveResource("lobby.schematic", false);

        BiomeBase[] a = BiomeBase.getBiomes();
        BiomeForest nb1 = new BiomeForest(0, 0);
        BiomeForest nb2 = new BiomeForest(24, 0);

        try
        {
            Method m1 = BiomeBase.class.getMethod("b", int.class);
            m1.setAccessible(true);

            Method m2 = BiomeBase.class.getMethod("a", String.class);
            m2.setAccessible(true);

            Method m3 = BiomeBase.class.getMethod("a", int.class);
            m3.setAccessible(true);

            Method m4 = BiomeBase.class.getMethod("a", float.class, float.class);
            m4.setAccessible(true);

            Field ff = BiomeBase.class.getDeclaredField("au");
            ff.setAccessible(true);

            List<BiomeMeta> mobs = new ArrayList<>();

            mobs.add(new BiomeMeta(EntitySheep.class, 12, 4, 4));
            mobs.add(new BiomeMeta(EntityRabbit.class, 10, 3, 3));
            mobs.add(new BiomeMeta(EntityPig.class, 10, 4, 4));
            mobs.add(new BiomeMeta(EntityChicken.class, 10, 4, 4));
            mobs.add(new BiomeMeta(EntityCow.class, 8, 4, 4));
            mobs.add(new BiomeMeta(EntityWolf.class, 5, 4, 4));

            ff.set(nb1, mobs);
            ff.set(nb2, mobs);

            m1.invoke(nb1, 353825);
            m2.invoke(nb1, "Oceane");
            m3.invoke(nb1, 5159473);
            m4.invoke(nb1, 0.7F, 0.8F);

            m1.invoke(nb2, 353825);
            m2.invoke(nb2, "Deep Oceane");
            m3.invoke(nb2, 5159473);
            m4.invoke(nb2, 0.7F, 0.8F);

            Field f1 = BiomeBase.class.getDeclaredField("OCEAN");
            Field f2 = BiomeBase.class.getDeclaredField("DEEP_OCEAN");
            this.setFinalStatic(f1, nb1);
            this.setFinalStatic(f2, nb2);

            a[0] = nb1;
            a[24] = nb2;

            Field f3 = BiomeBase.class.getDeclaredField("biomes");
            this.setFinalStatic(f3, a);
        }
        catch (Exception e) {}

        this.startTimer = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getPluginManager().isPluginEnabled("MasterBundle")) {
                    finishEnabling();
                }
            }
        }, 20L, 20L);


    }

    public void finishEnabling() {
        this.startTimer.cancel();

        game = new Game(arenaConfig.getString("name", "Surprise !"));
        game.setStatus(Status.Generating);

        Bukkit.getServer().getPluginManager().registerEvents(new NetworkListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CompassTargetter(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SpectatorListener(game), this);
        getCommand("start").setExecutor(new CommandStart(game));

        GameAPI.registerGame(this.config.getString("gameName", "survivalgames"), game);
        World world = Bukkit.getWorlds().get(0);
        world.getWorldBorder().setCenter(0D, 0D);
        world.getWorldBorder().setSize(500);
        world.getWorldBorder().setWarningDistance(20);
        world.getWorldBorder().setDamageBuffer(3D);
        world.getWorldBorder().setDamageAmount(2D);
        WorldGenerator.begin(world);
    }

    public void finishGeneration() {
        spawnBlock = new SpawnBlock(this);
        ready = true;
        spawnBlock.generate();
        game.updateStatus(Status.Available);
    }
}

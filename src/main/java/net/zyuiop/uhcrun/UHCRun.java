package net.zyuiop.uhcrun;

import net.minecraft.server.v1_8_R1.*;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.zyuiop.uhcrun.commands.CommandNextEvent;
import net.zyuiop.uhcrun.commands.CommandStart;
import net.zyuiop.uhcrun.commands.CommandUHC;
import net.zyuiop.uhcrun.game.BasicGame;
import net.zyuiop.uhcrun.game.SoloGame;
import net.zyuiop.uhcrun.game.TeamGame;
import net.zyuiop.uhcrun.listeners.CompassTargetter;
import net.zyuiop.uhcrun.listeners.NetworkListener;
import net.zyuiop.uhcrun.generator.BlocksRule;
import net.zyuiop.uhcrun.generator.SurvivalGamesPopulator;
import net.zyuiop.uhcrun.generator.WorldGenerator;
import net.zyuiop.uhcrun.listeners.PlayerListener;
import net.zyuiop.uhcrun.listeners.SpectatorListener;
import net.zyuiop.uhcrun.utils.Gui;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UHCRun extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private YamlConfiguration arenaConfig;
    public static UHCRun instance;
    public BasicGame game;
    public SurvivalGamesPopulator populator;
    private BukkitTask startTimer;
    private SpawnBlock spawnBlock;
    public static boolean ready;
    public static boolean isWorldLoaded = false;
    private HashMap<UUID, Gui> playersGui = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(final WorldInitEvent event) {
        if (event.getWorld().getEnvironment() == World.Environment.NORMAL) {
            event.getWorld().getPopulators().add(populator);
        }
    }

    @Override
    public void onDisable() {
        GameAPI.getArena().setStatus(Status.Stopping);
        GameAPI.getManager().sendArena();
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

    public void openGui(Player player, Gui gui) {
        if(this.playersGui.containsKey(player.getUniqueId())) {
            player.closeInventory();
            this.playersGui.remove(player.getUniqueId());
        }

        this.playersGui.put(player.getUniqueId(), gui);
        gui.display(player);
    }

    public void closeGui(Player player) {
        if(this.playersGui.containsKey(player.getUniqueId())) {
            player.closeInventory();
            this.playersGui.remove(player.getUniqueId());
        }
    }

    public void removePlayerFromList(UUID uuid)
    {
        if(this.playersGui.containsKey(uuid))
        {
            this.playersGui.remove(uuid);
        }
    }

    public Gui getPlayerGui(UUID uuid)
    {
        if(this.playersGui.containsKey(uuid))
        {
            return this.playersGui.get(uuid);
        }
        else
        {
            return null;
        }
    }


    public void onEnable() {
        instance = this;
        config = this.getConfig();

        File arenaFile = new File(this.getDataFolder(), "arena.yml");

        if (!arenaFile.exists())
            Bukkit.shutdown();

        populator = new SurvivalGamesPopulator();
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

        populator.registerRule(new BlocksRule(Material.DIAMOND_ORE, 0, 4, 0, 64, 5));
        populator.registerRule(new BlocksRule(Material.IRON_ORE, 0, 2, 0, 64, 15));
        populator.registerRule(new BlocksRule(Material.GOLD_ORE, 0, 2, 0, 64, 8));
        populator.registerRule(new BlocksRule(Material.LAPIS_ORE, 0, 3, 0, 64, 4));
        populator.registerRule(new BlocksRule(Material.OBSIDIAN, 0, 4, 0, 32, 6));

        Bukkit.getPluginManager().registerEvents(this, this);
        this.saveResource("lobby.schematic", false);
        this.saveResource("nether.schematic", false);

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
            m2.invoke(nb1, "Forest");
            m3.invoke(nb1, 5159473);
            m4.invoke(nb1, 0.7F, 0.8F);

            m1.invoke(nb2, 353825);
            m2.invoke(nb2, "Forest");
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
                    isWorldLoaded = true;
                    finishEnabling();
                }
            }
        }, 20L, 20L);


    }

    public void finishEnabling() {
        this.startTimer.cancel();

        int playersPerTeam = getConfig().getInt("playersPerTeam", 1);
        if (playersPerTeam <= 1)
            game = new SoloGame();
        else
            game = new TeamGame(playersPerTeam);
        game.setStatus(Status.Generating);

        Bukkit.getServer().getPluginManager().registerEvents(new NetworkListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CompassTargetter(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SpectatorListener(game), this);
        getCommand("start").setExecutor(new CommandStart(game));
        getCommand("uhcrun").setExecutor(new CommandUHC());
        getCommand("nextevent").setExecutor(new CommandNextEvent(game));

        GameAPI.registerGame(this.config.getString("gameName", "uhcrun"), game);
        World world = Bukkit.getWorlds().get(0);
        world.getWorldBorder().setCenter(0D, 0D);
        world.getWorldBorder().setSize(1000);
        world.getWorldBorder().setWarningDistance(20);
        world.getWorldBorder().setDamageBuffer(3D);
        world.getWorldBorder().setDamageAmount(2D);
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("randomTickSpeed", "45");

        final ShapedRecipe cobblePickaxe = new ShapedRecipe(new ItemStack(Material.STONE_PICKAXE));
        cobblePickaxe.shape("WWW", "ASA", "ASA");
        cobblePickaxe.setIngredient('W', Material.WOOD);
        cobblePickaxe.setIngredient('S', Material.STICK);
        cobblePickaxe.setIngredient('A', Material.AIR);

        final ShapedRecipe cobbleAxe = new ShapedRecipe(new ItemStack(Material.STONE_AXE));
        cobbleAxe.shape("WWA", "WSA", "ASA");
        cobbleAxe.setIngredient('W', Material.WOOD);
        cobbleAxe.setIngredient('S', Material.STICK);
        cobbleAxe.setIngredient('A', Material.AIR);

        final ShapedRecipe cobbleAxeB = new ShapedRecipe(new ItemStack(Material.STONE_AXE));
        cobbleAxeB.shape("AWW", "ASW", "ASA");
        cobbleAxeB.setIngredient('W', Material.WOOD);
        cobbleAxeB.setIngredient('S', Material.STICK);
        cobbleAxeB.setIngredient('A', Material.AIR);

        final ShapedRecipe cobbleSword = new ShapedRecipe(new ItemStack(Material.STONE_SWORD));
        cobbleSword.shape("AWA", "AWA", "ASA");
        cobbleSword.setIngredient('W', Material.WOOD);
        cobbleSword.setIngredient('S', Material.STICK);
        cobbleSword.setIngredient('A', Material.AIR);

		final ShapedRecipe cobbleShoveel = new ShapedRecipe(new ItemStack(Material.STONE_SPADE));
		cobbleShoveel.shape("AWA", "ASA", "ASA");
		cobbleShoveel.setIngredient('W', Material.WOOD);
		cobbleShoveel.setIngredient('S', Material.STICK);
		cobbleShoveel.setIngredient('A', Material.AIR);

        getServer().addRecipe(cobbleAxe);
        getServer().addRecipe(cobblePickaxe);
        getServer().addRecipe(cobbleSword);
        getServer().addRecipe(cobbleAxeB);
		getServer().addRecipe(cobbleSword);

        WorldGenerator.begin(world);
    }

    public void finishGeneration() {
        spawnBlock = new SpawnBlock(this);
        ready = true;
        spawnBlock.generate();
        game.updateStatus(Status.Available);
    }
}

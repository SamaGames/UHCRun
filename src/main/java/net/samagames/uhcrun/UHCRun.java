package net.samagames.uhcrun;

import net.minecraft.server.v1_8_R2.Block;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.uhcrun.commands.CommandNextEvent;
import net.samagames.uhcrun.commands.CommandStart;
import net.samagames.uhcrun.commands.CommandUHC;
import net.samagames.uhcrun.game.BasicGame;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.generator.BlocksRule;
import net.samagames.uhcrun.generator.SurvivalGamesPopulator;
import net.samagames.uhcrun.listeners.CompassTargetter;
import net.samagames.uhcrun.listeners.NetworkListener;
import net.samagames.uhcrun.listeners.PlayerListener;
import net.samagames.uhcrun.listeners.SpectatorListener;
import net.samagames.uhcrun.utils.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
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
    public static boolean gen = false;

   /* public void onLoad() {
        try {
            BlockObsidian obsi = new BlockObsidian();
            Method c = Block.class.getDeclaredMethod("c", float.class);
            c.setAccessible(true);
            Method b = Block.class.getDeclaredMethod("b", float.class);
            b.setAccessible(true);
            Method a = Block.class.getDeclaredMethod("a", StepSound.class);
            a.setAccessible(true);
            Method cname = Block.class.getDeclaredMethod("c", String.class);
            cname.setAccessible(true);

            obsi = (BlockObsidian) c.invoke(obsi, 0F);
            obsi = (BlockObsidian) b.invoke(obsi, 0F);
            obsi = (BlockObsidian) a.invoke(obsi, new StepSound("stone", 1.0F, 1.0F));
            obsi = (BlockObsidian) cname.invoke(obsi, "obsidian");
            Method register = Block.class.getDeclaredMethod("a", int.class, String.class, Block.class);
            register.setAccessible(true);
            register.invoke(null, 49, "obsidian", obsi);

            Field strenght = Block.class.getDeclaredField("strength");
            Field dur = Block.class.getDeclaredField("durability");
            strenght.setAccessible(true);
            dur.setAccessible(true);
            getLogger().info("Patched NMS obsidian !");
            getLogger().info("Properties of OBSIDIAN : " + strenght.getFloat(Block.getById(49)));
            getLogger().info("Properties of OBSIDIAN : " + dur.getFloat(Block.getById(49)));
            getLogger().info("Checking registered obsidian :");
            Object o = Block.REGISTRY.get(new MinecraftKey("obsidian"));
            getLogger().info("Object : " + o.toString());
            getLogger().info("Properties of OBSIDIAN : " + strenght.getFloat(o));
            getLogger().info("Properties of OBSIDIAN : " + dur.getFloat(o));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @EventHandler
    public void onChunkUnload(final ChunkUnloadEvent event) {
        if (game == null || !game.isDamages())
            event.setCancelled(true);
    }

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

        getLogger().info("Patching NMS classes...");


        File conf = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile(), "world");
        getLogger().info("Checking wether world exists at : " + conf.getAbsolutePath());
        if (!conf.exists()) {
            gen = true;
            getLogger().info("No world exists. Will be generated.");
        } else {
            getLogger().info("World found ! ");
        }

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

    protected Block getBlock(Block block, float first, float second, Block.StepSound i) {
        Method method = null;
        try {
            method = Block.class.getDeclaredMethod("c", float.class);
            method.setAccessible(true);
            block = (Block) method.invoke(block, first);

            method = Block.class.getDeclaredMethod("b", float.class);
            method.setAccessible(true);
            block = (Block) method.invoke(block, second);

            method = Block.class.getDeclaredMethod("a", Block.StepSound.class);
            method.setAccessible(true);
            block = (Block) method.invoke(block, i);
            return block;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return block;
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
        world.setGameRuleValue("randomTickSpeed", "3");

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

        if (gen)
            net.samagames.uhcrun.generator.WorldGenerator.begin(world);
        else
            net.samagames.uhcrun.generator.WorldLoader.begin(world);
    }

    public void finishGeneration() {
        spawnBlock = new SpawnBlock(this);
        ready = true;
        spawnBlock.generate();
        game.updateStatus(Status.Available);
    }
}

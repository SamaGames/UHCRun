package net.samagames.uhcrun;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IManagedGame;
import net.samagames.uhcrun.commands.CommandUHC;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.game.TeamGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class UHCRun extends JavaPlugin implements Listener {

    private static UHCRun instance;
    private FileConfiguration config;
    private BukkitTask startTimer;
    private boolean gen;
    private boolean isWorldLoaded;
    private SamaGamesAPI api;
    private boolean ready;
    private IManagedGame game;

    public void onEnable() {
        instance = this;
        api = SamaGamesAPI.get();

        config = this.getConfig();

        File arenaFile = new File(this.getDataFolder(), "arena.yml");

        if (!arenaFile.exists())
            Bukkit.shutdown();

        /*populator = new SurvivalGamesPopulator();
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);*/

        /*populator.registerRule(new BlocksRule(Material.DIAMOND_ORE, 0, 4, 0, 64, 5));
        populator.registerRule(new BlocksRule(Material.IRON_ORE, 0, 2, 0, 64, 15));
        populator.registerRule(new BlocksRule(Material.GOLD_ORE, 0, 2, 0, 64, 8));
        populator.registerRule(new BlocksRule(Material.LAPIS_ORE, 0, 3, 0, 64, 4));
        populator.registerRule(new BlocksRule(Material.OBSIDIAN, 0, 4, 0, 32, 6));*/

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

        this.startTimer = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("SamaGamesAPI")) {
                isWorldLoaded = true;
                finishEnabling();
            }
        }, 20L, 20L);


    }

    private void finishEnabling() {
        this.startTimer.cancel();

        int playersPerTeam = getConfig().getInt("playersPerTeam", 1);
        if (playersPerTeam <= 1)
            game = new SoloGame();
        else
            game = new TeamGame(playersPerTeam);
        //game.setStatus(Status.STARTING);

        Bukkit.getServer().getPluginManager().registerEvents(new NetworkListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(game), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CompassTargetter(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SpectatorListener(game), this);
        getCommand("uhcrun").setExecutor(new CommandUHC());
        getCommand("nextevent").setExecutor(new CommandNextEvent(game));

        api.getGameManager().registerGame(game);
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

    public static UHCRun getInstance() {
        return instance;
    }

    public boolean isWorldLoaded() {
        return isWorldLoaded;
    }

    public void finishGeneration() {
        spawnBlock = new SpawnBlock(this);
        ready = true;
        spawnBlock.generate();
        //game.updateStatus(Status.WAITING_FOR_PLAYERS);
    }

    public IManagedGame getGame() {
        return game;
    }
}

package net.samagames.uhcrun.game;

import com.google.gson.JsonPrimitive;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.samagames.api.games.IGameProperties;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.api.games.themachine.messages.IMessageManager;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.compatibility.GameAdaptator;
import net.samagames.uhcrun.listener.ChunkListener;
import net.samagames.uhcrun.task.GameLoop;
import net.samagames.uhcrun.utils.Colors;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.security.SecureRandom;
import java.util.*;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public abstract class AbstractGame extends net.samagames.api.games.Game<UHCPlayer>
{
    protected final UHCRun plugin;
    protected final GameAdaptator adaptator;
    protected final Server server;
    protected final SecureRandom rand;
    protected final List<Location> spawnPoints;
    protected final TreeMap<UUID, UHCPlayer> prevInGame;
    private final int maxSpawnLocations;
    private final int minPlayers;
    private GameLoop gameLoop;
    private IMessageManager messageManager;
    private Scoreboard scoreboard;
    private boolean pvpEnabled;
    private BukkitTask mainTask;
    private boolean damages;
    private final int preparingTime;
    private final int deathMatchSize;
    private final int reductionTime;

    public AbstractGame(UHCRun plugin , IGameProperties properties)
    {
        super("UHCRun", "UHCRun", "Vous jouez en " + properties.getMapName().replaceAll("_", " "), UHCPlayer.class);
        this.status = Status.STARTING;
        this.plugin = plugin;
        this.adaptator = plugin.getAdaptator();
        this.server = plugin.getServer();
        this.rand = new SecureRandom();
        this.maxSpawnLocations = properties.getMaxSlots();
        this.spawnPoints = new ArrayList<>();
        this.prevInGame = new TreeMap<>();
        UHCPlayer.setGame(this);

        this.preparingTime = properties.getOption("preparingTime", new JsonPrimitive(20)).getAsInt();
        this.deathMatchSize = properties.getOption("deathMatchSize", new JsonPrimitive(400)).getAsInt();
        this.reductionTime = properties.getOption("reductionTime", new JsonPrimitive(10)).getAsInt();
        this.minPlayers = properties.getMinSlots();
    }

    public void postInit(World world)
    {
        this.scoreboard = server.getScoreboardManager().getMainScoreboard();
        this.gameLoop = new GameLoop(this, plugin, server);
        this.disableDamages();
        this.computeSpawnLocations(world);
    }

    private void computeSpawnLocations(World world)
    {
        for (int i = 0; i < maxSpawnLocations; i++)
        {
            final Location randomLocation = new Location(world, MathHelper.nextInt(rand, -450, 450), 150, MathHelper.nextInt(rand, -450, 450));
            for (int y = 0; y < 16; y++)
            {
                world.getChunkAt(world.getBlockAt(randomLocation.getBlockX(), y * 16, randomLocation.getBlockZ())).load(true);
            }

            spawnPoints.add(randomLocation);
        }

        // Shuffle locations
        Collections.shuffle(this.spawnPoints);
    }

    @Override
    public void handlePostRegistration()
    {
        super.handlePostRegistration();
        this.messageManager = coherenceMachine.getMessageManager();
    }

    protected void removeFromGame(UUID uuid)
    {
        UHCPlayer player = this.gamePlayers.get(uuid);
        if (player != null && player.getPlayerIfOnline() != null)
        {
            player.setSpectator();
        }
    }

    public UHCRun getPlugin()
    {
        return plugin;
    }

    public GameAdaptator getAdaptator()
    {
        return adaptator;
    }

    @Override
    public void startGame()
    {
        super.startGame();
        adaptator.removeSpawn();

        Objective displayNameLife = scoreboard.registerNewObjective("vie", "health");
        Objective playerListLife = scoreboard.registerNewObjective("vieb", "health");

        playerListLife.setDisplayName(ChatColor.RED + "♥");
        displayNameLife.setDisplayName("HP");
        displayNameLife.setDisplaySlot(DisplaySlot.BELOW_NAME);
        playerListLife.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        this.mainTask = server.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);
        teleport();

        for (UUID uuid : getInGamePlayers().keySet())
        {
            Player player = server.getPlayer(uuid);
            if (player == null)
            {
                gamePlayers.remove(uuid);
                continue;
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setScoreboard(scoreboard);
            displayNameLife.getScore(player.getName()).setScore(20);
            playerListLife.getScore(player.getName()).setScore(20);
            player.setLevel(0);
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 24000, 0));
            ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "= UHCRun =");
            sign.addReceiver(player);
            gameLoop.addPlayer(player.getUniqueId(), sign);
        }
        server.getPluginManager().registerEvents(new ChunkListener(plugin), plugin);
    }

    @Override
    public void handleModeratorLogin(Player player) {
        super.handleModeratorLogin(player);
        this.rejoin(player, true);
    }

    public void rejoin(Player thePlayer, boolean isModerator)
    {
        if (thePlayer != null)
        {
            server.getScheduler().runTaskLater(plugin, () -> {

                thePlayer.setScoreboard(this.scoreboard);
                ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "= UHCRun =");
                sign.addReceiver(thePlayer);
                this.gameLoop.addPlayer(thePlayer.getUniqueId(), sign);
            }, 10L);

        }
    }

    public void stumpPlayer(Player player, boolean logout)
    {
        if (this.status == Status.IN_GAME)
        {
            Object lastDamager = Metadatas.getMetadata(plugin, player, "lastDamager");
            Player killer = null;
            if (lastDamager != null && lastDamager instanceof Player)
            {
                killer = (Player) lastDamager;
                if (killer.isOnline() && this.isInGame(killer.getUniqueId()))
                {
                    this.creditKillCoins(getPlayer(killer.getUniqueId()).addKill());
                    this.increaseStat(killer.getUniqueId(), "kills", 1);
                    killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));
                } else
                {
                    killer = null;
                }
            }

            if (logout)
            {
                messageManager.writePlayerReconnectTimeOut(player);
            } else if (killer != null)
            {
                server.broadcastMessage(this.coherenceMachine.getGameTag() + " " + player.getDisplayName() + ChatColor.GOLD + " a été tué par " + killer.getDisplayName());
            } else
            {
                server.broadcastMessage(this.coherenceMachine.getGameTag() + " " + player.getDisplayName() + ChatColor.GOLD + " est mort.");
            }

            this.checkStump(player);

            if (!logout)
            {
                this.increaseStat(player.getUniqueId(), "deaths", 1);
                Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
                removeFromGame(player.getUniqueId());
                player.setGameMode(GameMode.SPECTATOR);
                player.setHealth(20.0D);
            }
        }
    }

    protected void creditKillCoins(UHCPlayer player)
    {
        player.addCoins(20, "Un joueur tué !");
    }

    public boolean isInGame(UUID uniqueId)
    {
        return this.gamePlayers.containsKey(uniqueId) && !this.gamePlayers.get(uniqueId).isSpectator();
    }

    private String getDamageCause(EntityDamageEvent.DamageCause cause)
    {
        switch (cause)
        {
            case SUFFOCATION:
                return "Suffocation";
            case FALL:
                return "Chute";
            case FIRE:
            case FIRE_TICK:
                return "Feu";
            case LAVA:
                return "Lave";
            case DROWNING:
                return "Noyade";
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return "Explosion";
            case LIGHTNING:
                return "Foudre";
            case POISON:
                return "Poison";
            case MAGIC:
                return "Potion";
            case FALLING_BLOCK:
                return "Chute de blocs";
            default:
                return "Autre";
        }
    }

    public abstract void checkStump(Player player);

    protected abstract void teleport();

    public abstract void teleportDeathMatch();

    public int getPreparingTime()
    {
        return preparingTime;
    }

    public int getDeathMatchSize()
    {
        return deathMatchSize;
    }

    public int getReductionTime()
    {
        return reductionTime;
    }


    public void disablePVP()
    {
        this.pvpEnabled = false;
    }

    public void enablePVP()
    {
        this.pvpEnabled = true;
    }

    public boolean isPvpEnabled()
    {
        return pvpEnabled;
    }

    public void enableDamages()
    {
        this.damages = true;
    }

    public void disableDamages()
    {
        this.damages = false;
    }


    public boolean isDamagesEnabled()
    {
        return damages;
    }

    public GameLoop getGameLoop()
    {
        return gameLoop;
    }

    public ICoherenceMachine getCoherenceMachine()
    {
        return coherenceMachine;
    }

    public void effectsOnWinner(Player player)
    {
        server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            int timer = 0;

            @Override
            public void run()
            {
                if (this.timer < 20)
                {
                    Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    Random r = new Random();
                    int rt = r.nextInt(4) + 1;
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (rt == 1)
                    {
                        type = FireworkEffect.Type.BALL;
                    }

                    if (rt == 2)
                    {
                        type = FireworkEffect.Type.BALL_LARGE;
                    }

                    if (rt == 3)
                    {
                        type = FireworkEffect.Type.BURST;
                    }

                    if (rt == 4)
                    {
                        type = FireworkEffect.Type.CREEPER;
                    }

                    if (rt == 5)
                    {
                        type = FireworkEffect.Type.STAR;
                    }

                    int r1i = r.nextInt(15) + 1;
                    int r2i = r.nextInt(15) + 1;
                    Color c1 = Colors.getColor(r1i);
                    Color c2 = Colors.getColor(r2i);
                    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
                    fwm.addEffect(effect);
                    int rp = r.nextInt(2) + 1;
                    fwm.setPower(rp);
                    fw.setFireworkMeta(fwm);
                    this.timer++;
                }
            }
        }, 5L, 5L);
    }

    @Override
    public void handleGameEnd()
    {
        this.interrupt();
        super.handleGameEnd();
    }

    @Override
    public void handleReconnectTimeOut(Player player)
    {
        this.setSpectator(player);
        this.stumpPlayer(player, true);
    }

    public void interrupt()
    {
        this.mainTask.cancel();
    }

    public void resume()
    {
        this.mainTask = server.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);
    }
}
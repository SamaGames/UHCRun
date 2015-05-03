package net.samagames.uhcrun.game;

import com.google.gson.Gson;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.CoherenceMachine;
import net.samagames.gameapi.themachine.messages.MessageManager;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.database.IDatabase;
import net.samagames.uhcrun.game.data.StoredGame;
import net.samagames.uhcrun.task.BeginCountdown;
import net.samagames.utils.ObjectiveSign;
import net.zyuiop.MasterBundle.MasterBundle;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.ShardedJedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class Game implements IGame
{

    private final String mapName;
    private final short normalSlots, vipSlots;
    protected final UHCRun plugin;
    private final BukkitTask beginCountdown;
    private final MessageManager messageManager;
    protected AbstractSet<UUID> players;
    protected AbstractMap<UUID, Integer> kills;
    protected Status status;
    private StoredGame storedGame;
    private BukkitTask mainTask;
    private Scoreboard scoreboard;
    private Objective life;
    private CoherenceMachine coherenceMachine;
    private ObjectiveSign sign;
    private GameLoop gameLoop;

    public Game(String mapName, short normalSlots, short vipSlots, short minPlayers)
    {
        this.plugin = UHCRun.getInstance();
        this.status = Status.Idle;
        this.players = new CopyOnWriteArraySet<>();
        this.kills = new ConcurrentHashMap<>();
        this.mapName = mapName;
        this.normalSlots = normalSlots;
        this.vipSlots = vipSlots;

        this.coherenceMachine = new CoherenceMachine("UHCRun");

        this.messageManager = this.coherenceMachine.getMessageManager();
        this.beginCountdown = Bukkit.getScheduler().runTaskTimer(plugin, new BeginCountdown(this, getMaxPlayers(), minPlayers), 20L, 20L);
    }

    @Override
    public void postInit()
    {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    @Override
    public void start()
    {
        storedGame = new StoredGame(MasterBundle.getServerName(), System.currentTimeMillis(), mapName);
        plugin.removeSpawn();
        updateStatus(Status.InGame);

        life = scoreboard.registerNewObjective("vie", "health");
        Objective lifeb = scoreboard.registerNewObjective("vieb", "health");
        life.setDisplaySlot(DisplaySlot.BELOW_NAME);
        lifeb.setDisplayName("HP");
        life.setDisplayName("HP");
        lifeb.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "? UHCRun ?");

        if (beginCountdown != null) beginCountdown.cancel();

        gameLoop = new GameLoop(this);
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);

        //teleportPlayers();

        for (UUID uuid : players)
        {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
            {
                players.remove(uuid);
                return;
            }

            try
            {
                StatsApi.increaseStat(uuid, "uhcrun", "played", 1);
            } catch (Exception ignored)
            {
            }

            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setScoreboard(scoreboard);
            life.getScore(player.getName()).setScore(20);
            lifeb.getScore(player.getName()).setScore(20);
            player.setLevel(0);
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20 * 20, 0));
            sign.addReceiver(player);
            gameLoop.addPlayer(player.getUniqueId(), sign);
        }

        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

    @Override
    public void finish()
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            storedGame.setEndTime(System.currentTimeMillis());
            String json = new Gson().toJson(storedGame);
            IDatabase database = plugin.getDatabse();
            String gameId = MasterBundle.getServerName() + System.currentTimeMillis();
            database.hset("uhcrungames", gameId, json);

            TreeMap<UUID, Integer> ranks = new TreeMap<>((a, b) -> {
                Integer ka = kills.get(a);
                Integer kb = kills.get(b);
                ka = (ka == null) ? 0 : ka;
                kb = (kb == null) ? 0 : kb;
                if (ka >= kb)
                {
                    return -1;
                } else
                {
                    return 1;
                }
            });
            ranks.putAll(kills);
            Iterator<Map.Entry<UUID, Integer>> ids = ranks.entrySet().iterator();
            String[] top = new String[]{"", "", ""};
            int i = 0;
            while (i < 3 && ids.hasNext())
            {
                Map.Entry<UUID, Integer> val = ids.next();
                top[i] = Bukkit.getOfflinePlayer(val.getKey()).getName() + "" + ChatColor.AQUA + " (" + val.getValue() + ")";
                CoinsManager.creditJoueur(val.getKey(), (3 - i) * 10, true, true, "Rang " + (i + 1) + " au classement de kills !");
                i++;
            }

            Bukkit.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
            Bukkit.broadcastMessage(ChatColor.GOLD + "                        Classement Kills      ");
            Bukkit.broadcastMessage(ChatColor.GOLD + "                                                    ");
            Bukkit.broadcastMessage(ChatColor.YELLOW + " " + top[0] + ChatColor.GRAY + "  " + top[1] + ChatColor.GOLD + "  " + top[2]);
            Bukkit.broadcastMessage(ChatColor.GOLD + "                                                    ");
            Bukkit.broadcastMessage(ChatColor.GOLD + " Visualisez votre " + ChatColor.RED + ChatColor.BOLD + "d�briefing de partie" + ChatColor.GOLD + " ici : ");
            Bukkit.broadcastMessage(ChatColor.AQUA + " http://samagames.net/uhcrun/" + gameId);
            Bukkit.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> mainTask.cancel(), 30L);
        setStatus(Status.Stopping);
        GameAPI.getManager().sendArena();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(GameAPI::kickPlayer);
            Bukkit.getServer().shutdown();
        }, 20 * 30);
    }

    @Override
    public void join(Player player)
    {
        players.add(player.getUniqueId());
        messageManager.writeWelcomeInGameMessage(player);
        messageManager.writePlayerJoinArenaMessage(player, this);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(plugin.getSpawnLocation());
    }

    @Override
    public void quit(Player player)
    {

    }

    @Override
    public boolean hasTeleportPlayers()
    {
        return status != Status.Available && status != Status.Generating;
    }

    @Override
    public void enableDamages()
    {

    }

    @Override
    public void disableDamages()
    {

    }

    @Override
    public void enablePVP()
    {
        this.plugin.getServer()
    }

    @Override
    public int countGamePlayers()
    {
        return players.size();
    }

    @Override
    public int getMaxPlayers()
    {
        return normalSlots;
    }

    @Override
    public int getTotalMaxPlayers()
    {
        return getMaxPlayers() + getVIPSlots();
    }

    @Override
    public int getVIPSlots()
    {
        return vipSlots;
    }

    @Override
    public Status getStatus()
    {
        return status;
    }

    @Override
    public void setStatus(Status status)
    {
        this.status = status;
    }

    @Override
    public String getMapName()
    {
        return mapName;
    }

    @Override
    public boolean hasPlayer(UUID uuid)
    {
        return players.contains(uuid);
    }

    public CoherenceMachine getCoherenceMachine()
    {
        return coherenceMachine;
    }

    @Override
    public int getKills(UUID player)
    {
        return kills.get(player);
    }

    @Override
    public int getPreparingTime()
    {
        return 20;
    }

    @Override
    public void teleportDeathMatch()
    {

    }

    @Override
    public int getDeathMatchSize()
    {
        return 0;
    }

    @Override
    public int getReductionTime()
    {
        return 10;
    }
}

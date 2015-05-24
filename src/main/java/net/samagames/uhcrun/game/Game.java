package net.samagames.uhcrun.game;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.CoherenceMachine;
import net.samagames.gameapi.themachine.messages.MessageManager;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.database.IDatabase;
import net.samagames.uhcrun.game.data.SavedPlayer;
import net.samagames.uhcrun.game.data.StoredGame;
import net.samagames.uhcrun.task.BeginCountdown;
import net.samagames.uhcrun.task.GameLoop;
import net.samagames.uhcrun.utils.Metadatas;
import net.samagames.utils.ObjectiveSign;
import net.samagames.utils.Titles;
import net.zyuiop.MasterBundle.MasterBundle;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

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
public abstract class Game implements IGame
{

    protected final UHCRun plugin;
    private final String mapName;
    private final short normalSlots, vipSlots;
    private final BukkitTask beginCountdown;
    private final MessageManager messageManager;
    protected AbstractSet<UUID> players = new CopyOnWriteArraySet<>();
    protected AbstractMap<UUID, Integer> kills = new ConcurrentHashMap<>();
    protected Status status;
    private StoredGame storedGame;
    private BukkitTask mainTask;
    private Scoreboard scoreboard;
    private Objective life;
    private CoherenceMachine coherenceMachine;
    private GameLoop gameLoop;
    private boolean pvpEnabled;
    private boolean damages;

    public Game(String mapName, short normalSlots, short vipSlots, short minPlayers)
    {
        this.plugin = UHCRun.getInstance();
        this.status = Status.Idle;
        this.mapName = mapName;
        this.normalSlots = normalSlots;
        this.vipSlots = vipSlots;
        this.coherenceMachine = new CoherenceMachine("UHCRun");
        this.messageManager = this.coherenceMachine.getMessageManager();
        this.beginCountdown = Bukkit.getScheduler().runTaskTimer(plugin, new BeginCountdown(this, getMaxPlayers(), minPlayers, 121), 20L, 20L);
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

        if (beginCountdown != null) beginCountdown.cancel();

        gameLoop = new GameLoop(this);
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);
        teleport();

        for (UUID uuid : players)
        {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
            {
                players.remove(uuid);
                continue;
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 24000, 0));
            ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "≡ UHCRun ≡");
            sign.addReceiver(player);
            gameLoop.addPlayer(player.getUniqueId(), sign);
            kills.put(uuid, 0);
        }

        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

    protected abstract void teleport();

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

        Bukkit.getScheduler().runTaskLater(plugin, mainTask::cancel, 20);
        setStatus(Status.Stopping);
        GameAPI.getManager().sendArena();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try
            {
                Bukkit.getOnlinePlayers().forEach(GameAPI::kickPlayer);
            } catch (Exception ex)
            {
            }
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
        player.sendMessage("Tracker: https://docs.google.com/document/d/1AqPxBYF7QyJ5LE0mVhNDfbOgnOJKH8yBw9aGfliNCx0/edit?usp=sharing");
        player.teleport(plugin.getSpawnLocation());
    }

    @Override
    public void quit(Player player)
    {

    }

    @Override
    public boolean hasTeleportPlayers()
    {
        return status != Status.InGame;
    }

    @Override
    public void enableDamages()
    {
        this.damages = true;
    }

    @Override
    public void disableDamages()
    {
        this.damages = false;
    }

    @Override
    public boolean isDamagesEnabled()
    {
        return damages;
    }

    @Override
    public void disablePVP()
    {
        this.pvpEnabled = false;
    }

    @Override
    public void enablePVP()
    {
        this.pvpEnabled = true;
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
    public abstract void teleportDeathMatch();

    @Override
    public int getDeathMatchSize()
    {
        return 400;
    }

    @Override
    public int getReductionTime()
    {
        return 10;
    }

    @Override
    public boolean isPvpEnabled()
    {
        return pvpEnabled;
    }

    @Override
    public boolean isInGame(UUID player)
    {
        return players.contains(player);
    }

    @Override
    public void stumpPlayer(Player player, boolean logout)
    {
        this.players.remove(player.getUniqueId());
        if (this.status == Status.InGame)
        {
            Object lastDamager = Metadatas.getMetadata(player, "lastDamager");
            Player killer = null;
            SavedPlayer e;
            if (lastDamager != null && lastDamager instanceof Player)
            {
                killer = (Player) lastDamager;
                if (killer.isOnline() && this.isInGame(killer.getUniqueId()))
                {
                    this.creditKillCoins(killer);

                    try
                    {
                        StatsApi.increaseStat(killer, "uhcrun", "kills", 1);
                        this.addKill(killer.getUniqueId());
                        e = this.storedGame.getPlayer(killer.getUniqueId(), killer.getName());
                        e.kill(player);
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));
                    } catch (Exception ex)
                    {
                    }
                } else
                {
                    killer = null;
                }
            }

            if (logout)
            {
                Bukkit.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s\'est déconnecté.");
            } else if (killer != null)
            {
                Bukkit.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " a été tué par " + killer.getDisplayName());
            } else
            {
                Bukkit.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " est mort.");
            }

            this.checkStump(player);

            try
            {
                e = this.storedGame.getPlayer(player.getUniqueId(), player.getName());
                String killedBy;
                if (logout)
                {
                    killedBy = "Déconnexion";
                } else if (killer != null)
                {
                    killedBy = killer.getDisplayName();
                } else
                {
                    EntityDamageEvent.DamageCause cause = player.getLastDamageCause().getCause();
                    killedBy = getDamageCause(cause);
                }

                e.die(this.players.size(), killedBy, System.currentTimeMillis() - this.storedGame.getStartTime());
            } catch (Exception var9)
            {
                var9.printStackTrace();
            }

            player.setGameMode(GameMode.SPECTATOR);
            player.setHealth(20.0D);
            if (!logout)
            {
                try
                {
                    StatsApi.increaseStat(player, "uhcrun", "stumps", 1);
                } catch (Exception ex)
                {
                }

                Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
            }
        }
    }

    @Override
    public void addKill(UUID player)
    {
        Integer val = this.kills.get(player);

        // impossible but check it by security
        if (val == null) val = 0;

        this.kills.put(player, val + 1);
    }

    public String getDamageCause(EntityDamageEvent.DamageCause cause)
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

    public GameLoop getGameLoop()
    {
        return gameLoop;
    }
}

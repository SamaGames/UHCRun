package net.samagames.uhcrun.game;

import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.CoherenceMachine;
import net.samagames.api.games.themachine.messages.MessageManager;
import net.samagames.api.player.PlayerData;
import net.samagames.api.stats.StatsManager;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.data.SavedPlayer;
import net.samagames.uhcrun.game.data.StoredGame;
import net.samagames.uhcrun.task.BeginCountdown;
import net.samagames.uhcrun.task.GameLoop;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
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
public abstract class Game extends AbstractGame
{

    protected final UHCRun plugin;
    private final String mapName;
    private final short normalSlots, vipSlots, minPlayers;
    private BukkitTask beginCountdown;
    private MessageManager messageManager;
    protected AbstractSet<UUID> players = new CopyOnWriteArraySet<>();
    protected List<UUID> disconnected = new ArrayList<>();
    protected Map<UUID, PlayerData> playerDataCaches = new HashMap<>();
    protected AbstractMap<UUID, Integer> kills = new ConcurrentHashMap<>();
    protected Status status;
    protected CoherenceMachine coherenceMachine;
    protected StatsManager stats;
    private StoredGame storedGame;
    private BukkitTask mainTask;
    private Scoreboard scoreboard;
    private Objective life;
    private GameLoop gameLoop;
    private boolean pvpEnabled;
    private boolean damages;

    public Game(String mapName, short normalSlots, short vipSlots, short minPlayers)
    {
        this.plugin = UHCRun.getInstance();
        this.status = Status.NOT_RESPONDING;
        this.mapName = mapName;
        this.normalSlots = normalSlots;
        this.vipSlots = vipSlots;
        this.minPlayers = minPlayers;
        this.stats = plugin.getAPI().getStatsManager("uhcrun");

    }

    @Override
    public void postInit()
    {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.coherenceMachine = plugin.getAPI().getGameManager().getCoherenceMachine();
        this.messageManager = this.coherenceMachine.getMessageManager();
        this.beginCountdown = Bukkit.getScheduler().runTaskTimer(plugin, new BeginCountdown(this, getMaxPlayers(), minPlayers, 121), 20L, 20L);
    }

    @Override
    public void startGame()
    {
        storedGame = new StoredGame(plugin.getAPI().getServerName(), System.currentTimeMillis(), mapName);
        plugin.removeSpawn();
        updateStatus(Status.IN_GAME);

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
                stats.increase(uuid, "played", 1);
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
            String gameId = plugin.getAPI().getServerName() + System.currentTimeMillis();
            plugin.getAPI().getResource().hset("uhcrungames", gameId, json);

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
                getPlayerData(val.getKey()).creditCoins((3 - i) * 10, "Rang " + (i + 1) + " au classement de kills !", true);
                i++;
            }

            Bukkit.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
            Bukkit.broadcastMessage(ChatColor.GOLD + "                        Classement Kills      ");
            Bukkit.broadcastMessage(ChatColor.GOLD + "                                                    ");
            Bukkit.broadcastMessage(ChatColor.YELLOW + " " + top[0] + ChatColor.GRAY + "  " + top[1] + ChatColor.GOLD + "  " + top[2]);
            Bukkit.broadcastMessage(ChatColor.GOLD + "                                                    ");
            Bukkit.broadcastMessage(ChatColor.GOLD + " Visualisez votre " + ChatColor.RED + ChatColor.BOLD + "débriefing de partie" + ChatColor.GOLD + " ici : ");
            Bukkit.broadcastMessage(ChatColor.AQUA + " http://samagames.net/uhcrun/" + gameId);
            Bukkit.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
        });

        Bukkit.getScheduler().runTaskLater(plugin, mainTask::cancel, 20);
        setStatus(Status.REBOOTING);
        plugin.getAPI().getGameManager().refreshArena();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try
            {
                Bukkit.getOnlinePlayers().forEach(player -> plugin.getAPI().getGameManager().kickPlayer(player, "#FinDuGame"));
            } catch (Exception ex)
            {
            }
            Bukkit.getServer().shutdown();
        }, 20 * 30);
    }

    @Override
    public void playerJoin(Player player)
    {
        players.add(player.getUniqueId());
        messageManager.writeWelcomeInGameToPlayer(player);
        messageManager.writePlayerJoinToAll(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(ChatColor.GOLD + "Cette partie utilise une version Beta de l'UHCRun, des bugs peuvent survenir");
        //player.sendMessage(ChatColor.AQUA + "Report de bugs: https://bitbucket.org/samagames/uhcrun/issues");
        player.teleport(plugin.getSpawnLocation());
    }

    @Override
    public void playerDisconnect(Player player)
    {
        if (this.getStatus() == Status.IN_GAME)
        {
            this.gameLoop.removePlayer(player.getUniqueId());
            if (this.isPvpEnabled())
            {
                this.stumpPlayer(player, true);
                Location time = player.getLocation();
                World w = time.getWorld();
                ItemStack[] var4 = player.getInventory().getContents();

                for (ItemStack stack : var4)
                {
                    if (stack != null)
                    {
                        w.dropItemNaturally(time, stack);
                    }
                }
            } else
            {
                int var8 = this.getPreparingTime() * 60 - this.gameLoop.getTime();

                // ...

                disconnected.add(player.getUniqueId());
                Bukkit.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s\'est déconnecté. Il peut se reconnecter jusqu\'à la fin de la préparation.");
            }
        } else
        {
            this.players.remove(player.getUniqueId());
        }


    }


    @Override
    public void playerReconnect(Player player)
    {
        this.rejoin(player);
    }

    @Override
    public void playerReconnectTimeOut(Player player)
    {
        this.rejoin(player);
    }

    public void rejoin(Player pl)
    {

        if (pl != null)
        {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {

                pl.setScoreboard(this.scoreboard);
                ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "≡ UHCRun ≡");
                sign.addReceiver(pl);
                this.gameLoop.addPlayer(pl.getUniqueId(), sign);
                Bukkit.broadcastMessage(this.coherenceMachine.getGameTag() + pl.getDisplayName() + ChatColor.GOLD + " s\'est reconnecté.");
                disconnected.remove(pl.getUniqueId());
            }, 10L);

        }


    }

    @Override
    public boolean hasTeleportPlayers()
    {
        return status != Status.IN_GAME;
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
    public int getConnectedPlayers()
    {
        return players.size();
    }

    @Override
    public int getMaxPlayers()
    {
        return normalSlots + vipSlots;
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
    public String getGameName()
    {
        return plugin.getConfig().getString("gameName", "uhcrun");
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
        if (this.status == Status.IN_GAME)
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
                        stats.increase(killer.getUniqueId(), "kills", 1);
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
                    stats.increase(player.getUniqueId(), "stumps", 1);
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

    @Override
    public void startFight()
    {
        disconnected.clear();
    }

    @Override
    public boolean isDisconnected(UUID player)
    {
        return disconnected.contains(player);
    }


    @Override
    public PlayerData getPlayerData(UUID uuid)
    {
        return !playerDataCaches.containsKey(uuid) ? plugin.getAPI().getPlayerManager().getPlayerData(uuid) : playerDataCaches.get(uuid);
    }

    @Override
    public PlayerData getPlayerData(Player player)
    {
        return getPlayerData(player.getUniqueId());
    }
}

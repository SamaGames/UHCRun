package net.zyuiop.uhcrun.game;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.GameUtils;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.CoherenceMachine;
import net.samagames.gameapi.themachine.messages.MessageManager;
import net.samagames.gameapi.types.GameArena;
import net.samagames.utils.ObjectiveSign;
import net.samagames.utils.Titles;
import net.zyuiop.statsapi.StatsApi;
import net.zyuiop.uhcrun.UHCRun;
import net.zyuiop.uhcrun.tasks.BeginCountdown;
import net.zyuiop.uhcrun.tasks.GameLoop;
import net.zyuiop.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by vialarl on 16/01/2015.
 */
public abstract class BasicGame implements GameArena {
    protected CopyOnWriteArraySet<UUID> players = new CopyOnWriteArraySet<>();
    protected int maxPlayers = 20;
    protected int minPlayers = 7;
    protected int vipPlayers = 4;
    protected Status status;
    protected String mapName;
    protected UHCRun plugin;
    protected boolean pvpenabled = false;
    protected MessageManager messageManager;
    protected CoherenceMachine coherenceMachine;
    protected BukkitTask beginCountdown;
    protected BukkitTask mainLoop;
    protected ObjectiveSign objectiveSign;
    protected Objective life;
    public Scoreboard scoreboard;
    private boolean damages = false;
    protected ArrayList<SpawnLocation> spawns = new ArrayList<>();
    private GameLoop gameLoop;

    public BasicGame(String map, int min, int max, int vip) {
        maxPlayers = max;
        minPlayers = min;
        vipPlayers = vip;
        mapName = map;

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.status = Status.Idle;
        this.coherenceMachine = new CoherenceMachine("UHCRun");
        this.messageManager = new MessageManager(coherenceMachine);
        beginCountdown = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, new BeginCountdown(this, maxPlayers, minPlayers), 20L, 20L);
    }

    public void enablePVP() {
        this.pvpenabled = true;
    }

    public void disableDamages() {
        this.damages = false;
    }

    public Iterator<UUID> getPlayersIterator() {
        return players.iterator();
    }

    public void start() {
		updateStatus(Status.InGame);
        UHCRun.instance.removeSpawn();
        objectiveSign = new ObjectiveSign("sggameloop", ChatColor.AQUA +""+ChatColor.ITALIC + "≡ UHCRun ≡");
        life = scoreboard.registerNewObjective("vie", "health");
        Objective lifeb = scoreboard.registerNewObjective("vieb", "health");
        life.setDisplaySlot(DisplaySlot.BELOW_NAME);
        lifeb.setDisplayName("HP");
        life.setDisplayName("HP");
        lifeb.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        if (beginCountdown != null)
            beginCountdown.cancel();

        gameLoop = new GameLoop(this, objectiveSign);
        mainLoop = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, gameLoop, 20, 20);

        teleportAtStart();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            try {
                StatsApi.increaseStat(uuid, "uhcrun", "played", 1);
            } catch (Exception ignored) {}

            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setScoreboard(scoreboard);
            life.getScore(player.getName()).setScore(20);
            lifeb.getScore(player.getName()).setScore(20);
            player.setLevel(0);
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20 * 20, 0));
            objectiveSign.addReceiver(player);
		}

        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

    public abstract void teleportAtStart();

    public void stumpPlayer(Player player, boolean logout) {
        players.remove(player.getUniqueId());
        if (status != Status.InGame)
            return;

        Object lastDamager = Metadatas.getMetadata(player, "lastDamager");
        Player killer = null;
        if (lastDamager != null && lastDamager instanceof Player) {
            killer = (Player) lastDamager;
            if (!killer.isOnline() || !isInGame(killer.getUniqueId()))
                killer = null;
            else {
                creditKillCoins(killer);
                try {
                    StatsApi.increaseStat(killer, "uhcrun", "kills", 1);
                } catch (Exception ignored) {}
            }
        }

        if (logout)
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s'est déconnecté.");
        else if (killer != null)
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " a été tué par "+ killer.getDisplayName());
        else
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " est mort.");

        checkStump(player);

        if (logout)
            return;

        try {
            StatsApi.increaseStat(player, "uhcrun", "stumps", 1);
        } catch (Exception ignored) {}

        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20.0); // Le joueur ne voit pas l'écran de mort
        Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
    }

    public abstract void creditKillCoins(Player player);

    public abstract void checkStump(Player player);

    public boolean isDamages() {
        return damages;
    }

    public void finish() {
        mainLoop.cancel();
        setStatus(Status.Stopping);
        GameAPI.getManager().sendArena();
        Bukkit.getScheduler().runTaskLater(UHCRun.instance, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers())
                    GameAPI.kickPlayer(player);

                Bukkit.getServer().shutdown();
            }
        }, 20*30);
    }

    public void join(Player player) {
        players.add(player.getUniqueId());
        messageManager.writeWelcomeInGameMessage(player);
        messageManager.writePlayerJoinArenaMessage(player, this);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 162, 0));
    }

    public boolean isPvpenabled() {
        return pvpenabled;
    }

    public boolean isInGame(UUID player) {
        return players.contains(player);
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void enableDamage() {
        this.damages = true;
    }

    public abstract void teleportDeathmatch();
    /*
        Utils
     */

    public void updateStatus(Status status) {
        setStatus(status);
        GameAPI.getManager().sendArena();
    }

    @Override
    public int countGamePlayers() {
        return players.size();
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public int getTotalMaxPlayers() {
        return getMaxPlayers() + getVIPSlots();
    }

    @Override
    public int getVIPSlots() {
        return vipPlayers;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return players.contains(player);
    }

    public CoherenceMachine getCoherenceMachine() {
        return coherenceMachine;
    }

    public GameLoop getGameLoop() {
        return gameLoop;
    }

    public boolean isGameStarted() {
        return (status == Status.InGame);
    }
}

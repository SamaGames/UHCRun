package net.samagames.uhcrun.game;

import com.google.gson.Gson;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.IMasterControlledGame;
import net.samagames.api.games.IReconnectGame;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.CoherenceMachine;
import net.samagames.api.games.themachine.messages.MessageManager;
import net.samagames.api.network.JoinResponse;
import net.samagames.core.api.games.themachine.CoherenceMachineImpl;
import net.samagames.core.api.games.themachine.messages.MessageManagerImpl;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.datasaver.SavedPlayer;
import net.samagames.uhcrun.datasaver.StoredGame;
import net.samagames.uhcrun.tasks.BeginCountdown;
import net.samagames.uhcrun.tasks.GameLoop;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by vialarl on 16/01/2015.
 */
public abstract class BasicGame implements IMasterControlledGame, IReconnectGame {
    protected final SamaGamesAPI api;
    protected CopyOnWriteArraySet<UUID> players = new CopyOnWriteArraySet<>();
    protected int maxPlayers = 24;
    protected int minPlayers = 7;
    protected int vipPlayers = 4;
    protected Status status;
    protected String mapName;
    protected UHCRun plugin;
    protected boolean pvpEnabled = false;
    protected MessageManager messageManager;
    protected CoherenceMachine coherenceMachine;
    protected BukkitTask beginCountdown;
    protected BukkitTask mainLoop;
    protected Objective life;
    public Scoreboard scoreboard;
    private boolean damages = false;
    protected ArrayList<SpawnLocation> spawns = new ArrayList<>();
	protected ConcurrentHashMap<UUID, Integer> kills = new ConcurrentHashMap<>();
    private GameLoop gameLoop;
    private StoredGame storedGame;

    public BasicGame(String map, int min, int max, int vip) {
        api = SamaGamesAPI.get();
        maxPlayers = max;
        minPlayers = min;
        vipPlayers = vip;
        mapName = map;

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.status = Status.STARTING;
        this.coherenceMachine = SamaGamesAPI.get().getGameManager().getCoherenceMachine();
        this.messageManager = this.coherenceMachine.getMessageManager();
        beginCountdown = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, new BeginCountdown(this, maxPlayers, minPlayers), 20L, 20L);
    }

	public void addKill(UUID player) {
		Integer val = kills.get(player);
		if (val == null)
			val = 0;
		kills.put(player, val+1);
	}

	public int countKills(UUID player) {
		Integer val = kills.get(player);
		return (val == null) ? 0 : val;
	}

    public void enablePVP() {
        this.pvpEnabled = true;
    }

    public void disableDamages() {
        this.damages = false;
    }

    public Iterator<UUID> getPlayersIterator() {
        return players.iterator();
    }

    public void startGame() {
        // Init stored game //
        storedGame = new StoredGame(api.getServerName(), System.currentTimeMillis(), mapName);

		updateStatus(Status.IN_GAME);
        UHCRun.instance.removeSpawn();
        life = scoreboard.registerNewObjective("vie", "health");
        Objective lifeb = scoreboard.registerNewObjective("vieb", "health");
        life.setDisplaySlot(DisplaySlot.BELOW_NAME);
        lifeb.setDisplayName("HP");
        life.setDisplayName("HP");
        lifeb.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        if (beginCountdown != null)
            beginCountdown.cancel();

        gameLoop = new GameLoop(this);
        mainLoop = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, gameLoop, 20, 20);

        teleportAtStart();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            try {
                api.getStatsManager("uhcrun").increase(uuid, "played", 1);
            } catch (Exception ignored) {}

            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setScoreboard(scoreboard);
            life.getScore(player.getName()).setScore(20);
            lifeb.getScore(player.getName()).setScore(20);
            player.setLevel(0);
            player.getInventory().clear();
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20 * 20, 0));
			ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD +""+ChatColor.ITALIC + ChatColor.BOLD + "≡ UHCRun ≡");
			sign.addReceiver(player);
			gameLoop.addPlayer(player.getUniqueId(), sign);
		}

        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

    public abstract void teleportAtStart();

    public void stumpPlayer(Player player, boolean logout) {
        players.remove(player.getUniqueId());
        if (status != Status.IN_GAME)
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
                    api.getStatsManager("uhcrun").increase(killer.getUniqueId(), "kills", 1);
					addKill(killer.getUniqueId());

                    SavedPlayer savedPlayer = storedGame.getPlayer(killer.getUniqueId(), killer.getName());
                    savedPlayer.kill(player);
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

        try {
            // Sauvegarde
            SavedPlayer savedPlayer = storedGame.getPlayer(player.getUniqueId(), player.getName());
            String killedBy;
            if (logout)
                killedBy = "Déconnexion";
            else if (killer != null)
                killedBy = killer.getDisplayName();
            else {
                EntityDamageEvent.DamageCause cause = player.getLastDamageCause().getCause();
                killedBy = getDamageCause(cause);
            }
            savedPlayer.die(players.size(), killedBy, System.currentTimeMillis() - storedGame.getStartTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logout)
            return;

        try {
            api.getStatsManager("uhcrun").increase(player.getUniqueId(), "stumps", 1);
        } catch (Exception ignored) {}

        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20.0); // Le joueur ne voit pas l'écran de mort
        Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
    }

    public StoredGame getStoredGame() {
        return storedGame;
    }

    public static String getDamageCause(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
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

    public abstract void creditKillCoins(Player player);

    public abstract void checkStump(Player player);

    public boolean isDamages() {
        return damages;
    }

    public void finish() {
        Bukkit.getScheduler().runTaskAsynchronously(UHCRun.instance, () -> {
            storedGame.setEndTime(System.currentTimeMillis());
            String json = new Gson().toJson(storedGame);
            Jedis jedis = api.getResource();
            String gameId = api.getServerName() + System.currentTimeMillis();
            jedis.hset("uhcrungames", gameId, json);

            TreeMap<UUID, Integer> ranks = new TreeMap<>((a, b) -> {
                Integer ka = kills.get(a);
                Integer kb = kills.get(b);
                ka = (ka == null) ? 0 : ka;
                kb = (kb == null) ? 0 : kb;
                if (ka >= kb) {
                    return -1;
                } else {
                    return 1;
                }
            });
            ranks.putAll(kills);
            Iterator<Map.Entry<UUID, Integer>> ids = ranks.entrySet().iterator();
            String[] top = new String[]{"", "", ""};
            int i = 0;
            while (i < 3 && ids.hasNext()) {
                Map.Entry<UUID, Integer> val = ids.next();
                top[i] = Bukkit.getOfflinePlayer(val.getKey()).getName() + "" + ChatColor.AQUA + " (" + val.getValue() + ")";
                SamaGamesAPI.get().getPlayerManager().getPlayerData(val.getKey()).creditCoins((3 - i) * 10, "Rang " + (i + 1) + " au classement de kills !", true);
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

        Bukkit.getScheduler().runTaskLater(UHCRun.instance, () -> mainLoop.cancel(), 30L);
        //setStatus(Status.Stopping);

        // FIXME: no stopping status?
        api.getGameManager().setStatus(Status.FINISHED);
        Bukkit.getScheduler().runTaskLater(UHCRun.instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                api.getGameManager().kickPlayer(player, "");

            Bukkit.getServer().shutdown();
        }, 20 * 30);
    }

    @Override
    public void playerJoin(Player player) {
        players.add(player.getUniqueId());
        messageManager.writeWelcomeInGameToPlayer(player);
        messageManager.writePlayerJoinToAll(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(new Location(Bukkit.getWorld("world"), 0, 162, 0));
    }

    @Override
    public void playerDisconnect(Player player) {

    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
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
        api.getGameManager().setStatus(status);
        api.getGameManager().refreshArena();
    }

    @Override
    public int getConnectedPlayers() {
        return players.size();
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    public CoherenceMachine getCoherenceMachine() {
        return coherenceMachine;
    }

    public GameLoop getGameLoop() {
        return gameLoop;
    }

    public boolean isGameStarted() {
        return (status == Status.IN_GAME);
    }


    public boolean hasPlayer(UUID player) {
        return players.contains(player);
    }

    public String getGameName() { return "uhcrun";}

    @Override
    public JoinResponse requestJoin(UUID uuid, JoinResponse joinResponse) {

        return joinResponse;
    }

    @Override
    public JoinResponse requestPartyJoin(UUID uuid, Set<UUID> set, JoinResponse joinResponse) {
        return joinResponse;
    }

    @Override
    public void onModerationJoin(Player player) {

    }

    @Override
    public void playerReconnect(Player player) {

    }

    @Override
    public void playerReconnectTimeOut(Player player) {

    }

    public SamaGamesAPI getAPI()
    {
        return api;
    }
}

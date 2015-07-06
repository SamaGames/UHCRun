package net.samagames.uhcrun.game;

import com.google.gson.Gson;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.api.games.themachine.messages.IMessageManager;
import net.samagames.api.player.AbstractPlayerData;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.data.SavedPlayer;
import net.samagames.uhcrun.game.data.StoredGame;
import net.samagames.uhcrun.task.BeginCountdown;
import net.samagames.uhcrun.task.GameLoop;
import net.samagames.uhcrun.utils.Colors;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
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
public abstract class Game extends net.samagames.api.games.Game {

    protected AbstractMap<UUID, Integer> kills = new ConcurrentHashMap<>();
    protected AbstractSet<UUID> players = new CopyOnWriteArraySet<>();
    protected ICoherenceMachine coherenceMachine;
    protected List<UUID> disconnected = new ArrayList<>();
    protected Map<UUID, AbstractPlayerData> playerDataCaches = new HashMap<>();
    protected Status status;
    protected final Server server;
    protected final UHCRun plugin;
    protected final List<Location> spawnPoints;
    protected final Random rand;

    private StoredGame storedGame;
    private Scoreboard scoreboard;
    private GameLoop gameLoop;
    private boolean pvpEnabled, damages;
    private BukkitTask beginCountdown, mainTask;
    private IMessageManager messageManager;
    private final int minPlayers, maxLocations;


    public Game(int maxLocations) {
        super("UHCRun", UHCRun.getInstance().getConfig().getString("gameName", "UHCRun"), GamePlayer.class);
        this.plugin = UHCRun.getInstance();
        this.server = plugin.getServer();
        this.status = Status.NOT_RESPONDING;
        this.spawnPoints = new ArrayList<>();
        this.rand = new Random();
        this.maxLocations = maxLocations;
        this.minPlayers = plugin.getAPI().getGameManager().getGameProperties().getMinSlots();
    }

    public void postInit(World world) {
        this.scoreboard = server.getScoreboardManager().getMainScoreboard();
        this.coherenceMachine = plugin.getAPI().getGameManager().getCoherenceMachine();
        this.messageManager = this.coherenceMachine.getMessageManager();
        this.beginCountdown = server.getScheduler().runTaskTimer(plugin, new BeginCountdown(this, SamaGamesAPI.get().getGameManager().getGameProperties().getMaxSlots(), minPlayers, 121), 20L, 20L);
        this.disableDamages();
        this.computeSpawnLocations(world);
    }

    private void computeSpawnLocations(World world) {
        for (int i = 0; i < maxLocations; i++) {
            final Location randomLocation = new Location(world, -500 + rand.nextInt(500 - (-500) + 1), 150, -500 + rand.nextInt(500 - (-500) + 1));
            for (int y = 0; y < 16; y++) {
                world.getChunkAt(world.getBlockAt(randomLocation.getBlockX(), y * 16, randomLocation.getBlockZ())).load(true);
            }

            spawnPoints.add(randomLocation);
        }

        // Shuffle locations
        Collections.shuffle(this.spawnPoints);
    }

    @Override
    public void startGame() {
        // Init Online Stats Data
        storedGame = new StoredGame(plugin.getAPI().getServerName(), System.currentTimeMillis(), this.getClass().getSimpleName());

        // Remove the lobby
        plugin.removeSpawn();

        // Notify to the network that the game is starting
        setStatus(Status.IN_GAME);

        Objective displayNameLife = scoreboard.registerNewObjective("vie", "health");
        Objective playerListLife = scoreboard.registerNewObjective("vieb", "health");

        playerListLife.setDisplayName("HP");
        displayNameLife.setDisplayName("HP");
        displayNameLife.setDisplaySlot(DisplaySlot.BELOW_NAME);
        playerListLife.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        if (beginCountdown != null) {
            beginCountdown.cancel();
        }

        gameLoop = new GameLoop(this, plugin, server);
        mainTask = server.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);
        teleport();

        for (UUID uuid : players) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                continue;
            }

            try {
                this.increaseStat(uuid, "played", 1);
            } catch (Exception ignored) {
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
            kills.put(uuid, 0);
        }

        server.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

    public void finish() {
        server.getScheduler().runTaskAsynchronously(plugin, () -> {
            storedGame.setEndTime(System.currentTimeMillis());
            String json = new Gson().toJson(storedGame);
            String gameId = plugin.getAPI().getServerName() + System.currentTimeMillis();
            plugin.getAPI().getResource().hset("uhcrungames", gameId, json);

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
                top[i] = server.getOfflinePlayer(val.getKey()).getName() + "" + ChatColor.AQUA + " (" + val.getValue() + ")";
                getPlayerData(val.getKey()).creditCoins((3 - i) * 10, "Rang " + (i + 1) + " au classement de kills !", true);
                i++;
            }

            server.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
            server.broadcastMessage(ChatColor.GOLD + "                        Classement Kills      ");
            server.broadcastMessage(ChatColor.GOLD + "                                                    ");
            server.broadcastMessage(ChatColor.YELLOW + " " + top[0] + ChatColor.GRAY + "  " + top[1] + ChatColor.GOLD + "  " + top[2]);
            server.broadcastMessage(ChatColor.GOLD + "                                                    ");
            server.broadcastMessage(ChatColor.GOLD + " Visualisez votre " + ChatColor.RED + ChatColor.BOLD + "débriefing de partie" + ChatColor.GOLD + " ici : ");
            server.broadcastMessage(ChatColor.AQUA + " http://samagames.net/uhcrun/" + gameId);
            server.broadcastMessage(ChatColor.GOLD + "----------------------------------------------------");
        });

        server.getScheduler().runTaskLater(plugin, mainTask::cancel, 20);
        setStatus(Status.REBOOTING);
        plugin.getAPI().getGameManager().refreshArena();
        server.getScheduler().runTaskLater(plugin, () -> {
            try {
                server.getOnlinePlayers().forEach(player -> plugin.getAPI().getGameManager().kickPlayer(player, null));
                Thread.sleep(10000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            server.shutdown();
        }, 20 * 10);
    }


    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
        super.setStatus(status);
    }

    protected abstract void teleport();

    public boolean hasTeleportPlayers() {
        return status == Status.IN_GAME;
    }

    public void enableDamages() {
        this.damages = true;
    }

    public void disableDamages() {
        this.damages = false;
    }

    public boolean isDamagesEnabled() {
        return damages;
    }

    public void disablePVP() {
        this.pvpEnabled = false;
    }

    public void enablePVP() {
        this.pvpEnabled = true;
    }

    @Override
    public int getConnectedPlayers() {
        return players.size();
    }

    @Override
    public final String getGameName() {
        return plugin.getConfig().getString("gameName", "UHCRun");
    }

    public final ICoherenceMachine getCoherenceMachine() {
        return coherenceMachine;
    }

    public int getKills(UUID player) {
        return kills.get(player);
    }


    // FIXME: more modular system
    public int getPreparingTime() {
        return 20;
    }

    public int getDeathMatchSize() {
        return 400;
    }

    public int getReductionTime() {
        return 10;
    }

    public abstract void teleportDeathMatch();

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public boolean isInGame(UUID player) {
        return players.contains(player);
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public void stumpPlayer(Player player, boolean logout) {
        this.players.remove(player.getUniqueId());
        if (this.status == Status.IN_GAME) {
            Object lastDamager = Metadatas.getMetadata(plugin, player, "lastDamager");
            Player killer = null;
            SavedPlayer e;
            if (lastDamager != null && lastDamager instanceof Player) {
                killer = (Player) lastDamager;
                if (killer.isOnline() && this.isInGame(killer.getUniqueId())) {
                    this.creditKillCoins(getPlayerData(killer));

                    try {
                        this.increaseStat(killer.getUniqueId(), "kills", 1);
                        this.addKill(killer.getUniqueId());
                        e = this.storedGame.getPlayer(killer.getUniqueId(), killer.getName());
                        e.kill(player);
                        killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));
                    } catch (Exception ex) {
                    }
                } else {
                    killer = null;
                }
            }

            if (logout) {
                server.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s\'est déconnecté.");
            } else if (killer != null) {
                server.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " a été tué par " + killer.getDisplayName());
            } else {
                server.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " est mort.");
            }

            this.checkStump(player);

            try {
                e = this.storedGame.getPlayer(player.getUniqueId(), player.getName());
                String killedBy;
                if (logout) {
                    killedBy = "Déconnexion";
                } else if (killer != null) {
                    killedBy = killer.getDisplayName();
                } else {
                    EntityDamageEvent.DamageCause cause = player.getLastDamageCause().getCause();
                    killedBy = getDamageCause(cause);
                }

                e.die(this.players.size(), killedBy, System.currentTimeMillis() - this.storedGame.getStartTime());
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            player.setGameMode(GameMode.SPECTATOR);
            player.setHealth(20.0D);
            if (!logout) {
                try {
                    this.increaseStat(player.getUniqueId(), "stumps", 1);
                } catch (Exception ex) {
                }

                Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
            }
        }
    }

    public void addKill(UUID player) {
        Integer val = this.kills.get(player);

        // impossible but check it by security
        if (val == null) {
            val = 0;
        }

        this.kills.put(player, val + 1);
    }

    public void creditKillCoins(AbstractPlayerData killer) {
        killer.creditCoins(20, "Un joueur tué !", true);
    }

    public abstract void checkStump(Player player);

    public final GameLoop getGameLoop() {
        return gameLoop;
    }

    public void startFight() {
        disconnected.clear();
    }

    public AbstractPlayerData getPlayerData(UUID uuid) {
        return !playerDataCaches.containsKey(uuid) ? plugin.getAPI().getPlayerManager().getPlayerData(uuid) : playerDataCaches.get(uuid);
    }

    public AbstractPlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public UHCRun getPlugin() {
        return plugin;
    }

    // --- CONNECTION --- //

    @Override
    public void handleLogin(Player player, boolean reconnect) {

        if (!reconnect) {
            this.join(player);
        } else {
            this.rejoin(player, false);
        }

    }

    public void join(Player player) {
        players.add(player.getUniqueId());
        messageManager.writeWelcomeInGameToPlayer(player);
        messageManager.writePlayerJoinToAll(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(plugin.getSpawnLocation());
    }

    @Override
    public void handleLogout(Player player) {
        if (this.getStatus() == Status.IN_GAME) {
            this.gameLoop.removePlayer(player.getUniqueId());
            if (this.isPvpEnabled()) {
                this.stumpPlayer(player, true);
                Location time = player.getLocation();
                World w = time.getWorld();
                ItemStack[] var4 = player.getInventory().getContents();

                for (ItemStack stack : var4) {
                    if (stack != null) {
                        w.dropItemNaturally(time, stack);
                    }
                }
            } else {
                int var8 = this.getPreparingTime() * 60 - this.gameLoop.getTime();

                // ...

                disconnected.add(player.getUniqueId());
                server.broadcastMessage(this.coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s\'est déconnecté. Il peut se reconnecter jusqu\'à la fin de la préparation.");
            }
        } else {
            this.players.remove(player.getUniqueId());
        }


    }


    public boolean isDisconnected(UUID player) {
        return disconnected.contains(player);
    }

    @Override
    public void handleReconnectTimeOut(Player player) {
        // Handle time out
        this.rejoin(player, true);
    }

    public void rejoin(Player thePlayer, boolean timeOut) {
        if (thePlayer != null) {
            server.getScheduler().runTaskLater(plugin, () -> {

                thePlayer.setScoreboard(this.scoreboard);
                ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "? UHCRun ?");
                sign.addReceiver(thePlayer);
                this.gameLoop.addPlayer(thePlayer.getUniqueId(), sign);
                server.broadcastMessage(this.coherenceMachine.getGameTag() + thePlayer.getDisplayName() + ChatColor.GOLD + " s\'est reconnecté.");
                disconnected.remove(thePlayer.getUniqueId());
            }, 10L);

        }
    }

    private String getDamageCause(EntityDamageEvent.DamageCause cause) {
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

    public void effectsOnWinner(Player player) {
        server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int timer = 0;

            public void run() {
                if (this.timer < 20) {
                    Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    Random r = new Random();
                    int rt = r.nextInt(4) + 1;
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (rt == 1) {
                        type = FireworkEffect.Type.BALL;
                    }

                    if (rt == 2) {
                        type = FireworkEffect.Type.BALL_LARGE;
                    }

                    if (rt == 3) {
                        type = FireworkEffect.Type.BURST;
                    }

                    if (rt == 4) {
                        type = FireworkEffect.Type.CREEPER;
                    }

                    if (rt == 5) {
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
                    ++this.timer;
                }
            }
        }, 5L, 5L);
    }
}

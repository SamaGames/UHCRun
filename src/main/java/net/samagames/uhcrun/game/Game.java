package net.samagames.uhcrun.game;

import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.api.games.themachine.messages.IMessageManager;
import net.samagames.tools.Titles;
import net.samagames.tools.scoreboards.ObjectiveSign;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.data.SavedPlayer;
import net.samagames.uhcrun.game.data.StoredGame;
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

import java.util.*;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public abstract class Game extends net.samagames.api.games.Game<UHCPlayer> {

    protected final UHCRun plugin;
    protected final Server server;
    protected final Random rand;
    protected final List<Location> spawnPoints;
    protected final HashMap<UUID, UHCPlayer> prevInGame;
    private final int maxSpawnLocations;
    private final int minPlayers;
    private final StoredGame storedGame;
    private GameLoop gameLoop;
    private GameLoop.TimedEvent nextEvent;
    private IMessageManager messageManager;
    private Scoreboard scoreboard;
    private boolean pvpEnabled;
    private BukkitTask mainTask;
    private boolean damages;

    public Game(int maxLocations) {
        super("UHCRun", UHCRun.getInstance().getConfig().getString("gameName", "UHCRun"), UHCPlayer.class);
        this.plugin = UHCRun.getInstance();
        this.server = plugin.getServer();
        this.rand = new Random();
        this.maxSpawnLocations = maxLocations;
        this.minPlayers = plugin.getAPI().getGameManager().getGameProperties().getMinSlots();
        this.storedGame = new StoredGame(plugin.getAPI().getServerName(), System.currentTimeMillis(), this.getClass().getSimpleName());
        this.spawnPoints = new ArrayList<>();
        this.prevInGame = new HashMap<>();
        UHCPlayer.setGame(this);
    }

    public void postInit(World world) {
        this.scoreboard = server.getScoreboardManager().getMainScoreboard();
        this.gameLoop = new GameLoop(this, plugin, server);
        this.disableDamages();
        this.computeSpawnLocations(world);
    }

    private void computeSpawnLocations(World world) {
        for (int i = 0; i < maxSpawnLocations; i++) {
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
    public void handlePostRegistration() {
        super.handlePostRegistration();
        this.messageManager = coherenceMachine.getMessageManager();
    }

    protected void removeFromGame(UUID uuid) {
        UHCPlayer player = this.gamePlayers.get(uuid);
        if (player != null) {
            player.setSpectator();
        }
    }

    @Override
    public void handleGameEnd() {
        super.handleGameEnd();
    }

    public UHCRun getPlugin() {
        return plugin;
    }

    public void startGame() {
        super.startGame();
        plugin.removeSpawn();

        Objective displayNameLife = scoreboard.registerNewObjective("vie", "health");
        Objective playerListLife = scoreboard.registerNewObjective("vieb", "health");

        playerListLife.setDisplayName("HP");
        displayNameLife.setDisplayName("HP");
        displayNameLife.setDisplaySlot(DisplaySlot.BELOW_NAME);
        playerListLife.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        this.mainTask = server.getScheduler().runTaskTimer(plugin, gameLoop, 20, 20);
        teleport();

        for (UUID uuid : getInGamePlayers().keySet()) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                gamePlayers.remove(uuid);
                continue;
            }
            this.increaseStat(uuid, "played", 1);
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

    }

    public void rejoin(Player thePlayer) {
        if (thePlayer != null) {
            server.getScheduler().runTaskLater(plugin, () -> {

                thePlayer.setScoreboard(this.scoreboard);
                ObjectiveSign sign = new ObjectiveSign("sggameloop", ChatColor.GOLD + "" + ChatColor.ITALIC + ChatColor.BOLD + "? UHCRun ?");
                sign.addReceiver(thePlayer);
                this.gameLoop.addPlayer(thePlayer.getUniqueId(), sign);
                messageManager.writePlayerReconnected(thePlayer);
            }, 10L);

        }
    }

    public void stumpPlayer(Player player, boolean logout) {
        if (this.status == Status.IN_GAME) {
            Object lastDamager = Metadatas.getMetadata(plugin, player, "lastDamager");
            Player killer = null;
            SavedPlayer e;
            if (lastDamager != null && lastDamager instanceof Player) {
                killer = (Player) lastDamager;
                if (killer.isOnline() && this.isInGame(killer.getUniqueId())) {
                    this.creditKillCoins(getPlayer(killer.getUniqueId()).addKill());
                    this.increaseStat(killer.getUniqueId(), "kills", 1);
                    e = this.storedGame.getPlayer(killer.getUniqueId(), killer.getName());
                    e.kill(player);
                    killer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 1));
                } else {
                    killer = null;
                }
            }

            if (logout) {
                messageManager.writePlayerQuited(player);
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

                e.die(getInGamePlayers().size(), killedBy, System.currentTimeMillis() - this.storedGame.getStartTime());
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            player.setGameMode(GameMode.SPECTATOR);
            player.setHealth(20.0D);
            if (!logout) {
                this.increaseStat(player.getUniqueId(), "stumps", 1);
                Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
            }
            removeFromGame(player.getUniqueId());
        }
    }

    protected void creditKillCoins(UHCPlayer player) {
        player.addCoins(20, "Un joueur tué !");
    }

    public boolean isInGame(UUID uniqueId) {
        return this.gamePlayers.containsKey(uniqueId) && !this.gamePlayers.get(uniqueId).isSpectator();
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

    public abstract void checkStump(Player player);

    protected abstract void teleport();

    public abstract void teleportDeathMatch();

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



    public void disablePVP() {
        this.pvpEnabled = false;
    }

    public void enablePVP() {
        this.pvpEnabled = true;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
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

    public GameLoop getGameLoop() {
        return gameLoop;
    }

    public ICoherenceMachine getCoherenceMachine() {
        return coherenceMachine;
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
                    this.timer++;
                }
            }
        }, 5L, 5L);
    }
}

package net.zyuiop.survivalgames.game;

import com.google.common.collect.Lists;
import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.CoherenceMachine;
import net.samagames.gameapi.themachine.messages.MessageManager;
import net.samagames.gameapi.types.GameArena;
import net.samagames.utils.ObjectiveSign;
import net.samagames.utils.Titles;
import net.zyuiop.MasterBundle.StarsManager;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.survivalgames.SurvivalGames;
import net.zyuiop.survivalgames.tasks.BeginCountdown;
import net.zyuiop.survivalgames.tasks.GameLoop;
import net.zyuiop.survivalgames.utils.Colors;
import net.zyuiop.survivalgames.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
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
public class Game implements GameArena {
    protected CopyOnWriteArraySet<UUID> players = new CopyOnWriteArraySet<>();
    protected int maxPlayers = 20;
    protected int minPlayers = 7;
    protected int vipPlayers = 4;
    protected Status status;
    protected String mapName;
    protected SurvivalGames plugin;
    protected Location feast = null;
    protected boolean pvpenabled = false;
    protected MessageManager messageManager;
    private CoherenceMachine coherenceMachine;
    private BukkitTask beginCountdown;
    private BukkitTask mainLoop;
    private ObjectiveSign objectiveSign;
    private Objective life;
    private Scoreboard scoreboard;
    private boolean damages;
    private ArrayList<SpawnLocation> spawns = new ArrayList<>();

    public Game(String mapname) {
        this.mapName = mapname;
        this.status = Status.Idle;
        this.coherenceMachine = new CoherenceMachine("UHCRun");
        this.messageManager = new MessageManager(coherenceMachine);
        beginCountdown = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new BeginCountdown(this, maxPlayers, minPlayers), 20L, 20L);
        spawns.add(new SpawnLocation(0, 200));
        spawns.add(new SpawnLocation(0, 400));
        spawns.add(new SpawnLocation(200, 0));
        spawns.add(new SpawnLocation(400, 0));
        spawns.add(new SpawnLocation(400, 200));
        spawns.add(new SpawnLocation(200, 400));
        spawns.add(new SpawnLocation(400, 400));
        spawns.add(new SpawnLocation(200, 200));
        spawns.add(new SpawnLocation(0, -200));
        spawns.add(new SpawnLocation(0, -400));
        spawns.add(new SpawnLocation(-200, 0));
        spawns.add(new SpawnLocation(-400, 0));
        spawns.add(new SpawnLocation(-400, -200));
        spawns.add(new SpawnLocation(-200, -400));
        spawns.add(new SpawnLocation(-400, -400));
        spawns.add(new SpawnLocation(-200, -200));
        spawns.add(new SpawnLocation(400, -200));
        spawns.add(new SpawnLocation(-400, 200));
        spawns.add(new SpawnLocation(200, -400));
        spawns.add(new SpawnLocation(-200, 400));
        spawns.add(new SpawnLocation(-400, 400));
        spawns.add(new SpawnLocation(400, -400));
        spawns.add(new SpawnLocation(200, -200));
        spawns.add(new SpawnLocation(-200, 200));
    }

    public void enablePVP() {
        this.pvpenabled = true;
    }

    public void disableDamages() {
        this.damages = false;
    }

    public void start() {
        SurvivalGames.instance.removeSpawn();
        objectiveSign = new ObjectiveSign("sggameloop", ChatColor.AQUA +""+ChatColor.ITALIC + "≡ UHCRun ≡");
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        life = scoreboard.registerNewObjective("vie", "health");
        Objective lifeb = scoreboard.registerNewObjective("vieb", "health");
        life.setDisplaySlot(DisplaySlot.BELOW_NAME);
        lifeb.setDisplayName("HP");
        life.setDisplayName("HP");
        lifeb.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        updateStatus(Status.InGame);
        if (beginCountdown != null)
            beginCountdown.cancel();

        mainLoop = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new GameLoop(this, objectiveSign), 20, 20);

        List<Player> list = Lists.newArrayList();
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                players.remove(uuid);
                return;
            }

            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setScoreboard(scoreboard);
            player.setLevel(0);
            player.teleport(locationIterator.next().getSpawn(world));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60*20*32, 1));
            objectiveSign.addReceiver(player);
            list.add(player);
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            player.setHealth(10.0);
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            player.setHealth(20.0);
        }


        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }

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
            else
                CoinsManager.creditJoueur(killer.getUniqueId(), 20, true, true, "Un joueur tué !");
        }

        if (logout)
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " s'est déconnecté.");
        else if (killer != null)
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " a été tué par "+ killer.getDisplayName());
        else
            Bukkit.broadcastMessage(coherenceMachine.getGameTag() + player.getDisplayName() + ChatColor.GOLD + " est mort.");

        if (players.size() == 2)
            CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Troisième au classement !");

        if (players.size() == 1) {
            CoinsManager.creditJoueur(player.getUniqueId(), 50, true, true, "Second au classement !");
            StarsManager.creditJoueur(player.getUniqueId(), 1, "Second au classement !");

            UUID winnerId = players.iterator().next();
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner == null)
                finish();
            else
                win(winner);
        } else if (players.size() == 0) {
            finish();
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + players.size() + ChatColor.YELLOW + " joueur(s) en vie.");
        }

        if (logout)
            return;

        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(1.0); // Le joueur ne voit pas l'écran de mort
        Titles.sendTitle(player, 5, 70, 5, ChatColor.RED + "Vous êtes mort !", ChatColor.GOLD + "Vous êtes maintenant spectateur.");
    }

    public boolean isDamages() {
        return damages;
    }

    public void finish() {
        mainLoop.cancel();
        Bukkit.getScheduler().runTaskLater(SurvivalGames.instance, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers())
                    GameAPI.kickPlayer(player);

                Bukkit.getServer().shutdown();
            }
        }, 20*30);
    }

    public void win(final Player player) {
        CoinsManager.creditJoueur(player.getUniqueId(), 100, true, true, "Victoire !");
        StarsManager.creditJoueur(player.getUniqueId(), 2, "Victoire !");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de "+player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");

        for (Player user : Bukkit.getOnlinePlayers()) {
            Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
        }

        final int nb = 20;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SurvivalGames.instance, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb) {
                    return;
                }

                //Spawn the Firework, get the FireworkMeta.
                Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                //Our random generator
                Random r = new Random();

                //Get the type
                int rt = r.nextInt(4) + 1;
                FireworkEffect.Type type = FireworkEffect.Type.BALL;
                if (rt == 1)
                    type = FireworkEffect.Type.BALL;
                if (rt == 2)
                    type = FireworkEffect.Type.BALL_LARGE;
                if (rt == 3)
                    type = FireworkEffect.Type.BURST;
                if (rt == 4)
                    type = FireworkEffect.Type.CREEPER;
                if (rt == 5)
                    type = FireworkEffect.Type.STAR;

                //Get our random colours
                int r1i = r.nextInt(17) + 1;
                int r2i = r.nextInt(17) + 1;
                Color c1 = Colors.getColor(r1i);
                Color c2 = Colors.getColor(r2i);

                //Create our effect with this
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                //Then apply the effect to the meta
                fwm.addEffect(effect);

                //Generate some random power and set it
                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                //Then apply this to our rocket
                fw.setFireworkMeta(fwm);

                compteur++;
            }

        }, 5L, 5L);

        finish();
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

    @Override
    public int countGamePlayers() {
        return players.size();
    }

    public Location getFeast() {
        return feast;
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

    public void updateStatus(Status status) {
        setStatus(status);
        GameAPI.getManager().sendArena();
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

    public void enableDamage() {
        this.damages = true;
    }

    public void teleportDeathmatch() {
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                players.remove(uuid);
                return;
            }

            player.teleport(locationIterator.next().getDeathmatchSpawn(world));
        }
    }
}

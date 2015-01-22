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
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by vialarl on 16/01/2015.
 */
public class Game implements GameArena {
    protected CopyOnWriteArraySet<UUID> players = new CopyOnWriteArraySet<>();
    protected int maxPlayers = 45;
    protected int minPlayers = 15;
    protected int vipPlayers = 5;
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
    private boolean damages;

    public Game(String mapname) {
        this.mapName = mapname;
        this.status = Status.Idle;
        this.coherenceMachine = new CoherenceMachine("SurvivalGames");
        this.messageManager = new MessageManager(coherenceMachine);
        beginCountdown = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new BeginCountdown(this, maxPlayers, minPlayers), 20L, 20L);
    }

    public void enablePVP() {
        this.pvpenabled = true;
    }

    public void start() {
        SurvivalGames.instance.removeSpawn();
        objectiveSign = new ObjectiveSign("sggameloop", ChatColor.DARK_RED + "Survival Games");
        updateStatus(Status.InGame);
        if (beginCountdown != null)
            beginCountdown.cancel();

        mainLoop = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new GameLoop(this, objectiveSign), 20, 20);

        List<Player> list = Lists.newArrayList();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            objectiveSign.addReceiver(player);
            list.add(player);
        }

        World world = Bukkit.getWorld("world");
        final double xRangeMin = -25;
        final double zRangeMin = -25;
        final double xRangeMax = +25;
        final double zRangeMax = +25;
        final Location[] locations = getSpreadLocations(world, players.size(), xRangeMin, zRangeMin, xRangeMax, zRangeMax);
        range(world, 1, xRangeMin, zRangeMin, xRangeMax, zRangeMax, locations);
        spread(world, list, locations);

        Bukkit.broadcastMessage(coherenceMachine.getGameTag() + ChatColor.GOLD + "La partie commence !");
    }


    private int range(World world, double distance, double xRangeMin, double zRangeMin, double xRangeMax, double zRangeMax, Location[] locations) {
        Random random = new Random();
        boolean flag = true;
        double max;
        int i;
        for (i = 0; i < 10000 && flag; ++i) {
            flag = false;
            max = Float.MAX_VALUE;
            Location loc1;
            int j;
            for (int k = 0; k < locations.length; ++k) {
                Location loc2 = locations[k];
                j = 0;
                loc1 = new Location(world, 0, 0, 0);
                for (int l = 0; l < locations.length; ++l) {
                    if (k != l) {
                        Location loc3 = locations[l];
                        double dis = loc2.distanceSquared(loc3);
                        max = Math.min(dis, max);
                        if (dis < distance) {
                            ++j;
                            loc1.add(loc3.getX() - loc2.getX(), 0, 0);
                            loc1.add(loc3.getZ() - loc2.getZ(), 0, 0);
                        }
                    }
                }
                if (j > 0) {
                    loc2.setX(loc2.getX() / j);
                    loc2.setZ(loc2.getZ() / j);
                    double d7 = Math.sqrt(loc1.getX() * loc1.getX() + loc1.getZ() * loc1.getZ());
                    if (d7 > 0.0D) {
                        loc1.setX(loc1.getX() / d7);
                        loc2.add(-loc1.getX(), 0, -loc1.getZ());
                    } else {
                        double x = xRangeMin >= xRangeMax ? xRangeMin : random.nextDouble() * (xRangeMax - xRangeMin) + xRangeMin;
                        double z = zRangeMin >= zRangeMax ? zRangeMin : random.nextDouble() * (zRangeMax - zRangeMin) + zRangeMin;
                        loc2.setX(x);
                        loc2.setZ(z);
                    }
                    flag = true;
                }
                boolean swap = false;
                if (loc2.getX() < xRangeMin) {
                    loc2.setX(xRangeMin);
                    swap = true;
                } else if (loc2.getX() > xRangeMax) {
                    loc2.setX(xRangeMax);
                    swap = true;
                }
                if (loc2.getZ() < zRangeMin) {
                    loc2.setZ(zRangeMin);
                    swap = true;
                } else if (loc2.getZ() > zRangeMax) {
                    loc2.setZ(zRangeMax);
                    swap = true;
                }
                if (swap) {
                    flag = true;
                }
            }
            if (!flag) {
                Location[] locs = locations;
                int i1 = locations.length;
                for (j = 0; j < i1; ++j) {
                    loc1 = locs[j];
                    if (world.getHighestBlockYAt(loc1) == 0) {
                        double x = xRangeMin >= xRangeMax ? xRangeMin : random.nextDouble() * (xRangeMax - xRangeMin) + xRangeMin;
                        double z = zRangeMin >= zRangeMax ? zRangeMin : random.nextDouble() * (zRangeMax - zRangeMin) + zRangeMin;
                        locations[i] = (new Location(world, x, 0, z));
                        loc1.setX(x);
                        loc1.setZ(z);
                        flag = true;
                    }
                }
            }
        }
        if (i >= 10000) {
            return -1;
        } else {
            return i;
        }
    }

    private double spread(World world, List<Player> list, Location[] locations) {
        double distance = 0.0D;
        int i = 0;
        for (int j = 0; j < list.size(); ++j) {
            Player player = list.get(j);
            Location location = locations[i++];

            player.teleport(new Location(world, Math.floor(location.getX()) + 0.5D, world.getHighestBlockYAt((int) location.getX(), (int) location.getZ()), Math.floor(location.getZ()) + 0.5D));
            double value = Double.MAX_VALUE;
            for (int k = 0; k < locations.length; ++k) {
                if (location != locations[k]) {
                    double d = location.distanceSquared(locations[k]);
                    value = Math.min(d, value);
                }
            }
            distance += value;
        }
        distance /= list.size();
        return distance;
    }

    private Location[] getSpreadLocations(World world, int size, double xRangeMin, double zRangeMin, double xRangeMax, double zRangeMax) {
        Random random = new Random();
        Location[] locations = new Location[size];
        for (int i = 0; i < size; ++i) {
            double x = xRangeMin >= xRangeMax ? xRangeMin : random.nextDouble() * (xRangeMax - xRangeMin) + xRangeMin;
            double z = zRangeMin >= zRangeMax ? zRangeMin : random.nextDouble() * (zRangeMax - zRangeMin) + zRangeMin;
            locations[i] = (new Location(world, x, 0, z));
        }
        return locations;
    }

    public void stumpPlayer(Player player, boolean logout) {
        players.remove(player.getUniqueId());
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
}

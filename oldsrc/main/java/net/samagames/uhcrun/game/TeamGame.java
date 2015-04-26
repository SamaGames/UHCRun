package net.samagames.uhcrun.game;

import net.samagames.gameapi.GameAPI;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.utils.Colors;
import net.samagames.utils.Titles;
import net.zyuiop.MasterBundle.StarsManager;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.NameTagVisibility;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by vialarl on 16/01/2015.
 */
public class TeamGame extends BasicGame {

    protected CopyOnWriteArrayList<Team> teams = new CopyOnWriteArrayList<>();
    protected int personsPerTeam;

    @Override
    public String getMapName() {
        return "Equipes de "+personsPerTeam;
    }

    public TeamGame(int personsPerTeam) {
        super("Team", 4 * personsPerTeam, 11 * personsPerTeam, personsPerTeam);
        spawns.add(new SpawnLocation(0, 200));
        spawns.add(new SpawnLocation(0, 400));
        spawns.add(new SpawnLocation(200, 0));
        spawns.add(new SpawnLocation(400, 0));
        spawns.add(new SpawnLocation(200, 200));
        spawns.add(new SpawnLocation(0, - 200));
        spawns.add(new SpawnLocation(0, -400));
        spawns.add(new SpawnLocation(-200, 0));
        spawns.add(new SpawnLocation(-400, 0));
        spawns.add(new SpawnLocation(-200, -200));
        spawns.add(new SpawnLocation(200, -200));
        spawns.add(new SpawnLocation(- 200, 200));

        this.personsPerTeam = personsPerTeam;

        registerTeam("Blanc", ChatColor.WHITE, DyeColor.WHITE);
        registerTeam("Orange", ChatColor.GOLD, DyeColor.ORANGE);
        registerTeam("Bleu Clair", ChatColor.BLUE, DyeColor.LIGHT_BLUE);
        registerTeam("Bleu Foncé", ChatColor.DARK_BLUE, DyeColor.BLUE);
        registerTeam("Cyan", ChatColor.AQUA, DyeColor.CYAN);
        registerTeam("Jaune", ChatColor.YELLOW, DyeColor.YELLOW);
        registerTeam("Rose", ChatColor.LIGHT_PURPLE, DyeColor.PINK);
        registerTeam("Vert Foncé", ChatColor.DARK_GREEN, DyeColor.GREEN);
        registerTeam("Rouge", ChatColor.RED, DyeColor.RED);
        registerTeam("Violet", ChatColor.DARK_PURPLE, DyeColor.PURPLE);
        registerTeam("Gris", ChatColor.GRAY, DyeColor.GRAY);
        registerTeam("Noir", ChatColor.BLACK, DyeColor.BLACK);
    }

    @Override
    public void stumpPlayer(Player player, boolean logout) {
        if (logout && !isGameStarted()) {
            Team team = getPlayerTeam(player.getUniqueId());
            if (team != null)
                team.remove(player.getUniqueId());
        }
        super.stumpPlayer(player, logout);
    }

    @Override
    public void start() {
        Iterator<UUID> iterator = getPlayersIterator();
        while (iterator.hasNext()) {
            UUID id = iterator.next();
            Player player = Bukkit.getPlayer(id);
            if (player == null)
                continue;

            if (getPlayerTeam(id) == null) {
                for(Team team : teams) {
                    if(!team.isFull() && !team.isLocked()) {
                        team.join(id);
                        break;
                    }
                }

                if (getPlayerTeam(id) == null) {
                    player.sendMessage(ChatColor.RED + "Aucune team était apte à vous reçevoir, vous avez été réenvoyé dans le lobby.");
                    GameAPI.kickPlayer(player.getPlayer());
                }
            }
        }

        for (Team team : teams) {
            String name = team.getTeamName();
            if (name.length() > 12)
                name = name.substring(0, 12);

            if (scoreboard.getTeam(name) != null) {
                name = name + new Random().nextInt(9999);
            }

            org.bukkit.scoreboard.Team t = scoreboard.registerNewTeam(name);
            t.setDisplayName(team.getChatColor() + "");
            t.setNameTagVisibility(NameTagVisibility.ALWAYS);
            t.setAllowFriendlyFire(false);
            t.setPrefix(team.getChatColor()+"");
			try {
				t.setSuffix(ChatColor.RESET + "");
			} catch (Exception e) {
				e.printStackTrace();
			}

            boolean hasPlayers = false;
            for (UUID id : team.getPlayers()) {
                Player player = Bukkit.getPlayer(id);
                if (player == null)
                    team.remove(id);
                else {
					t.addPlayer(player);
					hasPlayers = true;
				}
            }

            if (!hasPlayers)
                teams.remove(team);
        }

        super.start();

        Bukkit.broadcastMessage(ChatColor.GOLD + "Pour parler dans le chat global, écrivez '!' suivi de votre message.");
    }

    @Override
    public void join(Player player) {
        super.join(player);
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = star.getItemMeta();
        starMeta.setDisplayName(ChatColor.GOLD + "Sélectionner une équipe");
        star.setItemMeta(starMeta);
        player.getInventory().setItem(4, star);
    }

    protected void registerTeam(String name, ChatColor chatColor, DyeColor color) {
        teams.add(new Team(color, chatColor, name, personsPerTeam));
    }

    public int getPersonsPerTeam() {
        return personsPerTeam;
    }

    public Team getPlayerTeam(UUID player) {
        for (Team team : teams) {
            if (team.hasPlayer(player))
                return team;
        }
        return null;
    }

    public CopyOnWriteArrayList<Team> getTeams() {
        return teams;
    }

    @Override
    public void teleportAtStart() {
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();

        for (Team team : teams) {
            if (!locationIterator.hasNext()) {
                teams.remove(team);
                for (UUID player : team.getPlayers()) {
                    Player p = Bukkit.getPlayer(player);
                    if (p != null)
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    players.remove(player);
                }
                continue;
            }

            SpawnLocation spawn = locationIterator.next();

            for (UUID player : team.getPlayers()) {
                Player p = Bukkit.getPlayer(player);
                if (p == null)
                    players.remove(player);
                else
                    p.teleport(spawn.getSpawn(world));
            }
        }
    }

    @Override
    public void creditKillCoins(Player player) {
        CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Un joueur tué !");
        Team team = getPlayerTeam(player.getUniqueId());
        if (team != null) {
            for (UUID id : team.getPlayers()) {
                if (!id.equals(player.getUniqueId()))
                    CoinsManager.creditJoueur(id, 10, true, true, "Votre équipe fait un kill !");
            }
        }
    }

    public void checkStump(final Player player) {
        Bukkit.getScheduler().runTaskLater(UHCRun.instance, new Runnable() {
            @Override
            public void run() {
                Team team = getPlayerTeam(player.getUniqueId());
                if (team == null)
                    return;

                int left = team.removePlayer(player.getUniqueId());
                if (left == 0) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "L'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.GOLD + " a été éliminée !");
                    teams.remove(team);

                    left = teams.size();
                    if (left == 1) {
                        win(teams.get(0));
						return;
                    } else if (left < 1) {
                        finish();
						return;
                    } else {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
                    }
                }

				// Check de sécurité
				for (Team t : teams) {
					int players = 0;
					if (t.getPlayers().size() > 0) {
						for (UUID id : t.getPlayers()) {
							if (Bukkit.getPlayer(id) != null)
								players++;
						}
					}

					if (players == 0) {
						Bukkit.broadcastMessage(ChatColor.GOLD + "L'équipe " + t.getChatColor() + t.getTeamName() + ChatColor.GOLD + " a été éliminée !");
						teams.remove(t);

						left = teams.size();
						if (left == 1) {
							win(teams.get(0));
						} else if (left < 1) {
							finish();
						} else {
							Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
						}
					}
				}
            }
        }, 2L);
    }

    public void win(final Team team) {
        try {
            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de l'équipe "+team.getChatColor() + team.getTeamName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");
            for (Player user : Bukkit.getOnlinePlayers()) {
                Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire !", "Bravo à l'équipe "+team.getChatColor() + team.getTeamName());
            }
        } catch (Exception ignored) {}


        for (final UUID playerID : team.getPlayers()) {
            CoinsManager.creditJoueur(playerID, 100, true, true, "Victoire !");
            StarsManager.creditJoueur(playerID, 2, "Victoire !");
            try {
                StatsApi.increaseStat(playerID, "uhcrun", "victories", 1);
            } catch (Exception ignored) {}

            final Player player = Bukkit.getPlayer(playerID);
            if (player == null)
                continue;

            final int nb = 20;
            Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCRun.instance, new Runnable() {
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
        }

        finish();
    }

    public void teleportDeathmatch() {
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();

        for (Team team : teams) {
            if (!locationIterator.hasNext()) {
                teams.remove(team);
                for (UUID player : team.getPlayers()) {
                    Player p = Bukkit.getPlayer(player);
                    if (p != null)
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    players.remove(player);
                }
                continue;
            }

            SpawnLocation spawn = locationIterator.next();

            for (UUID player : team.getPlayers()) {
                Player p = Bukkit.getPlayer(player);
                if (p == null)
                    players.remove(player);
                else
                    p.teleport(spawn.getDeathmatchSpawn(world));
            }
        }
    }
}

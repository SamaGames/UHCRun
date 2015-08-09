package net.samagames.uhcrun.game;

import net.samagames.api.games.Status;
import net.samagames.tools.Titles;
import net.samagames.uhcrun.game.data.Team;
import net.samagames.uhcrun.game.data.TeamList;
import net.samagames.uhcrun.game.team.TeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


// TODO: TEAM GUI
public class TeamGame extends Game {

    private final int personsPerTeam;
    private TeamList teams = new TeamList();
    private TeamSelector teamSelector;


    public TeamGame(int nbByTeam) {
        super(12);

        this.personsPerTeam = nbByTeam;
        try {
            this.teamSelector = new TeamSelector(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        plugin.getServer().getPluginManager().registerEvents(teamSelector, plugin);

        // Register team
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

    protected void registerTeam(String name, ChatColor chatColor, DyeColor color) {
        teams.add(new Team(this, name, color, chatColor));
    }

    @Override
    protected void teleport() {
        Iterator<Location> locationIterator = spawnPoints.iterator();
        List<Team> toRemove = new ArrayList<>();

        for (Team team : teams) {
            if (!locationIterator.hasNext() || team.isEmpty()) {
                toRemove.add(team);
                for (UUID player : team.getPlayersUUID()) {
                    Player p = server.getPlayer(player);
                    if (p != null) {
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    }
                    gamePlayers.remove(player);
                }
                continue;
            }

            Location location = locationIterator.next();

            for (UUID player : team.getPlayersUUID()) {
                Player p = server.getPlayer(player);
                if (p == null) {
                    gamePlayers.remove(player);
                } else {
                    p.teleport(location);
                }
            }
        }
        teams.removeAll(toRemove);
        toRemove.clear();
    }

    @Override
    public void teleportDeathMatch() {
        Iterator<Location> locationIterator = spawnPoints.iterator();
        List<Team> toRemove = new ArrayList<>();

        for (Team team : teams) {
            if (!locationIterator.hasNext()) {
                toRemove.add(team);
                for (UUID player : team.getPlayersUUID()) {
                    Player p = server.getPlayer(player);
                    if (p != null) {
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    }
                    gamePlayers.remove(player);
                }
                continue;
            }

            Location location = locationIterator.next();

            for (UUID player : team.getPlayersUUID()) {
                Player p = server.getPlayer(player);
                if (p == null) {
                    gamePlayers.remove(player);
                } else {
                    p.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
                }
            }
        }
        teams.removeAll(toRemove);
        toRemove.clear();
    }

    @Override
    public void creditKillCoins(UHCPlayer killer) {
        super.creditKillCoins(killer);

        UUID killerID = killer.getUUID();

        Team team = teams.getTeam(killerID);
        if (team != null) {
            team.getPlayersUUID().stream().filter(otherPlayer -> !otherPlayer.equals(killerID)).forEach(otherPlayer -> getPlayer(otherPlayer).addCoins(10, "Votre équipe fait un kill !"));
        }
    }

    @Override
    public void checkStump(final Player player) {
        server.getScheduler().runTaskLater(plugin, () -> {
            List<Team> toRemvove = new ArrayList<>();
            Team team = teams.getTeam(player.getUniqueId());
            if (team == null) {
                return;
            }

            int left = team.removePlayer(player.getUniqueId());
            if (left == 0) {
                server.broadcastMessage(ChatColor.GOLD + "L'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.GOLD + " a été éliminée !");
                teams.remove(team);

                left = teams.size();
                if (left == 1) {
                    win(teams.get(0));
                    return;
                } else if (left < 1) {
                    handleGameEnd();
                    return;
                } else {
                    server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
                }
            }

            // Security check
            for (Team t : teams) {
                int players1 = 0;
                if (!t.isEmpty()) {
                    for (UUID id : t.getPlayersUUID()) {
                        if (server.getPlayer(id) != null) {
                            players1++;
                        }
                    }
                }

                if (players1 == 0) {
                    server.broadcastMessage(ChatColor.GOLD + "L'équipe " + t.getChatColor() + t.getTeamName() + ChatColor.GOLD + " a été éliminée !");
                    toRemvove.add(t);

                    left = teams.size();
                    if (left == 1) {
                        win(teams.get(0));
                    } else if (left < 1) {
                        handleGameEnd();
                    } else {
                        server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
                    }
                }
            }
            teams.removeAll(toRemvove);
        }, 2L);
    }


    private void win(final Team team) {
        try {
            server.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");
            for (Player user : server.getOnlinePlayers()) {
                Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire !", "Bravo à l'équipe " + team.getChatColor() + team.getTeamName());
            }
        } catch (Exception ignored) {

        }


        for (final UUID playerID : team.getPlayersUUID()) {
            UHCPlayer playerData = getPlayer(playerID);
            playerData.addCoins(100, "Victoire !");
            playerData.addStars(2, "Victoire !");

            try {
                this.increaseStat(playerID, "victories", 1);
            } catch (Exception ignored) {

            }

            final Player player = server.getPlayer(playerID);
            if (player == null) {
                continue;
            }

            this.effectsOnWinner(player);
        }

        handleGameEnd();
    }

    @Override
    public void stumpPlayer(Player player, boolean logout) {
        if (logout && !getStatus().equals(Status.IN_GAME)) {
            Team team = teams.getTeam(player.getUniqueId());
            if (team != null) {
                team.remove(player.getUniqueId());
            }

        }
        super.stumpPlayer(player, logout);
    }

    public Team getPlayerTeam(UUID uniqueId) {
        return teams.getTeam(uniqueId);
    }

    public TeamList getTeams() {
        return teams;
    }

    public int getPersonsPerTeam() {
        return personsPerTeam;
    }
}

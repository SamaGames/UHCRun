package net.samagames.uhcrun.game;

import net.samagames.api.games.Status;
import net.samagames.api.player.AbstractPlayerData;
import net.samagames.tools.Titles;
import net.samagames.uhcrun.game.data.Team;
import net.samagames.uhcrun.game.data.TeamList;
import net.samagames.uhcrun.game.team.TeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.UUID;


// TODO: TEAM GUI
public class TeamGame extends Game {

    private TeamList teams = new TeamList();
    private final int personsPerTeam;
    private TeamSelector teamSelector;


    public TeamGame(int nbByTeam) {
        super(nbByTeam);

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
    public void join(Player player) {
        super.join(player);

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = star.getItemMeta();
        starMeta.setDisplayName(ChatColor.GOLD + "Sélectionner une équipe");
        star.setItemMeta(starMeta);
        player.getInventory().setItem(4, star);
    }

    @Override
    protected void teleport() {
        Iterator<Location> locationIterator = spawnPoints.iterator();

        for (Team team : teams) {
            if (!locationIterator.hasNext()) {
                teams.remove(team);
                for (UUID player : team.getPlayersUUID()) {
                    Player p = server.getPlayer(player);
                    if (p != null) {
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    }

                    players.remove(player);
                }
                continue;
            }

            Location location = locationIterator.next();

            for (UUID player : team.getPlayersUUID()) {
                Player p = server.getPlayer(player);
                if (p == null) {
                    players.remove(player);
                } else {
                    p.teleport(location);
                }
            }
        }
    }

    @Override
    public void teleportDeathMatch() {
        Iterator<Location> locationIterator = spawnPoints.iterator();

        for (Team team : teams) {
            if (!locationIterator.hasNext()) {
                teams.remove(team);
                for (UUID player : team.getPlayersUUID()) {
                    Player p = server.getPlayer(player);
                    if (p != null) {
                        p.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                    }

                    players.remove(player);
                }
                continue;
            }

            Location location = locationIterator.next();

            for (UUID player : team.getPlayersUUID()) {
                Player p = server.getPlayer(player);
                if (p == null) {
                    players.remove(player);
                } else {
                    p.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
                }
            }
        }
    }

    @Override
    public void creditKillCoins(AbstractPlayerData killer) {
        super.creditKillCoins(killer);

        UUID killerID = killer.getPlayerID();

        Team team = teams.getTeam(killerID);
        if (team != null) {
            team.getPlayersUUID().stream().filter(otherPlayer -> !otherPlayer.equals(killerID)).forEach(otherPlayer -> getPlayerData(otherPlayer).creditCoins(10, "Votre équipe fait un kill !", true));
        }
    }

    @Override
    public void checkStump(final Player player) {
        server.getScheduler().runTaskLater(plugin, () -> {
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
                    finish();
                    return;
                } else {
                    server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
                }
            }

            // Check de sécurité
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
                    teams.remove(t);

                    left = teams.size();
                    if (left == 1) {
                        win(teams.get(0));
                    } else if (left < 1) {
                        finish();
                    } else {
                        server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + teams.size() + ChatColor.YELLOW + " équipes en jeu.");
                    }
                }
            }
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
            AbstractPlayerData playerData = getPlayerData(playerID);
            playerData.creditCoins(100, "Victoire !", true);
            playerData.creditStars(2, "Victoire !", true);

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

        finish();
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

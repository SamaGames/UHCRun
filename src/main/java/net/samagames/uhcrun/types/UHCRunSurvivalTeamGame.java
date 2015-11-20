package net.samagames.uhcrun.types;

import net.samagames.survivalapi.game.SurvivalTeam;
import net.samagames.survivalapi.game.types.SurvivalTeamGame;
import net.samagames.uhcrun.UHCRunGameLoop;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class UHCRunSurvivalTeamGame extends SurvivalTeamGame<UHCRunGameLoop> implements UHCRunSurvivalGame
{
    public UHCRunSurvivalTeamGame(JavaPlugin plugin, String gameCodeName, String gameName, String gameDescription, String magicSymbol, int personsPerTeam)
    {
        super(plugin, gameCodeName, gameName, gameDescription, magicSymbol, UHCRunGameLoop.class, personsPerTeam);
    }

    @Override
    public void teleportDeathMatch()
    {
        Iterator<Location> locationIterator = this.spawns.iterator();
        ArrayList<SurvivalTeam> toRemove = new ArrayList<>();

        for (SurvivalTeam team : this.teams)
        {
            if (!locationIterator.hasNext())
            {
                toRemove.add(team);

                for (UUID playerUUID : team.getPlayersUUID().keySet())
                {
                    Player player = this.server.getPlayer(playerUUID);

                    if (player != null)
                        player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");

                    this.gamePlayers.remove(playerUUID);
                }

                continue;
            }

            Location location = locationIterator.next();

            for (UUID playerUUID : team.getPlayersUUID().keySet())
            {
                Player player = this.server.getPlayer(playerUUID);

                if (player == null)
                    this.gamePlayers.remove(playerUUID);
                else
                    player.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
            }
        }

        this.teams.removeAll(toRemove);
        toRemove.clear();
    }
}

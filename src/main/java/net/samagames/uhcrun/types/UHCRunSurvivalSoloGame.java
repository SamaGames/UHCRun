package net.samagames.uhcrun.types;

import net.samagames.survivalapi.game.types.SurvivalSoloGame;
import net.samagames.uhcrun.UHCRunGameLoop;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class UHCRunSurvivalSoloGame extends SurvivalSoloGame<UHCRunGameLoop> implements UHCRunSurvivalGame
{
    public UHCRunSurvivalSoloGame(JavaPlugin plugin, String gameCodeName, String gameName, String gameDescription, String magicSymbol)
    {
        super(plugin, gameCodeName, gameName, gameDescription, magicSymbol, UHCRunGameLoop.class);
    }

    @Override
    public void teleportDeathMatch()
    {
        Collections.shuffle(this.spawns);
        Iterator<Location> locationIterator = this.spawns.iterator();

        for (UUID uuid : (Set<UUID>) this.getInGamePlayers().keySet())
        {
            Player player = this.server.getPlayer(uuid);

            if (player == null)
            {
                this.gamePlayers.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext())
            {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                this.gamePlayers.remove(uuid);

                continue;
            }

            this.removeEffects(player);

            Location location = locationIterator.next();
            player.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
        }
    }
}

package net.samagames.uhcrun.game;

import net.samagames.tools.Titles;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SoloGame extends AbstractGame
{

    public SoloGame(UHCRun plugin)
    {
        super(plugin, plugin.getAdaptator().getAPI().getGameManager().getGameProperties(), plugin.getAdaptator().getAPI().getGameManager().getGameProperties().getMaxSlots());
    }

    @Override
    public void checkStump(Player player)
    {
        UHCPlayer playerData = getPlayer(player.getUniqueId());
        if (getInGamePlayers().size() == 3)
        {
            playerData.addCoins(20, "Troisième au classement !");
        } else if (getInGamePlayers().size() == 2)
        {
            playerData.addCoins(50, "Second au classement !");
            playerData.addStars(1, "Second au classement !");

            // HACK
            gamePlayers.remove(playerData.getUUID());
            UUID winnerId = getInGamePlayers().keySet().iterator().next();
            gamePlayers.put(playerData.getUUID(), playerData);

            Player winner = server.getPlayer(winnerId);
            if (winner == null)
            {
                handleGameEnd();
            } else
            {
                win(winner);
            }
        } else if (getInGamePlayers().size() == 1)
        {
            handleGameEnd();
        } else
        {
            server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + (getInGamePlayers().size() - 1) + ChatColor.YELLOW + " joueur(s) en vie.");
        }
    }

    public void win(final Player player)
    {
        final UHCPlayer playerData = this.getPlayer(player.getUniqueId());
        if (playerData != null)
        {
            playerData.addStars(2, "Victoire !");
            playerData.addCoins(100, "Victoire ! ");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    this.increaseStat(player.getUniqueId(), "wins", 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            server.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de " + player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");

            List<Player> players = new ArrayList<>();
            players.addAll(server.getOnlinePlayers());
            for (Player user : players)
            {
                Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
            }

            this.effectsOnWinner(player);
        }
        this.handleGameEnd();
    }

    @Override
    protected void teleport()
    {
        Iterator<Location> locationIterator = this.spawnPoints.iterator();

        for (UUID uuid : this.getInGamePlayers().keySet())
        {
            Player player = server.getPlayer(uuid);
            if (player == null)
            {
                gamePlayers.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext())
            {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                gamePlayers.remove(uuid);
                continue;
            }

            Location location = locationIterator.next();
            player.teleport(location);
        }

    }

    @Override
    public void teleportDeathMatch()
    {
        Collections.shuffle(this.spawnPoints);
        Iterator<Location> locationIterator = this.spawnPoints.iterator();

        for (UUID uuid : this.getInGamePlayers().keySet())
        {
            Player player = server.getPlayer(uuid);
            if (player == null)
            {
                gamePlayers.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext())
            {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                gamePlayers.remove(uuid);
                continue;
            }

            Location location = locationIterator.next();
            player.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
        }
    }
}

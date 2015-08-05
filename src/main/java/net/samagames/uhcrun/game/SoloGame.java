package net.samagames.uhcrun.game;

import net.samagames.api.SamaGamesAPI;
import net.samagames.tools.Titles;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SoloGame extends Game {


    public SoloGame() {
        super(SamaGamesAPI.get().getGameManager().getGameProperties().getMinSlots());
    }


    @Override
    public void postInit(World world) {
        super.postInit(world);

        for (int i = 0; i < plugin.getAPI().getGameManager().getGameProperties().getMaxSlots(); i++) {
            final Location randomLocation = new Location(world, -500 + rand.nextInt(500 - (-500) + 1), 150, -500 + rand.nextInt(500 - (-500) + 1));
            for (int y = 0; y < 16; y++) {
                world.getChunkAt(world.getBlockAt(randomLocation.getBlockX(), y * 16, randomLocation.getBlockZ())).load(true);
            }

            spawnPoints.add(randomLocation);
        }
    }

    @Override
    public void checkStump(Player player) {
        UHCPlayer playerData = getPlayer(player.getUniqueId());
        if (getInGamePlayers().size() == 3) {
            playerData.addCoins(20, "Troisième au classement !");
        }

        if (getInGamePlayers().size() == 2) {
            playerData.addCoins(50, "Second au classement !");
            playerData.addStars(1, "Second au classement !");

            // HACK
            this.gamePlayers.remove(playerData.getUUID());
            UUID winnerId = getInGamePlayers().keySet().iterator().next();
            this.gamePlayers.put(playerData.getUUID(), playerData);

            Player winner = server.getPlayer(winnerId);
            if (winner == null) {
                System.out.println("TEST");
                this.handleGameEnd();
            } else {
                this.win(winner);
            }
        } else if (getInGamePlayers().size() == 1) {
            this.handleGameEnd();
        } else {
            server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + getInGamePlayers().size() + ChatColor.YELLOW + " joueur(s) en vie.");
        }

    }

    public void win(final Player player) {
        final UHCPlayer playerData = this.getPlayer(player.getUniqueId());
        playerData.addStars(2, "Victoire !");
        playerData.addCoins(100, "Victoire ! ");

        try {
            this.increaseStat(player.getUniqueId(), "victories", 1);
        } catch (Exception ex) {
        }

        server.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de " + player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");

        for (Player user : server.getOnlinePlayers()) {
            Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
        }

        this.effectsOnWinner(player);
        this.handleGameEnd();
    }

    @Override
    protected void teleport() {
        Iterator<Location> locationIterator = this.spawnPoints.iterator();

        for (UUID uuid : this.getInGamePlayers().keySet()) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                gamePlayers.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                gamePlayers.remove(uuid);
                continue;
            }

            Location location = locationIterator.next();
            player.teleport(location);
        }

    }

    @Override
    public void teleportDeathMatch() {
        Collections.shuffle(this.spawnPoints);
        Iterator<Location> locationIterator = this.spawnPoints.iterator();

        for (UUID uuid : this.getInGamePlayers().keySet()) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                gamePlayers.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                gamePlayers.remove(uuid);
                continue;
            }

            Location location = locationIterator.next();
            player.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
        }
    }
}

package net.samagames.uhcrun.task;

import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.messages.IMessageManager;
import net.samagames.tools.Titles;
import net.samagames.uhcrun.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class BeginCountdown implements Runnable {

    private final int time;
    private final Game game;
    private final int maxPlayers;
    private final int minPlayers;
    private final IMessageManager messageManager;
    private boolean ready = false;
    private int countdown;

    public BeginCountdown(Game game, int maxPlayers, int minPlayers, int time) {
        this.game = game;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.time = time;
        this.countdown = time;
        this.messageManager = this.game.getCoherenceMachine().getMessageManager();
    }

    @Override
    public void run() {
        int nPlayers = game.getConnectedPlayers();

        if (nPlayers >= maxPlayers && countdown > (time / 4)) {
            ready = true;
            countdown = time / 4;
        } else {
            if (nPlayers >= minPlayers && !ready) {
                ready = true;
                game.setStatus(Status.STARTING);
                countdown = time / 2;
            }

            if (nPlayers >= ((double) maxPlayers / 100.0) * 65.0 && countdown > (time / 2) || (nPlayers >= ((double) maxPlayers / 100.0) * 85.0 && countdown > (time / 4))) {
                countdown = time / 4;
            }

            if (nPlayers < minPlayers && ready) {
                ready = false;
                messageManager.writeNotEnougthPlayersToStart();
                game.setStatus(Status.WAITING_FOR_PLAYERS);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setLevel(countdown - 1);
                }
            }

            if (ready) {
                timeBroadcast();
            }

        }
    }

    private void timeBroadcast() {
        countdown--;
        if (countdown == 0) {
            game.startGame();
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(countdown);
            if (countdown <= 5 || countdown == 10) {
                Titles.sendTitle(player, 2, 14, 2, ChatColor.GOLD + "Début dans " + ChatColor.RED + countdown + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
                player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1.0F, 1.0F);
            }
        }

        if (countdown <= 5 || countdown == 10 || countdown % 30 == 0) {
            messageManager.writeGameStartIn(countdown);
        }
    }
}

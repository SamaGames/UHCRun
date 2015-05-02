package net.samagames.uhcrun.task;

import net.samagames.gameapi.GameUtils;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.messages.StaticMessages;
import net.samagames.uhcrun.game.Game;
import net.samagames.utils.Titles;
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

    protected Game game;
    protected int maxPlayers = 0;
    protected int minPlayers = 0;
    protected boolean ready = false;
    protected int time = 121; // 2 minutes

    public BeginCountdown(Game game, int maxPlayers, int minPlayers) {
        this.game = game;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
    }

    @Override
    public void run() {
        int nPlayers = game.countGamePlayers();

        if (nPlayers >= maxPlayers && time > 31) {
            ready = true;
            time = 31;
        } else {
            if (nPlayers >= minPlayers && !ready) {
                ready = true;
                game.updateStatus(Status.Starting);
                time = 121;
            }

            if (nPlayers >= ((double)maxPlayers/100.0)*65.0 && time > 60) {
                time = 61;
            }

            if (nPlayers >= ((double)maxPlayers/100.0)*85.0 && time > 30) {
                time = 31;
            }

            if (nPlayers < minPlayers && ready) {
                ready = false;
                Bukkit.broadcastMessage(StaticMessages.NOTENOUGTHPLAYERS.get(game.getCoherenceMachine()));
                game.updateStatus(Status.Available);
                for (Player p : Bukkit.getOnlinePlayers())
                    p.setLevel(120);
            }

            if (ready) {
                time--;
                timeBroadcast();
            }
        }
    }

    public void timeBroadcast() {
        if (time == 0) {
            game.start();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setLevel(time);
            if (time <= 5 || time == 10) {
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "Début dans " + ChatColor.RED + time + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
            }
        }

        if (time <= 5 || time == 10 || time % 30 == 0) {
            Bukkit.broadcastMessage(StaticMessages.STARTIN.get(this.game.getCoherenceMachine()).replace("${TIME}", time + " seconde" + ((time > 1) ? "s" : "")));
        }

        if (time <= 5 || time == 10) {
            GameUtils.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}

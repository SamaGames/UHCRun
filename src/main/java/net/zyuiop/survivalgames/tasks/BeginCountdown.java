package net.zyuiop.survivalgames.tasks;

import net.samagames.gameapi.GameUtils;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.messages.StaticMessages;
import net.samagames.utils.Titles;
import net.zyuiop.survivalgames.game.BasicGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by zyuiop on 26/09/14.
 */
public class BeginCountdown implements Runnable {

    protected BasicGame parent;
    protected int maxPlayers = 0;
    protected int minPlayers = 0;
    protected boolean ready = false;
    protected int time = 121; // 2 minutes

    public BeginCountdown(BasicGame parentArena, int maxPlayers, int minPlayers) {
        this.parent = parentArena;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
    }

    @Override
    public void run() {
        int nPlayers = parent.countGamePlayers();

        if (nPlayers >= maxPlayers && time > 10) {
            ready = true;
            time = 10;
        } else {
            if (nPlayers >= minPlayers && !ready) {
                ready = true;
                parent.updateStatus(Status.Starting);
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
                Bukkit.broadcastMessage(StaticMessages.NOTENOUGTHPLAYERS.get(parent.getCoherenceMachine()));
                parent.updateStatus(Status.Available);
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
            parent.start();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setLevel(time);
            if (time <= 5 || time == 10) {
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "Début dans "+ChatColor.RED + time + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
            }
        }

        if (time <= 5 || time == 10 || time % 30 == 0) {
            parent.getMessageManager().writeStartGameCountdownMessage(time);
        }

        if (time <= 5 || time == 10) {
            GameUtils.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}

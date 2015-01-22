package net.zyuiop.survivalgames.tasks;

import net.samagames.utils.ObjectiveSign;
import net.zyuiop.survivalgames.SurvivalGames;
import net.zyuiop.survivalgames.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by zyuiop on 26/09/14.
 */
public class GameLoop implements Runnable {

    protected Game parent;
    protected int minutes = 0;
    protected int seconds = 0;
    protected TimedEvent nextEvent = null;

    protected ObjectiveSign objective;

    public GameLoop(Game parentArena, ObjectiveSign obj) {
        this.parent = parentArena;
        objective = obj;
        nextEvent = new TimedEvent(1, 0, ChatColor.GOLD + "Dégats actifs", ChatColor.GOLD) {
            @Override
            public void run() {
                parent.enableDamage();
                nextEvent = new TimedEvent(3, 0, ChatColor.GOLD + "PVP Actif", ChatColor.GOLD) {
                    @Override
                    public void run() {
                        parent.enablePVP();
                        createReductionEvent();
                    }
                };
            }
        };
    }

    protected void createReductionEvent() {
        nextEvent = new TimedEvent(16, 0, ChatColor.RED + "Réduction", ChatColor.RED) {
            @Override
            public void run() {
                Bukkit.getWorld("world").getWorldBorder().setSize(80, 420);
                Bukkit.broadcastMessage(ChatColor.GOLD + "La réduction de la map commence !");
                Bukkit.broadcastMessage(ChatColor.GOLD + "La map se réduit en "+ChatColor.RED + "80*80");
                Bukkit.broadcastMessage(ChatColor.GOLD + "La réduction sera terminée dans 6 minutes.");
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les bordures seront alors en coordonnées "+ChatColor.RED + "-40 +40");
                nextEvent = new TimedEvent(7, 0, "Fin de réduction", ChatColor.WHITE) {
                    @Override
                    public void run() {
                        Bukkit.broadcastMessage(ChatColor.GOLD + "La réduction de la map est a présent terminée.");
                        nextEvent = new TimedEvent(4, 0, ChatColor.RED + "Fin de partie", ChatColor.RED) {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(ChatColor.GOLD + "La partie se termine.");
                                Bukkit.getServer().shutdown();
                            }
                        };
                    }
                };
            }
        };
    }

    public String timeString(int minutes, int seconds) {
        String min = ((minutes < 10) ? "0" : "") + minutes;
        String sec = ((seconds < 10) ? "0" : "") + seconds;

        return min +" min "+sec+ " sec";
    }

    public String time(int minutes, int seconds) {
        String min = ((minutes < 10) ? "0" : "") + minutes;
        String sec = ((seconds < 10) ? "0" : "") + seconds;

        return min +":"+sec;
    }

    @Override
    public void run() {
        seconds++;
        if (seconds >= 60) {
            minutes++;
            seconds = 0;
        }

        this.objective.setLine(- 1, ChatColor.WHITE + " ");
        this.objective.setLine(- 2, timeString(minutes, seconds));
        this.objective.setLine(- 3, "Joueurs : " + ChatColor.AQUA + parent.countGamePlayers());

        int indice = -3;

        if (nextEvent != null) {
            this.objective.setLine(- 4, ChatColor.GRAY + "");
            this.objective.setLine(- 5, nextEvent.string);
            this.objective.setLine(- 6, nextEvent.color+"dans "+ time(nextEvent.minutes, nextEvent.seconds));

            if ((nextEvent.seconds == 0 && nextEvent.minutes <= 3) || (nextEvent.minutes == 0 && (nextEvent.seconds < 6 || nextEvent.seconds == 10 || nextEvent.seconds == 30)))
                Bukkit.broadcastMessage(parent.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + nextEvent.string + ChatColor.GOLD + " dans "+ ((nextEvent.minutes != 0) ? nextEvent.minutes + "min" : nextEvent.seconds + " sec"));
            nextEvent.decrement();
            indice = -6;
        }

        if (parent.getFeast() != null) {
            this.objective.setLine(indice-1, ChatColor.GOLD + "");
            this.objective.setLine(indice-2, ChatColor.GOLD + "Feast en :");
            this.objective.setLine(indice-3, ChatColor.GOLD + ""+ parent.getFeast().getBlockX()+";"+parent.getFeast().getBlockZ());
        }

        Bukkit.getScheduler().runTaskAsynchronously(SurvivalGames.instance, new Runnable() {
            @Override
            public void run() {
                objective.updateLines();
            }
        });
    }

    private abstract class TimedEvent {
        public int minutes;
        public int seconds;
        public String string;
        public ChatColor color;

        public TimedEvent(int minutes, int seconds, String string, ChatColor color) {
            this.minutes = minutes;
            this.seconds = seconds;
            this.string = string;
            this.color = color;
        }

        public abstract void run();

        public void decrement() {
            seconds --;
            if (seconds < 0) {
                minutes --;
                seconds = 59;
            }

            if (minutes < 0) {
                run();
            }
        }
    }
}

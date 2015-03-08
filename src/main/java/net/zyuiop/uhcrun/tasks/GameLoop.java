package net.zyuiop.uhcrun.tasks;

import net.samagames.utils.ObjectiveSign;
import net.zyuiop.uhcrun.UHCRun;
import net.zyuiop.uhcrun.game.BasicGame;
import net.zyuiop.uhcrun.game.TeamGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by zyuiop on 26/09/14.
 */
public class GameLoop implements Runnable {

    protected BasicGame parent;
    protected int minutes = 0;
    protected int seconds = 0;
    protected TimedEvent nextEvent = null;

    protected ObjectiveSign objective;

    public GameLoop(BasicGame parentArena, ObjectiveSign obj) {
        this.parent = parentArena;
        objective = obj;
        nextEvent = new TimedEvent(1, 0, ChatColor.GOLD + "Dégats actifs", ChatColor.GOLD) {
            @Override
            public void run() {

                Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats sont désormais actifs.");
                Bukkit.broadcastMessage(ChatColor.GOLD + "La map sera réduite dans 19 minutes. Le PVP sera activé à ce moment là.");
                parent.enableDamage();
                createReductionEvent();
            }
        };
    }

    public void forceNextEvent() {
        if (nextEvent != null)
            nextEvent.run();
    }

    protected void createReductionEvent() {
        nextEvent = new TimedEvent(19, 0, ChatColor.RED + "Téléportation", ChatColor.RED) {
            @Override
            public void run() {
                parent.disableDamages();
                parent.teleportDeathmatch();

                try {
                    Bukkit.getWorld("world").getWorldBorder().setSize(400, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    Bukkit.getWorld("world").getWorldBorder().setSize(10, 600);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Bukkit.broadcastMessage(ChatColor.GOLD + "La map est désormais réduite en 400 * 400");
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les bordures sont en coordonnées "+ChatColor.RED + "-200 +200");
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats et le PVP seront activés dans 30 secondes !");

                nextEvent = new TimedEvent(0, 30, ChatColor.RED + "PVP Activé", ChatColor.RED) {
                    @Override
                    public void run() {
                        parent.enablePVP();
                        parent.enableDamage();
                        Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats et le PVP sont maintenant activés. Bonne chance !");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "La map est maintenant en réduction constante pendant les 10 prochaines minutes.");
                        nextEvent = new TimedEvent(9, 30, "Fin de réduction", ChatColor.WHITE) {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(ChatColor.GOLD + "La map est désormais réduite. Fin de partie forcée dans 2 minutes.");
                                nextEvent = new TimedEvent(2, 0, ChatColor.RED + "Fin de partie", ChatColor.RED) {
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

        this.objective.setLine(- 1, ChatColor.WHITE + " " + ChatColor.RED + " ");
        this.objective.setLine(- 2, timeString(minutes, seconds));
        this.objective.setLine(- 3, "Joueurs : " + ChatColor.AQUA + parent.countGamePlayers());

        int lastLine = -3;
        if (parent instanceof TeamGame) {
            this.objective.setLine(- 4, ChatColor.WHITE + " ");
            this.objective.setLine(- 5, "Equipes : " + ChatColor.AQUA + ((TeamGame) parent).getTeams().size());
            lastLine = -5;
        }

        if (nextEvent != null) {
            this.objective.setLine(lastLine - 1, ChatColor.GRAY + "");
            this.objective.setLine(lastLine - 2, nextEvent.string);
            this.objective.setLine(lastLine - 3, nextEvent.color+"dans "+ time(nextEvent.minutes, nextEvent.seconds));

            if ((nextEvent.seconds == 0 && nextEvent.minutes <= 3 && nextEvent.minutes > 0)|| (nextEvent.minutes == 0 && (nextEvent.seconds < 6 || nextEvent.seconds == 10 || nextEvent.seconds == 30)))
                Bukkit.broadcastMessage(parent.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + nextEvent.string + ChatColor.GOLD + " dans "+ ((nextEvent.minutes != 0) ? nextEvent.minutes + "min" : nextEvent.seconds + " sec"));

            if (nextEvent.seconds == 0 && 0 == nextEvent.minutes)
                Bukkit.broadcastMessage(parent.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + nextEvent.string + ChatColor.GOLD + " maintenant !");
            nextEvent.decrement();
        }

        Bukkit.getScheduler().runTaskAsynchronously(UHCRun.instance, new Runnable() {
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
        public boolean wasRun = false;

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

            if ((minutes < 0 || (seconds == 0 && minutes == 0)) && !wasRun) {
                wasRun = true;
                run();
            }
        }
    }
}

package net.samagames.uhcrun.tasks;

import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.BasicGame;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.utils.ObjectiveSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zyuiop on 26/09/14.
 */
public class GameLoop implements Runnable {

    protected BasicGame parent;
    protected int minutes = 0;
    protected int seconds = 0;
    protected TimedEvent nextEvent = null;

    protected ConcurrentHashMap<UUID, ObjectiveSign> objectives = new ConcurrentHashMap<>();

    public GameLoop(BasicGame parentArena) {
        this.parent = parentArena;
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

	public void addPlayer(UUID uuid, ObjectiveSign sign) {
		this.objectives.put(uuid, sign);
	}

	public void removePlayer(UUID uuid) {
		this.objectives.remove(uuid);
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

        for (UUID player : objectives.keySet()) {
			final ObjectiveSign objective = objectives.get(player);
			Player player1 = Bukkit.getPlayer(player);
			if (player1 == null) {
				removePlayer(player);
				continue;
			}

			objective.setLine(-1, ChatColor.BLUE + " ");
			objective.setLine(-2, ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + parent.countGamePlayers());
			objective.setLine(-3, ChatColor.GRAY + "  ");

			int lastLine = -2;
			if (parent instanceof TeamGame) {
				objective.setLine(-3, ChatColor.GRAY + "Équipes : " + ChatColor.WHITE + ((TeamGame) parent).getTeams().size());
				objective.setLine(-4, ChatColor.RED + "   ");
				lastLine = -4;
			}

			if (nextEvent != null) {
				objective.setLine(lastLine - 1, nextEvent.string);
				objective.setLine(lastLine - 2, nextEvent.color+"dans "+ time(nextEvent.minutes, nextEvent.seconds));
                objective.setLine(lastLine - 3, ChatColor.GOLD + "     ");
				lastLine -= 3;
			}

			int kills = parent.countKills(player);
            if (kills > 0) {
					objective.setLine(lastLine - 1, ChatColor.GRAY + "Joueurs tués : " + ChatColor.WHITE + "" + kills);
                    objective.setLine(lastLine - 2, ChatColor.AQUA + "      ");
					lastLine -= 2;
            }


			objective.setLine(lastLine - 1, ChatColor.GRAY + "Bordure :");
			objective.setLine(lastLine - 2, ChatColor.WHITE + "-" + String.valueOf(((int) Bukkit.getWorld("world").getWorldBorder().getSize()) / 2) + " +" + String.valueOf(((int) Bukkit.getWorld("world").getWorldBorder().getSize()) / 2));
			objective.setLine(lastLine - 3, ChatColor.RED + "              ");
			objective.setLine(lastLine - 4, ChatColor.GRAY + "Temps : " + ChatColor.WHITE + time(minutes, seconds));
			objective.updateLines();

			Bukkit.getScheduler().runTaskAsynchronously(UHCRun.instance, new Runnable() {
				@Override
				public void run() {
					objective.updateLines();
				}
			});
		}

		if ((nextEvent.seconds == 0 && nextEvent.minutes <= 3 && nextEvent.minutes > 0)|| (nextEvent.minutes == 0 && (nextEvent.seconds < 6 || nextEvent.seconds == 10 || nextEvent.seconds == 30)))
			Bukkit.broadcastMessage(parent.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + nextEvent.string + ChatColor.GOLD + " dans "+ ((nextEvent.minutes != 0) ? nextEvent.minutes + "min" : nextEvent.seconds + " sec"));

		if (nextEvent.seconds == 0 && 0 == nextEvent.minutes)
			Bukkit.broadcastMessage(parent.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + nextEvent.string + ChatColor.GOLD + " maintenant !");

		nextEvent.decrement();
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

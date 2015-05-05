package net.samagames.uhcrun.task;

import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.IGame;
import net.samagames.utils.ObjectiveSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class GameLoop implements Runnable
{
    protected IGame game;
    protected int minutes = 0;
    protected int seconds = 0;
    protected GameLoop.TimedEvent nextEvent = null;
    protected ConcurrentHashMap<UUID, ObjectiveSign> objectives = new ConcurrentHashMap();
    private UHCRun plugin;

    public int getTime()
    {
        return this.minutes * 60 + this.seconds;
    }

    public GameLoop(IGame game)
    {
        this.game = game;
        this.plugin = UHCRun.getInstance();
        this.nextEvent = new GameLoop.TimedEvent(1, 0, ChatColor.GOLD + "Dégats actifs", ChatColor.GOLD)
        {
            public void run()
            {
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats sont désormais actifs.");
                Bukkit.broadcastMessage(ChatColor.GOLD + "La map sera réduite dans " + (game.getPreparingTime() - 1) + " minutes.Le PVP sera activé à ce moment là.");
                game.enableDamages();
                createReductionEvent();
            }
        };
    }

    public void forceNextEvent()
    {
        if (this.nextEvent != null)
        {
            this.nextEvent.run();
        }

    }

    public void addPlayer(UUID uuid, ObjectiveSign sign)
    {
        this.objectives.put(uuid, sign);
    }

    public void removePlayer(UUID uuid)
    {
        this.objectives.remove(uuid);
    }

    protected void createReductionEvent()
    {
        this.nextEvent = new GameLoop.TimedEvent(this.game.getPreparingTime() - 1, 0, ChatColor.RED + "Téléportation", ChatColor.RED)
        {
            @Override
            public void run()
            {
                game.disableDamages();
                game.teleportDeathMatch();
                int size = game.getDeathMatchSize();
                int border = size / 2;

                try
                {
                    Bukkit.getWorld("world").getWorldBorder().setSize((double) size, 0L);
                } catch (Exception var5)
                {
                    var5.printStackTrace();
                }

                try
                {
                    Bukkit.getWorld("world").getWorldBorder().setSize(10.0D, (long) (game.getReductionTime() * 60));
                } catch (Exception var4)
                {
                    var4.printStackTrace();
                }

                Bukkit.broadcastMessage(ChatColor.GOLD + "La map est désormais réduite en " + size + " * " + size);
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les bordures sont en coordonnées " + ChatColor.RED + "-" + border + " +" + border);
                Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats et le PVP seront activés dans 30 secondes !");
                nextEvent = new GameLoop.TimedEvent(0, 30, ChatColor.RED + "PVP Activé", ChatColor.RED)
                {
                    public void run()
                    {
                        game.enablePVP();
                        game.enableDamages();
                        Bukkit.broadcastMessage(ChatColor.GOLD + "Les dégats et le PVP sont maintenant activés. Bonne chance !");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "La map est maintenant en réduction constante pendant les 10 prochaines minutes.");
                        nextEvent = new GameLoop.TimedEvent(game.getReductionTime() - 1, 30, "Fin de réduction", ChatColor.WHITE)
                        {
                            public void run()
                            {
                                Bukkit.broadcastMessage(ChatColor.GOLD + "La map est désormais réduite. Fin de partie forcée dans 2 minutes.");
                                nextEvent = new GameLoop.TimedEvent(2, 0, ChatColor.RED + "Fin de partie", ChatColor.RED)
                                {
                                    public void run()
                                    {
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

    public String time(int minutes, int seconds)
    {
        String min = (minutes < 10 ? "0" : "") + minutes;
        String sec = (seconds < 10 ? "0" : "") + seconds;
        return min + ":" + sec;
    }

    public void run()
    {
        ++this.seconds;
        if (this.seconds >= 60)
        {
            ++this.minutes;
            this.seconds = 0;
        }

        Iterator var1 = this.objectives.keySet().iterator();

        while (var1.hasNext())
        {
            UUID player = (UUID) var1.next();
            final ObjectiveSign objective = this.objectives.get(player);
            Player player1 = Bukkit.getPlayer(player);
            if (player1 == null)
            {
                Bukkit.getLogger().info("Player null :  " + player);
                this.objectives.remove(player);
            } else
            {
                objective.setLine(1, ChatColor.BLUE + " ");
                objective.setLine(2, ChatColor.GRAY + "Joueurs : " + ChatColor.WHITE + this.game.countGamePlayers());
                objective.setLine(3, ChatColor.GRAY + "  ");
                int lastLine = 3;
                /*if(this.game instanceof TeamGame) {
                    objective.setLine(-3, ChatColor.GRAY + "Équipes : " + ChatColor.WHITE + ((TeamGame)this.game).getTeams().size());
                    objective.setLine(-4, ChatColor.RED + "   ");
                    lastLine = -4;
                }*/

                if (this.nextEvent != null)
                {
                    objective.setLine(lastLine + 1, this.nextEvent.string);
                    objective.setLine(lastLine + 2, this.nextEvent.color + "dans " + this.time(this.nextEvent.minutes, this.nextEvent.seconds));
                    objective.setLine(lastLine + 3, ChatColor.GOLD + "     ");
                    lastLine += 3;
                }

                int kills = this.game.getKills(player);
                if (kills > 0)
                {
                    objective.setLine(lastLine + 1, ChatColor.GRAY + "Joueurs tués : " + ChatColor.WHITE + "" + kills);
                    objective.setLine(lastLine + 2, ChatColor.AQUA + "      ");
                    lastLine += 2;
                }

                objective.setLine(lastLine + 1, ChatColor.GRAY + "Bordure :");
                objective.setLine(lastLine + 2, ChatColor.WHITE + "-" + (int) Bukkit.getWorld("world").getWorldBorder().getSize() / 2 + " +" + (int) Bukkit.getWorld("world").getWorldBorder().getSize() / 2);
                objective.setLine(lastLine + 3, ChatColor.RED + "              ");
                objective.setLine(lastLine + 4, ChatColor.GRAY + "Temps : " + ChatColor.WHITE + this.time(this.minutes, this.seconds));
                objective.updateLines();
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> objective.updateLines());
            }
        }

        if (this.nextEvent.seconds == 0 && this.nextEvent.minutes <= 3 && this.nextEvent.minutes > 0 || this.nextEvent.minutes == 0 && (this.nextEvent.seconds < 6 || this.nextEvent.seconds == 10 || this.nextEvent.seconds == 30))
        {
            Bukkit.broadcastMessage(this.game.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + this.nextEvent.string + ChatColor.GOLD + " dans " + (this.nextEvent.minutes != 0 ? this.nextEvent.minutes + "min" : this.nextEvent.seconds + " sec"));
        }

        if (this.nextEvent.seconds == 0 && 0 == this.nextEvent.minutes)
        {
            Bukkit.broadcastMessage(this.game.getCoherenceMachine().getGameTag() + ChatColor.GOLD + ChatColor.GOLD + this.nextEvent.string + ChatColor.GOLD + " maintenant !");
        }

        this.nextEvent.decrement();
    }

    private abstract class TimedEvent
    {
        public int minutes;
        public int seconds;
        public String string;
        public ChatColor color;
        public boolean wasRun = false;

        public TimedEvent(int minutes, int seconds, String string, ChatColor color)
        {
            this.minutes = minutes;
            this.seconds = seconds;
            this.string = string;
            this.color = color;
        }

        public abstract void run();

        public void decrement()
        {
            --this.seconds;
            if (this.seconds < 0)
            {
                --this.minutes;
                this.seconds = 59;
            }

            if ((this.minutes < 0 || this.seconds == 0 && this.minutes == 0) && !this.wasRun)
            {
                this.wasRun = true;
                this.run();
            }

        }
    }
}

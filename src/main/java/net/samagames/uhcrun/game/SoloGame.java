package net.samagames.uhcrun.game;

import net.samagames.uhcrun.utils.Colors;
import net.samagames.utils.Titles;
import net.zyuiop.MasterBundle.StarsManager;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SoloGame extends Game
{
    private final java.util.List<Location> spawnPoints;
    private final Random rand;

    public SoloGame(short normalSlots, short vipSlots, short minPlayers)
    {
        super("Solo", normalSlots, vipSlots, minPlayers);

        this.rand = new Random();
        this.spawnPoints = new ArrayList<>();
    }


    @Override
    public void postInit()
    {
        super.postInit();
        this.disableDamages();

        World world = Bukkit.getWorld("world");

        for (int i = 0; i < this.getMaxPlayers(); i++)
        {
            final Location randomLocation = new Location(world, -500 + rand.nextInt(500 - (-500) + 1), 150, -500 + rand.nextInt(500 - (-500) + 1));
            for (int y = 0; y < 16; y++)
            {
                world.getChunkAt(world.getBlockAt(randomLocation.getBlockX(), y * 16, randomLocation.getBlockZ())).load(true);
            }

            spawnPoints.add(randomLocation);
        }
    }

    @Override
    public void creditKillCoins(Player player)
    {
        CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Un joueur tué !");
    }

    @Override
    public void checkStump(Player player)
    {
        if (this.players.size() == 2)
        {
            CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Troisième au classement !");
        }

        if (this.players.size() == 1)
        {
            CoinsManager.creditJoueur(player.getUniqueId(), 50, true, true, "Second au classement !");
            StarsManager.creditJoueur(player.getUniqueId(), 1, "Second au classement !");
            UUID winnerId = this.players.iterator().next();
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner == null)
            {
                this.finish();
            } else
            {
                this.win(winner);
            }
        } else if (this.players.size() == 0)
        {
            this.finish();
        } else
        {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + this.players.size() + ChatColor.YELLOW + " joueur(s) en vie.");
        }

    }

    public void win(final Player player)
    {
        CoinsManager.creditJoueur(player.getUniqueId(), 100, true, true, "Victoire !");
        StarsManager.creditJoueur(player.getUniqueId(), 2, "Victoire !");

        try
        {
            StatsApi.increaseStat(player, "uhcrun", "victories", 1);
        } catch (Exception ex)
        {
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de " + player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");
        Iterator nb = Bukkit.getOnlinePlayers().iterator();

        while (nb.hasNext())
        {
            Player user = (Player) nb.next();
            Titles.sendTitle(user, Integer.valueOf(5), Integer.valueOf(70), Integer.valueOf(5), ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
        {
            int timer = 0;

            public void run()
            {
                if (this.timer < 20)
                {
                    Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    Random r = new Random();
                    int rt = r.nextInt(4) + 1;
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (rt == 1)
                    {
                        type = FireworkEffect.Type.BALL;
                    }

                    if (rt == 2)
                    {
                        type = FireworkEffect.Type.BALL_LARGE;
                    }

                    if (rt == 3)
                    {
                        type = FireworkEffect.Type.BURST;
                    }

                    if (rt == 4)
                    {
                        type = FireworkEffect.Type.CREEPER;
                    }

                    if (rt == 5)
                    {
                        type = FireworkEffect.Type.STAR;
                    }

                    int r1i = r.nextInt(17) + 1;
                    int r2i = r.nextInt(17) + 1;
                    Color c1 = Colors.getColor(r1i);
                    Color c2 = Colors.getColor(r2i);
                    FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();
                    fwm.addEffect(effect);
                    int rp = r.nextInt(2) + 1;
                    fwm.setPower(rp);
                    fw.setFireworkMeta(fwm);
                    ++this.timer;
                }
            }
        }, 5L, 5L);
        this.finish();
    }

    @Override
    protected void teleport()
    {
        Collections.shuffle(this.spawnPoints);
        Iterator<Location> locationIterator = this.spawnPoints.iterator();
        Iterator<UUID> playerIterator = this.players.iterator();

        while (playerIterator.hasNext())
        {
            UUID uuid = playerIterator.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
            {
                this.players.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext())
            {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                this.players.remove(uuid);
                continue;
            }

            Location location = locationIterator.next();
            player.teleport(location);
        }

    }
}

package net.samagames.uhcrun.game;

import net.samagames.uhcrun.UHCRun;
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

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

/**
 * Created by vialarl on 16/01/2015.
 */
public class SoloGame extends BasicGame {

    public SoloGame() {
        super("Solo", 10, 20, 4);
        spawns.add(new SpawnLocation(0, 200));
        spawns.add(new SpawnLocation(0, 400));
        spawns.add(new SpawnLocation(200, 0));
        spawns.add(new SpawnLocation(400, 0));
        spawns.add(new SpawnLocation(400, 200));
        spawns.add(new SpawnLocation(200, 400));
        spawns.add(new SpawnLocation(400, 400));
        spawns.add(new SpawnLocation(200, 200));
        spawns.add(new SpawnLocation(0, - 200));
        spawns.add(new SpawnLocation(0, -400));
        spawns.add(new SpawnLocation(-200, 0));
        spawns.add(new SpawnLocation(-400, 0));
        spawns.add(new SpawnLocation(-400, -200));
        spawns.add(new SpawnLocation(- 200, - 400));
        spawns.add(new SpawnLocation(-400, -400));
        spawns.add(new SpawnLocation(-200, -200));
        spawns.add(new SpawnLocation(400, -200));
        spawns.add(new SpawnLocation(-400, 200));
        spawns.add(new SpawnLocation(200, -400));
        spawns.add(new SpawnLocation(-200, 400));
        spawns.add(new SpawnLocation(-400, 400));
        spawns.add(new SpawnLocation(400, -400));
        spawns.add(new SpawnLocation(200, -200));
        spawns.add(new SpawnLocation(- 200, 200));
    }

    @Override
    public void teleportAtStart() {
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                players.remove(uuid);
                return;
            }

            player.teleport(locationIterator.next().getSpawn(world));
        }
    }

    @Override
    public void creditKillCoins(Player player) {
        CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Un joueur tué !");
    }

    public void checkStump(Player player) {
        if (players.size() == 2)
            CoinsManager.creditJoueur(player.getUniqueId(), 20, true, true, "Troisième au classement !");

        if (players.size() == 1) {
            CoinsManager.creditJoueur(player.getUniqueId(), 50, true, true, "Second au classement !");
            StarsManager.creditJoueur(player.getUniqueId(), 1, "Second au classement !");

            UUID winnerId = players.iterator().next();
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner == null)
                finish();
            else
                win(winner);
        } else if (players.size() == 0) {
            finish();
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + players.size() + ChatColor.YELLOW + " joueur(s) en vie.");
        }
    }

    public void win(final Player player) {
        CoinsManager.creditJoueur(player.getUniqueId(), 100, true, true, "Victoire !");
        StarsManager.creditJoueur(player.getUniqueId(), 2, "Victoire !");
        try {
            StatsApi.increaseStat(player, "uhcrun", "victories", 1);
        } catch (Exception ignored) {}

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de "+player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");

        for (Player user : Bukkit.getOnlinePlayers()) {
            Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
        }

        final int nb = 20;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCRun.instance, new Runnable() {
            int compteur = 0;

            public void run() {

                if (compteur >= nb) {
                    return;
                }

                //Spawn the Firework, get the FireworkMeta.
                Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                FireworkMeta fwm = fw.getFireworkMeta();

                //Our random generator
                Random r = new Random();

                //Get the type
                int rt = r.nextInt(4) + 1;
                FireworkEffect.Type type = FireworkEffect.Type.BALL;
                if (rt == 1)
                    type = FireworkEffect.Type.BALL;
                if (rt == 2)
                    type = FireworkEffect.Type.BALL_LARGE;
                if (rt == 3)
                    type = FireworkEffect.Type.BURST;
                if (rt == 4)
                    type = FireworkEffect.Type.CREEPER;
                if (rt == 5)
                    type = FireworkEffect.Type.STAR;

                //Get our random colours
                int r1i = r.nextInt(17) + 1;
                int r2i = r.nextInt(17) + 1;
                Color c1 = Colors.getColor(r1i);
                Color c2 = Colors.getColor(r2i);

                //Create our effect with this
                FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

                //Then apply the effect to the meta
                fwm.addEffect(effect);

                //Generate some random power and set it
                int rp = r.nextInt(2) + 1;
                fwm.setPower(rp);

                //Then apply this to our rocket
                fw.setFireworkMeta(fwm);

                compteur++;
            }

        }, 5L, 5L);

        finish();
    }

    public void teleportDeathmatch() {
        World world = Bukkit.getWorld("world");
        Collections.shuffle(spawns);
        Iterator<SpawnLocation> locationIterator = spawns.iterator();
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                players.remove(uuid);
                return;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                players.remove(uuid);
                return;
            }

            player.teleport(locationIterator.next().getDeathmatchSpawn(world));
        }
    }
}

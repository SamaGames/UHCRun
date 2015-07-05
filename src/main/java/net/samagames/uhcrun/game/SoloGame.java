package net.samagames.uhcrun.game;

import net.samagames.api.player.AbstractPlayerData;
import net.samagames.tools.Titles;
import net.samagames.uhcrun.utils.Colors;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SoloGame extends Game {
    private final java.util.List<Location> spawnPoints;
    private final Random rand;

    public SoloGame() {
        super("Solo");

        this.rand = new Random();
        this.spawnPoints = new ArrayList<>();
    }


    @Override
    public void postInit() {
        super.postInit();
        this.disableDamages();

        World world = server.getWorld("world");

        for (int i = 0; i < plugin.getAPI().getGameManager().getGameProperties().getMaxSlots(); i++) {
            final Location randomLocation = new Location(world, -500 + rand.nextInt(500 - (-500) + 1), 150, -500 + rand.nextInt(500 - (-500) + 1));
            for (int y = 0; y < 16; y++) {
                world.getChunkAt(world.getBlockAt(randomLocation.getBlockX(), y * 16, randomLocation.getBlockZ())).load(true);
            }

            spawnPoints.add(randomLocation);
        }
    }

    @Override
    public void creditKillCoins(Player player) {
        AbstractPlayerData playerData = getPlayerData(player);
        playerData.creditCoins(20, "Un joueur tué !", true);
    }

    @Override
    public void checkStump(Player player) {
        AbstractPlayerData playerData = getPlayerData(player);
        if (this.players.size() == 2) {
            playerData.creditCoins(20, "Troisième au classement !", true);
        }

        if (this.players.size() == 1) {
            playerData.creditCoins(50, "Second au classement !", true);
            playerData.creditStars(1, "Second au classement !");
            UUID winnerId = this.players.iterator().next();
            Player winner = server.getPlayer(winnerId);
            if (winner == null) {
                this.finish();
            } else {
                this.win(winner);
            }
        } else if (this.players.size() == 0) {
            this.finish();
        } else {
            server.broadcastMessage(ChatColor.YELLOW + "Il reste encore " + ChatColor.AQUA + this.players.size() + ChatColor.YELLOW + " joueur(s) en vie.");
        }

    }

    public void win(final Player player) {
        final AbstractPlayerData playerData = plugin.getAPI().getPlayerManager().getPlayerData(player.getUniqueId());
        playerData.creditStars(2, "Victoire !");
        playerData.creditCoins(100, "Victoire ! ", true);

        try {
            this.increaseStat(player.getUniqueId(), "victories", 1);
        } catch (Exception ex) {
        }

        server.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Victoire de " + player.getDisplayName() + ChatColor.GOLD + "" + ChatColor.BOLD + " !");

        for (Player user : server.getOnlinePlayers()) {
            Titles.sendTitle(user, 5, 70, 5, ChatColor.GOLD + "Victoire de " + player.getDisplayName(), "");
        }

        server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int timer = 0;

            public void run() {
                if (this.timer < 20) {
                    Firework fw = (Firework) player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    Random r = new Random();
                    int rt = r.nextInt(4) + 1;
                    FireworkEffect.Type type = FireworkEffect.Type.BALL;
                    if (rt == 1) {
                        type = FireworkEffect.Type.BALL;
                    }

                    if (rt == 2) {
                        type = FireworkEffect.Type.BALL_LARGE;
                    }

                    if (rt == 3) {
                        type = FireworkEffect.Type.BURST;
                    }

                    if (rt == 4) {
                        type = FireworkEffect.Type.CREEPER;
                    }

                    if (rt == 5) {
                        type = FireworkEffect.Type.STAR;
                    }

                    int r1i = r.nextInt(15) + 1;
                    int r2i = r.nextInt(15) + 1;
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
    protected void teleport() {
        Collections.shuffle(this.spawnPoints);
        Iterator<Location> locationIterator = this.spawnPoints.iterator();

        for (UUID uuid : this.players) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                this.players.remove(uuid);
                continue;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                this.players.remove(uuid);
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

        for (UUID uuid : this.players) {
            Player player = server.getPlayer(uuid);
            if (player == null) {
                this.players.remove(uuid);
                return;
            }

            if (!locationIterator.hasNext()) {
                player.kickPlayer(ChatColor.RED + "Plus de place dans la partie.");
                this.players.remove(uuid);
                return;
            }

            Location location = locationIterator.next();

            player.teleport(new Location(location.getWorld(), location.getX() * 4 / 10, 150.0, location.getZ() * 4 / 10));
        }
    }
}

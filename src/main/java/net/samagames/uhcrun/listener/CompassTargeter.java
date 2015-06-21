package net.samagames.uhcrun.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import net.samagames.uhcrun.UHCRun;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class CompassTargeter implements Listener {
    private UHCRun plugin;
    private HashMap<UUID, BukkitTask> tasks = new HashMap<>();

    public CompassTargeter(UHCRun plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK) || ev.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (ev.getItem() == null) {
                return;
            }

            if (ev.getItem().getType().equals(Material.COMPASS)) {
                final Player p = ev.getPlayer();
                Player nearest = null;
                for (Entity e : p.getNearbyEntities(500D, 256D, 500D)) {
                    if (e instanceof Player) {
                        Player current = (Player) e;
                        if (!plugin.getGame().isInGame(current.getUniqueId())) {
                            continue;
                        }

                        if (nearest == null || e.getLocation().distance(p.getLocation()) < e.getLocation().distance(nearest.getLocation())) {
                            nearest = current;
                        }
                    }
                }

                if (nearest == null) {
                    p.sendMessage(ChatColor.RED + "Aucune personne n'a été trouvée.");
                } else {
                    p.sendMessage(ChatColor.GREEN + "Votre boussole pointe désormais vers " + ChatColor.GOLD + nearest.getName());
                    p.setCompassTarget(nearest.getLocation());
                    targetPlayer(p, nearest);
                }
            }
        }
    }

    private void unregisterTask(UUID player) {
        BukkitTask current = tasks.get(player);
        if (current != null) {
            current.cancel();
        }
        tasks.remove(player);
    }

    private void targetPlayer(final Player player, final Player target) {
        BukkitTask sched = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (target.isOnline()) {
                player.setCompassTarget(target.getLocation());
            } else {
                unregisterTask(player.getUniqueId());
            }
        }, 10L, 10L);
        updateTask(player.getUniqueId(), sched);
    }

    private void updateTask(UUID player, BukkitTask task) {
        unregisterTask(player);
        tasks.put(player, task);
    }
}

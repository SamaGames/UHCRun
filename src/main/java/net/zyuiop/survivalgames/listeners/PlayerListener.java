package net.zyuiop.survivalgames.listeners;

import net.samagames.gameapi.types.GameArena;
import net.zyuiop.survivalgames.SurvivalGames;
import net.zyuiop.survivalgames.game.Game;
import net.zyuiop.survivalgames.utils.Metadatas;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by {USER}
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class PlayerListener implements Listener {

    protected Game game;

    public PlayerListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            if (!game.isDamages())
                event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Material mat = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();

        switch (mat) {
            case IRON_ORE:
                loc.getWorld().dropItem(loc, new ItemStack(Material.IRON_INGOT, 1));
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                break;
            case GOLD_ORE:
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                loc.getWorld().dropItem(loc, new ItemStack(Material.GOLD_INGOT, 1));
                break;
            case LOG: case LOG_2:
                final List<Block> bList = new ArrayList<Block>();
                bList.add(event.getBlock());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < bList.size(); i++) {
                            Block block = bList.get(i);
                            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2 || block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
                                for (ItemStack item : block.getDrops()) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                                }
                                block.setType(Material.AIR);
                            }
                            for (BlockFace face : BlockFace.values()) {
                                if (block.getRelative(face).getType() == Material.LOG || block.getRelative(face).getType() == Material.LOG_2 || block.getRelative(face).getType() == Material.LEAVES || block.getRelative(face).getType() == Material.LEAVES_2) {
                                    bList.add(block.getRelative(face));
                                }
                            }
                            bList.remove(block);
                        }
                        if (bList.size() == 0)
                            cancel();
                    }
                }.runTaskTimer(SurvivalGames.instance, 1, 1);
                break;
        }
    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Entity damager = event.getDamager();
            EntityType type = event.getEntityType();

            if (type == EntityType.ARROW || type == EntityType.PRIMED_TNT || type == EntityType.PLAYER || damager instanceof Projectile) {
                if (!game.isPvpenabled()) {
                    event.setCancelled(true);
                } else {
                    Player playerdamager = null;
                    if (damager instanceof Player) {
                        playerdamager = (Player) damager;
                    } else if (damager instanceof Arrow) {
                        Arrow arrow = (Arrow) damager;
                        Entity shooter = arrow.getShooter();
                        if (shooter instanceof Player)
                            playerdamager = (Player) shooter;
                    }

                    if (playerdamager != null) {
                        Metadatas.setMetadata(damaged, "lastDamager", playerdamager);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (game.isInGame(event.getPlayer().getUniqueId()))
            game.stumpPlayer(event.getPlayer(), true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (game.isInGame(event.getEntity().getUniqueId()))
            game.stumpPlayer(event.getEntity(), false);
        event.setDeathMessage("");
    }
}

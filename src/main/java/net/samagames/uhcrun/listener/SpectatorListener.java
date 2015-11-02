package net.samagames.uhcrun.listener;

import net.md_5.bungee.api.ChatColor;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.game.AbstractGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SpectatorListener implements Listener
{
    private AbstractGame game;

    public SpectatorListener(AbstractGame parent)
    {
        this.game = parent;
    }

    private boolean cancel(Player p)
    {
        return game.getStatus() != Status.IN_GAME || !game.isInGame(p.getUniqueId());
    }

    @EventHandler
    public void onLoseFood(FoodLevelChangeEvent event)
    {
        event.setCancelled(cancel((Player) event.getEntity()));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onRain(WeatherChangeEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player)
        {
            e.setCancelled(cancel((Player) e.getEntity()));
        }
    }

    @EventHandler
    public void pickup(PlayerPickupItemEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void pickup(PlayerDropItemEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler (ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event)
    {
        if ((game.getStatus() == Status.READY_TO_START || game.getStatus() == Status.WAITING_FOR_PLAYERS) && event.getTo().getY() < 125)
        {
            event.setCancelled(true);
            event.getPlayer().teleport(game.getPlugin().getSpawnLocation());
            event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Mais où allez vous comme ça ?!");
        }
    }

    @EventHandler
    public void onBukket(PlayerBucketFillEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketEmptyEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent e)
    {
        if (e.getEntity() instanceof Player)
        {
            e.setCancelled(cancel((Player) e.getEntity()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (game.getStatus() != Status.IN_GAME && event.getMaterial().equals(Material.WOOD_DOOR) && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)))
        {
            SamaGamesAPI.get().getGameManager().kickPlayer(event.getPlayer(), "");
        }
    }

}

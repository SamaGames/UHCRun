package net.samagames.uhcrun.listener;

import net.md_5.bungee.api.ChatColor;
import net.samagames.gameapi.json.Status;
import net.samagames.uhcrun.game.IGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

public class SpectatorListener implements Listener
{
    protected IGame game;

    public SpectatorListener(IGame parent)
    {
        this.game = parent;
    }

    public boolean cancel(Player p)
    {
        return game.getStatus() != Status.InGame ? true : !game.isInGame(p.getUniqueId());
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
        if (e.getEntity() instanceof Player) e.setCancelled(cancel((Player) e.getEntity()));
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

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if ((game.getStatus() == Status.Starting || game.getStatus() == Status.Available || game.getStatus() == Status.Generating) && event.getTo().getY() < 125)
        {
            event.setCancelled(true);
            event.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0, 162, 0));
            event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "Mais où vous allez comme ça ?!");
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
            e.setCancelled(cancel((Player) e.getEntity()));
    }

}

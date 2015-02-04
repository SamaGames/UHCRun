package net.zyuiop.survivalgames.listeners;

import net.samagames.gameapi.json.Status;
import net.zyuiop.survivalgames.game.BasicGame;
import org.bukkit.Material;
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

import java.util.ArrayList;

/**
 * Created by zyuiop on 13/09/14.
 */
public class SpectatorListener implements Listener {

    protected BasicGame arena;
    public ArrayList<Material> whitelist = new ArrayList<>();

    public SpectatorListener(BasicGame parent) {
        this.arena = parent;
    }

    public boolean cancel(Player p) {
        if (arena.getStatus() != Status.InGame) {
            return true;
        }

        return !arena.isInGame(p.getUniqueId());
    }

    @EventHandler
    public void onLoseFood(FoodLevelChangeEvent event) {
        event.setCancelled(cancel((Player) event.getEntity()));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onRain(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(cancel((Player)e.getEntity()));
    }

    @EventHandler
    public void pickup(PlayerPickupItemEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void pickup(PlayerDropItemEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketFillEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketEmptyEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent e) {
        if (e.getEntity() instanceof Player)
            e.setCancelled(cancel((Player) e.getEntity()));
    }

}

package net.samagames.uhcrun.listener;

import net.samagames.gameapi.GameUtils;
import net.samagames.gameapi.json.Status;
import net.samagames.uhcrun.game.IGame;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class GameListener implements Listener
{

    private IGame game;

    public GameListener(IGame game)
    {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player damaged = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (damager instanceof Player)
            {
                if (!game.isPvpEnabled())
                {
                    event.setCancelled(true);
                    return;
                }
                Metadatas.setMetadata(damaged, "lastDamager", damager);

                if (((Player) damager).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
                {
                    event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                }
            } else if (damager instanceof Projectile)
            {
                Projectile arrow = (Projectile) damager;
                Entity shooter = (Entity) arrow.getShooter();
                if (shooter instanceof Player)
                {
                    if (!game.isPvpEnabled())
                    {
                        event.setCancelled(true);
                        return;
                    }
                    Metadatas.setMetadata(damaged, "lastDamager", shooter);

                    if (((Player) shooter).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE))
                    {
                        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void brewevent(BrewEvent event)
    {
        if (event.getContents().getIngredient().getType() == Material.GLOWSTONE_DUST)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        Metadatas.setMetadata(event.getItemDrop(), "playerDrop", true);
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event)
    {
        if (Metadatas.getMetadata(event.getEntity(), "playerDrop") != null)
            return;

        String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";

        ArrayList<String> customLore = new ArrayList<>();
        Material mat = event.getEntity().getItemStack().getType();
        ItemMeta me = event.getEntity().getItemStack().getItemMeta();
        if (me != null && me.getLore() != null && me.getLore().contains(CHECK_LINE))
            return;

        switch (mat)
        {
            case IRON_ORE:
                event.getEntity().setItemStack(new ItemStack(Material.IRON_INGOT, 2));
                break;
            case SAND:
                event.getEntity().setItemStack(new ItemStack(Material.GLASS, 1));
                break;
            case GRAVEL:
            case FLINT:
                if (new Random().nextDouble() < 0.75)
                {
                    ItemStack loot = new ItemStack(Material.ARROW, 3);
                    ItemMeta meta = loot.getItemMeta();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Arrow");
                    customLore.add(CHECK_LINE);
                    meta.setLore(customLore);
                    loot.setItemMeta(meta);
                    event.getEntity().setItemStack(loot);
                }
                break;
            case GOLD_ORE:
                event.getEntity().setItemStack(new ItemStack(Material.GOLD_INGOT, 2));
                break;
            case COAL:
                event.getEntity().setItemStack(new ItemStack(Material.TORCH, 3));
                break;
            case DIAMOND:
                ItemStack loot = new ItemStack(Material.DIAMOND, event.getEntity().getItemStack().getAmount() * 2);
                ItemMeta meta = loot.getItemMeta();
                customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
                customLore.add(CHECK_LINE);
                meta.setLore(customLore);
                loot.setItemMeta(meta);
                event.getEntity().setItemStack(loot);
                break;
            case CACTUS:
                event.getEntity().setItemStack(new ItemStack(Material.LOG, 2));
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST))
        {
            Chest chest = (Chest) event.getClickedBlock().getState();
            int slot = 0;
            while (slot < chest.getInventory().getSize())
            {
                ItemStack stack = chest.getInventory().getItem(slot);
                if (stack == null)
                {
                    slot++;
                    continue;
                }

                if (stack.getType() == Material.DIAMOND)
                {
                    String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";
                    ItemMeta meta = stack.getItemMeta();
                    ArrayList<String> customLore = new ArrayList<>();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
                    customLore.add(CHECK_LINE);
                    meta.setLore(customLore);
                    stack.setItemMeta(meta);

                    chest.getInventory().setItem(slot, stack);
                }
                slot++;
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event)
    {
        if (game.isInGame(event.getPlayer().getUniqueId()))
        {
            game.stumpPlayer(event.getPlayer(), true);
            if (game.getStatus() == Status.InGame)
            {
                Location l = event.getPlayer().getLocation();
                World w = l.getWorld();
                for (ItemStack stack : event.getPlayer().getInventory().getContents())
                {
                    if (stack != null)
                    {
                        w.dropItemNaturally(l, stack);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        if (game.isInGame(event.getEntity().getUniqueId()))
        {
            game.stumpPlayer(event.getEntity(), false);
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));
            if (event.getEntity().getKiller() != null)
                event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1));
            GameUtils.broadcastSound(Sound.WITHER_DEATH);
        }
        event.setDeathMessage(game.getCoherenceMachine().getGameTag() + event.getDeathMessage());
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        if (event.getEntityType() == EntityType.WITCH || event.getEntityType() == EntityType.GUARDIAN)
            event.setCancelled(true);
    }

    public void onDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
            if (!game.isDamagesEnabled())
                event.setCancelled(true);
    }
}

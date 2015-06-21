package net.samagames.uhcrun.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.samagames.api.games.Status;
import net.samagames.tools.GameUtils;
import net.samagames.uhcrun.game.Game;
import net.samagames.uhcrun.generator.WorldLoader;
import net.samagames.uhcrun.utils.Metadatas;


/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class GameListener implements Listener {

    private Game game;
    private Random random;

    public GameListener(Game game) {
        this.game = game;
        this.random = new Random();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (damager instanceof Player) {
                if (!game.isPvpEnabled()) {
                    event.setCancelled(true);
                    return;
                }
                Metadatas.setMetadata(game.getPlugin(), damaged, "lastDamager", damager);

                if (((Player) damager).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                    event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                }
            } else if (damager instanceof Projectile) {
                Projectile arrow = (Projectile) damager;
                Entity shooter = (Entity) arrow.getShooter();
                if (shooter instanceof Player) {
                    if (!game.isPvpEnabled()) {
                        event.setCancelled(true);
                        return;
                    }
                    Metadatas.setMetadata(game.getPlugin(), damaged, "lastDamager", shooter);

                    if (((Player) shooter).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBrewUse(BrewEvent event) {
        if (event.getContents().getIngredient().getType() == Material.GLOWSTONE_DUST) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Metadatas.setMetadata(game.getPlugin(), event.getItemDrop(), "playerDrop", true);
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        if (Metadatas.getMetadata(game.getPlugin(), event.getEntity(), "playerDrop") != null) {
            return;
        }

        String checkline = ChatColor.GRAY + "© Aperture Science - All rights reserved";

        ArrayList<String> customLore = new ArrayList<>();
        Material mat = event.getEntity().getItemStack().getType();
        ItemMeta me = event.getEntity().getItemStack().getItemMeta();
        if (me != null && me.getLore() != null && me.getLore().contains(checkline)) {
            return;
        }

        switch (mat) {
            case IRON_ORE:
                event.getEntity().setItemStack(new ItemStack(Material.IRON_INGOT, 2));
                break;
            case SAND:
                event.getEntity().setItemStack(new ItemStack(Material.GLASS, 1));
                break;
            case GRAVEL:
            case FLINT:
                if (random.nextDouble() < 0.75) {
                    ItemStack loot = new ItemStack(Material.ARROW, 3);
                    ItemMeta meta = loot.getItemMeta();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Arrow");
                    customLore.add(checkline);
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
                customLore.add(checkline);
                meta.setLore(customLore);
                loot.setItemMeta(meta);
                event.getEntity().setItemStack(loot);
                break;
            case CACTUS:
                event.getEntity().setItemStack(new ItemStack(Material.LOG, 2));
                break;
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST)) {
            Chest chest = (Chest) event.getClickedBlock().getState();
            int slot = 0;
            while (slot < chest.getInventory().getSize()) {
                ItemStack stack = chest.getInventory().getItem(slot);
                if (stack == null) {
                    slot++;
                    continue;
                }

                if (stack.getType() == Material.DIAMOND) {
                    String checkline = ChatColor.GRAY + "© Aperture Science - All rights reserved";
                    ItemMeta meta = stack.getItemMeta();
                    ArrayList<String> customLore = new ArrayList<>();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
                    customLore.add(checkline);
                    meta.setLore(customLore);
                    stack.setItemMeta(meta);

                    chest.getInventory().setItem(slot, stack);
                }
                slot++;
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (game.isInGame(event.getEntity().getUniqueId())) {
            game.stumpPlayer(event.getEntity(), false);
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));
            if (event.getEntity().getKiller() != null) {
                event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 20, 1));
            }
            GameUtils.broadcastSound(Sound.WITHER_DEATH);
        }
        event.setDeathMessage(game.getCoherenceMachine().getGameTag() + event.getDeathMessage());
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.WITCH || event.getEntityType() == EntityType.GUARDIAN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !game.isDamagesEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Cow) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.RAW_BEEF) {
                    newDrops.add(new ItemStack(Material.COOKED_BEEF, stack.getAmount() * 2));
                } else if (stack.getType() == Material.LEATHER) {
                    newDrops.add(new ItemStack(Material.LEATHER, stack.getAmount() * 2));
                }
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Sheep) {
            List<ItemStack> newDrops = event.getDrops().stream().filter(stack -> stack.getType() == Material.MUTTON).map(stack -> new ItemStack(Material.COOKED_MUTTON, stack.getAmount() * 2)).collect(Collectors.toList());
            if (random.nextInt(32) >= 16) {
                newDrops.add(new ItemStack(Material.LEATHER, random.nextInt(5) + 1));
            }
            if (random.nextInt(32) >= 16) {
                newDrops.add(new ItemStack(Material.STRING, random.nextInt(2) + 1));
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Pig) {
            List<ItemStack> newDrops = event.getDrops().stream().filter(stack -> stack.getType() == Material.PORK).map(stack -> new ItemStack(Material.GRILLED_PORK, stack.getAmount() * 2)).collect(Collectors.toList());
            if (random.nextInt(32) >= 16) {
                newDrops.add(new ItemStack(Material.LEATHER, random.nextInt(5) + 1));
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Rabbit) {
            List<ItemStack> newDrops = event.getDrops().stream().filter(stack -> stack.getType() == Material.RABBIT).map(stack -> new ItemStack(Material.COOKED_RABBIT, stack.getAmount() * 2)).collect(Collectors.toList());
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else {
            if (entity instanceof Chicken) {
                List<ItemStack> newDrops = new ArrayList<>();
                for (ItemStack stack : event.getDrops()) {
                    if (stack.getType() == Material.RAW_CHICKEN) {
                        newDrops.add(new ItemStack(Material.COOKED_CHICKEN, stack.getAmount() * 2));
                    } else if (stack.getType() == Material.FEATHER) {
                        ItemStack loot = new ItemStack(Material.ARROW, stack.getAmount());
                        ItemMeta meta = loot.getItemMeta();
                        String checkline = ChatColor.GRAY + "© Aperture Science - All rights reserved";
                        ArrayList<String> customLore = new ArrayList<>();
                        customLore.add(ChatColor.GRAY + "Aperture™ Companion Arrow");
                        customLore.add(checkline);
                        meta.setLore(customLore);
                        loot.setItemMeta(meta);
                        newDrops.add(loot);
                    }
                }
                event.getDrops().clear();
                event.getDrops().addAll(newDrops);
            } else if (entity instanceof Squid) {
                List<ItemStack> newDrops = new ArrayList<>();
                if (random.nextInt(32) >= 8) {
                    newDrops.add(new ItemStack(Material.COOKED_FISH, random.nextInt(5) + 1));
                }
                event.getDrops().clear();
                event.getDrops().addAll(newDrops);
            } else if (entity instanceof Skeleton) {
                List<ItemStack> newDrops = new ArrayList<>();
                for (ItemStack stack : event.getDrops()) {
                    if (stack.getType() == Material.ARROW) {
                        newDrops.add(new ItemStack(Material.ARROW, stack.getAmount() * 2));
                    }
                    if (stack.getType() == Material.BOW) {
                        stack.setDurability((short) 0);
                        newDrops.add(stack);
                    }
                }
                event.getDrops().clear();
                event.getDrops().addAll(newDrops);
            }
        }
        event.setDroppedExp(event.getDroppedExp() * 2);
    }

    @EventHandler
    public void onLoseFood(FoodLevelChangeEvent event) {
        event.setCancelled(this.game.getStatus() != Status.IN_GAME || !this.game.isInGame(event.getEntity().getUniqueId()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!game.isPvpEnabled() && (event.getBlockPlaced().getType() == Material.LAVA || event.getBlockPlaced().getType() == Material.STATIONARY_LAVA)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Le PVP est désactivé, l'utilisation de sources de lave est interdite.");
        }

        int x = event.getBlockPlaced().getX();
        int y = event.getBlockPlaced().getY();
        int z = event.getBlockPlaced().getZ();

        if (x > -50 && x < 50 && z > -50 && z < 50 && y > WorldLoader.getHighestNaturalBlockAt(x, z) + 17) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.RED + "Towers" + ChatColor.DARK_RED + "] " + ChatColor.RED + "Les Towers sont interdites en UHCRun.");
        }
    }
}

package net.samagames.uhcrun.listener;

import net.samagames.uhcrun.game.AbstractGame;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class BlockListener implements Listener
{
    private final AbstractGame game;
    private final List<String> privateBlocks;
    private final Map<Location, UUID> blocksOwner = new HashMap<>();

    public BlockListener(AbstractGame game)
    {
        this.game = game;
        this.privateBlocks = (List<String>) game.getPlugin().getProperties().getOptions().getOrDefault("privateBlocks", new ArrayList<>());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlock();
        if (block.getType().equals(Material.LOG) || block.getType().equals(Material.LOG_2))
        {
            Metadatas.setMetadata(game.getPlugin(), block, "placed", 1);
        } else if (privateBlocks.contains(event.getBlockPlaced().getType().name().toUpperCase()))
        {
            blocksOwner.put(event.getBlockPlaced().getLocation(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBeginBreak(BlockDamageEvent event)
    {
        event.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
        if (event.getBlock().getType() == Material.OBSIDIAN)
        {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20000, 2, true, true));
        } else
        {
            event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        Material mat = event.getBlock().getType();
        event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);

        switch (mat)
        {
            case LOG:
            case LOG_2:
                final List<Block> bList = new ArrayList<>();
                checkLeaves(event.getBlock());
                bList.add(event.getBlock());
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        for (int i = 0; i < bList.size(); i++)
                        {
                            Block block = bList.get(i);

                            // Is it tagged by the system?
                            if (Metadatas.getMetadata(game.getPlugin(), block, "placed") != null)
                            {
                                // Ignore this block
                                bList.remove(block);
                                continue;
                            }
                            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2)
                            {
                                for (ItemStack item : block.getDrops())
                                {
                                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                                }

                                block.setType(Material.AIR);
                                checkLeaves(block);
                            }
                            for (BlockFace face : BlockFace.values())
                            {
                                if (block.getRelative(face).getType() == Material.LOG || block.getRelative(face).getType() == Material.LOG_2)
                                {
                                    bList.add(block.getRelative(face));
                                }
                            }
                            bList.remove(block);
                        }
                        if (bList.isEmpty())
                        {
                            cancel();
                        }
                    }
                }.runTaskTimer(game.getPlugin(), 2, 1);
                break;
            case DIAMOND_ORE:
            case LAPIS_ORE:
            case GOLD_ORE:
            case OBSIDIAN:
            case IRON_ORE:
            case REDSTONE_ORE:
            case QUARTZ_ORE:
                event.setCancelled(true);
                event.getBlock().breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
                break;
            default:
                break;
        }

        if (blocksOwner.containsKey(event.getBlock().getLocation()) && blocksOwner.get(event.getBlock().getLocation()) != event.getPlayer().getUniqueId())
        {
            UUID id = blocksOwner.get(event.getBlock().getLocation());
            if (id == null || id.equals(event.getPlayer().getUniqueId()) || (game instanceof TeamGame && ((TeamGame) game).getPlayerTeam(id) == ((TeamGame) game).getPlayerTeam(event.getPlayer().getUniqueId())))
            {
                blocksOwner.remove(event.getBlock().getLocation());
            } else if (!game.isPvpEnabled())
            {
                event.getPlayer().sendMessage(ChatColor.RED + "Ce block appartient à " + blocksOwner.get(event.getBlock().getLocation())  + ". Vous ne pouvez pas le casser durant la préparation !");
                event.setCancelled(true);
            }
        }

        event.getPlayer().giveExp(event.getExpToDrop() * 2);
    }

    private void checkLeaves(Block block)
    {
        Location loc = block.getLocation();
        final World world = loc.getWorld();
        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();
        final int range = 4;
        final int off = range + 1;

        if (!validChunk(world, x - off, y - off, z - off, x + off, y + off, z + off))
        {
            return;
        }

        Bukkit.getServer().getScheduler().runTask(game.getPlugin(), () -> {
            for (int offX = -range; offX <= range; offX++)
            {
                for (int offY = -range; offY <= range; offY++)
                {
                    for (int offZ = -range; offZ <= range; offZ++)
                    {
                        if (world.getBlockAt(x + offX, y + offY, z + offZ).getType() == Material.LEAVES || world.getBlockAt(x + offX, y + offY, z + offZ).getType() == Material.LEAVES_2)
                        {
                            breakLeaf(world, x + offX, y + offY, z + offZ);
                        }
                    }
                }
            }
        });
    }

    private void breakLeaf(World world, int x, int y, int z)
    {
        Block block = world.getBlockAt(x, y, z);

        byte range = 4;
        byte max = 32;
        int[] blocks = new int[max * max * max];
        int off = range + 1;
        int mul = max * max;
        int div = max / 2;


        // Compute leaf
        if (validChunk(world, x - off, y - off, z - off, x + off, y + off, z + off))
        {
            int offX;
            int offY;
            int offZ;

            for (offX = -range; offX <= range; offX++)
            {
                for (offY = -range; offY <= range; offY++)
                {
                    for (offZ = -range; offZ <= range; offZ++)
                    {
                        Material mat = world.getBlockAt(x + offX, y + offY, z + offZ).getType();
                        blocks[(offX + div) * mul + (offY + div) * max + offZ + div] = mat == Material.LOG || mat == Material.LOG_2 ? 0 : mat == Material.LEAVES || mat == Material.LEAVES_2 ? -2 : -1;
                    }
                }
            }

            for (offX = 1; offX <= 4; offX++)
            {
                for (offY = -range; offY <= range; offY++)
                {
                    for (offZ = -range; offZ <= range; offZ++)
                    {
                        for (int i = -range; i <= range; i++)
                        {
                            if (blocks[(offY + div) * mul + (offZ + div) * max + i + div] == offX - 1)
                            {
                                if (blocks[(offY + div - 1) * mul + (offZ + div) * max + i + div] == -2)
                                {
                                    blocks[(offY + div - 1) * mul + (offZ + div) * max + i + div] = offX;
                                }

                                if (blocks[(offY + div + 1) * mul + (offZ + div) * max + i + div] == -2)
                                {
                                    blocks[(offY + div + 1) * mul + (offZ + div) * max + i + div] = offX;
                                }

                                if (blocks[(offY + div) * mul + (offZ + div - 1) * max + i + div] == -2)
                                {
                                    blocks[(offY + div) * mul + (offZ + div - 1) * max + i + div] = offX;
                                }

                                if (blocks[(offY + div) * mul + (offZ + div + 1) * max + i + div] == -2)
                                {
                                    blocks[(offY + div) * mul + (offZ + div + 1) * max + i + div] = offX;
                                }

                                if (blocks[(offY + div) * mul + (offZ + div) * max + (i + div - 1)] == -2)
                                {
                                    blocks[(offY + div) * mul + (offZ + div) * max + (i + div - 1)] = offX;
                                }

                                if (blocks[(offY + div) * mul + (offZ + div) * max + i + div + 1] == -2)
                                {
                                    blocks[(offY + div) * mul + (offZ + div) * max + i + div + 1] = offX;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (blocks[div * mul + div * max + div] < 0)
        {
            LeavesDecayEvent event = new LeavesDecayEvent(block);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled())
            {
                return;
            }

            block.breakNaturally();

            if (10 > new Random().nextInt(100))
            {
                world.playEffect(block.getLocation(), Effect.STEP_SOUND, Material.LEAVES);
            }
        }
    }

    private boolean validChunk(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        if (maxY >= 0 && minY < world.getMaxHeight())
        {
            minX >>= 4;
            minZ >>= 4;
            maxX >>= 4;
            maxZ >>= 4;

            for (int x = minX; x <= maxX; x++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    if (!world.isChunkLoaded(x, z))
                    {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }
}

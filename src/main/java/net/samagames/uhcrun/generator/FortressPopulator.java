package net.samagames.uhcrun.generator;


import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import net.minecraft.server.v1_8_R3.StructurePieceTreasure;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class FortressPopulator extends BlockPopulator
{
    private List<StructurePieceTreasure> chestLoots;
    private UHCRun plugin;
    private File file;
    private Logger logger;
    private BukkitWorld bukkitWorld;
    private CuboidClipboard cuboidClipboard;
    private Random random;

    public FortressPopulator(UHCRun plugin, List<Map<String, Object>> netherChestLoots)
    {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "/nether.schematic");
        this.logger = Bukkit.getLogger();
        this.chestLoots = new ArrayList<>();
        try
        {
            this.cuboidClipboard = SchematicFormat.MCEDIT.load(file);
        } catch (IOException | DataException e)
        {
            e.printStackTrace();
        }
        chestLoots.addAll(netherChestLoots.stream().map(loot -> new StructurePieceTreasure(CraftItemStack.asNMSCopy(new ItemStack(Material.getMaterial((String) loot.get("id")))), ((Double) loot.get("minimumChance")).intValue(), ((Double) loot.get("maximumChance")).intValue(), ((Double) loot.get("weight")).intValue())).collect(Collectors.toList()));
        this.random = new Random();
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        if (bukkitWorld == null)
        {
            this.bukkitWorld = new BukkitWorld(world);
        }

        final int i = random.nextInt(1000);
        if (i > 0 && i < 10)
        {
            int xFortress = chunk.getX() * 16 + random.nextInt(15);
            int zFortress = chunk.getZ() * 16 + random.nextInt(15);
            generateBlazeFortress(world, xFortress, zFortress);
        }
    }

    private void generateBlazeFortress(World world, int x, int z)
    {
        if (!plugin.isWorldLoaded())
        {
            return;
        }
        logger.info("Generating fortress at " + x + "; " + z);

        EditSession es;
        if (file.exists())
        {
            try
            {
                com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(x, 40, z);
                Chunk chunk = world.getChunkAt(new org.bukkit.Location(world, x, 40, z));
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                chunk.load(true);
                int cx = chunkX - 3;
                int cz = chunkZ - 3;
                while (cx < chunkX + 3)
                {
                    while (cz < chunkZ + 3)
                    {
                        if (cx != chunkX || cz != chunkZ)
                        {
                            world.getChunkAt(cx, cz).load(true);
                        }
                        cz++;
                    }
                    cx++;
                }
                world.getChunkAt(chunkX - 1, chunkZ + 1).load(true);
                world.getChunkAt(chunkX - 1, chunkZ - 1).load(true);
                world.getChunkAt(chunkX - 1, chunkZ).load(true);
                world.getChunkAt(chunkX + 1, chunkZ + 1).load(true);
                world.getChunkAt(chunkX + 1, chunkZ - 1).load(true);
                world.getChunkAt(chunkX + 1, chunkZ).load(true);
                world.getChunkAt(chunkX, chunkZ + 1).load(true);
                world.getChunkAt(chunkX, chunkZ - 1).load(true);


                es = new EditSession(bukkitWorld, 2000000);
                es.setFastMode(true);

                cuboidClipboard.paste(es, v, false);

                int bx = x;
                while (bx < x + 35)
                {
                    int bz = z;
                    while (bz < z + 35)
                    {
                        int by = 40;
                        while (by > 0)
                        {
                            Block block = new org.bukkit.Location(world, bx, by, bz).getBlock();
                            if (block.getType() == Material.MOB_SPAWNER)
                            {
                                block.setType(Material.STONE);
                                block.setType(Material.MOB_SPAWNER);
                                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                                spawner.setSpawnedType(EntityType.BLAZE);
                                spawner.setDelay(1);
                                spawner.update();
                                logger.fine("Spawner configured at " + bx + " , " + by + " , " + bz);
                                break;
                            } else if (block.getType() == Material.CHEST && !chestLoots.isEmpty())
                            {
                                CraftChest chest = (CraftChest) block.getState();
                                chest.getBlockInventory().clear();
                                StructurePieceTreasure.a(random, chestLoots, chest.getTileEntity(), 4);
                            }
                            by--;
                        }
                        bz++;
                    }
                    bx++;
                }
            } catch (MaxChangedBlocksException ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            logger.severe("File does not exist.");
        }
    }
}

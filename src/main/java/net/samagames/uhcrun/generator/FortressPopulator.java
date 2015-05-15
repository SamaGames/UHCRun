package net.samagames.uhcrun.generator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class FortressPopulator extends BlockPopulator
{

    private UHCRun plugin;
    private File file;
    private Logger logger;
    private BukkitWorld bukkitWorld;
    private CuboidClipboard cuboidClipboard;

    public FortressPopulator(UHCRun plugin)
    {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "/nether.schematic");
        this.logger = Bukkit.getLogger();
        try
        {
            this.cuboidClipboard = SchematicFormat.MCEDIT.load(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (DataException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        if (bukkitWorld == null)
            this.bukkitWorld = new BukkitWorld(world);

        final int i = random.nextInt(1000);
        if (i > 0 && i < 5)
        {
            int xFortress = chunk.getX() * 16 + random.nextInt(15);
            int zFortress = chunk.getZ() * 16 + random.nextInt(15);
            generateBlazeFortress(world, xFortress, zFortress);
        }
    }

    public void generateBlazeFortress(World world, int x, int z)
    {
        if (!plugin.isWorldLoaded())
            return;
        logger.fine("Generating fortress at " + x + "; " + z);

        EditSession es;
        if (file.exists())
        {
            try
            {
                com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(x, 40, z);
                Chunk chunk = world.getChunkAt(new org.bukkit.Location(world, x, 40, z));
                chunk.load(true);
                int cx = chunk.getX() - 3;
                int cz = chunk.getZ() - 3;
                while (cx < chunk.getX() + 3)
                {
                    while (cz < chunk.getZ() + 3)
                    {
                        if (cx != chunk.getX() || cz != chunk.getZ())
                        {
                            world.getChunkAt(cx, cz).load(true);
                        }
                        cz++;
                    }
                    cx++;
                }
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() + 1).load(true);
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() - 1).load(true);
                world.getChunkAt(chunk.getX() - 1, chunk.getZ()).load(true);
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() + 1).load(true);
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() - 1).load(true);
                world.getChunkAt(chunk.getX() + 1, chunk.getZ()).load(true);
                world.getChunkAt(chunk.getX(), chunk.getZ() + 1).load(true);
                world.getChunkAt(chunk.getX(), chunk.getZ() - 1).load(true);


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
                            Location block = new org.bukkit.Location(world, bx, by, bz);
                            if (block.getBlock().getType() == Material.MOB_SPAWNER)
                            {
                                block.getBlock().setType(Material.STONE);
                                block.getBlock().setType(Material.MOB_SPAWNER);
                                CreatureSpawner spawner = (CreatureSpawner) block.getBlock().getState();
                                spawner.setSpawnedType(EntityType.BLAZE);
                                spawner.setDelay(1);
                                spawner.update();
                                logger.fine("Spawner configured at " + bx + " , " + by + " , " + bz);
                                break;
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
            logger.severe(("File does not exist."));
        }
    }
}

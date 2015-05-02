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

    public FortressPopulator(UHCRun plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        if (random.nextInt(1000) <= 10)
        {
            int xFortress = chunk.getX() * 16 + random.nextInt(15);
            int zFortress = chunk.getZ() * 16 + random.nextInt(15);
            generateBlazeFortress(xFortress, zFortress);
        }
    }

    public void generateBlazeFortress(int x, int z)
    {
        if (!plugin.isWorldLoaded())
            return;
        Bukkit.getLogger().info("Generating fortress at " + x + "; " + z);
        File file = new File(plugin.getDataFolder(), "/nether.schematic");
        EditSession es;
        if (file.exists())
        {
            try
            {
                com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(x, 40, z);
                World worldf = Bukkit.getWorld("world");
                Chunk chunk = worldf.getChunkAt(new org.bukkit.Location(worldf, x, 40, z));
                chunk.load(true);
                int cx = chunk.getX() - 3;
                int cz = chunk.getZ() - 3;
                while (cx < chunk.getX() + 3)
                {
                    while (cz < chunk.getZ() + 3)
                    {
                        if (cx != chunk.getX() || cz != chunk.getZ())
                        {
                            worldf.getChunkAt(cx, cz).load(true);
                        }
                        cz++;
                    }
                    cx++;
                }
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ() - 1).load(true);
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ()).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ() - 1).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ()).load(true);
                worldf.getChunkAt(chunk.getX(), chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX(), chunk.getZ() - 1).load(true);

                BukkitWorld BWf = new BukkitWorld(worldf);
                es = new EditSession(BWf, 2000000);
                es.setFastMode(true);
                CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
                c1.paste(es, v, false);

                int bx = x;
                while (bx < x + 35)
                {
                    int bz = z;
                    while (bz < z + 35)
                    {
                        int by = 40;
                        while (by > 0)
                        {
                            Location block = new org.bukkit.Location(worldf, bx, by, bz);
                            if (block.getBlock().getType() == Material.MOB_SPAWNER)
                            {
                                block.getBlock().setType(Material.STONE);
                                block.getBlock().setType(Material.MOB_SPAWNER);
                                CreatureSpawner spawner = (CreatureSpawner) block.getBlock().getState();
                                spawner.setSpawnedType(EntityType.BLAZE);
                                spawner.setDelay(1);
                                spawner.update();
                                Bukkit.getLogger().info("Spawner configured at " + bx + " , " + by + " , " + bz);
                                break;
                            }
                            by--;
                        }
                        bz++;
                    }
                    bx++;
                }
            } catch (MaxChangedBlocksException | IOException | DataException ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            Bukkit.getLogger().severe(("File does not exist."));
        }
    }
}

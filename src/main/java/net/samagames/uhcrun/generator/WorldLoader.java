package net.samagames.uhcrun.generator;

import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class WorldLoader
{
    private BukkitTask task;
    private int lastShow = -1;
    private int numberChunk;
    private UHCRun plugin;

    public WorldLoader(UHCRun plugin)
    {
        this.plugin = plugin;
    }

    public static Integer getHighestNaturalBlockAt(int x, int z)
    {
        return Pos.getY(x, z);
    }

    public void begin(final World world)
    {
        long startTime = System.currentTimeMillis();

        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable()
        {
            private int todo = (1200 * 1200) / 256;
            private int x = -600;
            private int z = -600;

            @Override
            public void run()
            {
                int i = 0;
                while (i < 50)
                {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = numberChunk * 100 / todo;
                    if (percentage > lastShow && percentage % 10 == 0)
                    {
                        lastShow = percentage;
                        plugin.getLogger().info("Loading chunks (" + percentage + "%)");
                    }

                    z += 16;
                    if (z >= 600)
                    {
                        z = -600;
                        x += 16;
                    }

                    if (x >= 600)
                    {
                        task.cancel();
                        plugin.finishGeneration(world, System.currentTimeMillis() - startTime);
                        return;
                    }

                    numberChunk++;
                    i++;
                }
            }
        }, 1L, 1L);
    }

    public void computeTop(World world)
    {
        int x = -500;
        while (x < 500)
        {
            int z = -500;
            while (z < 500)
            {
                Pos.registerY(x, world.getHighestBlockYAt(x, z), z);
                z++;
            }
            x++;
        }
    }


    private static final class Pos
    {
        private static List<Pos> highestBlocks = new ArrayList<>();
        int x, y, z;

        Pos(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }


        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public int getZ()
        {
            return z;
        }

        public static int getY(int x, int z)
        {
            for (Pos pos : highestBlocks)
            {
                if (pos.getX() == x && pos.getZ() == z)
                {
                    return pos.getY();
                }
            }

            return 255;
        }

        public static void registerY(int x, int y, int z)
        {
            highestBlocks.add(new Pos(x, y, z));
        }
    }
}

package net.samagames.uhcrun.generator;

import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class WorldLoader
{
    private static Map<int[], Integer> highestBlocks = new HashMap<>();
    private BukkitTask task;
    private int lastShow = -1;
    private int numberChunk;
    private UHCRun plugin = UHCRun.getInstance();

    public static Integer getHighestNaturalBlockAt(int x, int z)
    {
        final int[] loc = new int[]{x, z};

        if (highestBlocks.containsKey(loc))
        {
            return highestBlocks.get(loc);
        }

        return 255;
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
        int x = -50;
        while (x < 50)
        {
            int z = -50;
            while (z < 50)
            {
                Block block = world.getHighestBlockAt(x, z);
                highestBlocks.put(new int[]{x, z}, block.getY());
                z++;
            }
            x++;
        }
    }
}

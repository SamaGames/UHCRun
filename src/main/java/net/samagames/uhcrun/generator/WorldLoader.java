package net.samagames.uhcrun.generator;


import net.samagames.api.games.Status;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class WorldLoader
{
    private static Map<PartialLocation, Integer> highestBlocks = new HashMap<>();
    private BukkitTask task;
    private int lastShow = -1;
    private int numberChunk;
    private UHCRun plugin = UHCRun.getInstance();

    public static Integer getHighestNaturalBlockAt(int x, int z)
    {
        PartialLocation loc = new PartialLocation(x, z);

        if (highestBlocks.containsKey(loc))
            return highestBlocks.get(loc);

        return 255;
    }

    public void begin(final World world)
    {
        long startTime = System.currentTimeMillis();
        int x = -50;
        while (x < 50)
        {
            int z = -50;
            while (z < 50)
            {
                Block block = world.getHighestBlockAt(x, z);
                highestBlocks.put(new PartialLocation(x, z), block.getY());
                z++;
            }
            x++;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable()
        {
            private int todo = (1200 * 1200) / (16 * 16);
            private int x = -600;
            private int z = -600;

            @Override
            public void run()
            {
                int i = 0;
                while (i < 50)
                {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = (numberChunk * 100 / todo);
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
                        plugin.getGame().updateStatus(Status.WAITING_FOR_PLAYERS);
                        plugin.getLogger().info("Ready in " + (System.currentTimeMillis() - startTime) + "ms");
                        return;
                    }

                    numberChunk++;
                    i++;
                }
            }
        }, 1L, 1L);
    }

    public static class PartialLocation
    {
        private final int x;
        private final int z;

        public PartialLocation(int x, int z)
        {
            this.x = x;
            this.z = z;
        }

        public int getX()
        {
            return x;
        }

        public int getZ()
        {
            return z;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof PartialLocation))
                return false;

            PartialLocation that = (PartialLocation) o;

            return x == that.x && z == that.z;

        }

        @Override
        public int hashCode()
        {
            return x * z;
        }

        @Override
        public String toString()
        {
            return "PartialLocation{" +
                    "x=" + x +
                    ", z=" + z +
                    '}';
        }
    }
}

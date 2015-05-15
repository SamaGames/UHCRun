package net.samagames.uhcrun.generator;


import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.network.NetworkManager;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class WorldLoader
{
    private BukkitTask task;
    private int lastShow = -1;
    private int numberChunk;
    private UHCRun plugin = UHCRun.getInstance();
    private static HashMap<PartialLocation, Integer> highestBlocks = new HashMap<>();

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
            private NetworkManager networkManager = GameAPI.getManager();

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
                        networkManager.sendArena(networkManager.buildJson(plugin.getGame(), percentage));
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
                        plugin.getGame().updateStatus(Status.Available);
                        plugin.getLogger().info("Ready in " + (System.currentTimeMillis() - startTime) + "ms");
                        return;
                    }

                    numberChunk++;
                    i++;
                }
            }
        }, 1L, 1L);
    }

    public boolean loadChunks(World world, int radius, int sx, int sz)
    {
        ArrayList<Chunk> loaded = new ArrayList();
        Logger log = plugin.getLogger();
        if (world == null)
        {
            log.info("Error world does not exist");
            return false;
        }
        int maxX = sx + 17 + radius;
        int maxZ = sz + 17 + radius;
        int minX = sx - 1 - radius;
        int minZ = sz - 1 - radius;
        String start = new Date().toString();

        int fail = 0;
        int total = 0;

        for (int x = minX; x < maxX; x += 16)
        {
            log.info("Generating Chunks for X: " + x + " Z: " + minZ + " to " + maxZ);
            for (int z = minZ; z < maxZ; z += 16)
            {
                Chunk c = world.getChunkAt(x, z);
                if (!c.load(true)) {
                    fail++;
                }
                total++;
                loaded.add(c);

                c = null;
            }
        }
        log.info("Execution Time:");
        log.info("Start: " + start);
        log.info("Finish: " + new Date().toString());
        log.info("Failed Chunks: " + fail + ", Loaded: " + (total - fail) + " Chunks: " + total);
        log.info("Finished");
        return true;
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

            if (x != that.x)
                return false;
            return z == that.z;

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

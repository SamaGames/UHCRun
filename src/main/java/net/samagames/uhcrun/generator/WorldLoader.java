package net.samagames.uhcrun.generator;

import net.samagames.api.games.Status;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.TreeMap;


public class WorldLoader {
    private static Map<int[], Integer> highestBlocks = new TreeMap<>();
    private BukkitTask task;
    private int lastShow = -1;
    private int numberChunk;
    private UHCRun plugin = UHCRun.getInstance();

    public static Integer getHighestNaturalBlockAt(int x, int z) {
        final int[] loc = new int[]{x, z};

        if (highestBlocks.containsKey(loc)) {
            return highestBlocks.get(loc);
        }

        return 255;
    }

    public void begin(final World world) {
        long startTime = System.currentTimeMillis();
        int x = -50;
        while (x < 50) {
            int z = -50;
            while (z < 50) {
                Block block = world.getHighestBlockAt(x, z);
                highestBlocks.put(new int[]{x, z}, block.getY());
                z++;
            }
            x++;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int todo = (1200 * 1200) / 256;
            private int x = -600;
            private int z = -600;

            @Override
            public void run() {
                int i = 0;
                while (i < 50) {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = numberChunk * 100 / todo;
                    if (percentage > lastShow && percentage % 10 == 0) {
                        lastShow = percentage;
                        plugin.getLogger().info("Loading chunks (" + percentage + "%)");
                    }

                    z += 16;
                    if (z >= 600) {
                        z = -600;
                        x += 16;
                    }

                    if (x >= 600) {
                        task.cancel();
                        plugin.getGame().setStatus(Status.WAITING_FOR_PLAYERS);
                        plugin.getLogger().info("Ready in " + (System.currentTimeMillis() - startTime) + "ms");
                        return;
                    }

                    numberChunk++;
                    i++;
                }
            }
        }, 1L, 1L);
    }
}

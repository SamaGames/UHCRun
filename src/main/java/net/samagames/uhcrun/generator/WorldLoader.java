package net.samagames.uhcrun.generator;

import net.samagames.api.SamaGamesAPI;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class WorldLoader {
    public static BukkitTask task;
    public static int lastShow = -1;
    public static int numberChunk = 0;
    public static HashMap<PartialLocation, Integer> highestBlocks = new HashMap<>();

    public static Integer getHighestNaturalBlockAt(int x, int z) {
        PartialLocation loc = new PartialLocation(x, z);
        for (Map.Entry<PartialLocation, Integer> en : highestBlocks.entrySet()) {
            if (en.getKey().equals(loc))
                return en.getValue();
        }

        return 255;
    }

    public static void begin(final World world) {
        int x = -50;
        while (x < 50) {
            int z = -50;
            while (z < 50) {
                Block block = world.getHighestBlockAt(x, z);
                highestBlocks.put(new PartialLocation(x, z), block.getY());
                z++;
            }
            x++;
        }

        task = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, new Runnable() {
            private int todo = (1200*1200)/(16*16);
            private int x = -600;
            private int z = -600;

            @Override
            public void run() {
                int i = 0;
                while (i < 50) {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = (numberChunk * 100 / todo);
                    if (percentage > lastShow && percentage % 10 == 0) {
                        lastShow = percentage;
                        SamaGamesAPI.get().getGameManager().refreshArena();
                    }

                    z+=16;
                    if (z >= 600) {
                        z = - 600;
                        x += 16;
                    }

                    if (x >= 600)  {
                        WorldLoader.finish();
                        return;
                    }

                    numberChunk++;
                    i++;
                }
            }
        }, 1L, 1L);
    }

    private static void finish() {
        task.cancel();
        UHCRun.instance.finishGeneration();
    }

    public static class PartialLocation {
        private final int x;
        private final int z;

        public PartialLocation(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (! (o instanceof PartialLocation))
                return false;

            PartialLocation that = (PartialLocation) o;

            if (x != that.x)
                return false;
            return z == that.z;

        }

        @Override
        public String toString() {
            return "PartialLocation{" +
                    "x=" + x +
                    ", z=" + z +
                    '}';
        }
    }
}

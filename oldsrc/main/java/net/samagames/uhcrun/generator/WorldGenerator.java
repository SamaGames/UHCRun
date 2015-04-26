package net.samagames.uhcrun.generator;

import net.samagames.gameapi.GameAPI;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class WorldGenerator {
    public static BukkitTask task;
    public static int lastShow = -1;
    public static int numberChunk = 0;

    public static void begin(final World world)
    {
        task = Bukkit.getScheduler().runTaskTimer(UHCRun.instance, new Runnable() {
            private int todo = (1200*1200)/(16*16);
            private int x = -600;
            private int z = -600;

            @Override
            public void run() {
                int i = 0;
                while (i < 3) {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = (numberChunk * 100 / todo);
                    if (percentage > lastShow) {
                        lastShow = percentage;
                        GameAPI.getManager().sendArena(GameAPI.getManager().buildJson(GameAPI.getArena(), percentage));
                    }

                    z+=16;
                    if (z >= 600) {
                        z = - 600;
                        x += 16;
                    }

                    if (x >= 600)  {
                        WorldGenerator.finish();
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
}
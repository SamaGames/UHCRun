package net.zyuiop.survivalgames.generator;

import net.samagames.gameapi.GameAPI;
import net.zyuiop.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

public class WorldGenerator {
    public static BukkitTask task;
    public static int lastShow = -1;
    public static int numberChunk = 0;

    public static void begin(final World world)
    {
        task = Bukkit.getScheduler().runTaskTimer(SurvivalGames.instance, new Runnable() {
            private int todo = (516*516)/(16*16);
            private int x = -250;
            private int z = -250;

            @Override
            public void run() {
                int i = 0;
                while (i < 4) {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = (numberChunk * 100 / todo);
                    if (percentage > lastShow) {
                        lastShow = percentage;
                        GameAPI.getManager().sendArena(GameAPI.getManager().buildJson(GameAPI.getArena(), percentage));
                    }

                    z+=16;
                    if (z >= 250) {
                        z = - 250;
                        x += 16;
                    }

                    if (x >= 250)  {
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
        SurvivalGames.instance.finishGeneration();

    }
}
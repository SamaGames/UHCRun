package net.zyuiop.survivalgames.generator;

import net.minecraft.server.v1_8_R1.WorldGenCaves;
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
            private int todo = (1100*1100)/(16*16);
            private int x = -550;
            private int z = -550;

            @Override
            public void run() {
                int i = 0;
                while (i < 7) {
                    world.getChunkAt(world.getBlockAt(x, 64, z)).load(true);
                    int percentage = (numberChunk * 100 / todo);
                    if (percentage > lastShow) {
                        lastShow = percentage;
                        GameAPI.getManager().sendArena(GameAPI.getManager().buildJson(GameAPI.getArena(), percentage));
                    }

                    z+=16;
                    if (z >= 550) {
                        z = - 550;
                        x += 16;
                    }

                    if (x >= 550)  {
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
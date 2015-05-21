package net.samagames.uhcrun.listener;

import net.samagames.gameapi.json.Status;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkListener implements Runnable, Listener
{

    private Map<Chunk, Long> lastChunkCleanUp;
    private UHCRun plugin;

    public ChunkListener()
    {
        this.plugin = UHCRun.getInstance();
        // Allow Concurrent modification
        lastChunkCleanUp = new ConcurrentHashMap<>();
        Bukkit.getScheduler().runTaskTimer(plugin, this, 20, 200);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(final ChunkUnloadEvent event)
    {
        if(!lastChunkCleanUp.containsKey(event.getChunk()))
            lastChunkCleanUp.put(event.getChunk(), 0L);

        event.setCancelled(true);
    }

    @Override
    public void run()
    {
        final long currentTime = System.currentTimeMillis();
        // Clear entities
        for(Chunk chunk : lastChunkCleanUp.keySet())
        {
            if(plugin.getGame().getStatus() == Status.Generating || !chunk.isLoaded() || (currentTime - lastChunkCleanUp.get(chunk) <= 10000))
                continue;

            for (Entity entity : chunk.getEntities())
                if (!(entity instanceof Item || entity instanceof HumanEntity))
                    entity.remove();

            lastChunkCleanUp.replace(chunk, currentTime);
        }

    }
}

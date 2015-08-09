package net.samagames.uhcrun.listener;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class ChunkListener implements Runnable, Listener {

    private Map<Chunk, Long> lastChunkCleanUp;

    public ChunkListener(JavaPlugin plugin) {
        // Allow Concurrent modification
        lastChunkCleanUp = new ConcurrentHashMap<>();
        Bukkit.getScheduler().runTaskTimer(plugin, this, 20, 200);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(final ChunkUnloadEvent event) {
        if (!lastChunkCleanUp.containsKey(event.getChunk())) {
            lastChunkCleanUp.put(event.getChunk(), System.currentTimeMillis());
        }

        event.setCancelled(true);
    }

    @Override
    public void run() {
        final long currentTime = System.currentTimeMillis();
        final List<Chunk> toRemove = new ArrayList<>();
        // Clear entities
        for (Chunk chunk : lastChunkCleanUp.keySet()) {
            if (!chunk.isLoaded() || (currentTime - lastChunkCleanUp.get(chunk) <= 40000)) {
                continue;
            }

            if (containPlayer(chunk)) {
                toRemove.add(chunk);
                continue;
            }


            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Item || entity instanceof HumanEntity)) {
                    Bukkit.getLogger().info("Removing " + entity);
                    entity.remove();
                }
            }

            lastChunkCleanUp.remove(chunk);
        }

        // Remove Chunks that contains players (don't need to be cleaned)
        toRemove.stream().filter(lastChunkCleanUp::containsKey).forEach(lastChunkCleanUp::remove);

    }

    private boolean containPlayer(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof HumanEntity) {
                return true;
            }
        }
        return false;
    }
}

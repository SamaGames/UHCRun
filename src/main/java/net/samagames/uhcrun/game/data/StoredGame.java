package net.samagames.uhcrun.game.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class StoredGame
{
    private final String server;
    private final long startTime;
    private final String gameType;
    private Map<UUID, SavedPlayer> players = new HashMap<>();
    private long endTime;

    public StoredGame(String server, long startTime, String gameType)
    {
        this.server = server;
        this.startTime = startTime;
        this.gameType = gameType;
    }

    public SavedPlayer getPlayer(UUID player, String name)
    {
        SavedPlayer pl = players.get(player);
        if (pl == null)
        {
            players.put(player, new SavedPlayer(name, player));
            return players.get(player);
        }
        return pl;
    }

    public SavedPlayer getPlayer(UUID player)
    {
        return players.get(player);
    }

    public Map<UUID, SavedPlayer> getPlayers()
    {
        return players;
    }

    public String getServer()
    {
        return server;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long time)
    {
        this.endTime = time;
    }

    public String getGameType()
    {
        return gameType;
    }
}

package net.zyuiop.survivalgames.game;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.types.GameArena;
import net.zyuiop.survivalgames.SurvivalGames;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by vialarl on 16/01/2015.
 */
public class Game implements GameArena {
    protected CopyOnWriteArraySet<UUID> players;
    protected int maxPlayers;
    protected int minPlayers;
    protected int vipPlayers;
    protected Status status;
    protected String mapName;
    protected SurvivalGames plugin;

    public void start() {

    }

    @Override
    public int countGamePlayers() {
        return players.size();
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public int getTotalMaxPlayers() {
        return getMaxPlayers() + getVIPSlots();
    }

    @Override
    public int getVIPSlots() {
        return vipPlayers;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    public void updateStatus(Status status) {
        setStatus(status);
        GameAPI.getManager().sendArena();
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return players.contains(player);
    }
}

package net.samagames.uhcrun.game;

import net.samagames.api.games.StatusEnum;
import org.bukkit.entity.Player;

public class SoloGame implements net.samagames.api.games.IManagedGame {
    @Override
    public void startGame() {

    }

    @Override
    public void playerJoin(Player player) {

    }

    @Override
    public void playerDisconnect(Player player) {

    }

    @Override
    public int getMaxPlayers() {
        return 0;
    }

    @Override
    public int getTotalMaxPlayers() {
        return 0;
    }

    @Override
    public int getConnectedPlayers() {
        return 0;
    }

    @Override
    public StatusEnum getStatus() {
        return null;
    }

    @Override
    public String getMapName() {
        return null;
    }

    @Override
    public String getGameName() {
        return null;
    }
}

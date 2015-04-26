package net.samagames.uhcrun.game;

import net.samagames.api.games.IManagedGame;
import net.samagames.api.games.StatusEnum;
import org.bukkit.entity.Player;

/**
 * Created by Thog92 on 27/04/2015.
 */
public class TeamGame implements IManagedGame {

    public TeamGame(int playersPerTeam) {
    }

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
        return "";
    }

    @Override
    public String getGameName() {
        return "UHC Run";
    }
}

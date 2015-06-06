package net.samagames.uhcrun.game;

import net.samagames.api.games.IReconnectGame;
import net.samagames.api.games.Status;
import net.samagames.api.games.themachine.CoherenceMachine;
import net.samagames.api.player.PlayerData;
import net.samagames.uhcrun.task.GameLoop;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public abstract class AbstractGame extends IReconnectGame
{

    public abstract void postInit();

    public abstract void startGame();

    public abstract void finish();

    public void updateStatus(Status status)
    {
        setStatus(status);
    }

    public abstract boolean hasTeleportPlayers();

    public abstract void enableDamages();

    public abstract void disableDamages();

    public abstract boolean isDamagesEnabled();

    public abstract void disablePVP();

    public abstract void enablePVP();

    public abstract CoherenceMachine getCoherenceMachine();

    public abstract int getKills(UUID player);

    public abstract int getPreparingTime();

    public abstract void teleportDeathMatch();

    public abstract int getDeathMatchSize();

    public abstract int getReductionTime();

    public abstract boolean isPvpEnabled();

    public abstract boolean isInGame(UUID player);

    public abstract void stumpPlayer(Player entity, boolean b);

    public abstract void addKill(UUID player);

    public abstract void creditKillCoins(Player killer);

    public abstract void checkStump(Player player);

    public abstract GameLoop getGameLoop();

    public abstract boolean isDisconnected(UUID player);

    public abstract void startFight();

    public abstract PlayerData getPlayerData(UUID uuid);

    public PlayerData getPlayerData(Player player)
    {
        return getPlayerData(player.getUniqueId());
    }
}

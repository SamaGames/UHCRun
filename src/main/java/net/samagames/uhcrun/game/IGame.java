package net.samagames.uhcrun.game;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.themachine.CoherenceMachine;
import net.samagames.gameapi.types.GameArena;
import net.samagames.uhcrun.task.GameLoop;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public interface IGame extends GameArena
{

    void postInit();

    void start();

    void finish();

    default void updateStatus(Status status)
    {
        setStatus(status);
        GameAPI.getManager().sendArena();
    }

    void join(Player player);

    void quit(Player player);

    void rejoin(UUID playerID);

    boolean hasTeleportPlayers();

    void enableDamages();

    void disableDamages();

    boolean isDamagesEnabled();

    void disablePVP();

    void enablePVP();

    CoherenceMachine getCoherenceMachine();

    int getKills(UUID player);

    int getPreparingTime();

    void teleportDeathMatch();

    int getDeathMatchSize();

    int getReductionTime();

    boolean isPvpEnabled();

    boolean isInGame(UUID player);

    void stumpPlayer(Player entity, boolean b);

    void addKill(UUID player);

    void creditKillCoins(Player killer);

    void checkStump(Player player);

    GameLoop getGameLoop();

    boolean isDisconnected(UUID player);
}

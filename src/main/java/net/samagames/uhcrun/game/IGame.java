package net.samagames.uhcrun.game;

import net.samagames.gameapi.json.Status;
import net.samagames.gameapi.types.GameArena;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public interface IGame extends GameArena
{

    void updateStatus(Status available);


    void join(Player player);
}

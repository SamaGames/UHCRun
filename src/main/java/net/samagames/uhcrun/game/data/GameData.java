package net.samagames.uhcrun.game.data;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public abstract class GameData
{

    public boolean equals(Object o)
    {
        if (o instanceof GameData)
            return ((GameData) o).getIdentification().equals(this.getIdentification());
        return false;
    }

    protected abstract String getIdentification();
}

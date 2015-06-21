package net.samagames.uhcrun.game.data;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public abstract class GameData {

    @Override
    public boolean equals(Object o) {
        return o instanceof GameData && ((GameData) o).getIdentification().equals(this.getIdentification());
    }

    protected abstract String getIdentification();
}

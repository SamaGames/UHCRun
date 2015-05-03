package net.samagames.uhcrun.database;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public interface IDatabase
{

    void hset(String uhcrungames, String gameId, String json);
}

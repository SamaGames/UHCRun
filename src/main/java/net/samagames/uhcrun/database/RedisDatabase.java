package net.samagames.uhcrun.database;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class RedisDatabase implements IDatabase
{
    private ShardedJedisPool pool;

    public RedisDatabase(ShardedJedisPool pool)
    {
        this.pool = pool;
    }

    @Override
    public void hset(String key, String field, String value)
    {
        ShardedJedis jedis = pool.getResource();
        jedis.hset(key, field, value);
    }
}

package net.zyuiop.survivalgames.game;

import net.zyuiop.survivalgames.SpawnBlock;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by {USER}
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SpawnLocation {
    protected double x;
    protected double z;

    public SpawnLocation(int x, int z) {
        this.x = x+0.5;
        this.z = z+0.5;
    }

    public Location getSpawn(World world) {
        return new Location(world, x, 150.0, z);
    }

    public Location getDeathmatchSpawn(World world) {
        return new Location(world, x/4, 150.0, z/4);
    }
}

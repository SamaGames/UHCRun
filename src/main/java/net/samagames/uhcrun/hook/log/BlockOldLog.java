package net.samagames.uhcrun.hook.log;

import net.minecraft.server.v1_8_R3.BlockLog1;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class BlockOldLog extends BlockLog1 implements CustomLog
{

    @Override
    public void remove(World world, BlockPosition pos, IBlockData data)
    {
        this.remove(world, pos, data, true);
    }
}

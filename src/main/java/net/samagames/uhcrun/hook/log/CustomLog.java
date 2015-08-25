package net.samagames.uhcrun.hook.log;

import net.minecraft.server.v1_8_R3.*;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public interface CustomLog
{

    default void remove(World world, BlockPosition pos, IBlockData data, boolean oldWood)
    {

        if (data.equals(world.getType(pos)))
        {
            return;
        }

        //System.out.println("===============Remove " + world.getType(pos).getBlock().getClass().getCanonicalName() +  " at " + pos);
        byte range = 20;
        int offset = range + 1;
        if (world.areChunksLoadedBetween(pos.a(-offset, -offset, -offset), pos.a(offset, offset, offset)))
        {

            for (BlockPosition blockPos : BlockPosition.a(pos.a(-range, -range, -range), pos.a(range, range, range)))
            {
                IBlockData blockState = world.getType(blockPos);

                //System.out.println(blockState.getBlock());

                if (blockState.getBlock().getMaterial() == Material.LEAVES)
                {
                    blockState.getBlock().dropNaturally(world, blockPos, blockState, 1.0F, 0);
                    world.setTypeAndData(pos, Blocks.AIR.getBlockData(), 3);
                }
            }
        }
        //System.out.println("===============");
    }

}

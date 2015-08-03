package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R3.*;

import java.util.Iterator;

public interface CustomLog {

    default void remove(World world, BlockPosition pos, IBlockData data, boolean oldWood) {

        if (data.equals(world.getType(pos))) {
            return;
        }

        //System.out.println("===============Remove " + world.getType(pos).getBlock().getClass().getCanonicalName() +  " at " + pos);
        byte range = 20;
        int offset = range + 1;
        if (world.areChunksLoadedBetween(pos.a(-offset, -offset, -offset), pos.a(offset, offset, offset))) {
            Iterator<BlockPosition> blockInRange = BlockPosition.a(pos.a(-range, -range, -range), pos.a(range, range, range)).iterator();

            while (blockInRange.hasNext()) {
                BlockPosition blockPos = blockInRange.next();
                IBlockData blockState = world.getType(blockPos);

                //System.out.println(blockState.getBlock());

                if (blockState.getBlock().getMaterial() == Material.LEAVES) {
                    blockState.getBlock().dropNaturally(world, blockPos, blockState, 1.0F, 0);
                    world.setTypeAndData(pos, Blocks.AIR.getBlockData(), 3);
                }
            }
        }
        //System.out.println("===============");
    }

}

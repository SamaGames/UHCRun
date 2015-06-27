package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R2.*;

import java.util.Iterator;

public interface CustomLog {

    default void remove(World world, BlockPosition pos) {
        System.out.println("Remove " + world.getType(pos).getBlock().getClass().getCanonicalName() +  " at " + pos);
        byte range = 4;
        int offset = range + 1;
        if (world.areChunksLoadedBetween(pos.a(-offset, -offset, -offset), pos.a(offset, offset, offset))) {
            Iterator<BlockPosition> blockInRange = BlockPosition.a(pos.a(-range, -range, -range), pos.a(range, range, range)).iterator();

            while (blockInRange.hasNext()) {
                BlockPosition blockPos = blockInRange.next();
                IBlockData blockState = world.getType(blockPos);

                if (blockState.getBlock().getMaterial() == Material.LEAVES) {

                    if (blockState.get(BlockLeaves.CHECK_DECAY).booleanValue()) {
                        blockState.getBlock().remove(world, blockPos, blockState);
                    } else {
                        world.setTypeAndData(blockPos, blockState.set(BlockLeaves.CHECK_DECAY, false), 4);
                    }
                }
            }
        }
    }
}

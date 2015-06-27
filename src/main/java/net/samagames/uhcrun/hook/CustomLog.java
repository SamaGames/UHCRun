package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.World;

import java.util.Iterator;

public interface CustomLog {

    default void remove(World world, BlockPosition pos) {
        byte range = 4;
        int offset = range + 1;
        if (world.areChunksLoadedBetween(pos.a(-offset, -offset, -offset), pos.a(offset, offset, offset))) {
            Iterator<BlockPosition> blockInRange = BlockPosition.a(pos.a(-range, -range, -range), pos.a(range, range, range)).iterator();

            while (blockInRange.hasNext()) {
                BlockPosition blockPos = blockInRange.next();
                IBlockData blockState = world.getType(blockPos);

                if (blockState.getBlock().getMaterial() == Material.LEAVES) {
                    blockState.getBlock().remove(world, blockPos, blockState);
                }
            }
        }
    }
}

package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R2.BlockLog1;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.World;

public class BlockOldLog extends BlockLog1 implements CustomLog {

    @Override
    public void remove(World world, BlockPosition pos, IBlockData data) {
        this.remove(world, pos, data, true);
    }
}

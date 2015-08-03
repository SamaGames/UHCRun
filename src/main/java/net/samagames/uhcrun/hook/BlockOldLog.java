package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R3.BlockLog1;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;

public class BlockOldLog extends BlockLog1 implements CustomLog {

    @Override
    public void remove(World world, BlockPosition pos, IBlockData data) {
        this.remove(world, pos, data, true);
    }
}

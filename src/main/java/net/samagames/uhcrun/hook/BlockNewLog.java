package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R3.BlockLog2;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.World;

public class BlockNewLog extends BlockLog2 implements CustomLog {
    @Override
    public void remove(World world, BlockPosition pos, IBlockData data) {
        this.remove(world, pos, data, false);
    }
}

package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R2.*;

public class BlockOldLog extends BlockLog1 implements CustomLog {

    @Override
    public void remove(World world, BlockPosition pos, IBlockData data) {
        this.remove(world, pos);
    }
}

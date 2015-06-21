package net.samagames.uhcrun.hook;

import java.util.Random;

import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockLeaves1;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.World;

public class BlockLeavesHookedCompatibility extends BlockLeaves1 {
    Random random = new Random();

    @Override
    public void doPhysics(World world, BlockPosition blockposition, IBlockData iblockdata, Block block) {
        world.b(blockposition, this, 4 + random.nextInt(7), 100); //Force decay (4~11 ticks)
    }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.zyuiop.uhcrun.generator;

import com.google.common.base.Objects;
import java.util.Random;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.BlockSand;
import net.minecraft.server.v1_8_R2.Blocks;
import net.minecraft.server.v1_8_R2.ChunkSnapshot;
import net.minecraft.server.v1_8_R2.IBlockData;
import net.minecraft.server.v1_8_R2.Material;
import net.minecraft.server.v1_8_R2.MathHelper;
import net.minecraft.server.v1_8_R2.World;
import net.minecraft.server.v1_8_R2.WorldGenBase;

public class WorldGenCaves extends WorldGenBase {
    public WorldGenCaves() {
    }

    protected void a(long var1, int var3, int var4, ChunkSnapshot var5, double var6, double var8, double var10) {
        this.a(var1, var3, var4, var5, var6, var8, var10, 1.0F + this.b.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void a(long var1, int var3, int var4, ChunkSnapshot var5, double var6, double var8, double var10, float var12, float var13, float var14, int var15, int var16, double var17) {
        double var19 = (double)(var3 * 16 + 8);
        double var21 = (double)(var4 * 16 + 8);
        float var23 = 0.0F;
        float var24 = 0.0F;
        Random var25 = new Random(var1);
        if(var16 <= 0) {
            int var26 = this.a * 16 - 16;
            var16 = var26 - var25.nextInt(var26 / 4);
        }

        boolean var64 = false;
        if(var15 == -1) {
            var15 = var16 / 2;
            var64 = true;
        }

        int var27 = var25.nextInt(var16 / 2) + var16 / 4;

        for(boolean var28 = var25.nextInt(6) == 0; var15 < var16; ++var15) {
            double var29 = 1.5D + (double)(MathHelper.sin((float)var15 * 3.1415927F / (float)var16) * var12 * 1.0F);
            double var31 = var29 * var17;
            float var33 = MathHelper.cos(var14);
            float var34 = MathHelper.sin(var14);
            var6 += (double)(MathHelper.cos(var13) * var33);
            var8 += (double)var34;
            var10 += (double)(MathHelper.sin(var13) * var33);
            if(var28) {
                var14 *= 0.92F;
            } else {
                var14 *= 0.7F;
            }

            var14 += var24 * 0.1F;
            var13 += var23 * 0.1F;
            var24 *= 0.9F;
            var23 *= 0.75F;
            var24 += (var25.nextFloat() - var25.nextFloat()) * var25.nextFloat() * 2.0F;
            var23 += (var25.nextFloat() - var25.nextFloat()) * var25.nextFloat() * 4.0F;
            if(!var64 && var15 == var27 && var12 > 1.0F && var16 > 0) {
                this.a(var25.nextLong(), var3, var4, var5, var6, var8, var10, var25.nextFloat() * 0.5F + 0.5F, var13 - 1.5707964F, var14 / 3.0F, var15, var16, 1.0D);
                this.a(var25.nextLong(), var3, var4, var5, var6, var8, var10, var25.nextFloat() * 0.5F + 0.5F, var13 + 1.5707964F, var14 / 3.0F, var15, var16, 1.0D);
                return;
            }

            if(var64 || var25.nextInt(4) != 0) {
                double var35 = var6 - var19;
                double var37 = var10 - var21;
                double var39 = (double)(var16 - var15);
                double var41 = (double)(var12 + 2.0F + 16.0F);
                if(var35 * var35 + var37 * var37 - var39 * var39 > var41 * var41) {
                    return;
                }

                if(var6 >= var19 - 16.0D - var29 * 2.0D && var10 >= var21 - 16.0D - var29 * 2.0D && var6 <= var19 + 16.0D + var29 * 2.0D && var10 <= var21 + 16.0D + var29 * 2.0D) {
                    int var43 = MathHelper.floor(var6 - var29) - var3 * 16 - 1;
                    int var44 = MathHelper.floor(var6 + var29) - var3 * 16 + 1;
                    int var45 = MathHelper.floor(var8 - var31) - 1;
                    int var46 = MathHelper.floor(var8 + var31) + 1;
                    int var47 = MathHelper.floor(var10 - var29) - var4 * 16 - 1;
                    int var48 = MathHelper.floor(var10 + var29) - var4 * 16 + 1;
                    if(var43 < 0) {
                        var43 = 0;
                    }

                    if(var44 > 16) {
                        var44 = 16;
                    }

                    if(var45 < 1) {
                        var45 = 1;
                    }

                    if(var46 > 248) {
                        var46 = 248;
                    }

                    if(var47 < 0) {
                        var47 = 0;
                    }

                    if(var48 > 16) {
                        var48 = 16;
                    }

                    boolean var49 = false;

                    int var50;
                    for(var50 = var43; !var49 && var50 < var44; ++var50) {
                        for(int var51 = var47; !var49 && var51 < var48; ++var51) {
                            for(int var52 = var46 + 1; !var49 && var52 >= var45 - 1; --var52) {
                                if(var52 >= 0 && var52 < 256) {
                                    IBlockData var53 = var5.a(var50, var52, var51);
                                    if(var53.getBlock() == Blocks.FLOWING_WATER || var53.getBlock() == Blocks.WATER) {
                                        var49 = true;
                                    }

                                    if(var52 != var45 - 1 && var50 != var43 && var50 != var44 - 1 && var51 != var47 && var51 != var48 - 1) {
                                        var52 = var45;
                                    }
                                }
                            }
                        }
                    }

                    if(!var49) {
                        for(var50 = var43; var50 < var44; ++var50) {
                            double var54 = ((double)(var50 + var3 * 16) + 0.5D - var6) / var29;

                            for(int var65 = var47; var65 < var48; ++var65) {
                                double var56 = ((double)(var65 + var4 * 16) + 0.5D - var10) / var29;
                                boolean var58 = false;
                                if(var54 * var54 + var56 * var56 < 1.0D) {
                                    for(int var59 = var46; var59 > var45; --var59) {
                                        double var60 = ((double)(var59 - 1) + 0.5D - var8) / var31;
                                        if(var60 > -0.7D && var54 * var54 + var60 * var60 + var56 * var56 < 1.0D) {
                                            IBlockData var62 = var5.a(var50, var59, var65);
                                            IBlockData var63 = (IBlockData)Objects.firstNonNull(var5.a(var50, var59 + 1, var65), Blocks.AIR.getBlockData());
                                            if(var62.getBlock() == Blocks.GRASS || var62.getBlock() == Blocks.MYCELIUM) {
                                                var58 = true;
                                            }

                                            if(this.a(var62, var63)) {
                                                if(var59 - 1 < 10) {
                                                    var5.a(var50, var59, var65, Blocks.LAVA.getBlockData());
                                                } else {
                                                    var5.a(var50, var59, var65, Blocks.AIR.getBlockData());
                                                    if(var63.getBlock() == Blocks.SAND) {
                                                        var5.a(var50, var59 + 1, var65, var63.get(BlockSand.VARIANT) == BlockSand.EnumSandVariant.RED_SAND?Blocks.RED_SANDSTONE.getBlockData():Blocks.SANDSTONE.getBlockData());
                                                    }

                                                    if(var58 && var5.a(var50, var59 - 1, var65).getBlock() == Blocks.DIRT) {
                                                        var5.a(var50, var59 - 1, var65, this.c.getBiome(new BlockPosition(var50 + var3 * 16, 0, var65 + var4 * 16)).ak.getBlock().getBlockData());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if(var64) {
                            break;
                        }
                    }
                }
            }
        }

    }

    protected boolean a(IBlockData var1, IBlockData var2) {
        return var1.getBlock() == Blocks.STONE?true:(var1.getBlock() == Blocks.DIRT?true:(var1.getBlock() == Blocks.GRASS?true:(var1.getBlock() == Blocks.HARDENED_CLAY?true:(var1.getBlock() == Blocks.STAINED_HARDENED_CLAY?true:(var1.getBlock() == Blocks.SANDSTONE?true:(var1.getBlock() == Blocks.RED_SANDSTONE?true:(var1.getBlock() == Blocks.MYCELIUM?true:(var1.getBlock() == Blocks.SNOW_LAYER?true:(var1.getBlock() == Blocks.SAND || var1.getBlock() == Blocks.GRAVEL) && var2.getBlock().getMaterial() != Material.WATER))))))));
    }

    protected void a(World var1, int var2, int var3, int var4, int var5, ChunkSnapshot var6) {
        int var7 = this.b.nextInt(this.b.nextInt(this.b.nextInt(15) + 1) + 1);
        if(this.b.nextInt(7) != 0) {
            var7 = 0;
        }

        for(int var8 = 0; var8 < var7; ++var8) {
            double var9 = (double)(var2 * 16 + this.b.nextInt(16));
            double var11 = (double)this.b.nextInt(this.b.nextInt(120) + 8);
            double var13 = (double)(var3 * 16 + this.b.nextInt(16));
            int var15 = 1;
            if(this.b.nextInt(4) == 0) {
                this.a(this.b.nextLong(), var4, var5, var6, var9, var11, var13);
                var15 += this.b.nextInt(4);
            }

            for(int var16 = 0; var16 < var15; ++var16) {
                float var17 = this.b.nextFloat() * 3.1415927F * 2.0F;
                float var18 = (this.b.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var19 = this.b.nextFloat() * 2.0F + this.b.nextFloat();
                if(this.b.nextInt(10) == 0) {
                    var19 *= this.b.nextFloat() * this.b.nextFloat() * 3.0F + 1.0F;
                }

                this.a(this.b.nextLong(), var4, var5, var6, var9, var11, var13, var19, var17, var18, 0, 0, 1.0D);
            }
        }

    }
}

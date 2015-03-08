//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.zyuiop.uhcrun.generator;

import java.util.Random;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Blocks;
import net.minecraft.server.v1_8_R1.ChunkSnapshot;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.MathHelper;
import net.minecraft.server.v1_8_R1.World;
import net.minecraft.server.v1_8_R1.WorldGenBase;

public class WorldGenCanyon extends WorldGenBase {
    private float[] d = new float[1024];

    public WorldGenCanyon() {
    }

    protected void a(long var1, int var3, int var4, ChunkSnapshot var5, double var6, double var8, double var10, float var12, float var13, float var14, int var15, int var16, double var17) {
        Random random = new Random(var1);
        double var20 = (double)(var3 * 16 + 8);
        double var22 = (double)(var4 * 16 + 8);
        float var24 = 0.0F;
        float var25 = 0.0F;
        if(var16 <= 0) {
            int var26 = this.a * 16 - 16;
            var16 = var26 - random.nextInt(var26 / 4);
        }

        boolean var63 = false;
        if(var15 == -1) {
            var15 = var16 / 2;
            var63 = true;
        }

        float var27 = 1.0F;

        for(int var28 = 0; var28 < 256; ++var28) {
            if(var28 == 0 || random.nextInt(3) == 0) {
                var27 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
            }

            this.d[var28] = var27 * var27;
        }

        for(; var15 < var16; ++var15) {
            double var29 = 1.5D + (double)(MathHelper.sin((float)var15 * 3.1415927F / (float)var16) * var12 * 1.0F);
            double var31 = var29 * var17;
            var29 *= (double)random.nextFloat() * 0.25D + 0.75D;
            var31 *= (double)random.nextFloat() * 0.25D + 0.75D;
            float var33 = MathHelper.cos(var14);
            float var34 = MathHelper.sin(var14);
            var6 += (double)(MathHelper.cos(var13) * var33);
            var8 += (double)var34;
            var10 += (double)(MathHelper.sin(var13) * var33);
            var14 *= 0.7F;
            var14 += var25 * 0.05F;
            var13 += var24 * 0.05F;
            var25 *= 0.8F;
            var24 *= 0.5F;
            var25 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            var24 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if(true /*var63 || var19.nextInt(4) != 0*/) {
                double var35 = var6 - var20;
                double var37 = var10 - var22;
                double var39 = (double)(var16 - var15);
                double var41 = (double)(var12 + 2.0F + 16.0F);
                if(var35 * var35 + var37 * var37 - var39 * var39 > var41 * var41) {
                    return;
                }

                if(var6 >= var20 - 16.0D - var29 * 2.0D && var10 >= var22 - 16.0D - var29 * 2.0D && var6 <= var20 + 16.0D + var29 * 2.0D && var10 <= var22 + 16.0D + var29 * 2.0D) {
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

                    var49 = false;
                    if(!var49) {
                        for(var50 = var43; var50 < var44; ++var50) {
                            double var54 = ((double)(var50 + var3 * 16) + 0.5D - var6) / var29;

                            for(int var64 = var47; var64 < var48; ++var64) {
                                double var56 = ((double)(var64 + var4 * 16) + 0.5D - var10) / var29;
                                boolean var58 = false;
                                //if(var54 * var54 + var56 * var56 < 1.0D) {
                                    for(int var59 = var46; var59 > var45; --var59) {
                                        double var60 = ((double)(var59 - 1) + 0.5D - var8) / var31;
                                        if(true || (var54 * var54 + var56 * var56) * (double)this.d[var59 - 1] + var60 * var60 / 6.0D < 1.0D) {
                                            IBlockData var62 = var5.a(var50, var59, var64);
                                            if(var62.getBlock() == Blocks.GRASS) {
                                                var58 = true;
                                            }

                                            if(var62.getBlock() == Blocks.STONE || var62.getBlock() == Blocks.DIRT || var62.getBlock() == Blocks.GRASS) {
                                                if(var59 - 1 < 10) {
                                                    var5.a(var50, var59, var64, Blocks.FLOWING_LAVA.getBlockData());
                                                } else {
                                                    var5.a(var50, var59, var64, Blocks.AIR.getBlockData());
                                                    if(var58 && var5.a(var50, var59 - 1, var64).getBlock() == Blocks.DIRT) {
                                                        var5.a(var50, var59 - 1, var64, this.c.getBiome(new BlockPosition(var50 + var3 * 16, 0, var64 + var4 * 16)).ak);
                                                    }
                                                }
                                            }
                                        }
                                    }
                               // }
                            }
                        }

                        /*if(var63) {
                            break;
                        }*/
                    }
                }
            }
        }

    }

    protected void a(World var1, int var2, int var3, int var4, int var5, ChunkSnapshot var6) {
        double var7 = (double)(var2 * 16 + this.b.nextInt(16));
            double var9 = (double)(this.b.nextInt(this.b.nextInt(40) + 8) + 20);
            double var11 = (double)(var3 * 16 + this.b.nextInt(16));
            byte var13 = 1;

            for(int var14 = 0; var14 < var13; ++var14) {
                float var15 = this.b.nextFloat() * 3.1415927F * 2.0F;
                float var16 = (this.b.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float var17 = (this.b.nextFloat() * 2.0F + this.b.nextFloat()) * 2.0F;
                this.a(this.b.nextLong(), var4, var5, var6, var7, var9, var11, var17, var15, var16, 0, 0, 3.0D);
            }
    }
}

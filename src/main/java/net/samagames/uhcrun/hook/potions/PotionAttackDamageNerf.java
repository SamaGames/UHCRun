package net.samagames.uhcrun.hook.potions;

import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MobEffectAttackDamage;
import net.minecraft.server.v1_8_R3.MobEffectList;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class PotionAttackDamageNerf extends MobEffectAttackDamage
{
    public PotionAttackDamageNerf(int i, MinecraftKey minecraftKey, boolean b, int i1)
    {
        super(i, minecraftKey, b, i1);
        this.b(4, 0);
    }

    @Override
    public double a(int id, AttributeModifier modifier)
    {
        double result = super.a(id, modifier);
        if (this.id == MobEffectList.INCREASE_DAMAGE.id)
        {
            result /= 3;
        }
        return result;
    }
}

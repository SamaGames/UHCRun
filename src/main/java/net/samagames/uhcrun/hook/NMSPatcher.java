package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.samagames.uhcrun.utils.Reflection;
import net.samagames.uhcrun.hook.potions.PotionAttackDamageNerf;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class NMSPatcher
{
    public NMSPatcher()
    {

    }

    public void patchBiomes() throws ReflectiveOperationException
    {
        BiomeBase[] biomes = BiomeBase.getBiomes();
        Map<String, BiomeBase> biomesMap = BiomeBase.o;
        BiomeBase defaultBiome = BiomeBase.FOREST;

        Field defaultBiomeField = BiomeBase.class.getDeclaredField("ad");
        Reflection.setFinalStatic(defaultBiomeField, defaultBiome);

        // FIXME: more modular system
        biomesMap.remove("Ocean");
        biomesMap.remove("Beach");
        biomesMap.remove("FrozenOcean");
        biomesMap.remove("Deep Ocean");
        biomesMap.remove("Cold Beach");
        biomesMap.remove("Extreme Hills");
        biomesMap.remove("Extreme Hills+");

        /*JsonElement blacklistedBiomes = api.getGameManager().getGameProperties().getOption("blacklistedBiomes", null);
        if (blacklistedBiomes != null)
        {
            for (JsonElement biome : blacklistedBiomes.getAsJsonArray())
            {
                biomesMap.remove(biome.getAsString());
            }
        }*/


        for (int i = 0; i < biomes.length; i++)
        {
            if (biomes[i] != null && !biomesMap.containsKey(biomes[i].ah))
            {
                biomes[i] = defaultBiome;
            }
        }

        Reflection.setFinalStatic(BiomeBase.class.getDeclaredField("biomes"), biomes);
    }

    public void patchPotions() throws ReflectiveOperationException
    {
        // HACK: Force Bukkit to accept potions
        Reflection.setFinalStatic(PotionEffectType.class.getDeclaredField("acceptingNew"), true);

        // Avoid Bukkit to throw a exception during instanciation
        Field byIdField =  Reflection.getField(PotionEffectType.class, true, "byId");
        Field byNameField =  Reflection.getField(PotionEffectType.class, true, "byName");
        ((Map) byNameField.get(null)).remove("increase_damage");
        ((PotionEffectType[]) byIdField.get(null))[5] = null;
        Bukkit.getLogger().info("Patching Strength Potion (130% => 43.3%, 260% =>86.6%)");
        Reflection.setFinalStatic(MobEffectList.class.getDeclaredField("INCREASE_DAMAGE"), (new PotionAttackDamageNerf(5, new MinecraftKey("strength"), false, 9643043)).c("potion.damageBoost").a(GenericAttributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 2.5D, 2));
        Bukkit.getLogger().info("Potions patched");
    }
}

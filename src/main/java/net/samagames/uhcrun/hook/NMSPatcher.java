package net.samagames.uhcrun.hook;

import net.minecraft.server.v1_8_R3.*;
import net.samagames.uhcrun.compatibility.GameProperties;
import net.samagames.uhcrun.hook.potions.PotionAttackDamageNerf;
import net.samagames.uhcrun.utils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class NMSPatcher
{
    private final Logger logger;
    private final GameProperties properties;

    public NMSPatcher(GameProperties properties)
    {
        this.properties = properties;
        this.logger = Bukkit.getLogger();
    }

    public void patchBiomes() throws ReflectiveOperationException
    {
        BiomeBase[] biomes = BiomeBase.getBiomes();
        Map<String, BiomeBase> biomesMap = BiomeBase.o;
        BiomeBase defaultBiome = BiomeBase.FOREST;

        fixAnimals();

        Field defaultBiomeField = BiomeBase.class.getDeclaredField("ad");

        Reflection.setFinalStatic(defaultBiomeField, defaultBiome);

        if (properties.getOptions().containsKey("blacklistedBiomes"))
        {
            ((List<String>) properties.getOptions().get("blacklistedBiomes")).forEach(biomesMap::remove);
        }

        // Force enable reeds for beach biomes and fix racio
        setReedsPerChunk(BiomeBase.BEACH, 10);
        setReedsPerChunk(BiomeBase.STONE_BEACH, 10);
        for (int i = 0; i < biomes.length; i++)
        {
            if (biomes[i] != null)
            {
                if (!biomesMap.containsKey(biomes[i].ah))
                {
                    biomes[i] = defaultBiome;
                }
                biomes[i] = addAnimals(biomes[i]);
                setReedsPerChunk(biomes[i], (int) Reflection.getValue(biomes[i].as, BiomeDecorator.class, true, "F") * (Integer) (properties.getOptions().getOrDefault("reedsMultiplier", 2)));
            }
        }

        Reflection.setFinalStatic(BiomeBase.class.getDeclaredField("biomes"), biomes);
    }

    private void setReedsPerChunk(BiomeBase biome, int value) throws NoSuchFieldException, IllegalAccessException
    {
        Reflection.setValue(biome.as, BiomeDecorator.class, true, "F", value);
    }

    public void fixAnimals() throws ReflectiveOperationException {
        addAnimalsSpawn("PLAINS", BiomeBase.PLAINS);
        addAnimalsSpawn("DESERT", BiomeBase.DESERT);
        addAnimalsSpawn("EXTREME_HILLS", BiomeBase.EXTREME_HILLS);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
        addAnimalsSpawn("TAIGA", BiomeBase.TAIGA);
        addAnimalsSpawn("SWAMPLAND", BiomeBase.SWAMPLAND);
        addAnimalsSpawn("RIVER", BiomeBase.RIVER);
        addAnimalsSpawn("FROZEN_OCEAN", BiomeBase.FROZEN_OCEAN);
        addAnimalsSpawn("FROZEN_RIVER", BiomeBase.FROZEN_RIVER);
        addAnimalsSpawn("MUSHROOM_ISLAND", BiomeBase.MUSHROOM_ISLAND);
        addAnimalsSpawn("MUSHROOM_SHORE", BiomeBase.MUSHROOM_SHORE);
        addAnimalsSpawn("BEACH", BiomeBase.BEACH);
        addAnimalsSpawn("DESERT_HILLS", BiomeBase.DESERT_HILLS);
        addAnimalsSpawn("FOREST_HILLS", BiomeBase.FOREST_HILLS);
        addAnimalsSpawn("TAIGA_HILLS", BiomeBase.TAIGA_HILLS);
        addAnimalsSpawn("SMALL_MOUNTAINS", BiomeBase.SMALL_MOUNTAINS);
        addAnimalsSpawn("JUNGLE", BiomeBase.JUNGLE);
        addAnimalsSpawn("JUNGLE_HILLS", BiomeBase.JUNGLE_HILLS);
        addAnimalsSpawn("JUNGLE_EDGE", BiomeBase.JUNGLE_EDGE);
        addAnimalsSpawn("STONE_BEACH", BiomeBase.STONE_BEACH);
        addAnimalsSpawn("COLD_BEACH", BiomeBase.COLD_BEACH);
        addAnimalsSpawn("BIRCH_FOREST", BiomeBase.BIRCH_FOREST);
        addAnimalsSpawn("BIRCH_FOREST_HILLS", BiomeBase.BIRCH_FOREST_HILLS);
        addAnimalsSpawn("ROOFED_FOREST", BiomeBase.ROOFED_FOREST);
        addAnimalsSpawn("COLD_TAIGA", BiomeBase.COLD_TAIGA);
        addAnimalsSpawn("COLD_TAIGA_HILLS", BiomeBase.COLD_TAIGA_HILLS);
        addAnimalsSpawn("MEGA_TAIGA", BiomeBase.MEGA_TAIGA);
        addAnimalsSpawn("MEGA_TAIGA_HILLS", BiomeBase.MEGA_TAIGA_HILLS);
        addAnimalsSpawn("EXTREME_HILLS_PLUS", BiomeBase.EXTREME_HILLS_PLUS);
        addAnimalsSpawn("SAVANNA", BiomeBase.SAVANNA);
        addAnimalsSpawn("SAVANNA_PLATEAU", BiomeBase.SAVANNA_PLATEAU);
        addAnimalsSpawn("MESA", BiomeBase.MESA);
        addAnimalsSpawn("MESA_PLATEAU_F", BiomeBase.MESA_PLATEAU_F);
        addAnimalsSpawn("MESA_PLATEAU", BiomeBase.MESA_PLATEAU);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
    }

    public void addAnimalsSpawn(String name, BiomeBase biomeBase) throws ReflectiveOperationException {

        Field biome = BiomeBase.class.getDeclaredField(name);
        Field defaultMobField = BiomeBase.class.getDeclaredField("au");
        defaultMobField.setAccessible(true);

        ArrayList<BiomeBase.BiomeMeta> mobs = new ArrayList<>();

        mobs.add(new BiomeBase.BiomeMeta(EntitySheep.class, 15, 10, 10));
        mobs.add(new BiomeBase.BiomeMeta(EntityRabbit.class, 4, 3, 5));
        mobs.add(new BiomeBase.BiomeMeta(EntityPig.class, 15, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityChicken.class, 20, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityCow.class, 15, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityWolf.class, 5, 5, 30));

        defaultMobField.set(biomeBase, mobs);
        Reflection.setFinalStatic(biome, biomeBase);
    }

    public BiomeBase addAnimals(BiomeBase biomeBase) throws NoSuchFieldException, IllegalAccessException {
        Field defaultMobField = BiomeBase.class.getDeclaredField("au");
        defaultMobField.setAccessible(true);

        ArrayList<BiomeBase.BiomeMeta> mobs = new ArrayList<>();

        mobs.add(new BiomeBase.BiomeMeta(EntitySheep.class, 15, 10, 10));
        mobs.add(new BiomeBase.BiomeMeta(EntityRabbit.class, 4, 3, 5));
        mobs.add(new BiomeBase.BiomeMeta(EntityPig.class, 15, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityChicken.class, 20, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityCow.class, 15, 20, 40));
        mobs.add(new BiomeBase.BiomeMeta(EntityWolf.class, 5, 5, 30));

        defaultMobField.set(biomeBase, mobs);
        return biomeBase;
    }

    public void patchPotions() throws ReflectiveOperationException
    {
        // HACK: Force Bukkit to accept potions
        Reflection.setFinalStatic(PotionEffectType.class.getDeclaredField("acceptingNew"), true);

        // Avoid Bukkit to throw a exception during instanciation
        Field byIdField = Reflection.getField(PotionEffectType.class, true, "byId");
        Field byNameField = Reflection.getField(PotionEffectType.class, true, "byName");
        ((Map) byNameField.get(null)).remove("increase_damage");
        ((PotionEffectType[]) byIdField.get(null))[5] = null;
        logger.info("Patching Strength Potion (130% => 43.3%, 260% =>86.6%)");
        Reflection.setFinalStatic(MobEffectList.class.getDeclaredField("INCREASE_DAMAGE"), (new PotionAttackDamageNerf(5, new MinecraftKey("strength"), false, 9643043)).c("potion.damageBoost").a(GenericAttributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 2.5D, 2));
        logger.info("Potions patched");
    }
}

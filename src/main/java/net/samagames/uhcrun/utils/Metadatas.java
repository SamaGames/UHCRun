package net.samagames.uhcrun.utils;

import net.samagames.uhcrun.UHCRun;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class Metadatas
{
    public static Object getMetadata(JavaPlugin plugin, Metadatable object, String key)
    {
        List<MetadataValue> values = object.getMetadata(key);
        for (MetadataValue value : values)
            if (value.getOwningPlugin().equals(plugin))
                return value.value();

        return null;
    }

    public static void setMetadata(JavaPlugin plugin, Metadatable object, String key, Object value)
    {
        object.setMetadata(key, new FixedMetadataValue(plugin, value));
    }
}


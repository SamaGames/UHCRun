package net.samagames.uhcrun.utils;

import net.samagames.uhcrun.UHCRun;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.List;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class Metadatas
{
    public static Object getMetadata(Metadatable object, String key)
    {
        List<MetadataValue> values = object.getMetadata(key);
        for (MetadataValue value : values)
        {
            // Plugins are singleton objects, so using == is safe here
            if (value.getOwningPlugin() == UHCRun.getInstance())
            {
                return value.value();
            }
        }
        return null;
    }

    public static void setMetadata(Metadatable object, String key, Object value)
    {
        object.setMetadata(key, new FixedMetadataValue(UHCRun.getInstance(), value));
    }
}


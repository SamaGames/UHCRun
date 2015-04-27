package net.samagames.uhcrun.utils;

import net.samagames.uhcrun.UHCRun;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.List;

/**
 * Created by zyuiop on 26/09/14.
 */
public class Metadatas {
    public static Object getMetadata(Metadatable object, String key) {
        List<MetadataValue> values = object.getMetadata(key);
        for (MetadataValue value : values) {
            // Plugins are singleton objects, so using == is safe here
            if (value.getOwningPlugin() == UHCRun.instance) {
                return value.value();
            }
        }
        return null;
    }

    public static void setMetadata(Metadatable object, String key, Object value) {
        object.setMetadata(key, new FixedMetadataValue(UHCRun.instance,value));
    }
}

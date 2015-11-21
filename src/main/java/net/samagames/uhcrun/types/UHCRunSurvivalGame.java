package net.samagames.uhcrun.types;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public interface UHCRunSurvivalGame
{
    void teleportDeathMatch();

    default void removeEffects(Player player)
    {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
    }
}

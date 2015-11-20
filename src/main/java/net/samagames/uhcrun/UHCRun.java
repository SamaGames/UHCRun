package net.samagames.uhcrun;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.modules.block.RapidOresModule;
import net.samagames.survivalapi.modules.block.AutomaticTNTModule;
import net.samagames.survivalapi.modules.combat.DropMyEffectsModule;
import net.samagames.survivalapi.modules.craft.*;
import net.samagames.survivalapi.modules.entity.RapidUsefullModule;
import net.samagames.survivalapi.modules.gameplay.ConstantPotionModule;
import net.samagames.survivalapi.modules.gameplay.RapidFoodModule;
import net.samagames.survivalapi.modules.gameplay.RapidStackingModule;
import net.samagames.uhcrun.types.UHCRunSurvivalSoloGame;
import net.samagames.uhcrun.types.UHCRunSurvivalTeamGame;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UHCRun extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        SurvivalGame game;

        int nb = SamaGamesAPI.get().getGameManager().getGameProperties().getOption("playersPerTeam", new JsonPrimitive(1)).getAsInt();

        SurvivalAPI.get().loadModule(DisableLevelTwoPotionModule.class, null);
        SurvivalAPI.get().loadModule(DisableNotchAppleModule.class, null);
        SurvivalAPI.get().loadModule(DisableSpeckedMelonModule.class, null);

        SurvivalAPI.get().loadModule(RapidToolsModule.class, null);
        SurvivalAPI.get().loadModule(RapidFoodModule.class, null);
        SurvivalAPI.get().loadModule(RapidStackingModule.class, null);
        SurvivalAPI.get().loadModule(RapidUsefullModule.class, null);
        SurvivalAPI.get().loadModule(RapidOresModule.class, new RapidOresModule.ConfigurationBuilder().build());

        SurvivalAPI.get().loadModule(AutomaticTNTModule.class, null);
        SurvivalAPI.get().loadModule(WaterMovingBootsModule.class, null);
        SurvivalAPI.get().loadModule(DropMyEffectsModule.class, null);

        ConstantPotionModule.ConfigurationBuilder constantPotionConfiguration = new ConstantPotionModule.ConfigurationBuilder();
        constantPotionConfiguration.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 255, 2));
        constantPotionConfiguration.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 255, 1));

        SurvivalAPI.get().loadModule(ConstantPotionModule.class, constantPotionConfiguration.build());

        if (nb > 1)
            game = new UHCRunSurvivalTeamGame(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 20 minutes", "=", nb);
        else
            game = new UHCRunSurvivalSoloGame(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 20 minutes", "=");

        SamaGamesAPI.get().getGameManager().registerGame(game);
    }
}
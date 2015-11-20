package net.samagames.uhcrun;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.modules.block.RapidOresModule;
import net.samagames.survivalapi.modules.craft.DisableLevelTwoPotionModule;
import net.samagames.survivalapi.modules.craft.DisableNotchAppleModule;
import net.samagames.survivalapi.modules.craft.DisableSpeckedMelonModule;
import net.samagames.survivalapi.modules.craft.RapidToolsModule;
import net.samagames.survivalapi.modules.gameplay.RapidFoodModule;
import net.samagames.survivalapi.modules.gameplay.RapidStackingModule;
import net.samagames.uhcrun.types.UHCRunSurvivalSoloGame;
import net.samagames.uhcrun.types.UHCRunSurvivalTeamGame;
import org.bukkit.plugin.java.JavaPlugin;

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
        SurvivalAPI.get().loadModule(RapidOresModule.class, new RapidOresModule.ConfigurationBuilder().build());

        if (nb > 1)
            game = new UHCRunSurvivalTeamGame(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 20 minutes", "=", nb);
        else
            game = new UHCRunSurvivalSoloGame(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 20 minutes", "=");

        SamaGamesAPI.get().getGameManager().registerGame(game);
    }
}
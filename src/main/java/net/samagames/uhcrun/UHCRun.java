package net.samagames.uhcrun;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.types.run.RunBasedGameLoop;
import net.samagames.survivalapi.game.types.run.RunBasedSoloGame;
import net.samagames.survivalapi.game.types.run.RunBasedTeamGame;
import org.bukkit.plugin.java.JavaPlugin;

public class UHCRun extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        SurvivalGame game;

        int nb = SamaGamesAPI.get().getGameManager().getGameProperties().getOption("playersPerTeam", new JsonPrimitive(1)).getAsInt();

        if (nb > 1)
            game = new RunBasedTeamGame<>(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 30 minutes", "=", RunBasedGameLoop.class, nb);
        else
            game = new RunBasedSoloGame<>(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 30 minutes", "=", RunBasedGameLoop.class);

        SamaGamesAPI.get().getGameManager().setMaxReconnectTime(20);
        SamaGamesAPI.get().getGameManager().registerGame(game);
    }
}
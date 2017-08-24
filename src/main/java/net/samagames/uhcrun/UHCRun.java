package net.samagames.uhcrun;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamesNames;
import net.samagames.survivalapi.SurvivalAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.types.run.RunBasedGameLoop;
import net.samagames.survivalapi.game.types.run.RunBasedSoloGame;
import net.samagames.survivalapi.game.types.run.RunBasedTeamGame;
import net.samagames.survivalapi.modules.craft.OneShieldModule;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This file is part of UHCRun.
 *
 * UHCRun is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UHCRun is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with UHCRun.  If not, see <http://www.gnu.org/licenses/>.
 */
public class UHCRun extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        SurvivalGame game;

        int nb = SamaGamesAPI.get().getGameManager().getGameProperties().getOption("playersPerTeam", new JsonPrimitive(1)).getAsInt();
        SurvivalAPI.get().loadModule(OneShieldModule.class, null);

        if (nb > 1)
            game = new RunBasedTeamGame<>(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 30 minutes", "=", RunBasedGameLoop.class, nb);
        else
            game = new RunBasedSoloGame<>(this, "uhcrun", "UHCRun", "L’expérience d’un UHC en 30 minutes", "=", RunBasedGameLoop.class);

        SamaGamesAPI.get().getGameManager().setMaxReconnectTime(20);
        SamaGamesAPI.get().getStatsManager().setStatsToLoad(GamesNames.UHCRUN, true);
        SamaGamesAPI.get().getShopsManager().setShopToLoad(GamesNames.UHCRUN, true);
        SamaGamesAPI.get().getGameManager().setGameStatisticsHelper(new UHCRunStatisticsHelper());
        SamaGamesAPI.get().getGameManager().registerGame(game);
    }
}
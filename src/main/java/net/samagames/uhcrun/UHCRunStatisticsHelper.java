package net.samagames.uhcrun;

import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGameStatisticsHelper;

import java.util.UUID;

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
public class UHCRunStatisticsHelper implements SurvivalGameStatisticsHelper
{
    @Override
    public void increaseKills(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByKills(1);
    }

    @Override
    public void increaseDeaths(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByDeaths(1);
    }

    @Override
    public void increaseDamages(UUID uuid, double damages)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByDamages((int) damages);
    }

    @Override
    public void increasePlayedTime(UUID uuid, long playedTime)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByPlayedTime(playedTime);
    }

    @Override
    public void increasePlayedGames(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByPlayedGames(1);
    }

    @Override
    public void increaseWins(UUID uuid)
    {
        SamaGamesAPI.get().getStatsManager().getPlayerStats(uuid).getUHCRunStatistics().incrByWins(1);
    }
}
package net.samagames.uhcrun.game;

import com.google.gson.JsonObject;
import net.samagames.api.games.*;

/**
 * Created by Thog9 on 08/09/2015.
 */
public class UHCRunSummary extends GameSummary
{
    public UHCRunSummary(net.samagames.api.games.Game game)
    {
        super(game);
    }

    @Override
    public JsonObject make()
    {
        return new JsonObject();
    }
}

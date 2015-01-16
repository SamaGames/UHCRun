package net.zyuiop.survivalgames.listeners;

import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.events.PreJoinPlayerEvent;
import net.zyuiop.survivalgames.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by vialarl on 16/01/2015.
 */
public class NetworkListener implements Listener {

    private Game game;

    public NetworkListener(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPreJoin(PreJoinPlayerEvent event) {

    }

    @EventHandler
    public void onFinishJoin(FinishJoinPlayerEvent event) {

    }

}

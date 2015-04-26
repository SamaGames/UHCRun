package net.samagames.uhcrun.listeners;

import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.events.PreJoinPlayerEvent;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.BasicGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NetworkListener implements Listener {

    private BasicGame game;

    public NetworkListener(BasicGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPreJoin(PreJoinPlayerEvent event) {
        if (! UHCRun.ready)
            event.refuse(ChatColor.RED + "Map en génération.");
    }


    @EventHandler (ignoreCancelled = true)
    public void onFinishJoin(FinishJoinPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player == null) {
            event.refuse(ChatColor.RED + "Une erreur s'est produite durant votre connexion au jeu.");
            return;
        }

        game.join(player);
    }

}

package net.samagames.uhcrun.listener;

import net.samagames.uhcrun.game.AbstractGame;
import org.bukkit.event.Listener;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class LoginListener implements Listener
{
    private AbstractGame game;

    public LoginListener(AbstractGame game)
    {
        this.game = game;
    }

    /*@EventHandler
    public void onPreJoin(PreJoinPlayerEvent event)
    {
        if (game.getStatus() == Status.Generating)
            event.refuse(ChatColor.RED + "Map en génération.");
        else if (game.getStatus() == Status.InGame && !game.isDisconnected(event.getPlayer()))
            event.refuse(ChatColor.RED + "La partie a déjà commencé !");

    }


    @EventHandler(ignoreCancelled = true)
    public void onFinishJoin(FinishJoinPlayerEvent event)
    {
        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player == null)
        {
            event.refuse(ChatColor.RED + "Une erreur s'est produite durant votre connexion au jeu.");
            return;
        }

        game.join(player);
    }*/
}

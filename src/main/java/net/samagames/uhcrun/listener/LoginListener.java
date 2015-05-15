package net.samagames.uhcrun.listener;

import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.events.PreJoinPlayerEvent;
import net.samagames.gameapi.json.Status;
import net.samagames.uhcrun.game.IGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class LoginListener implements Listener
{
    private IGame game;

    public LoginListener(IGame game)
    {
        this.game = game;
    }

    @EventHandler
    public void onPreJoin(PreJoinPlayerEvent event)
    {
        if (game.getStatus() == Status.Generating)
            event.refuse(ChatColor.RED + "Map en génération.");
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
    }
}

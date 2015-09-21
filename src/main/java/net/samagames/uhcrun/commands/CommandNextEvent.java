package net.samagames.uhcrun.commands;

import net.samagames.uhcrun.game.AbstractGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class CommandNextEvent implements CommandExecutor
{

    private AbstractGame game;

    public CommandNextEvent(AbstractGame game)
    {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        if (game.getGameLoop() != null && game.isGameStarted())
        {
            game.getGameLoop().forceNextEvent();
        } else
        {
            commandSender.sendMessage(ChatColor.DARK_RED + "Erreur: la partie n'a pas commencé !");
        }
        return true;
    }
}

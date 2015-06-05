package net.samagames.uhcrun.commands;

import net.samagames.uhcrun.game.AbstractGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog92
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class CommandStart implements CommandExecutor
{

    private AbstractGame game;

    public CommandStart(AbstractGame game)
    {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        game.startGame();
        return true;
    }
}

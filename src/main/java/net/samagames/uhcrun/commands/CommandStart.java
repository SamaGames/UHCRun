package net.samagames.uhcrun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.samagames.api.games.Status;
import net.samagames.uhcrun.game.Game;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class CommandStart implements CommandExecutor {

    private Game game;

    public CommandStart(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (game.getStatus() != Status.IN_GAME) {
            game.startGame();
        }

        return true;
    }
}

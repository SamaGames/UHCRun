package net.samagames.uhcrun.commands;

import net.samagames.uhcrun.game.IGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandNextEvent implements CommandExecutor
{

    private IGame game;

    public CommandNextEvent(IGame game)
    {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        game.getGameLoop().forceNextEvent();
        return true;
    }
}

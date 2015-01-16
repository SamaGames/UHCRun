package net.zyuiop.survivalgames.commands;

import net.zyuiop.survivalgames.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by vialarl on 16/01/2015.
 */
public class CommandStart implements CommandExecutor {

    private Game game;

    public CommandStart(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        return false;
    }
}

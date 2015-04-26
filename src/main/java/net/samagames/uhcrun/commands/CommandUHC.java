package net.samagames.uhcrun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Thog92 on 27/04/2015.
 */
public class CommandUHC implements CommandExecutor {

    private CommandExecutor help = new CommandHelp();
    private CommandExecutor invite = new CommandInvite();
    private CommandExecutor join;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 0)
        {
            switch (strings[0])
            {
                case "help":
                    help.onCommand(commandSender, command, s, strings);
                    break;

                case "invite":
                    invite.onCommand(commandSender, command, s, strings);
                    break;

                case "join":
                    join.onCommand(commandSender, command, s, strings);
                    break;
            }
        }
        else
        {
            return false;
        }

        return true;
    }
}

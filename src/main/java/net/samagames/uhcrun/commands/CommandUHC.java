package net.samagames.uhcrun.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandUHC implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
    {
        if(strings.length != 0)
        {
            switch (strings[0])
            {
                case "help":
                    CommandHelp.onCommand(cs, cmnd, string, strings);
                    break;
                    
                case "invite":
                    CommandInvite.onCommand(cs, cmnd, string, strings);
                    break;
                    
                case "join":
                    CommandJoin.onCommand(cs, cmnd, string, strings);
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

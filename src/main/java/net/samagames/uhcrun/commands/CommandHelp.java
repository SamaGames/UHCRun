package net.samagames.uhcrun.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Thog92 on 27/04/2015.
 */
public class CommandHelp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "UHC Run" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "Aide du plugin");

        addCommandInfo(commandSender, "help", "Affiche cette aide");
        addCommandInfo(commandSender, "start", "Lance la partie");
        addCommandInfo(commandSender, "adminteam", "Options de la team d'administration");
        return true;
    }

    private void addCommandInfo(CommandSender sender, String commandName, String description) {
        sender.sendMessage(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "UHC Run" + ChatColor.DARK_AQUA + "] " + ChatColor.WHITE + "/uhc " + commandName + ": " + description + ChatColor.RESET);
    }
}

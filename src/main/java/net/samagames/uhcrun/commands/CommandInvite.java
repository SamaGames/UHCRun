package net.samagames.uhcrun.commands;

import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.TeamGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Thog92 on 27/04/2015.
 */
public class CommandInvite implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player) {
            if (UHCRun.getInstance().getGame() instanceof TeamGame) {
                TeamGame game = (TeamGame) UHCRun.getInstance().getGame();
                if(!game.isGameStarted()) {
                    String inviter = commandSender.getName();
                    String invited = strings[1];
                    UUID aInviter = ((Player) cs).getUniqueId();
                    UUID aInvited = Bukkit.getPlayer(invited).getUniqueId();

                    Team team = game.getPlayerTeam(aInviter);
                    if (team != null && !team.isFull()) {
                        if (team.isInvited(aInvited))
                            team.removeInvite(aInvited);

                        team.invite(inviter, aInvited);
                        Bukkit.getPlayer(inviter).sendMessage(ChatColor.GREEN + "Le joueur a bien été invité !");
                    } else {
                        Bukkit.getPlayer(inviter).sendMessage(ChatColor.RED + "Votre équipe est pleine !");
                    }
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Vous n'êtes pas dans la version en équipes de l'UHCRun.");
            }
        }
        return true;
    }
}

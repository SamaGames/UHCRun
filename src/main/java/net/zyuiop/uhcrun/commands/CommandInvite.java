package net.zyuiop.uhcrun.commands;

import net.zyuiop.uhcrun.UHCRun;
import net.zyuiop.uhcrun.game.Team;
import net.zyuiop.uhcrun.game.TeamGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandInvite {
    public static boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(cs instanceof Player) {
            if (UHCRun.instance.game instanceof TeamGame) {
                TeamGame game = (TeamGame) UHCRun.instance.game;
                if(!game.isGameStarted()) {
                    String inviter = cs.getName();
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
                cs.sendMessage(ChatColor.RED + "Vous n'êtes pas dans la version en équipes de l'UHCRun.");
            }
        }
                
        return true;
    }
}

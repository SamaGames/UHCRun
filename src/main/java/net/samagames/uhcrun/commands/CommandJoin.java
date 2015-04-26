package net.samagames.uhcrun.commands;

import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.Team;
import net.samagames.uhcrun.game.TeamGame;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandJoin
{
    public static boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(cs instanceof Player) {
            if (UHCRun.instance.game instanceof TeamGame) {
                TeamGame game = (TeamGame) UHCRun.instance.game;
                if(!game.isGameStarted()) {
                    ChatColor teamColor = ChatColor.getByChar(strings[1]);
                    UUID aPlayer = ((Player)cs).getUniqueId();
                    Team team = null;
                    for (Team t : game.getTeams())
                        if (t.getChatColor() == teamColor)
                            team = t;

                    try {
                        if (game.getPlayerTeam(aPlayer) != null)
                            game.getPlayerTeam(aPlayer).remove(aPlayer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    if (team == null) {
                        cs.sendMessage(ChatColor.RED + "Cette équipe n'existe pas.");
                        return true;
                    }

                    if (team.isInvited(aPlayer)) {
                        if (!team.isFull()) {
                            team.join(aPlayer);
                            cs.sendMessage(game.getCoherenceMachine().getGameTag() + ChatColor.YELLOW + "Vous êtes entré dans l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.YELLOW + " !");
                        } else {
                            cs.sendMessage(ChatColor.RED + "L'équipe est pleine.");
                        }
                    } else {
                        cs.sendMessage(ChatColor.RED + "Vous n'êtes pas invité dans cette équipe.");
                    }
                }
            } else {
                cs.sendMessage(ChatColor.RED + "Vous n'êtes pas dans la version en équipes de l'UHCRun.");
            }
        }

        return true;
    }
}

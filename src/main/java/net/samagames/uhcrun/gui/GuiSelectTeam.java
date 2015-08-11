package net.samagames.uhcrun.gui;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import net.samagames.tools.chat.FancyMessage;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.game.data.Team;
import net.samagames.uhcrun.game.team.TeamSelector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class GuiSelectTeam extends Gui {
    private static TeamGame game;
    private static TeamSelector selector;
    private Field signField;
    private Field isEditable;
    private Method openSign;
    private Method setEditor;
    private Method getHandle;


    public GuiSelectTeam() {
        if (game == null || selector == null) {
            this.game = (TeamGame) UHCRun.getInstance().getGame();
            this.selector = TeamSelector.getInstance();
        }
    }

    @Override
    public void display(Player player) {
        this.inventory = Bukkit.getServer().createInventory(null, 54, "Sélection d'équipe");

        try {
            this.signField = CraftSign.class.getDeclaredField("sign");
            this.signField.setAccessible(true);
            this.isEditable = TileEntitySign.class.getDeclaredField("isEditable");
            this.isEditable.setAccessible(true);
            this.getHandle = CraftPlayer.class.getDeclaredMethod("getHandle", new Class[0]);
            this.openSign = EntityHuman.class.getDeclaredMethod("openSign", new Class[]{TileEntitySign.class});
            this.setEditor = TileEntitySign.class.getDeclaredMethod("a", new Class[]{EntityHuman.class});
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        int last = 10;

        for (Team team : game.getTeams()) {
            String name = team.getChatColor() + "Equipe " + team.getTeamName() + " [" + team.getPlayersUUID().size() + "/" + game.getPersonsPerTeam() + "]";

            ArrayList<String> lores = new ArrayList<>();

            if (team.isLocked()) {
                lores.add(ChatColor.RED + "L'équipe est fermée !");
                lores.add("");
            }

            for (UUID uuid : team.getPlayersUUID()) {
                if (game.getPlugin().getServer().getPlayer(uuid) != null) {
                    lores.add(team.getChatColor() + " - " + Bukkit.getPlayer(uuid).getName());
                } else {
                    team.remove(uuid);
                }
            }

            setSlotData(name, team.getIcon(), last, lores.toArray(new String[lores.size()]), "team_" + team.getChatColor());

            if (last == 16) {
                last = 19;
            } else {
                last++;
            }
        }

        setSlotData("Sortir de l'équipe", Material.ARROW, 31, null, "leave");

        String[] lores = new String[]{ChatColor.GREEN + "Réservé aux VIP :)"};

        setSlotData("Ouvrir/Fermer l'équipe", Material.BARRIER, 39, lores, "openclose");
        setSlotData("Changer le nom de l'équipe", Material.BOOK_AND_QUILL, 40, lores, "teamname");
        setSlotData("Inviter un joueur", Material.FEATHER, 41, lores, "invit");

        player.openInventory(this.inventory);
    }

    @Override
    public void onClick(final Player player, ItemStack stack, String action) {

        if (action.startsWith("team_")) {
            for (Team team : game.getTeams()) {
                if (action.equals("team_" + team.getChatColor())) {
                    if (!team.isLocked()) {
                        if (team.canJoin()) {
                            if (game.getPlayerTeam(player.getUniqueId()) != null) {
                                game.getPlayerTeam(player.getUniqueId()).remove(player.getUniqueId());
                            }
                            team.join(player.getUniqueId());
                            player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.YELLOW + "Vous êtes entré dans l'équipe " + team.getChatColor() + team.getTeamName() + ChatColor.YELLOW + " !");
                        } else {
                            player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "L'équipe choisie est pleine.");
                        }
                    } else {
                        player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "L'équipe choisie est fermée !");
                    }

                    break;
                }
            }

            selector.openGui(player, new GuiSelectTeam());
        } else if ("teamname".equals(action)) {
            if (game.getPlugin().getAPI().getPermissionsManager().hasPermission(player, "uhc.teamname")) {
                if (game.getPlayerTeam(player.getUniqueId()) != null) {
                    final Block block = player.getWorld().getBlockAt(0, 250, 150);
                    block.setTypeIdAndData(Material.SIGN_POST.getId(), (byte) 2, false);
                    Sign sign = (Sign) block.getState();
                    sign.setLine(0, game.getPlayerTeam(player.getUniqueId()).getTeamName());
                    sign.update(true);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(game.getPlugin(), () -> {
                        try {
                            final Object signTile = signField.get(block.getState());
                            final Object entityPlayer = getHandle.invoke(player, new Object[0]);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(game.getPlugin(), () -> {
                                try {
                                    openSign.invoke(entityPlayer, new Object[]{signTile});
                                    setEditor.invoke(signTile, new Object[]{entityPlayer});
                                    isEditable.set(signTile, true);
                                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                                    ex.printStackTrace();
                                }
                            }, 5L);
                        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    }, 1L);
                } else {
                    player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez avoir une équipe pour pouvoir utiliser cette fonction !");
                }
            } else {
                player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez être VIP pour pouvoir utiliser cette fonction !");
            }
        } else if ("openclose".equals(action)) {
            if (game.getPlugin().getAPI().getPermissionsManager().hasPermission(player, "uhc.teamlock")) {
                if (game.getPlayerTeam(player.getUniqueId()) != null) {
                    if (game.getPlayerTeam(player.getUniqueId()).isLocked()) {
                        game.getPlayerTeam(player.getUniqueId()).setLocked(false);
                        player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.GREEN + "Votre équipe est maintenant ouverte !");
                    } else {
                        game.getPlayerTeam(player.getUniqueId()).setLocked(true);
                        player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Votre équipe est maintenant fermée !");
                    }
                } else {
                    player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez avoir une équipe pour pouvoir utiliser cette fonction !");
                }
            } else {
                player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez être VIP pour pouvoir utiliser cette fonction !");
            }
        } else if ("invit".equals(action)) {
            if (game.getPlugin().getAPI().getPermissionsManager().hasPermission(player, "uhc.teaminvite")) {
                if (game.getPlayerTeam(player.getUniqueId()) != null) {
                    player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.YELLOW + "Vous pouvez inviter les joueurs suivants :");

                    Iterator<UUID> iter = game.getInGamePlayers().keySet().iterator();
                    while (iter.hasNext()) {
                        UUID aInvite = iter.next();
                        System.out.println(aInvite);
                        if (game.getPlayerTeam(aInvite) == null) {
                            if (Bukkit.getPlayer(aInvite) != null) {
                                new FancyMessage(" - " + Bukkit.getPlayer(aInvite).getName() + " ")
                                        .color(ChatColor.GRAY)
                                        .then("[Inviter]")
                                        .color(ChatColor.GREEN)
                                        .style(ChatColor.BOLD)
                                        .command("/uhc invite " + Bukkit.getPlayer(aInvite).getName())
                                        .send(player);
                            }
                        }
                    }
                } else {
                    player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez avoir une équipe pour pouvoir utiliser cette fonction !");
                }
            } else {
                player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez être VIP pour pouvoir utiliser cette fonction !");
            }
        } else if ("leave".equals(action)) {
            if (game.getPlayerTeam(player.getUniqueId()) != null) {
                game.getPlayerTeam(player.getUniqueId()).remove(player.getUniqueId());
                player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.GREEN + "Vous avez quitté l'équipe !");
            } else {
                player.sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Vous devez avoir une équipe pour pouvoir utiliser cette fonction !");
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}

package net.samagames.uhcrun.game.team;

import net.samagames.api.games.Status;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.gui.Gui;
import net.samagames.uhcrun.gui.GuiSelectTeam;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class TeamSelector implements Listener
{

    private static TeamSelector instance;
    private final TeamGame game;
    private Map<UUID, Gui> playersGui;

    public TeamSelector(TeamGame game) throws IllegalAccessException
    {
        if (instance != null)
        {
            throw new IllegalAccessException("Instance already defined!");
        }
        instance = this;
        this.game = game;
        this.playersGui = new TreeMap<>();
    }

    public static TeamSelector getInstance()
    {
        return instance;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        if (game.getStatus().equals(Status.IN_GAME))
        {
            event.getHandlers().unregister(this);
        } else if (event.getItem() != null && event.getItem().getType() == Material.NETHER_STAR)
        {
            this.openGui(event.getPlayer(), new GuiSelectTeam());
        }
    }


    @EventHandler
    public void clickEvent(InventoryClickEvent event)
    {
        if (game.getStatus().equals(Status.IN_GAME))
        {
            event.getHandlers().unregister(this);
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null && event.getView().getType() != InventoryType.PLAYER)
        {
            Gui gui = this.playersGui.get(event.getWhoClicked().getUniqueId());
            if (gui != null)
            {
                String action = gui.getAction(event.getSlot());
                if (action != null)
                {
                    gui.onClick((Player) event.getWhoClicked(), event.getCurrentItem(), action, event.getClick());
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        if (game.getStatus().equals(Status.IN_GAME))
        {
            event.getHandlers().unregister(this);
            return;
        }

        if (!game.getStatus().equals(Status.IN_GAME))
        {
            event.getBlock().setType(Material.AIR);
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            String name = event.getLine(0);
            name = name.trim();

            if (!name.isEmpty())
            {
                team.setTeamName(name);
                event.getPlayer().sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.GREEN + "Le nom de votre équipe est désormais : " + team.getChatColor() + team.getTeamName());
                this.openGui(event.getPlayer(), new GuiSelectTeam());
            } else
            {
                event.getPlayer().sendMessage(game.getCoherenceMachine().getGameTag() + " " + ChatColor.RED + "Le nom de l'équipe ne peut être vide.");
                this.openGui(event.getPlayer(), new GuiSelectTeam());
            }
        }
    }

    public void openGui(Player player, Gui gui)
    {
        if (this.playersGui.containsKey(player.getUniqueId()))
        {
            player.closeInventory();
            this.playersGui.remove(player.getUniqueId());
        }

        this.playersGui.put(player.getUniqueId(), gui);
        gui.display(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event)
    {

        if (!game.getStatus().equals(Status.IN_GAME))
        {
            return;
        }

        if (event.getMessage().startsWith("!"))
        {
            String message = event.getMessage().substring(1);
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            if (team != null)
            {
                event.setFormat(team.getChatColor() + "[" + team.getTeamName() + "] " + event.getPlayer().getName() + " : " + ChatColor.WHITE + message);
            }
        } else
        {
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            if (team != null)
            {
                event.setCancelled(true);
                String message = team.getChatColor() + "(Equipe) " + event.getPlayer().getName() + " : " + ChatColor.GOLD + ChatColor.ITALIC + event.getMessage();
                for (UUID id : team.getPlayersUUID())
                {
                    Player player = game.getPlugin().getServer().getPlayer(id);
                    if (player != null)
                    {
                        player.sendMessage(message);
                    }
                }
            }
        }
    }
}


package net.samagames.uhcrun.game;

import net.samagames.tools.chat.FancyMessage;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by {USER}
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class Team {

    public final DyeColor color;
    public final ChatColor chatColor;
    public String teamName;
    public CopyOnWriteArrayList<UUID> players = new CopyOnWriteArrayList<>();
    public boolean locked;
    public ItemStack icon;
    public int maxPlayers;
    private ArrayList<UUID> invited = new ArrayList<>();

    public Team(DyeColor color, ChatColor chatColor, String teamName, int maxPlayers) {
        this.color = color;
        this.chatColor = chatColor;
        this.teamName = teamName;
        this.icon = new ItemStack(Material.WOOL, 1, color.getData());
        this.maxPlayers = maxPlayers;
    }

    public boolean canJoin() {
        return (!this.isLocked() && !this.isFull());
    }

    public boolean isFull() {
        return this.players.size() >= maxPlayers;
    }

    public void join(UUID player) {
        Player newJoiner = Bukkit.getPlayer(player);

        if (newJoiner != null) {
            for (UUID member : players) {
                Player user = Bukkit.getPlayer(member);
                if (user != null)
                    user.sendMessage(UHCRun.instance.game.getCoherenceMachine().getGameTag() + ChatColor.AQUA + newJoiner.getName() + ChatColor.YELLOW + " a rejoint l'équipe.");
            }
        }

        this.players.add(player);
    }

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public DyeColor getColor() {
        return color;
    }

    public int removePlayer(UUID player) {
        players.remove(player);
        int size = players.size();
        if (size == 0 && isLocked())
            setLocked(false);
        return size;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getTeamName() {
        return teamName;
    }

    public CopyOnWriteArrayList<UUID> getPlayers() {
        return players;
    }

    public boolean hasPlayer(UUID player) {
        return players.contains(player);
    }

    public boolean isLocked() {
        return locked;
    }

    public void remove(UUID player) {
        players.remove(player);
        int size = players.size();
        if (size == 0 && isLocked())
            setLocked(false);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void invite(String inviter, UUID invited)
    {
        this.invited.add(invited);

        new FancyMessage("Vous avez été invité dans l'équipe " + this.getTeamName() + " par " + inviter + " ")
                .color(ChatColor.GOLD)
                .style(ChatColor.BOLD)
                .then("[Rejoindre]")
                .color(ChatColor.GREEN)
                .style(ChatColor.BOLD)
                .command("/uhc join " + this.getChatColor().getChar())
                .send(Bukkit.getPlayer(invited));
    }

    public void removeInvite(UUID uuid)
    {
        if(this.invited.contains(uuid))
            this.invited.remove(uuid);
    }

    public boolean isInvited(UUID uuid)
    {
        return this.invited.contains(uuid);
    }

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (! (o instanceof Team))
			return false;

		Team team = (Team) o;

		if (chatColor != team.chatColor)
			return false;
		if (color != team.color)
			return false;
		if (teamName != null ? ! teamName.equals(team.teamName) : team.teamName != null)
			return false;

		return true;
	}
}

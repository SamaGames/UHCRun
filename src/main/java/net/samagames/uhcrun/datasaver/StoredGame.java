package net.samagames.uhcrun.datasaver;

import java.util.HashMap;
import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class StoredGame {

	private HashMap<UUID, SavedPlayer> players = new HashMap<>();
	private final String server;
	private final long startTime;
	private long endTime;
	private final String gameType;

	public StoredGame(String server, long startTime, String gameType) {
		this.server = server;
		this.startTime = startTime;
		this.gameType = gameType;
	}

	public void setEndTime(long time) {
		this.endTime = time;
	}

	public SavedPlayer getPlayer(UUID player, String name) {
		SavedPlayer pl = players.get(player);
		if (pl == null) {
			players.put(player, new SavedPlayer(name, player));
			return players.get(player);
		}
		return pl;
	}

	public SavedPlayer getPlayer(UUID player) {
		return players.get(player);
	}

	public HashMap<UUID, SavedPlayer> getPlayers() {
		return players;
	}

	public String getServer() {
		return server;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public String getGameType() {
		return gameType;
	}
}

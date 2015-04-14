package net.zyuiop.uhcrun.datasaver;

import org.bukkit.Material;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class DamageReceived {

	private final Material weapon;
	private final String source;
	private final String targetDisplay;
	private double damage;

	public DamageReceived(Material weapon, String source, String targetDisplay, double damage) {
		this.weapon = weapon;
		this.source = source;
		this.targetDisplay = targetDisplay;
		this.damage = damage;
	}

	public Material getWeapon() {
		return weapon;
	}

	public String getSource() {
		return source;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public String getIdentification() {
		return weapon.toString() + "-" + source;
	}

	public boolean equals(Object o) {
		if (o instanceof DamageReceived)
			return ((DamageReceived) o).getIdentification().equals(this.getIdentification());
		return false;
	}
}

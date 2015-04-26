package net.samagames.uhcrun.datasaver;

import org.bukkit.Material;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class DamageDone {

	private final Material weapon;
	private final String target;
	private final String targetDisplay;
	private double damage;

	public DamageDone(Material weapon, String target, String targetDisplay, double damage) {
		this.weapon = weapon;
		this.target = target;
		this.targetDisplay = targetDisplay;
		this.damage = damage;
	}

	public Material getWeapon() {
		return weapon;
	}

	public String getTarget() {
		return target;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public String getIdentification() {
		return weapon.toString() + "-" + target;
	}

	public boolean equals(Object o) {
		if (o instanceof DamageDone)
			return ((DamageDone) o).getIdentification().equals(this.getIdentification());
		return false;
	}
}

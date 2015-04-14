package net.zyuiop.uhcrun.datasaver;

import org.bukkit.Material;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class HealingSource {

	private final String healMaterial;
	private double healAmount;

	public HealingSource(String healMaterial, double healAmount) {
		this.healMaterial = healMaterial;
		this.healAmount = healAmount;
	}

	public String getHealMaterial() {
		return healMaterial;
	}

	public double getHealAmount() {
		return healAmount;
	}

	public void setHealAmount(double healAmount) {
		this.healAmount = healAmount;
	}

	public String getIdentification() {
		return healMaterial;
	}

	public boolean equals(Object o) {
		if (o instanceof HealingSource)
			return ((HealingSource) o).getIdentification().equals(this.getIdentification());
		return false;
	}
}

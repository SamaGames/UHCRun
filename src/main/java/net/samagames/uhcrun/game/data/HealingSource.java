package net.samagames.uhcrun.game.data;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class HealingSource extends GameData {
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
}

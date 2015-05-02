package net.samagames.uhcrun.game.data;

import org.bukkit.Material;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 * FIXME: Can break the stats
 */
public class GameDamageData extends GameData
{
    private Material weapon;
    private String entityName;
    private double damage;

    public GameDamageData(String entityName, Material weapon, double damage)
    {
        this.weapon = weapon;
        this.entityName = entityName;
        this.damage = damage;
    }

    public Material getWeapon() {
        return weapon;
    }

    public String getEntity() {
        return entityName;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String getIdentification() {
        return weapon.toString() + "-" + entityName;
    }
}

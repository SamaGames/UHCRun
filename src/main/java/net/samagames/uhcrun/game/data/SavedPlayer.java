package net.samagames.uhcrun.game.data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class SavedPlayer
{
    private final String name;
    private final UUID uuid;
    private Map<String, GameDamageData> damageDone = new HashMap<>();
    private Map<String, GameDamageData> damageTaken = new HashMap<>();
    private Map<String, HealingSource> healingDone = new HashMap<>();
    private Map<String, String> hasKilled = new HashMap<>();
    private String killedBy;
    private int finalRanking;
    private long deadAfter;
    private double totalDamageTaken = 0;
    private double totalDamageMade = 0;
    private double totalHealthRegen = 0;

    public SavedPlayer(String name, UUID uuid)
    {
        this.name = name;
        this.uuid = uuid;
    }

    public void kill(Player player)
    {
        hasKilled.put(player.getName(), player.getDisplayName());
    }

    public void die(int ranking, String killer, long timeBeforeDeath)
    {
        this.finalRanking = ranking;
        this.killedBy = killer;
        this.deadAfter = timeBeforeDeath;
    }

    public void doDamage(GameDamageData damage)
    {
        GameDamageData stored = damageDone.get(damage.getIdentification());
        if (stored == null)
            stored = damage;
        else
            stored.setDamage(stored.getDamage() + damage.getDamage());
        damageDone.put(stored.getIdentification(), stored);
        totalDamageMade += damage.getDamage();
    }

    public void takeDamage(GameDamageData received)
    {
        GameDamageData stored = damageTaken.get(received.getIdentification());
        if (stored == null)
            stored = received;
        else
            stored.setDamage(stored.getDamage() + received.getDamage());
        damageTaken.put(stored.getIdentification(), stored);
        totalDamageTaken += received.getDamage();
    }

    public void heal(HealingSource heal)
    {
        HealingSource stored = healingDone.get(heal.getIdentification());
        if (stored == null)
            stored = heal;
        else
            stored.setHealAmount(stored.getHealAmount() + heal.getHealAmount());
        healingDone.put(stored.getIdentification(), stored);
        totalHealthRegen += heal.getHealAmount();
    }

    public String getName()
    {
        return name;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public Map<String, GameDamageData> getDamageDone()
    {
        return damageDone;
    }

    public Map<String, GameDamageData> getDamageTaken()
    {
        return damageTaken;
    }

    public Map<String, HealingSource> getHealingDone()
    {
        return healingDone;
    }

    public String getKilledBy()
    {
        return killedBy;
    }

    public void setKilledBy(String killedBy)
    {
        this.killedBy = killedBy;
    }

    public int getFinalRanking()
    {
        return finalRanking;
    }

    public void setFinalRanking(int finalRanking)
    {
        this.finalRanking = finalRanking;
    }

    public double getTotalDamageTaken()
    {
        return totalDamageTaken;
    }

    public double getTotalDamageMade()
    {
        return totalDamageMade;
    }

    public double getTotalHealthRegen()
    {
        return totalHealthRegen;
    }
}

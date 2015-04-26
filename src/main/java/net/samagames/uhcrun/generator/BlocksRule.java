package net.samagames.uhcrun.generator;

import org.bukkit.Material;

/**
 * Created by charles on 14/08/2014.
 */
public class BlocksRule {

    public Material id;
    public int data;
    public int round;
    public int minY;
    public int maxY;
    public int size;

    public BlocksRule(Material type, int data, int round, int minY, int maxY, int size) {
        this.id = type;
        this.data = data;
        this.round = round;
        this.minY = minY;
        this.maxY = maxY;
        this.size = size;
    }
}

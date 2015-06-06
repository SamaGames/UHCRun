package net.samagames.uhcrun.utils;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */

import org.bukkit.Color;

public class Colors
{

    /**
     * REQUIERE A REWRITE (SWITCH)
     *
     * @param i: color number
     * @return the Color object
     */
    public static Color getColor(int i)
    {
        Color c = Color.WHITE;
        if (i == 1) c = Color.AQUA;
        else if (i == 2) c = Color.BLACK;
        else if (i == 3) c = Color.BLUE;
        else if (i == 4) c = Color.FUCHSIA;
        else if (i == 5) c = Color.GRAY;
        else if (i == 6) c = Color.GREEN;
        else if (i == 7) c = Color.LIME;
        else if (i == 8) c = Color.MAROON;
        else if (i == 9) c = Color.NAVY;
        else if (i == 10) c = Color.OLIVE;
        else if (i == 11) c = Color.ORANGE;
        else if (i == 12) c = Color.PURPLE;
        else if (i == 13) c = Color.RED;
        else if (i == 14) c = Color.SILVER;
        else if (i == 15) c = Color.TEAL;
        else if (i == 17) c = Color.YELLOW;

        return c;
    }
}

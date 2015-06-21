package net.samagames.uhcrun.generator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class LobbyPopulator
{
    private UHCRun plugin;
    private EditSession es;

    public LobbyPopulator(UHCRun pl)
    {
        this.plugin = pl;
    }

    public void generate()
    {
        this.plugin.getLogger().info("Generating Looby...");
        File file = new File(this.plugin.getDataFolder(), "/lobby.schematic");

        if (file.exists())
        {
            try
            {
                Vector v = new Vector(0, 200, 0);
                World worldf = Bukkit.getWorld("world");
                worldf.loadChunk(0, 0);
                BukkitWorld BWf = new BukkitWorld(worldf);
                this.es = new EditSession(BWf, 2000000);
                this.es.setFastMode(true);
                CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
                c1.paste(this.es, v, true);
            } catch (MaxChangedBlocksException | IOException | DataException ex)
            {
                ex.printStackTrace();
            }

        } else
        {
            this.plugin.getLogger().severe(("File does not exist. Abort..."));
        }

        this.plugin.getLogger().info("Done.");
    }

    public void remove()
    {
        this.es.undo(this.es);
    }
}

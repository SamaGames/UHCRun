package net.samagames.uhcrun.generator;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;




/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Thog
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class LobbyPopulator {
    private final Logger logger;
    private final File file;
    private EditSession es;

    public LobbyPopulator(Logger logger, File file) {
        this.logger = logger;
        this.file = new File(file, "/lobby.schematic");
    }

    public void generate() {
        logger.info("Generating Looby...");

        if (file.exists()) {
            try {
                Vector v = new Vector(0, 200, 0);
                World worldf = Bukkit.getWorld("world");
                worldf.loadChunk(0, 0);
                BukkitWorld bwf = new BukkitWorld(worldf);
                this.es = new EditSession(bwf, 2000000);
                this.es.setFastMode(true);
                CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
                c1.paste(this.es, v, true);
            } catch (MaxChangedBlocksException | IOException | DataException ex) {
                ex.printStackTrace();
            }

        } else {
            logger.severe("File does not exist. Abort...");
        }

        logger.info("Done.");
    }

    public void remove() {
        this.es.undo(this.es);
    }
}

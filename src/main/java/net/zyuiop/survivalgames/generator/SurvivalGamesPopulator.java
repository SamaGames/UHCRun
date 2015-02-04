package net.zyuiop.survivalgames.generator;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import net.zyuiop.survivalgames.SurvivalGames;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Vector;

/**
 * Created by charles on 14/08/2014.
 */
public class SurvivalGamesPopulator extends BlockPopulator {

    private List<BlocksRule> blocks = new ArrayList<>();
    private Map<Material, Material> removeBlocks = new HashMap<>();

    public SurvivalGamesPopulator() {
    }

    public void replaceBlock(Material replace, Material replaceWith) {
        removeBlocks.put(replace, replaceWith);
    }

    public void registerRule(BlocksRule rule) {
        blocks.add(rule);
    }

    public void registerRule(BlocksRule rule, Material replaceOthers) {
        registerRule(rule);
        removeBlocks.put(rule.id, replaceOthers);
    }

    public void generateBlazeFortress(int x, int z) {
        if (!SurvivalGames.isWorldLoaded)
            return;


        Bukkit.getLogger().info("Generating fortress at "+x+ "; "+z);
        File file = new File(SurvivalGames.instance.getDataFolder(), "/nether.schematic");
        EditSession es;
        if (file.exists()) {
            try {
                com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(x, 40, z);
                World worldf = Bukkit.getWorld("world");
                Chunk chunk = worldf.getChunkAt(new org.bukkit.Location(worldf, x, 40, z));
                chunk.load(true);
                int cx = chunk.getX() - 3;
                int cz = chunk.getZ() - 3;
                while (cx < chunk.getX()+3) {
                    while (cz < chunk.getZ()+3) {
                        if (cx != chunk.getX() ||cz != chunk.getZ()) {
                            worldf.getChunkAt(cx, cz).load(true);
                        }
                        cz++;
                    }
                    cx++;
                }
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ() - 1).load(true);
                worldf.getChunkAt(chunk.getX() - 1, chunk.getZ()).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ() - 1).load(true);
                worldf.getChunkAt(chunk.getX() + 1, chunk.getZ()).load(true);
                worldf.getChunkAt(chunk.getX(), chunk.getZ() + 1).load(true);
                worldf.getChunkAt(chunk.getX(), chunk.getZ() - 1).load(true);

                BukkitWorld BWf = new BukkitWorld(worldf);
                es = new EditSession(BWf, 2000000);
                es.setFastMode(true);
                CuboidClipboard c1 = SchematicFormat.MCEDIT.load(file);
                c1.paste(es, v, false);

                int bx = x;
                while (bx < x+35) {
                    int bz = z;
                    while (bz < z+35) {
                        int by = 40;
                        while (by > 0) {
                            Location block = new org.bukkit.Location(worldf, bx, by, bz);
                            if (block.getBlock().getType() == Material.MOB_SPAWNER) {
                                block.getBlock().setType(Material.STONE);
                                block.getBlock().setType(Material.MOB_SPAWNER);
                                CreatureSpawner spawner = (CreatureSpawner) block.getBlock().getState();
                                spawner.setSpawnedType(EntityType.BLAZE);
                                spawner.setDelay(1);
                                spawner.update();
                                Bukkit.getLogger().info("Spawner configured at "+bx+" - "+by+" - "+bz);
                                break;
                            }
                            by--;
                        }
                        bz++;
                    }
                    bx++;
                }
            } catch (MaxChangedBlocksException | IOException | DataException ex) {
                ex.printStackTrace();
            }
        } else {
            Bukkit.getLogger().severe(("File does not exist."));
        }
    }

    public void populate(World world, Random random, Chunk chunk) {
        int x = 0;
        while (x < 16) {
            int z = 0;
            while (z < 16) {
                int y = 0;
                while (y < world.getMaxHeight()) {
                    Block block = chunk.getBlock(x, y, z);
                    if (removeBlocks.containsKey(block.getType()))
                        block.setType(removeBlocks.get(block.getType()));

                    y++;
                }
                z++;
            }
            x++;
        }

        for (BlocksRule bloc : blocks) {
            try {
                for (int i = 0; i < bloc.round; i++) {
                    x = chunk.getX() * 16 + random.nextInt(16);
                    int y = bloc.minY + random.nextInt(bloc.maxY - bloc.minY);
                    int z = chunk.getZ() * 16 + random.nextInt(16);

                    generate(world, random, x, y, z, bloc.size, bloc);

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (random.nextInt(1000) <= 10) {
            int xFortress = chunk.getX()*16 + random.nextInt(15);
            int zFortress = chunk.getZ()*16 + random.nextInt(15);
            generateBlazeFortress(xFortress, zFortress);
        }
    }

    private void generate (World world, Random rand, int x, int y, int z, int size, BlocksRule material) {
        double rpi = rand.nextDouble() * Math.PI;
        double x1 = x + 8 + Math.sin(rpi) * size / 8.0F;
        double x2 = x + 8 - Math.sin(rpi) * size / 8.0F;
        double z1 = z + 8 + Math.cos(rpi) * size / 8.0F;
        double z2 = z + 8 - Math.cos(rpi) * size / 8.0F;
        double y1 = y + rand.nextInt(3) + 2;
        double y2 = y + rand.nextInt(3) + 2;
        for (int i = 0; i <= size; i++) {
            double xPos = x1 + (x2 - x1) * i / size;
            double yPos = y1 + (y2 - y1) * i / size;
            double zPos = z1 + (z2 - z1) * i / size;
            double fuzz = rand.nextDouble() * size / 16.0D;
            double fuzzXZ = (Math.sin((float) (i * Math.PI / size)) + 1.0F) * fuzz + 1.0D;
            double fuzzY = (Math.sin((float) (i * Math.PI / size)) + 1.0F) * fuzz + 1.0D;
            int xStart = (int)Math.floor(xPos - fuzzXZ / 2.0D);
            int yStart = (int)Math.floor(yPos - fuzzY / 2.0D);
            int zStart = (int)Math.floor(zPos - fuzzXZ / 2.0D);
            int xEnd = (int)Math.floor(xPos + fuzzXZ / 2.0D);
            int yEnd = (int)Math.floor(yPos + fuzzY / 2.0D);
            int zEnd = (int)Math.floor(zPos + fuzzXZ / 2.0D);
            for (int ix = xStart; ix <= xEnd; ix++) {
                double xThresh = (ix + 0.5D - xPos) / (fuzzXZ / 2.0D);
                if (xThresh * xThresh < 1.0D) {
                    for (int iy = yStart; iy <= yEnd; iy++) {
                        double yThresh = (iy + 0.5D - yPos) / (fuzzY / 2.0D);
                        if (xThresh * xThresh + yThresh * yThresh < 1.0D) {
                            for (int iz = zStart; iz <= zEnd; iz++) {
                                double zThresh = (iz + 0.5D - zPos) / (fuzzXZ / 2.0D);
                                if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0D) {
                                    Block block = tryGetBlock(world, ix, iy, iz);
                                    if (block != null && block.getType() == Material.STONE) {
                                        block.setType(material.id);
                                        block.setData((byte) material.data);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private Block tryGetBlock(World world, int x, int y, int z) {
        int cx = x >> 4;
        int cz = z >> 4;

        if ((!world.isChunkLoaded(cx, cz)) &&
                (!world.loadChunk(cx, cz, false))) {
            return null;
        }

        Chunk chunk = world.getChunkAt(cx, cz);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlock(x & 0xF, y, z & 0xF);
    }
}

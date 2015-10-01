package net.samagames.uhcrun.compatibility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.commands.CommandNextEvent;
import net.samagames.uhcrun.game.AbstractGame;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.generator.LobbyPopulator;
import net.samagames.uhcrun.listener.*;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Thog9 on 25/08/2015.
 */
public class GameAdaptator implements Listener
{
    private LobbyPopulator loobyPopulator;
    private final UHCRun plugin;
    private SamaGamesAPI samaGamesAPI;
    private AbstractGame game;
    private PluginManager pluginManager;

    public GameAdaptator(UHCRun plugin)
    {
        this.plugin = plugin;
        this.pluginManager = Bukkit.getPluginManager();
        this.samaGamesAPI = SamaGamesAPI.get();
    }

    public void onEnable()
    {
        int nb = samaGamesAPI.getGameManager().getGameProperties().getOption("playersPerTeam", new JsonPrimitive(1)).getAsInt();

        if (nb > 1)
        {
            this.game = new TeamGame(plugin, nb);
        } else
        {
            this.game = new SoloGame(plugin);
        }

        samaGamesAPI.getGameManager().registerGame(game);
        samaGamesAPI.getGameManager().setMaxReconnectTime(game.getReductionTime());
        pluginManager.registerEvents(new SpectatorListener(game), plugin);
        pluginManager.registerEvents(new GameListener(game), plugin);
        pluginManager.registerEvents(new CompassTargeter(this), plugin);
        pluginManager.registerEvents(new StackListener(2), plugin);
        pluginManager.registerEvents(this, plugin);

        plugin.getCommand("nextevent").setExecutor(new CommandNextEvent(game));
    }

    public void postInit(World world)
    {
        JsonArray defaults = new JsonArray();
        defaults.add(new JsonPrimitive(0.6));
        defaults.add(new JsonPrimitive(152D));
        defaults.add(new JsonPrimitive(0.6));
        JsonArray spawnPos = samaGamesAPI.getGameManager().getGameProperties().getOption("spawnPos", defaults).getAsJsonArray();

        Location spawnLocation = new Location(world, spawnPos.get(0).getAsDouble(), spawnPos.get(1).getAsDouble(), spawnPos.get(2).getAsDouble());
        plugin.setSpawnLocation(spawnLocation);
        world.setSpawnLocation(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

        game.setStatus(Status.STARTING);
        // Add the lobby
        loobyPopulator = new LobbyPopulator(plugin.getLogger(), plugin.getDataFolder());
        loobyPopulator.generate();
        pluginManager.registerEvents(new CraftListener(), plugin);
        pluginManager.registerEvents(new BlockListener(game), plugin);

        game.postInit(world);
    }

    @EventHandler
    public void onPreJoin(PlayerJoinEvent event)
    {
        if (game == null || game.getStatus() == Status.WAITING_FOR_PLAYERS)
        {
            event.getPlayer().teleport(plugin.getSpawnLocation());
        }
    }

    public AbstractGame getGame()
    {
        return game;
    }

    public SamaGamesAPI getAPI()
    {
        return samaGamesAPI;
    }

    public void loadEnd()
    {
        game.setStatus(Status.WAITING_FOR_PLAYERS);
    }

    public UHCRun getPlugin()
    {
        return plugin;
    }

    public void removeSpawn()
    {
        loobyPopulator.remove();
    }

    public boolean checkAndDownloadWorld(File worldDir)
    {
        // Force reload
        samaGamesAPI.getGameManager().getGameProperties().reload();

        File worldZip = new File(worldDir.getParentFile(), "world.zip");
        JsonElement worldStorage = samaGamesAPI.getGameManager().getGameProperties().getConfig("worldStorage", null);
        if (worldStorage == null)
        {
            plugin.getLogger().severe("worldStorage not defined");
            return false;
        }

        URL worldStorageURL = null;
        String mapID = "No file found";

        try
        {
            worldStorageURL = new URL(worldStorage.getAsString() + "get.php");
            BufferedReader in = new BufferedReader(new InputStreamReader(worldStorageURL.openStream()));
            mapID = in.readLine();
            in.close();
            plugin.getLogger().info(mapID);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        if ("No file found".equals(mapID))
        {
            if (worldZip.exists())
            {
                plugin.getLogger().warning("No map availaible but found world.zip in local, assuming to use it.");
                boolean result = this.extractWorld(worldZip, worldDir);
                try
                {
                    worldStorageURL = new URL(worldStorage + "clean.php?name=" + mapID);
                    URLConnection connection = worldStorageURL.openConnection();
                    connection.connect();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return result;
            }
            return false;
        } else if (worldZip.exists())
        {
            plugin.getLogger().warning("world.zip already exist! Is that a Hydro managed server?");
            worldZip.delete();
        }


        try
        {
            worldStorageURL = new URL(worldStorage + "download.php?name=" + mapID);
            ReadableByteChannel rbc = Channels.newChannel(worldStorageURL.openStream());
            FileOutputStream fos = new FileOutputStream(worldZip);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        try
        {
            worldStorageURL = new URL(worldStorage + "clean.php?name=" + mapID);
            URLConnection connection = worldStorageURL.openConnection();
            connection.connect();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return this.extractWorld(worldZip, worldDir);
    }

    private boolean extractWorld(File worldZip, File worldDir)
    {
        if (worldDir.exists() && !worldDir.delete())
        {
            plugin.getLogger().severe("World cannot be removed! This is a critical error!");
        }

        try
        {
            ZipFile zipFile = new ZipFile(worldZip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(worldDir.getParent(), entry.getName());
                if (entry.isDirectory())
                {
                    entryDestination.mkdirs();
                } else
                {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
            zipFile.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}

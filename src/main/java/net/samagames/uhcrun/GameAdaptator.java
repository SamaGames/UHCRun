package net.samagames.uhcrun;

import com.google.gson.JsonPrimitive;
import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.commands.CommandNextEvent;
import net.samagames.uhcrun.game.Game;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.generator.LobbyPopulator;
import net.samagames.uhcrun.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Thog9 on 25/08/2015.
 */
public class GameAdaptator implements Listener
{
    private LobbyPopulator loobyPopulator;
    private final UHCRun plugin;
    private SamaGamesAPI samaGamesAPI;
    private Game game;
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
            this.game = new TeamGame(nb);
        } else
        {
            this.game = new SoloGame();
        }

        samaGamesAPI.getGameManager().registerGame(game);
        samaGamesAPI.getGameManager().setMaxReconnectTime(game.getReductionTime());
        pluginManager.registerEvents(new SpectatorListener(game), plugin);
        pluginManager.registerEvents(new GameListener(game), plugin);
        pluginManager.registerEvents(new CompassTargeter(this), plugin);
        pluginManager.registerEvents(this, plugin);

        plugin.getCommand("nextevent").setExecutor(new CommandNextEvent(game));
    }

    public void postInit(World world)
    {
        game.setStatus(Status.STARTING);
        // Add the lobby
        loobyPopulator = new LobbyPopulator(plugin.getLogger(), plugin.getDataFolder());
        loobyPopulator.generate();
        pluginManager.registerEvents(new CraftListener(), plugin);
        pluginManager.registerEvents(new BlockListener(40), plugin);

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

    public Game getGame()
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
}

package net.zyuiop.survivalgames;

import net.samagames.gameapi.GameAPI;
import net.zyuiop.survivalgames.commands.CommandStart;
import net.zyuiop.survivalgames.game.Game;
import net.zyuiop.survivalgames.listeners.NetworkListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by vialarl on 16/01/2015.
 */
public class SurvivalGames extends JavaPlugin {

    private FileConfiguration config;
    public static SurvivalGames instance;

    public void onEnable() {
        instance = this;
        config = this.getConfig();
        Game game = new Game();

        Bukkit.getServer().getPluginManager().registerEvents(new NetworkListener(game), this);
        getCommand("start").setExecutor(new CommandStart(game));

        GameAPI.registerGame(config.getString("gameName", "survivalgames"), game);
    }
}

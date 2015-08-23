package net.samagames.uhcrun.game;

import net.samagames.api.games.GamePlayer;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.UHCRun;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UHCPlayer extends GamePlayer
{
    private static Game game;
    private int kills;

    public UHCPlayer(Player player)
    {
        super(player);
    }

    public static void setGame(Game game)
    {
        UHCPlayer.game = game;
    }

    @Override
    public void handleLogin(boolean reconnect)
    {
        super.handleLogin(reconnect);
        Player player = getPlayerIfOnline();
        if (player == null)
        {
            return;
        }
        if (!reconnect)
        {
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(UHCRun.getInstance().getSpawnLocation());
            if (game instanceof TeamGame)
            {
                ItemStack star = new ItemStack(Material.NETHER_STAR);
                ItemMeta starMeta = star.getItemMeta();
                starMeta.setDisplayName(ChatColor.GOLD + "Sélectionner une équipe");
                star.setItemMeta(starMeta);
                player.getInventory().setItem(4, star);
            }
        } else
        {
            game.rejoin(player, false);
        }

    }

    @Override
    public void handleLogout()
    {
        super.handleLogout();
        if (spectator)
        {
            return;
        }
        Player player = getPlayerIfOnline();
        if (game.getStatus() == Status.IN_GAME)
        {
            game.getGameLoop().removePlayer(player.getUniqueId());
            if (game.isPvpEnabled())
            {
                System.out.println(player);
                game.stumpPlayer(player, true);
                Location time = player.getLocation();
                World w = time.getWorld();
                ItemStack[] var4 = player.getInventory().getContents();

                for (ItemStack stack : var4)
                {
                    if (stack != null)
                    {
                        w.dropItemNaturally(time, stack);
                    }
                }
            } else
            {
                game.getCoherenceMachine().getMessageManager().writePlayerQuited(player);
            }
        }
    }

    public UHCPlayer addKill()
    {
        kills++;
        return this;
    }

    public int getKills()
    {
        return kills;
    }
}

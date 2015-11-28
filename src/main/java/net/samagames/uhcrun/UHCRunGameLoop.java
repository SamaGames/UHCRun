package net.samagames.uhcrun;

import net.samagames.api.SamaGamesAPI;
import net.samagames.survivalapi.game.SurvivalGame;
import net.samagames.survivalapi.game.SurvivalGameLoop;
import net.samagames.survivalapi.game.SurvivalPlayer;
import net.samagames.uhcrun.types.UHCRunSurvivalGame;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class UHCRunGameLoop extends SurvivalGameLoop
{
    public UHCRunGameLoop(JavaPlugin plugin, Server server, SurvivalGame game)
    {
        super(plugin, server, game);
    }

    @Override
    public void createDamageEvent()
    {
        this.nextEvent = new TimedEvent(1, 0, "Dégats actifs", ChatColor.GREEN, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats sont désormais actifs.", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Le map sera réduite dans 19 minutes. Le PvP sera activé à ce moment là.", true);
            this.game.enableDamages();

            this.createTeleportationEvent();
        });
    }

    public void createTeleportationEvent()
    {
        this.nextEvent = new TimedEvent(19, 0, "Téléportation", ChatColor.YELLOW, () ->
        {
            SamaGamesAPI.get().getGameManager().setMaxReconnectTime(-1);

            this.game.disableDamages();
            ((UHCRunSurvivalGame) this.game).teleportDeathMatch();

            for (SurvivalPlayer player : (Collection<SurvivalPlayer>) this.game.getInGamePlayers().values())
            {
                player.getPlayerIfOnline().removePotionEffect(PotionEffectType.SPEED);
                player.getPlayerIfOnline().removePotionEffect(PotionEffectType.FAST_DIGGING);
            }

            this.game.getWorldBorder().setSize(400.0D);
            this.game.getWorldBorder().setSize(10.0D, 10L * 60L);

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est désormais réduite. Les bordures sont en coordonnées " + ChatColor.RED + "-" + (this.game.getWorldBorder().getSize() / 2) + " +" + (this.game.getWorldBorder().getSize() / 2) + ChatColor.RESET + ".", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP seront activés dans 30 secondes !", true);

            this.createDeathmatchEvent();
        });
    }

    public void createDeathmatchEvent()
    {
        this.nextEvent = new TimedEvent(0, 30, "PvP activé", ChatColor.RED, () ->
        {
            this.game.enableDamages();
            this.game.enablePVP();

            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("Les dégats et le PvP sont maintenant activés. Bonne chance !", true);
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est maintenant en réduction constante pendant les 10 prochaines minutes.", true);

            this.createReducingEvent();
        });
    }

    @Override
    public void createReducingEvent()
    {
        this.nextEvent = new TimedEvent(9, 30, "Fin de la réduction", ChatColor.RED, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La map est désormais réduite. Fin de la partie forcée dans 2 minutes !", true);

            this.createEndEvent();
        });
    }

    public void createEndEvent()
    {
        this.nextEvent = new TimedEvent(2, 0, "Fin de la partie", ChatColor.RED, () ->
        {
            this.game.getCoherenceMachine().getMessageManager().writeCustomMessage("La partie se termine.", true);
            this.server.shutdown();
        });
    }
}

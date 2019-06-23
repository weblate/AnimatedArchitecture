package nl.pim16aap2.bigdoors.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.bigdoors.BigDoors;

public class LoginResourcePackHandler implements Listener
{
    private final BigDoors plugin;
    private final String url;

    public LoginResourcePackHandler(BigDoors plugin, String url)
    {
        this.plugin = plugin;
        this.url    = url;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        try
        {
            // Slight delay so the player actually fully exists.
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if (event.getPlayer() != null)
                        event.getPlayer().setResourcePack(url);
                }
            }.runTaskLater(plugin, 10);
        }
        catch (Exception e)
        {
            plugin.getMyLogger().logException(e);
        }
    }
}

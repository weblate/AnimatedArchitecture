package nl.pim16aap2.bigdoors.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.bigdoors.BigDoors;

public class LoginMessageHandler implements Listener
{
    BigDoors plugin;
    String   message;

    public LoginMessageHandler(BigDoors plugin)
    {
        this.plugin  = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        try
        {
            Player player = event.getPlayer();
            // Normally, only send to those with permission, so they can disable it.
            // But when it's a devbuild, also send it to everyone who's OP, to make it
            // a bit harder to get around the message.
            if (player.hasPermission("bigdoors.admin") || player.isOp() && BigDoors.DEVBUILD)
                // Slight delay so the player actually receives the message;
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        String loginString = plugin.getLoginString();
                        if (loginString != "")
                            player.sendMessage(ChatColor.AQUA + plugin.getLoginString());
                    }
                }.runTaskLater(plugin, 10);
        }
        catch (Exception e)
        {
            plugin.getMyLogger().logException(e);
        }
    }
}

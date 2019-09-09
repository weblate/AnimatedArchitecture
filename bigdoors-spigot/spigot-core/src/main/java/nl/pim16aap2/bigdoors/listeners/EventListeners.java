package nl.pim16aap2.bigdoors.listeners;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigotutil.SpigotAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a listener that keeps track of various events.
 *
 * @author Pim
 */
public class EventListeners implements Listener
{
    private final BigDoorsSpigot plugin;

    public EventListeners(final BigDoorsSpigot plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Listens to players interacting with the world to check if they are using a BigDoors tool.
     *
     * @param event The {@link PlayerInteractEvent}.
     */
    @EventHandler
    public void onLeftClick(final PlayerInteractEvent event)
    {
        try
        {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null &&
                plugin.getTF().isTool(event.getPlayer().getInventory().getItemInMainHand()))
            {
                plugin.getToolUser(event.getPlayer()).ifPresent(
                    TU ->
                    {
                        TU.selector(event.getClickedBlock().getLocation());
                        event.setCancelled(true);
                    }
                );
            }
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Listens for the {@link PlayerJoinEvent} to make sure their latest name is updated in the database.
     *
     * @param event The {@link PlayerJoinEvent}.
     */
    @EventHandler
    public void onLogin(final PlayerJoinEvent event)
    {
        try
        {
            BigDoors.get().getDatabaseManager().updatePlayer(SpigotAdapter.wrapPlayer(event.getPlayer()));
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Listens for the {@link PlayerQuitEvent} to make sure all processes they are active in are cancelled properly.
     *
     * @param event The {@link PlayerQuitEvent}.
     */
    @EventHandler
    public void onLogout(final PlayerQuitEvent event)
    {
        try
        {
            plugin.onPlayerLogout(event.getPlayer());
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Checks if a player is a {@link nl.pim16aap2.bigdoors.toolusers.ToolUser} or not.
     *
     * @param player The {@link Player}.
     * @return True if a player is a {@link nl.pim16aap2.bigdoors.toolusers.ToolUser}.
     */
    private boolean isToolUser(final @NotNull Player player)
    {
        return plugin.getToolUser(player).isPresent();
    }

    /**
     * Listens for players trying to drop their BigDoors stick.
     * <p>
     * If they aren't a {@link nl.pim16aap2.bigdoors.toolusers.ToolUser}, the stick is deleted, as they shouldn't have
     * had it in the first place. Otherwise, it's just cancelled.
     *
     * @param event The {@link PlayerDropItemEvent}.
     */
    @EventHandler
    public void onItemDropEvent(PlayerDropItemEvent event)
    {
        try
        {
            if (plugin.getTF().isTool(event.getItemDrop().getItemStack()))
            {
                if (isToolUser(event.getPlayer()))
                    event.setCancelled(true);
                else
                    event.getItemDrop().remove();
            }
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Listens to players interacting with an inventory, to make sure they cannot move the BigDoors stick to another
     * inventory.
     *
     * @param event The {@link InventoryClickEvent}.
     */
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event)
    {
        try
        {
            if (!plugin.getTF().isTool(event.getCurrentItem()))
                return;
            if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) ||
                !event.getClickedInventory().getType().equals(InventoryType.PLAYER))
            {
                if (event.getWhoClicked() instanceof Player)
                {
                    if (isToolUser((Player) event.getWhoClicked()))
                        event.setCancelled(true);
                    else
                        event.getInventory().removeItem(event.getCurrentItem());
                }
                event.setCancelled(true); // TODO: Check this.
            }
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Listens to players interacting with an inventory, to make sure they cannot move the BigDoors stick to another
     * inventory.
     *
     * @param event The {@link InventoryDragEvent}.
     */
    @EventHandler
    public void inventoryDragEvent(InventoryDragEvent event)
    {
        try
        {
            event.getNewItems().forEach(
                (K, V) ->
                {
                    if (plugin.getTF().isTool(V))
                        event.setCancelled(true);
                });
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }

    /**
     * Listens to players attempting to move the BigDoors stick to another inventory.
     *
     * @param event The {@link InventoryMoveItemEvent}.
     */
    @EventHandler
    public void onItemMoved(InventoryMoveItemEvent event)
    {
        try
        {
            if (!plugin.getTF().isTool(event.getItem()))
                return;

            Inventory src = event.getSource();
            if (src instanceof PlayerInventory && ((PlayerInventory) src).getHolder() instanceof Player)
            {
                if (isToolUser((Player) ((PlayerInventory) src).getHolder()))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            src.removeItem(event.getItem());
        }
        catch (Exception e)
        {
            plugin.getPLogger().logException(e);
        }
    }
}

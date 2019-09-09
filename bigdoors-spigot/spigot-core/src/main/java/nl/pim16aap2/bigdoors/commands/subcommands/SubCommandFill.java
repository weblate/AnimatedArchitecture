package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigotutil.SpigotAdapter;
import nl.pim16aap2.bigdoors.util.PLogger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SubCommandFill extends SubCommand
{
    protected static final String help = "Replaces all the blocks in this door by stone. Not particularly useful usually.";
    protected static final String argsHelp = "<doorUID>";
    protected static final int minArgCount = 2;
    protected static final CommandData command = CommandData.FILLDOOR;

    public SubCommandFill(final @NotNull BigDoorsSpigot plugin, final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    /**
     * Replaces all blocks between the minimum and maximum coordinates of a {@link AbstractDoorBase} with stone.
     *
     * @param door The {@link AbstractDoorBase}.
     */
    public boolean execute(final @NotNull AbstractDoorBase door)
    {
        World bukkitWorld = SpigotAdapter.getBukkitWorld(door.getWorld());
        if (bukkitWorld == null)
        {
            PLogger.get().logException(new NullPointerException("World " + door.getWorld().toString() + " is null!"));
            return true;
        }

        for (int i = door.getMinimum().getX(); i <= door.getMaximum().getX(); ++i)
            for (int j = door.getMinimum().getY(); j <= door.getMaximum().getY(); ++j)
                for (int k = door.getMinimum().getZ(); k <= door.getMaximum().getZ(); ++k)
                    bukkitWorld.getBlockAt(i, j, k).setType(Material.STONE);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd,
                             final @NotNull String label, final @NotNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException, IllegalArgumentException
    {
        BigDoors.get().getDatabaseManager().getDoor(CommandManager.getLongFromArg(args[1]))
                .whenComplete((optionalDoor, throwable) -> optionalDoor.ifPresent(this::execute));
        return true;
    }
}

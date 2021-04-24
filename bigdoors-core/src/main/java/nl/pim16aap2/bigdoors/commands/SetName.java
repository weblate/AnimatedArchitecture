package nl.pim16aap2.bigdoors.commands;

import lombok.NonNull;
import lombok.ToString;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.tooluser.ToolUser;
import nl.pim16aap2.bigdoors.tooluser.creator.Creator;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the setName command, which is used to provide a name for a {@link ToolUser}.
 *
 * @author Pim
 */
@ToString
public class SetName extends BaseCommand
{
    private final @NonNull String name;

    protected SetName(final @NonNull ICommandSender commandSender, final @NonNull String name)
    {
        super(commandSender);
        this.name = name;
    }

    /**
     * Runs the {@link SetName} command.
     *
     * @param commandSender The {@link ICommandSender} responsible for providing the name.
     * @param name          The new name specified by the command sender.
     * @return See {@link BaseCommand#run()}.
     */
    public static @NonNull CompletableFuture<Boolean> run(final @NonNull ICommandSender commandSender,
                                                          final @NonNull String name)
    {
        return new SetName(commandSender, name).run();
    }

    @Override
    public @NonNull CommandDefinition getCommand()
    {
        return CommandDefinition.SET_NAME;
    }

    @Override
    protected boolean availableForNonPlayers()
    {
        return false;
    }

    @Override
    protected boolean validInput()
    {
        if (Util.isValidDoorName(name))
            return true;

        // TODO: Localization
        getCommandSender().sendMessage("The name \"" + name + "\" is not valid! Please select a different name");
        return false;
    }

    @Override
    protected @NonNull CompletableFuture<Boolean> executeCommand(final @NonNull BooleanPair permissions)
    {
        final IPPlayer player = (IPPlayer) getCommandSender();
        final Optional<ToolUser> tu = BigDoors.get().getToolUserManager().getToolUser(player.getUUID());
        if (tu.isPresent() && tu.get() instanceof Creator)
            tu.get().handleInput(name);
        else
            // TODO: Localization
            getCommandSender().sendMessage("Failed to process input: We are not waiting for any input!");
        return CompletableFuture.completedFuture(true);
    }
}

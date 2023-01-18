package nl.pim16aap2.bigdoors.commands;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.factories.ITextFactory;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.movable.AbstractMovable;
import nl.pim16aap2.bigdoors.movable.MovableAttribute;
import nl.pim16aap2.bigdoors.movable.MovableBase;
import nl.pim16aap2.bigdoors.text.TextType;
import nl.pim16aap2.bigdoors.util.movableretriever.MovableRetriever;
import nl.pim16aap2.bigdoors.util.movableretriever.MovableRetrieverFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents the base BigDoors command.
 * <p>
 * This handles all the basics shared by all commands and contains some utility methods for common actions.
 *
 * @author Pim
 */
@ToString
@Flogger
public abstract class BaseCommand
{
    /**
     * The entity (e.g. player, server, or command block) that initiated the command.
     * <p>
     * This is the entity that is held responsible for the command (i.e. their permissions are checked, and they will
     * receive error/success/information messages when applicable).
     */
    @Getter
    private final ICommandSender commandSender;

    protected final ILocalizer localizer;
    protected final ITextFactory textFactory;

    public BaseCommand(ICommandSender commandSender, ILocalizer localizer, ITextFactory textFactory)
    {
        this.commandSender = commandSender;
        this.localizer = localizer;
        this.textFactory = textFactory;
    }

    /**
     * Gets the {@link CommandDefinition} that contains the definition of this {@link BaseCommand}.
     *
     * @return The {@link CommandDefinition} that contains the definition of this {@link BaseCommand}.
     */
    public abstract CommandDefinition getCommand();

    /**
     * Checks if the input is valid before starting any potentially expensive tasks.
     *
     * @return True if the input is valid.
     */
    protected boolean validInput()
    {
        return true;
    }

    /**
     * Checks if the {@link #commandSender} has access to a given {@link MovableAttribute} for a given movable.
     *
     * @param door
     *     The movable to check.
     * @param doorAttribute
     *     The {@link MovableAttribute} to check.
     * @param hasBypassPermission
     *     Whether the {@link #commandSender} has bypass permission or not.
     * @return True if the command sender has access to the provided attribute for the given movable.
     */
    protected boolean hasAccessToAttribute(
        AbstractMovable door, MovableAttribute doorAttribute, boolean hasBypassPermission)
    {
        if (hasBypassPermission || !commandSender.isPlayer())
            return true;

        return commandSender.getPlayer()
                            .flatMap(door::getOwner)
                            .map(doorOwner -> doorAttribute.canAccessWith(doorOwner.permission()))
                            .orElse(false);
    }

    /**
     * Checks if this {@link BaseCommand} is available for {@link IPPlayer}s.
     *
     * @return True if an {@link IPPlayer} can execute this command.
     */
    protected boolean availableForPlayers()
    {
        return true;
    }

    /**
     * Checks if this {@link BaseCommand} is available for non-{@link IPPlayer}s (e.g. the server).
     *
     * @return True if a non-{@link IPPlayer} can execute this command.
     */
    protected boolean availableForNonPlayers()
    {
        return true;
    }

    /**
     * Executes the command.
     *
     * @return True if the command could be executed successfully or if the command execution failed through no fault of
     * the {@link ICommandSender}.
     */
    public final CompletableFuture<Boolean> run()
    {
        log();
        if (!validInput())
        {
            log.atFine().log("Invalid input for command: %s", this);
            return CompletableFuture.completedFuture(false);
        }

        final boolean isPlayer = commandSender instanceof IPPlayer;
        if (isPlayer && !availableForPlayers())
        {
            log.atFine().log("Command not allowed for players: %s", this);
            commandSender.sendMessage(textFactory, TextType.ERROR,
                                      localizer.getMessage("commands.base.error.no_permission_for_command"));
            return CompletableFuture.completedFuture(true);
        }
        if (!isPlayer && !availableForNonPlayers())
        {
            log.atFine().log("Command not allowed for non-players: %s", this);
            commandSender.sendMessage(textFactory, TextType.ERROR,
                                      localizer.getMessage("commands.base.error.only_available_for_players"));
            return CompletableFuture.completedFuture(true);
        }

        // We return true in case of an exception, because it cannot (should not?) be possible
        // for an exception to be caused be the command sender.
        return startExecution().exceptionally(
            throwable ->
            {
                log.atSevere().withCause(throwable).log("Failed to execute command: %s", this);
                if (commandSender.isPlayer())
                    commandSender.sendMessage(textFactory, TextType.ERROR,
                                              localizer.getMessage("commands.base.error.generic"));
                return true;
            });
    }

    /**
     * Starts the execution of this command. It performs the permission check (See {@link #hasPermission()}) and runs
     * {@link #executeCommand(PermissionsStatus)} if the {@link ICommandSender} has access to either the user or the
     * admin permission.
     *
     * @return True if the command could be executed successfully or if the command execution failed through no fault of
     * the {@link ICommandSender}.
     */
    protected final CompletableFuture<Boolean> startExecution()
    {
        return hasPermission().thenApplyAsync(this::handlePermissionResult);
    }

    private boolean handlePermissionResult(PermissionsStatus permissionResult)
    {
        if (!permissionResult.hasAnyPermission())
        {
            log.atFine().log("Permission for command: %s: %s", this, permissionResult);
            commandSender.sendMessage(textFactory, TextType.ERROR,
                                      localizer.getMessage("commands.base.error.no_permission_for_command"));
            return true;
        }
        try
        {
            return executeCommand(permissionResult).get(30, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Encountered issue running command: " + this, e);
        }
    }

    /**
     * Executes the command. This method is only called if {@link #validInput()} and {@link #hasPermission()} are true.
     * <p>
     * Note that this method is called asynchronously.
     *
     * @param permissions
     *     Whether the {@link ICommandSender} has user and/or admin permissions.
     * @return True if the method execution was successful.
     */
    protected abstract CompletableFuture<Boolean> executeCommand(PermissionsStatus permissions);

    /**
     * Ensures the command is logged.
     */
    private void log()
    {
        log.atFinest().log("Running command %s: %s", getCommand().getName(), this);
    }

    /**
     * Attempts to get an {@link MovableBase} based on the provided {@link MovableRetrieverFactory} and the current
     * {@link ICommandSender}.
     * <p>
     * If no movable is found, the {@link ICommandSender} will be informed.
     *
     * @param doorRetriever
     *     The {@link MovableRetrieverFactory} to use
     * @return The {@link MovableBase} if one could be retrieved.
     */
    protected CompletableFuture<Optional<AbstractMovable>> getMovable(MovableRetriever doorRetriever)
    {
        return commandSender.getPlayer().map(doorRetriever::getMovableInteractive)
                            .orElseGet(doorRetriever::getMovable).thenApplyAsync(
                movable ->
                {
                    log.atFine().log("Retrieved movable %s for command: %s", movable, this);
                    if (movable.isPresent())
                        return movable;
                    commandSender.sendMessage(textFactory, TextType.ERROR,
                                              localizer.getMessage("commands.base.error.cannot_find_target_movable"));
                    return Optional.empty();
                });
    }

    /**
     * Checks if the {@link ICommandSender} has the required permissions to use this command. See
     * {@link CommandDefinition#getUserPermission()} and {@link CommandDefinition#getAdminPermission()}.
     *
     * @return A pair of booleans that indicates whether the user has access to the user and admin permission nodes
     * respectively. For both, true indicates that they do have access to the node and false that they do not.
     */
    protected CompletableFuture<PermissionsStatus> hasPermission()
    {
        return commandSender.hasPermission(getCommand());
    }
}

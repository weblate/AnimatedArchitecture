package nl.pim16aap2.bigdoors.doors;

import com.google.common.base.Preconditions;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IMainThreadExecutor;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.util.DoorToggleResult;
import nl.pim16aap2.bigdoors.util.PLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents the class that can open, close, and toggle doors.
 *
 * @author Pim
 */
public final class DoorOpener
{
    @Nullable
    private static DoorOpener instance;
    @NotNull
    private final PLogger pLogger;

    private DoorOpener(final @NotNull PLogger pLogger)
    {
        this.pLogger = pLogger;
    }

    /**
     * Initializes the {@link DoorOpener}. If it has already been initialized, it'll return that instance instead.
     *
     * @param pLogger The logger used for error logging.
     * @return The instance of this {@link DoorOpener}.
     */
    @NotNull
    public static DoorOpener init(final @NotNull PLogger pLogger)
    {
        return (instance == null) ? instance = new DoorOpener(pLogger) : instance;
    }

    /**
     * Gets the instance of the {@link DoorOpener} if it exists.
     *
     * @return The instance of the {@link DoorOpener}.
     */
    @NotNull
    public static DoorOpener get()
    {
        Preconditions.checkState(instance != null,
                                 "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    /**
     * Opens a door asynchronously; If it is currently closed, it will be opened. Otherwise nothing happens.
     *
     * @param futureDoor  The door to toggle.
     * @param cause       What caused this action.
     * @param initiator   The player that initiated the DoorAction.
     * @param time        The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                    limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @return The future result of the opening (will be available before the door starts its animation).
     */
    @NotNull
    public CompletableFuture<DoorToggleResult> openDoor(final @NotNull CompletableFuture<Optional<DoorBase>> futureDoor,
                                                        final @NotNull DoorActionCause cause,
                                                        final @Nullable UUID initiator, final double time,
                                                        final boolean instantOpen)
    {
        return animateDoorSync(futureDoor, cause, initiator, time, instantOpen, DoorActionType.OPEN);
    }

    /**
     * Closes a door asynchronously; If it is currently open, it will be closed. Otherwise nothing happens.
     *
     * @param futureDoor  The door to toggle.
     * @param cause       What caused this action.
     * @param initiator   The player that initiated the DoorAction.
     * @param time        The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                    limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @return The future result of the closing (will be available before the door starts its animation).
     */
    @NotNull
    public CompletableFuture<DoorToggleResult> closeDoor(
        final @NotNull CompletableFuture<Optional<DoorBase>> futureDoor, final @NotNull DoorActionCause cause,
        final @Nullable UUID initiator, final double time, final boolean instantOpen)
    {
        return animateDoorSync(futureDoor, cause, initiator, time, instantOpen, DoorActionType.CLOSE);
    }

    /**
     * Toggles a door asynchronously; If it is currently open, it will be closed. If it is currently closed, it will be
     * opened.
     *
     * @param futureDoor  The door to toggle.
     * @param cause       What caused this action.
     * @param initiator   The player that initiated the DoorAction.
     * @param time        The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                    limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @return The future result of the toggle (will be available before the door starts its animation).
     */
    @NotNull
    public CompletableFuture<DoorToggleResult> toggleDoor(
        final @NotNull CompletableFuture<Optional<DoorBase>> futureDoor, final @NotNull DoorActionCause cause,
        final @Nullable UUID initiator, final double time, final boolean instantOpen)
    {
        return animateDoorSync(futureDoor, cause, initiator, time, instantOpen, DoorActionType.TOGGLE);
    }

    /**
     * Toggles, opens, or closes a door asynchronously.
     *
     * @param futureDoor     The door to toggle.
     * @param cause          What caused this action.
     * @param initiator      The player that initiated the DoorAction.
     * @param time           The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                       limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen    If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @param doorActionType Whether the door should be toggled, opened, or closed.
     * @return The future result of the toggle (will be available before the door starts its animation).
     */
    @NotNull
    public CompletableFuture<DoorToggleResult> animateDoorSync(
        final @NotNull CompletableFuture<Optional<DoorBase>> futureDoor, final @NotNull DoorActionCause cause,
        final @Nullable UUID initiator, final double time, final boolean instantOpen,
        final @NotNull DoorActionType doorActionType)
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                Optional<DoorBase> optionalDoor;
                try
                {
                    optionalDoor = futureDoor.get();
                }
                catch (InterruptedException | ExecutionException e)
                {
                    pLogger.logException(e);
                    optionalDoor = Optional.empty();
                }
                return optionalDoor.map(
                    (door) -> animateDoorOnMainThread(door, cause, initiator, time, instantOpen, doorActionType))
                                   .orElse(DoorToggleResult.ERROR);
            });
    }

    /**
     * Initiates a door animation on the main thread. May be called from any thread.
     *
     * @param door           The door.
     * @param cause          What caused this action.
     * @param initiator      The player that initiated the DoorAction.
     * @param time           The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                       limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen    If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @param doorActionType Whether the door should be toggled, opened, or closed.
     * @return The result of the animation attempt.
     */
    private DoorToggleResult animateDoorOnMainThread(final @NotNull DoorBase door, final @NotNull DoorActionCause cause,
                                                     @Nullable UUID initiator, final double time, boolean instantOpen,
                                                     final @NotNull DoorActionType doorActionType)
    {
        IMainThreadExecutor<DoorToggleResult> mainThreadExecutor = BigDoors.newMainThreadExecutor();

        DoorToggleResult result = mainThreadExecutor
            .supplyOnMainThread(() -> animateDoorSync(door, cause, initiator, time,
                                                      instantOpen, doorActionType));
        return result == null ? DoorToggleResult.ERROR : result;
    }

    /**
     * Attempts to toggle, open, or close a door. Must only be called from the main thread!
     *
     * @param door        The door.
     * @param cause       What caused this action.
     * @param initiator   The player that initiated the DoorAction.
     * @param time        The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                    limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @return The result of the attempt.
     */
    @NotNull
    public DoorToggleResult animateDoor(final @NotNull DoorBase door, final @NotNull DoorActionCause cause,
                                        @Nullable UUID initiator, final double time, boolean instantOpen,
                                        final @NotNull DoorActionType doorActionType)
    {
        return animateDoorOnMainThread(door, cause, initiator, time, instantOpen, doorActionType);
    }

    /**
     * Attempts to toggle, open, or close a door. Must only be called from the main thread!
     *
     * @param door        The door.
     * @param cause       What caused this action.
     * @param initiator   The player that initiated the DoorAction.
     * @param time        The amount of time this {@link DoorBase} will try to use to move. The maximum speed is
     *                    limited, so at a certain point lower values will not increase door speed.
     * @param instantOpen If the {@link DoorBase} should be opened instantly (i.e. skip animation) or not.
     * @return The result of the attempt.
     */
    @NotNull
    private DoorToggleResult animateDoorSync(final @NotNull DoorBase door, final @NotNull DoorActionCause cause,
                                             @Nullable UUID initiator, final double time, boolean instantOpen,
                                             final @NotNull DoorActionType doorActionType)
    {
        if (!BigDoors.onMainThread(Thread.currentThread().getId()))
        {
            BigDoors.compareThreads(Thread.currentThread().getId());
            pLogger.logException(new IllegalThreadStateException("Doors can only be animated on the main thread!"));
            return DoorToggleResult.ERROR;
        }

        if (initiator == null)
            initiator = door.getPlayerUUID();

        if (doorActionType == DoorActionType.OPEN)
            return door.open(cause, initiator, time, instantOpen);
        if (doorActionType == DoorActionType.CLOSE)
            return door.close(cause, initiator, time, instantOpen);
        if (doorActionType == DoorActionType.TOGGLE)
            return door.toggle(cause, initiator, time, instantOpen);
        return DoorToggleResult.ERROR;
    }
}

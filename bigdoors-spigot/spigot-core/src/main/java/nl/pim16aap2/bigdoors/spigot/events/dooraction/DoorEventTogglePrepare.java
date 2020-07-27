package nl.pim16aap2.bigdoors.spigot.events.dooraction;

import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.events.dooraction.IDoorEventTogglePrepare;
import nl.pim16aap2.bigdoors.util.vector.IVector3DiConst;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Implementation of {@link IDoorEventTogglePrepare} for the Spigot platform.
 *
 * @author Pim
 */
public class DoorEventTogglePrepare extends DoorEventToggleStart implements IDoorEventTogglePrepare
{
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /** {@inheritDoc} */
    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    private boolean isCancelled = false;

    /**
     * Constructs a door action event.
     *
     * @param door             The door.
     * @param cause            What caused the action.
     * @param actionType       The type of action.
     * @param responsible      Who is responsible for this door. If null, the door's owner will be used.
     * @param time             The number of seconds the door will take to open. Note that there are other factors that
     *                         affect the total time as well.
     * @param animationSkipped If true, the door will skip the animation and open instantly.
     * @param newMinimum       The new minimum coordinates of the door after the toggle.
     * @param newMaximum       The new maximum coordinates of the door after the toggle.
     */
    public DoorEventTogglePrepare(final @NotNull AbstractDoorBase door, final @NotNull DoorActionCause cause,
                                  final @NotNull DoorActionType actionType,
                                  final @NotNull Optional<IPPlayer> responsible, final double time,
                                  final boolean animationSkipped, final @NotNull IVector3DiConst newMinimum,
                                  final @NotNull IVector3DiConst newMaximum)
    {
        super(door, cause, actionType, responsible, time, animationSkipped, newMinimum, newMaximum);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public HandlerList getHandlers()
    {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS_LIST;
    }
}

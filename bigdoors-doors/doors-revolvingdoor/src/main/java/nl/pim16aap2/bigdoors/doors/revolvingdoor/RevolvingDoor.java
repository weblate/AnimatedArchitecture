package nl.pim16aap2.bigdoors.doors.revolvingdoor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.pim16aap2.bigdoors.api.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IStationaryDoorArchetype;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.api.util.CuboidConst;
import nl.pim16aap2.bigdoors.api.util.RotateDirection;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Revolving Door doorType.
 *
 * @author Pim
 * @see AbstractDoorBase
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RevolvingDoor extends AbstractDoorBase implements IStationaryDoorArchetype
{
    private static final @NotNull DoorType DOOR_TYPE = DoorTypeRevolvingDoor.get();

    /**
     * The number of quarter circles (so 90 degree rotations) this door will make before stopping.
     *
     * @return The number of quarter circles this door will rotate.
     */
    @Getter
    @Setter
    @PersistentVariable
    private int quarterCircles;

    public RevolvingDoor(final @NotNull DoorData doorData, final int quarterCircles)
    {
        super(doorData);
        this.quarterCircles = quarterCircles;
    }

    public RevolvingDoor(final @NotNull DoorData doorData)
    {
        this(doorData, 1);
    }

    @Override
    public @NotNull DoorType getDoorType()
    {
        return DOOR_TYPE;
    }

    @Override
    public synchronized @NotNull RotateDirection getCurrentToggleDir()
    {
        return getOpenDir();
    }

    @Override
    public @NotNull RotateDirection cycleOpenDirection()
    {
        return getOpenDir().equals(RotateDirection.CLOCKWISE) ?
               RotateDirection.COUNTERCLOCKWISE : RotateDirection.CLOCKWISE;
    }

    @Override
    protected @NotNull BlockMover constructBlockMover(final @NotNull DoorActionCause cause, final double time,
                                                      final boolean skipAnimation, final @NotNull CuboidConst newCuboid,
                                                      final @NotNull IPPlayer responsible,
                                                      final @NotNull DoorActionType actionType)
        throws Exception
    {
        // TODO: Get rid of this.
        double fixedTime = time < 0.5 ? 5 : time;

        return new RevolvingDoorMover(this, fixedTime, DoorOpeningUtility.getMultiplier(this), getCurrentToggleDir(),
                                      responsible, quarterCircles, cause, newCuboid, actionType);
    }
}

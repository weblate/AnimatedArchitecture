package nl.pim16aap2.bigdoors.doors.drawbridge;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IHorizontalAxisAligned;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.ITimerToggleable;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.RotateDirection;

import java.util.Optional;

/**
 * Represents a DrawBrige doorType.
 *
 * @author Pim
 */
@EqualsAndHashCode(callSuper = true)
public class Drawbridge extends AbstractDoor implements IHorizontalAxisAligned, ITimerToggleable
{
    @EqualsAndHashCode.Exclude
    private static final DoorType DOOR_TYPE = DoorTypeDrawbridge.get();

    @Getter
    @Setter
    @PersistentVariable
    protected int autoCloseTime;

    @Getter
    @Setter
    @PersistentVariable
    protected int autoOpenTime;

    /**
     * Describes if this drawbridge's vertical position points (when taking the engine Y value as center) up <b>(=
     * TRUE)</b> or down <b>(= FALSE)</b>
     *
     * @return True if this {@link Drawbridge}'s vertical stance points up.
     */
    @Getter
    @PersistentVariable
    protected boolean modeUp;

    public Drawbridge(DoorBase doorBase, int autoCloseTime, int autoOpenTime, boolean modeUp)
    {
        super(doorBase);
        this.autoOpenTime = autoOpenTime;
        this.autoCloseTime = autoCloseTime;
        this.modeUp = modeUp;
    }

    public Drawbridge(DoorBase doorBase, boolean modeUp)
    {
        this(doorBase, -1, -1, modeUp);
    }

    @SuppressWarnings("unused")
    private Drawbridge(DoorBase doorBase)
    {
        this(doorBase, false); // Add tmp/default values
    }

    @Override
    public DoorType getDoorType()
    {
        return DOOR_TYPE;
    }

    @Override
    public synchronized RotateDirection getCurrentToggleDir()
    {
        return isOpen() ? RotateDirection.getOpposite(getOpenDir()) : getOpenDir();
    }

    @Override
    public synchronized Optional<Cuboid> getPotentialNewCoordinates()
    {
        final RotateDirection rotateDirection = getCurrentToggleDir();
        final double angle;
        if (rotateDirection == RotateDirection.NORTH || rotateDirection == RotateDirection.WEST)
            angle = -Math.PI / 2;
        else if (rotateDirection == RotateDirection.SOUTH || rotateDirection == RotateDirection.EAST)
            angle = Math.PI / 2;
        else
        {
            BigDoors.get().getPLogger()
                    .severe("Invalid open direction \"" + rotateDirection.name() + "\" for door: " + getDoorUID());
            return Optional.empty();
        }

        final Cuboid cuboid = getCuboid();
        if (rotateDirection == RotateDirection.NORTH || rotateDirection == RotateDirection.SOUTH)
            return Optional.of(cuboid.updatePositions(vec -> vec.rotateAroundXAxis(getEngine(), angle)));
        else
            return Optional.of(cuboid.updatePositions(vec -> vec.rotateAroundZAxis(getEngine(), angle)));
    }

    @Override
    protected BlockMover constructBlockMover(DoorActionCause cause, double time, boolean skipAnimation,
                                             Cuboid newCuboid, IPPlayer responsible, DoorActionType actionType)
        throws Exception
    {
        return new BridgeMover<>(time, this, getCurrentToggleDir(), skipAnimation,
                                 DoorOpeningUtility.getMultiplier(this), responsible, newCuboid, cause, actionType);
    }

    @Override
    public boolean isNorthSouthAligned()
    {
        final RotateDirection openDir = getOpenDir();
        return openDir == RotateDirection.NORTH || openDir == RotateDirection.SOUTH;
    }
}

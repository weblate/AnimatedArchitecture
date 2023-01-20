package nl.pim16aap2.bigdoors.movable.portcullis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Locked;
import nl.pim16aap2.bigdoors.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.events.movableaction.MovableActionCause;
import nl.pim16aap2.bigdoors.events.movableaction.MovableActionType;
import nl.pim16aap2.bigdoors.movable.AbstractMovable;
import nl.pim16aap2.bigdoors.movable.MovableBase;
import nl.pim16aap2.bigdoors.movable.MovableSnapshot;
import nl.pim16aap2.bigdoors.movable.movablearchetypes.IDiscreteMovement;
import nl.pim16aap2.bigdoors.movable.movablearchetypes.ITimerToggleable;
import nl.pim16aap2.bigdoors.movabletypes.MovableType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;

import javax.annotation.concurrent.GuardedBy;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Portcullis movable type.
 *
 * @author Pim
 * @see MovableBase
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Portcullis extends AbstractMovable implements IDiscreteMovement, ITimerToggleable
{
    private static final MovableType MOVABLE_TYPE = MovableTypePortcullis.get();

    @EqualsAndHashCode.Exclude
    private final ReentrantReadWriteLock lock;

    @PersistentVariable
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read)
    @Setter(onMethod_ = @Locked.Write)
    protected int blocksToMove;

    @PersistentVariable
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read)
    @Setter(onMethod_ = @Locked.Write)
    protected int autoCloseTime;

    @PersistentVariable
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read)
    @Setter(onMethod_ = @Locked.Write)
    protected int autoOpenTime;

    public Portcullis(MovableBase base, int blocksToMove, int autoCloseTime, int autoOpenTime)
    {
        super(base);
        this.lock = getLock();
        this.blocksToMove = blocksToMove;
        this.autoCloseTime = autoCloseTime;
        this.autoOpenTime = autoOpenTime;
    }

    public Portcullis(MovableBase base, int blocksToMove)
    {
        this(base, blocksToMove, -1, -1);
    }

    @SuppressWarnings("unused")
    private Portcullis(MovableBase base)
    {
        this(base, -1); // Add tmp/default values
    }

    @Override
    @Locked.Read
    protected double getLongestAnimationCycleDistance()
    {
        return blocksToMove;
    }

    @Override
    protected double calculateAnimationTime(double target)
    {
        return super.calculateAnimationTime(target + (isCurrentToggleDirUp() ? -0.2D : 0.2D));
    }

    @Override
    public Cuboid getAnimationRange()
    {
        final Cuboid cuboid = getCuboid();
        final Vector3Di min = cuboid.getMin();
        final Vector3Di max = cuboid.getMax();

        return new Cuboid(min.add(0, -getBlocksToMove(), 0), max.add(0, getBlocksToMove(), 0));
    }

    @Override
    public MovableType getType()
    {
        return MOVABLE_TYPE;
    }

    @Override
    @Locked.Read
    public RotateDirection getCurrentToggleDir()
    {
        return isOpen() ? getOpenDir() : RotateDirection.getOpposite(getOpenDir());
    }

    @Override
    public Optional<Cuboid> getPotentialNewCoordinates()
    {
        return Optional.of(getCuboid().move(0, getDirectedBlocksToMove(), 0));
    }

    /**
     * @return True if the current toggle dir goes up.
     */
    private boolean isCurrentToggleDirUp()
    {
        return getCurrentToggleDir() == RotateDirection.UP;
    }

    /**
     * @return The signed number of blocks to move (positive for up, negative for down).
     */
    private int getDirectedBlocksToMove()
    {
        return isCurrentToggleDirUp() ? getBlocksToMove() : -getBlocksToMove();
    }

    @Override
    @Locked.Read
    protected BlockMover constructBlockMover(
        BlockMover.Context context, MovableSnapshot movableSnapshot, MovableActionCause cause, double time,
        boolean skipAnimation, Cuboid newCuboid, IPPlayer responsible, MovableActionType actionType)
        throws Exception
    {
        return new VerticalMover(
            context, this, movableSnapshot, time, skipAnimation, getDirectedBlocksToMove(), responsible, newCuboid,
            getAnimationRange(), cause, actionType);
    }
}

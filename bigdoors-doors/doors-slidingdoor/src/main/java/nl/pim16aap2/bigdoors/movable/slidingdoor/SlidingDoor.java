package nl.pim16aap2.bigdoors.movable.slidingdoor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Locked;
import nl.pim16aap2.bigdoors.movable.AbstractMovable;
import nl.pim16aap2.bigdoors.movable.movablearchetypes.IDiscreteMovement;
import nl.pim16aap2.bigdoors.movable.serialization.DeserializationConstructor;
import nl.pim16aap2.bigdoors.movable.serialization.PersistentVariable;
import nl.pim16aap2.bigdoors.movabletypes.MovableType;
import nl.pim16aap2.bigdoors.moveblocks.IAnimationComponent;
import nl.pim16aap2.bigdoors.moveblocks.MovementRequestData;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.MovementDirection;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.Rectangle;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;

import javax.annotation.concurrent.GuardedBy;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Sliding Door movable type.
 *
 * @author Pim
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SlidingDoor extends AbstractMovable implements IDiscreteMovement
{
    private static final MovableType MOVABLE_TYPE = MovableSlidingDoor.get();

    @EqualsAndHashCode.Exclude
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ReentrantReadWriteLock lock;

    @PersistentVariable("blocksToMove")
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read)
    protected int blocksToMove;

    @DeserializationConstructor
    public SlidingDoor(AbstractMovable.MovableBaseHolder base, @PersistentVariable("blocksToMove") int blocksToMove)
    {
        super(base);
        this.lock = getLock();
        this.blocksToMove = blocksToMove;
    }

    @Override
    public MovableType getType()
    {
        return MOVABLE_TYPE;
    }

    @Override
    @Locked.Read
    protected double calculateAnimationCycleDistance()
    {
        return blocksToMove;
    }

    @Override
    @Locked.Read
    protected Rectangle calculateAnimationRange()
    {
        final Cuboid cuboid = getCuboid();
        final Vector3Di min = cuboid.getMin();
        final Vector3Di max = cuboid.getMax();

        final Cuboid cuboidRange = switch (getCurrentToggleDir())
            {
                case NORTH -> new Cuboid(min.add(0, 0, -blocksToMove), max.add(0, 0, 0)); // -z
                case EAST -> new Cuboid(min.add(0, 0, 0), max.add(blocksToMove, 0, 0)); // +x
                case SOUTH -> new Cuboid(min.add(0, 0, 0), max.add(0, 0, blocksToMove)); // +z
                case WEST -> new Cuboid(min.add(-blocksToMove, 0, 0), max.add(0, 0, 0)); // -x
                default -> cuboid.grow(blocksToMove, 0, blocksToMove);
            };
        return cuboidRange.asFlatRectangle();
    }

    @Override
    public boolean canSkipAnimation()
    {
        return true;
    }

    @Override
    public MovementDirection cycleOpenDirection()
    {
        final MovementDirection openDirection = getOpenDir();
        return openDirection.equals(MovementDirection.NORTH) ? MovementDirection.EAST :
               openDirection.equals(MovementDirection.EAST) ? MovementDirection.SOUTH :
               openDirection.equals(MovementDirection.SOUTH) ? MovementDirection.WEST : MovementDirection.NORTH;
    }

    @Override
    @Locked.Read
    public MovementDirection getCurrentToggleDir()
    {
        return isOpen() ? getOpenDir() : MovementDirection.getOpposite(getOpenDir());
    }

    @Override
    @Locked.Read
    public Optional<Cuboid> getPotentialNewCoordinates()
    {
        final Vector3Di vec = PBlockFace.getDirection(Util.getPBlockFace(getCurrentToggleDir()));
        return Optional.of(getCuboid().move(getBlocksToMove() * vec.x(), 0, getBlocksToMove() * vec.z()));
    }

    @Override
    @Locked.Read
    protected IAnimationComponent constructAnimationComponent(MovementRequestData data)
    {
        return new SlidingDoorAnimationComponent(data, getCurrentToggleDir(), getBlocksToMove());
    }

    @Override
    @Locked.Write
    public void setBlocksToMove(int blocksToMove)
    {
        this.blocksToMove = blocksToMove;
        super.invalidateAnimationData();
    }
}

package nl.pim16aap2.animatedarchitecture.structures.slidingdoor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Locked;
import nl.pim16aap2.animatedarchitecture.core.annotations.Deserialization;
import nl.pim16aap2.animatedarchitecture.core.annotations.PersistentVariable;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.IAnimationComponent;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.structurearchetypes.IDiscreteMovement;
import nl.pim16aap2.animatedarchitecture.core.util.BlockFace;
import nl.pim16aap2.animatedarchitecture.core.util.Cuboid;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.Rectangle;
import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;

import javax.annotation.concurrent.GuardedBy;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Sliding Door structure type.
 *
 * @author Pim
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SlidingDoor extends AbstractStructure implements IDiscreteMovement
{
    @EqualsAndHashCode.Exclude
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ReentrantReadWriteLock lock;

    @PersistentVariable(value = "blocksToMove")
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read)
    protected int blocksToMove;

    @Deserialization
    public SlidingDoor(BaseHolder base, @PersistentVariable(value = "blocksToMove") int blocksToMove)
    {
        super(base, StructureTypeSlidingDoor.get());
        this.lock = getLock();
        this.blocksToMove = blocksToMove;
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
        return isOpen() ? MovementDirection.getOpposite(getOpenDir()) : getOpenDir();
    }

    @Override
    @Locked.Read
    public Optional<Cuboid> getPotentialNewCoordinates()
    {
        final Vector3Di vec = BlockFace.getDirection(Util.getBlockFace(getCurrentToggleDir()));
        return Optional.of(getCuboid().move(getBlocksToMove() * vec.x(), 0, getBlocksToMove() * vec.z()));
    }

    @Override
    @Locked.Read
    protected IAnimationComponent constructAnimationComponent(AnimationRequestData data)
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

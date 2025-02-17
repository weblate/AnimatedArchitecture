package nl.pim16aap2.animatedarchitecture.structures.windmill;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Locked;
import nl.pim16aap2.animatedarchitecture.core.annotations.Deserialization;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.IAnimationComponent;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.structurearchetypes.IHorizontalAxisAligned;
import nl.pim16aap2.animatedarchitecture.core.structures.structurearchetypes.IPerpetualMover;
import nl.pim16aap2.animatedarchitecture.core.util.Cuboid;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.Rectangle;
import nl.pim16aap2.animatedarchitecture.structures.drawbridge.Drawbridge;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Windmill structure type.
 *
 * @author Pim
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Windmill extends AbstractStructure implements IHorizontalAxisAligned, IPerpetualMover
{
    @EqualsAndHashCode.Exclude
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ReentrantReadWriteLock lock;

    @Deserialization
    public Windmill(BaseHolder base)
    {
        super(base, StructureTypeWindmill.get());
        this.lock = getLock();
    }

    @Override
    public boolean canSkipAnimation()
    {
        return false;
    }

    @Override
    public Optional<Cuboid> getPotentialNewCoordinates()
    {
        return Optional.of(getCuboid());
    }

    @Override
    public MovementDirection getCurrentToggleDir()
    {
        return getOpenDir();
    }

    @Override
    public boolean isNorthSouthAnimated()
    {
        final MovementDirection openDir = getOpenDir();
        return openDir == MovementDirection.NORTH || openDir == MovementDirection.SOUTH;
    }

    @Override
    @Locked.Read
    protected double calculateAnimationCycleDistance()
    {
        return Drawbridge.getMaxRadius(isNorthSouthAnimated(), getCuboid(), getRotationPoint()) * Math.TAU;
    }

    @Override
    @Locked.Read
    protected Rectangle calculateAnimationRange()
    {
        final double maxRadius = Drawbridge.getMaxRadius(isNorthSouthAnimated(), getCuboid(), getRotationPoint());
        return Drawbridge.calculateAnimationRange(maxRadius, getCuboid());
    }

    @Override
    public MovementDirection cycleOpenDirection()
    {
        final MovementDirection openDir = getOpenDir();
        return openDir.equals(MovementDirection.NORTH) ? MovementDirection.EAST :
               openDir.equals(MovementDirection.EAST) ? MovementDirection.SOUTH :
               openDir.equals(MovementDirection.SOUTH) ? MovementDirection.WEST : MovementDirection.NORTH;
    }

    @Override
    @Locked.Read
    protected IAnimationComponent constructAnimationComponent(AnimationRequestData data)
    {
        return new WindmillAnimationComponent(data, getCurrentToggleDir(), isNorthSouthAnimated());
    }
}

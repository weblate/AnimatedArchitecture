package nl.pim16aap2.animatedarchitecture.structures.garagedoor;

import lombok.extern.flogger.Flogger;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationUtil;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.Animator;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.IAnimator;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Dd;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;

import java.util.function.BiFunction;

/**
 * Represents a {@link Animator} for {@link GarageDoor}s.
 *
 * @author Pim
 */
@Flogger
public final class SectionalGarageDoorAnimationComponent extends CounterWeightGarageDoorAnimationComponent
{
    private final double resultHeight;
    private final BiFunction<IAnimatedBlock, Double, Vector3Dd> getVector;
    private final double step;

    public SectionalGarageDoorAnimationComponent(AnimationRequestData data, MovementDirection movementDirection)
    {
        super(data, movementDirection);

        resultHeight = oldCuboid.getMax().y() + 1.0;

        final BiFunction<IAnimatedBlock, Double, Vector3Dd> getVectorTmp;
        switch (movementDirection)
        {
            case NORTH -> getVectorTmp = this::getVectorDownNorth;
            case EAST -> getVectorTmp = this::getVectorDownEast;
            case SOUTH -> getVectorTmp = this::getVectorDownSouth;
            case WEST -> getVectorTmp = this::getVectorDownWest;
            default -> throw new IllegalStateException(
                "Failed to open garage door \"" + snapshot.getUid()
                    + "\". Reason: Invalid movement direction \"" +
                    movementDirection + "\"");
        }

        final Vector3Di dims = oldCuboid.getDimensions();
        final int blocksToMove;
        if (oldCuboid.getDimensions().y() > 1)
        {
            blocksToMove = dims.y();
            getVector = this::getVectorUp;
        }
        else
        {
            blocksToMove = dims.multiply(directionVec).absolute().sum();
            getVector = getVectorTmp;
        }

        final int animationDuration =
            AnimationUtil.getAnimationTicks(data.getAnimationTime(), data.getServerTickTime());
        step = (blocksToMove + 0.5f) / animationDuration;
    }

    private Vector3Dd getVectorUp(IAnimatedBlock animatedBlock, double stepSum)
    {
        final double currentHeight = Math.min(resultHeight, animatedBlock.getStartY() + stepSum);
        double xMod = 0;
        double yMod = stepSum;
        double zMod = 0;

        if (currentHeight >= oldCuboid.getMax().y())
        {
            final double horizontal = Math.max(0, stepSum - animatedBlock.getRadius() + 0.5);
            xMod = directionVec.x() * horizontal;
            yMod = Math.min(resultHeight - animatedBlock.getStartY(), stepSum);
            zMod = directionVec.z() * horizontal;
        }
        return new Vector3Dd(animatedBlock.getStartX() + xMod,
                             animatedBlock.getStartY() + yMod,
                             animatedBlock.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownNorth(IAnimatedBlock animatedBlock, double stepSum)
    {
        final double goalZ = snapshot.getRotationPoint().z();
        final double pivotZ = goalZ + 1.5;
        final double currentZ = Math.max(goalZ, animatedBlock.getStartZ() - stepSum);

        final double xMod = 0;
        double yMod = 0;
        double zMod = -stepSum;

        if (currentZ <= pivotZ)
        {
            yMod = -Math.max(0, stepSum - animatedBlock.getRadius() + 0.5);
            zMod = Math.max(goalZ - animatedBlock.getStartPosition().z() + 0.5, zMod);
        }

        return new Vector3Dd(animatedBlock.getStartX() + xMod,
                             animatedBlock.getStartY() + yMod,
                             animatedBlock.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownSouth(IAnimatedBlock animatedBlock, double stepSum)
    {
        final double goalZ = snapshot.getRotationPoint().z();
        final double pivotZ = goalZ - 1.5;
        final double currentZ = Math.min(goalZ, animatedBlock.getStartZ() + stepSum);

        final double xMod = 0;
        double yMod = 0;
        double zMod = stepSum;

        if (currentZ >= pivotZ)
        {
            yMod = -Math.max(0, stepSum - animatedBlock.getRadius() + 0.5);
            zMod = Math.min(goalZ - animatedBlock.getStartPosition().z() + 0.5, zMod);
        }
        return new Vector3Dd(animatedBlock.getStartX() + xMod,
                             animatedBlock.getStartY() + yMod,
                             animatedBlock.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownEast(IAnimatedBlock animatedBlock, double stepSum)
    {
        final double goalX = snapshot.getRotationPoint().x();
        final double pivotX = goalX - 1.5;
        final double currentX = Math.min(goalX, animatedBlock.getStartX() + stepSum);

        double xMod = stepSum;
        double yMod = 0;
        final double zMod = 0;

        if (currentX >= pivotX)
        {
            xMod = Math.min(goalX - animatedBlock.getStartPosition().x() + 0.5, xMod);
            yMod = -Math.max(0, stepSum - animatedBlock.getRadius() + 0.5);
        }
        return new Vector3Dd(animatedBlock.getStartX() + xMod,
                             animatedBlock.getStartY() + yMod,
                             animatedBlock.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownWest(IAnimatedBlock animatedBlock, double stepSum)
    {
        final double goalX = snapshot.getRotationPoint().x();
        final double pivotX = goalX + 1.5;
        final double currentX = Math.max(goalX, animatedBlock.getStartX() - stepSum);

        double xMod = -stepSum;
        double yMod = 0;
        final double zMod = 0;

        if (currentX <= pivotX)
        {
            xMod = Math.max(goalX - animatedBlock.getStartPosition().x() + 0.5, xMod);
            yMod = -Math.max(0, stepSum - animatedBlock.getRadius() + 0.5);
        }

        return new Vector3Dd(animatedBlock.getStartX() + xMod,
                             animatedBlock.getStartY() + yMod,
                             animatedBlock.getStartZ() + zMod);
    }

    @Override
    public void executeAnimationStep(IAnimator animator, int ticks, int ticksRemaining)
    {
        final double stepSum = step * ticks;

        for (final IAnimatedBlock animatedBlock : animator.getAnimatedBlocks())
            animator.applyMovement(animatedBlock, getVector.apply(animatedBlock, stepSum), ticksRemaining);
    }
}

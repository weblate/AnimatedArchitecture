package nl.pim16aap2.bigdoors.movable.windmill;

import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.movable.drawbridge.DrawbridgeAnimationComponent;
import nl.pim16aap2.bigdoors.moveblocks.Animator;
import nl.pim16aap2.bigdoors.moveblocks.IAnimator;
import nl.pim16aap2.bigdoors.moveblocks.MovementRequestData;
import nl.pim16aap2.bigdoors.util.MathUtil;
import nl.pim16aap2.bigdoors.util.MovementDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.IVector3D;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;

/**
 * Represents a {@link Animator} for {@link Windmill}s.
 *
 * @author Pim
 */
public class WindmillAnimationComponent extends DrawbridgeAnimationComponent
{
    private final double step;

    public WindmillAnimationComponent(
        MovementRequestData data, MovementDirection movementDirection, boolean isNorthSouthAligned)
    {
        super(data, movementDirection, isNorthSouthAligned);

        step = MathUtil.HALF_PI / animationDuration;
    }

    @Override
    public Vector3Dd getFinalPosition(IVector3D startLocation, float radius)
    {
        return Vector3Dd.of(startLocation);
    }

    @Override
    public void executeAnimationStep(IAnimator animator, int ticks, int ticksRemaining)
    {
        final double stepSum = step * ticks;
        for (final IAnimatedBlock animatedBlock : animator.getAnimatedBlocks())
            animator.applyMovement(animatedBlock, getGoalPos(stepSum, animatedBlock), ticksRemaining);
    }

    @Override
    public float getRadius(int xAxis, int yAxis, int zAxis)
    {
        // Get the current radius of a block between used axis (either x and y, or z and y).
        // When the rotation point is positioned along the NS axis, the X values does not change for this type.
        final double deltaA = snapshot.getRotationPoint().yD() - yAxis;
        final double deltaB =
            northSouth ? (snapshot.getRotationPoint().z() - zAxis) :
            (snapshot.getRotationPoint().x() - xAxis);
        return (float) Math.sqrt(Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
    }

    @Override
    public float getStartAngle(int xAxis, int yAxis, int zAxis)
    {
        // Get the angle between the used axes (either x and y, or z and y).
        // When the rotation point is positioned along the NS axis, the X values does not change for this type.
        final double deltaA =
            northSouth ? snapshot.getRotationPoint().z() - zAxis :
            snapshot.getRotationPoint().x() - xAxis;
        final double deltaB = snapshot.getRotationPoint().yD() - yAxis;

        return (float) Util.clampAngleRad(Math.atan2(deltaA, deltaB));
    }
}

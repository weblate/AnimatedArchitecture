package nl.pim16aap2.bigdoors.movable.bigdoor;

import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.movable.AbstractMovable;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.moveblocks.MovementRequestData;
import nl.pim16aap2.bigdoors.util.MathUtil;
import nl.pim16aap2.bigdoors.util.MovementDirection;
import nl.pim16aap2.bigdoors.util.vector.IVector3D;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;

@Flogger
public class BigDoorMover extends BlockMover
{
    private final Vector3Dd rotationCenter;
    private final int halfEndCount;
    private final double angle;
    private final double step;

    public BigDoorMover(AbstractMovable movable, MovementRequestData data, MovementDirection movementDirection)
        throws Exception
    {
        super(movable, data, movementDirection);

        angle = movementDirection == MovementDirection.CLOCKWISE ? MathUtil.HALF_PI :
                movementDirection == MovementDirection.COUNTERCLOCKWISE ? -MathUtil.HALF_PI : 0.0D;

        if (angle == 0.0D)
            log.atSevere()
               .log("Invalid open direction '%s' for movable: %d", movementDirection.name(), getMovableUID());

        rotationCenter = new Vector3Dd(
            snapshot.getRotationPoint().x() + 0.5, oldCuboid.getMin().y(),
            snapshot.getRotationPoint().z() + 0.5);

        step = angle / super.animationDuration;
        halfEndCount = super.animationDuration / 2;
    }

    @Override
    protected Vector3Dd getFinalPosition(IVector3D startLocation, float radius)
    {
        return getGoalPos(angle, startLocation.xD(), startLocation.yD(), startLocation.zD());
    }

    @Override
    protected void executeAnimationStep(int ticks, int ticksRemaining)
    {
        if (ticks == halfEndCount)
            applyRotation();

        final double stepSum = step * ticks;
        final double cos = Math.cos(stepSum);
        final double sin = Math.sin(stepSum);

        for (final IAnimatedBlock animatedBlock : getAnimatedBlocks())
            applyMovement(animatedBlock, getGoalPos(animatedBlock, cos, sin), ticksRemaining);
    }

    private Vector3Dd getGoalPos(double cos, double sin, double startX, double startY, double startZ)
    {
        final double translatedX = startX - rotationCenter.x();
        final double translatedZ = startZ - rotationCenter.z();

        final double changeX = translatedX * cos - translatedZ * sin;
        final double changeZ = translatedX * sin + translatedZ * cos;

        return new Vector3Dd(rotationCenter.x() + changeX, startY, rotationCenter.z() + changeZ);
    }

    private Vector3Dd getGoalPos(double angle, double startX, double startY, double startZ)
    {
        return getGoalPos(Math.cos(angle), Math.sin(angle), startX, startY, startZ);
    }

    private Vector3Dd getGoalPos(IAnimatedBlock animatedBlock, double cos, double sin)
    {
        return getGoalPos(cos, sin, animatedBlock.getStartX(), animatedBlock.getStartY(), animatedBlock.getStartZ());
    }

    static float getRadius(IVector3D rotationPoint, int xAxis, int zAxis)
    {
        final double deltaA = rotationPoint.xD() - xAxis;
        final double deltaB = rotationPoint.zD() - zAxis;
        return (float) Math.sqrt(Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
    }

    @Override
    protected float getRadius(int xAxis, int yAxis, int zAxis)
    {
        return getRadius(snapshot.getRotationPoint(), xAxis, zAxis);
    }

    @Override
    protected float getStartAngle(int xAxis, int yAxis, int zAxis)
    {
        return (float) Math.atan2(snapshot.getRotationPoint().xD() - xAxis,
                                  snapshot.getRotationPoint().zD() - zAxis);
    }
}

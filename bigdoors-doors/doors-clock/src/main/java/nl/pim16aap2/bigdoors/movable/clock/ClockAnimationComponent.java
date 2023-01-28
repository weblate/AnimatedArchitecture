package nl.pim16aap2.bigdoors.movable.clock;

import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.movable.windmill.WindmillAnimationComponent;
import nl.pim16aap2.bigdoors.moveblocks.Animator;
import nl.pim16aap2.bigdoors.moveblocks.IAnimator;
import nl.pim16aap2.bigdoors.moveblocks.MovementRequestData;
import nl.pim16aap2.bigdoors.util.MathUtil;
import nl.pim16aap2.bigdoors.util.MovementDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.WorldTime;
import nl.pim16aap2.bigdoors.util.vector.IVector3D;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;

import java.util.function.Function;

/**
 * Represents a {@link Animator} for {@link Clock}s.
 *
 * @author Pim
 */
public final class ClockAnimationComponent extends WindmillAnimationComponent
{
    /**
     * Method to determine if a given {@link IAnimatedBlock} is part of the little hand or the big hand of a clock.
     * Represented as a {@link Function} because TODO: Finish this sentence  ?? wut ??
     */
    private final Function<IAnimatedBlock, Boolean> isHourArm;

    /**
     * The step of 1 minute on a clock, or 1/60th of a circle in radians.
     */
    private static final float MINUTE_STEP = (float) Math.PI / 30;

    /**
     * The step of 1 hour on a clock, or 1/12th of a circle in radians.
     */
    private static final float HOUR_STEP = (float) Math.PI / 6;

    /**
     * The step of 1 minute between two full ours on a clock, or 1/720th of a circle in radians.
     */
    private static final float HOUR_SUB_STEP = (float) Math.PI / 360;

    /**
     * This value should be either 1 or -1. It is used to change the sign of the angle based on which way the clock
     * should rotate.
     */
    private final int angleDirectionMultiplier;

    public ClockAnimationComponent(
        MovementRequestData data, MovementDirection movementDirection, boolean isNorthSouthAligned)
    {
        super(data, movementDirection, isNorthSouthAligned);

        isHourArm = northSouth ? this::isHourArmNS : this::isHourArmEW;
        angleDirectionMultiplier =
            (movementDirection == MovementDirection.EAST || movementDirection == MovementDirection.SOUTH) ? -1 : 1;
    }

    @Override
    public Animator.MovementMethod getMovementMethod()
    {
        return Animator.MovementMethod.TELEPORT;
    }

    /**
     * Checks is a given {@link IAnimatedBlock} is the hour arm or the minute arm.
     *
     * @return True if the block is part of the hour arm.
     */
    private boolean isHourArmNS(IAnimatedBlock animatedBlock)
    {
        return ((int) animatedBlock.getPosition().z()) == snapshot.getRotationPoint().z();
    }

    /**
     * Checks is a given {@link IAnimatedBlock} is the hour arm or the minute arm.
     *
     * @return True if the block is part of the hour arm.
     */
    private boolean isHourArmEW(IAnimatedBlock animatedBlock)
    {
        return ((int) animatedBlock.getPosition().x()) == snapshot.getRotationPoint().x();
    }

    @Override
    public Vector3Dd getFinalPosition(IVector3D startLocation, float radius)
    {
        return Vector3Dd.of(startLocation);
    }

    @Override
    public void executeAnimationStep(IAnimator animator, int ticks, int ticksRemaining)
    {
        final WorldTime worldTime = snapshot.getWorld().getTime();

        final double hourAngle = angleDirectionMultiplier *
            ClockAnimationComponent.hoursToAngle(worldTime.getHours(), worldTime.getMinutes());

        final double minuteAngle =
            angleDirectionMultiplier * ClockAnimationComponent.minutesToAngle(worldTime.getMinutes());

        // Move the hour arm at a lower tickRate than the minute arm.
        final boolean moveHourArm = ticks % 10 == 0;

        for (final IAnimatedBlock animatedBlock : animator.getAnimatedBlocks())
            if (Math.abs(animatedBlock.getRadius()) > MathUtil.EPS)
            {
                // Move the little hand at a lower interval than the big hand.
                // TODO: Just store the hour and minute arms separately.
                final boolean hourArm = isHourArm.apply(animatedBlock);
                if (!moveHourArm && hourArm)
                    continue;

                final double timeAngle = hourArm ? hourAngle : minuteAngle;
                animator.applyMovement(animatedBlock, getGoalPos(timeAngle, animatedBlock), ticksRemaining);
            }
    }

    /**
     * Converts a time in minutes (60 per circle) to an angle in radians, with 0 minutes pointing up, and 30 minutes
     * pointing down.
     *
     * @param minutes
     *     The time in minutes since the last full hour.
     * @return The angle.
     */
    private static float minutesToAngle(int minutes)
    {
        return (float) Util.clampAngleRad(-minutes * MINUTE_STEP);
    }

    /**
     * Converts a time in hours (12 per circle) to an angle in radians, with 0, 12 hours pointing up, and 6, 18 hours
     * pointing down.
     *
     * @param hours
     *     The time in hours.
     * @param minutes
     *     The time in minutes since the last full hour.
     * @return The angle.
     */
    private static float hoursToAngle(int hours, int minutes)
    {
        return (float) Util.clampAngleRad(-hours * HOUR_STEP - minutes * HOUR_SUB_STEP);
    }
}

package nl.pim16aap2.bigdoors.doors.slidingdoor;

import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PSound;
import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.PSoundDescription;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link BlockMover} for {@link SlidingDoor}.
 *
 * @author Pim
 */
public class SlidingMover extends BlockMover
{
    private final boolean northSouth;
    private final int moveX;
    private final int moveZ;

    private double step;

    private @Nullable IAnimatedBlock firstBlockData = null;

    protected final int blocksToMove;

    public SlidingMover(
        Context context, AbstractDoor door, double time, boolean skipAnimation, int blocksToMove,
        RotateDirection openDirection, double multiplier, IPPlayer player, Cuboid newCuboid,
        DoorActionCause cause, DoorActionType actionType)
        throws Exception
    {
        super(context, door, time, skipAnimation, openDirection, player, newCuboid, cause, actionType);
        this.blocksToMove = blocksToMove;

        northSouth = openDirection.equals(RotateDirection.NORTH) || openDirection.equals(RotateDirection.SOUTH);

        moveX = northSouth ? 0 : blocksToMove;
        moveZ = northSouth ? blocksToMove : 0;

        double speed = 1;
        double pcMult = multiplier;
        pcMult = pcMult == 0.0 ? 1.0 : pcMult;
        final int maxSpeed = 6;

        // If the time isn't default, calculate speed.
        if (time != 0.0)
        {
            speed = Math.abs(blocksToMove) / time;
            super.time = time;
        }

        // If the non-default exceeds the max-speed or isn't set, calculate default speed.
        if (time == 0.0 || speed > maxSpeed)
        {
            speed = 1.4 * pcMult;
            speed = speed > maxSpeed ? maxSpeed : speed;
            super.time = Math.abs(blocksToMove) / speed;
        }

        init();
        super.startAnimation();
    }

    /**
     * Used for initializing variables such as {@link #endCount} and {@link #soundActive}.
     */
    protected void init()
    {
        super.endCount = (int) (20 * super.time);
        step = ((double) blocksToMove) / ((double) super.endCount);
        super.soundActive = new PSoundDescription(PSound.DRAGGING, 0.8f, 0.7f);
        super.soundFinish = new PSoundDescription(PSound.THUD, 0.2f, 0.15f);
    }

    @Override
    protected IPLocation getNewLocation(double radius, double xAxis, double yAxis, double zAxis)
    {
        return locationFactory.create(world, xAxis + moveX, yAxis, zAxis + moveZ);
    }

    @Override
    protected Vector3Dd getFinalPosition(IAnimatedBlock animatedBlock)
    {
        final Vector3Dd startLocation = animatedBlock.getStartPosition();
        final IPLocation finalLoc = getNewLocation(animatedBlock.getRadius(), startLocation.x(),
                                                   startLocation.y(), startLocation.z());
        return new Vector3Dd(finalLoc.getBlockX() + 0.5, finalLoc.getBlockY(), finalLoc.getBlockZ() + 0.5);
    }

    @Override
    protected void prepareAnimation()
    {
        // Gets the first block, which will be used as a base for the movement of all other blocks in the animation.
        firstBlockData = animatedBlocks.isEmpty() ? null : animatedBlocks.get(0);
    }

    protected Vector3Dd getGoalPos(IAnimatedBlock animatedBlock, double stepSum)
    {
        return animatedBlock.getStartPosition().add(northSouth ? 0 : stepSum, 0, northSouth ? stepSum : 0);
    }

    @Override
    protected void executeAnimationStep(int ticks)
    {
        if (firstBlockData == null)
            return;

        final double stepSum = step * ticks;
        for (final IAnimatedBlock animatedBlock : animatedBlocks)
            animatedBlock.teleport(getGoalPos(animatedBlock, stepSum));
    }
}

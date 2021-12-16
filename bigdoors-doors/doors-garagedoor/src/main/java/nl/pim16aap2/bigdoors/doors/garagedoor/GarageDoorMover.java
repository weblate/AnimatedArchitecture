package nl.pim16aap2.bigdoors.doors.garagedoor;

import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PBlockData;
import nl.pim16aap2.bigdoors.api.PSound;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.PSoundDescription;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;

import java.util.function.BiFunction;

/**
 * Represents a {@link BlockMover} for {@link GarageDoor}s.
 *
 * @author Pim
 */
public class GarageDoorMover extends BlockMover
{
    private final double resultHeight;
    private final Vector3Di directionVec;
    private final BiFunction<PBlockData, Double, Vector3Dd> getVector;
    private final int xLen;
    private final int yLen;
    private final int zLen;
    private final boolean northSouth;
    protected int blocksToMove;

    private double step;

    public GarageDoorMover(Context context, GarageDoor door, double time, double multiplier, boolean skipAnimation,
                           RotateDirection rotateDirection, IPPlayer player, Cuboid newCuboid, DoorActionCause cause,
                           DoorActionType actionType)
        throws Exception
    {
        super(context, door, time, skipAnimation, rotateDirection, player, newCuboid, cause, actionType);

        resultHeight = door.getMaximum().y() + 1.0D;

        BiFunction<PBlockData, Double, Vector3Dd> getVectorTmp;
        switch (rotateDirection)
        {
            case NORTH:
                directionVec = PBlockFace.getDirection(PBlockFace.NORTH);
                getVectorTmp = this::getVectorDownNorth;
                northSouth = true;
                break;
            case EAST:
                directionVec = PBlockFace.getDirection(PBlockFace.EAST);
                getVectorTmp = this::getVectorDownEast;
                northSouth = false;
                break;
            case SOUTH:
                directionVec = PBlockFace.getDirection(PBlockFace.SOUTH);
                getVectorTmp = this::getVectorDownSouth;
                northSouth = true;
                break;
            case WEST:
                directionVec = PBlockFace.getDirection(PBlockFace.WEST);
                getVectorTmp = this::getVectorDownWest;
                northSouth = false;
                break;
            default:
                throw new IllegalStateException("Failed to open garage door \"" + getDoorUID()
                                                    + "\". Reason: Invalid rotateDirection \"" +
                                                    rotateDirection + "\"");
        }

        xLen = xMax - xMin;
        yLen = yMax - yMin;
        zLen = zMax - zMin;

        if (!door.isOpen())
        {
            blocksToMove = yLen + 1;
            getVector = this::getVectorUp;
        }
        else
        {
            blocksToMove = Math.abs((xLen + 1) * directionVec.x()
                                        + (yLen + 1) * directionVec.y()
                                        + (zLen + 1) * directionVec.z());
            getVector = getVectorTmp;
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
        step = (blocksToMove + 0.5f) / super.endCount;
        super.soundActive = new PSoundDescription(PSound.DRAWBRIDGE_RATTLING, 0.8f, 0.7f);
        super.soundFinish = new PSoundDescription(PSound.THUD, 0.2f, 0.15f);
    }

    private Vector3Dd getVectorUp(PBlockData block, double stepSum)
    {
        final double currentHeight = Math.min(resultHeight, block.getStartY() + stepSum);
        double xMod = 0;
        double yMod = stepSum;
        double zMod = 0;

        if (currentHeight >= door.getMaximum().y())
        {
            final double horizontal = Math.max(0, stepSum - block.getRadius() - 0.5);
            xMod = directionVec.x() * horizontal;
            yMod = Math.min(resultHeight - block.getStartY(), stepSum);
            zMod = directionVec.z() * horizontal;
        }
        return new Vector3Dd(block.getStartX() + xMod, block.getStartY() + yMod, block.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownNorth(PBlockData block, double stepSum)
    {
        final double goalZ = door.getRotationPoint().z();
        final double pivotZ = goalZ + 1.5;
        final double currentZ = Math.max(goalZ, block.getStartZ() - stepSum);

        final double xMod = 0;
        double yMod = 0;
        double zMod = -stepSum;

        if (currentZ <= pivotZ)
        {
            yMod = -Math.max(0, stepSum - block.getRadius() + 0.5);
            zMod = Math.max(goalZ - block.getStartLocation().getZ() + 0.5, zMod);
        }

        return new Vector3Dd(block.getStartX() + xMod, block.getStartY() + yMod, block.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownSouth(PBlockData block, double stepSum)
    {
        final double goalZ = door.getRotationPoint().z();
        final double pivotZ = goalZ - 1.5;
        final double currentZ = Math.min(goalZ, block.getStartZ() + stepSum);

        final double xMod = 0;
        double yMod = 0;
        double zMod = stepSum;

        if (currentZ >= pivotZ)
        {
            yMod = -Math.max(0, stepSum - block.getRadius() + 0.5);
            zMod = Math.min(goalZ - block.getStartLocation().getZ() + 0.5, zMod);
        }
        return new Vector3Dd(block.getStartX() + xMod, block.getStartY() + yMod, block.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownEast(PBlockData block, double stepSum)
    {
        final double goalX = door.getRotationPoint().x();
        final double pivotX = goalX - 1.5;
        final double currentX = Math.min(goalX, block.getStartX() + stepSum);

        double xMod = stepSum;
        double yMod = 0;
        final double zMod = 0;

        if (currentX >= pivotX)
        {
            xMod = Math.min(goalX - block.getStartLocation().getX() + 0.5, xMod);
            yMod = -Math.max(0, stepSum - block.getRadius() + 0.5);
        }
        return new Vector3Dd(block.getStartX() + xMod, block.getStartY() + yMod, block.getStartZ() + zMod);
    }

    private Vector3Dd getVectorDownWest(PBlockData block, double stepSum)
    {
        final double goalX = door.getRotationPoint().x();
        final double pivotX = goalX + 1.5;
        final double currentX = Math.max(goalX, block.getStartX() - stepSum);

        double xMod = -stepSum;
        double yMod = 0;
        final double zMod = 0;

        if (currentX <= pivotX)
        {
            xMod = Math.max(goalX - block.getStartLocation().getX() + 0.5, xMod);
            yMod = -Math.max(0, stepSum - block.getRadius() + 0.5);
        }

        return new Vector3Dd(block.getStartX() + xMod, block.getStartY() + yMod, block.getStartZ() + zMod);
    }

    @Override
    protected IPLocation getNewLocation(double radius, double xAxis, double yAxis, double zAxis)
    {
        double newX;
        double newY;
        double newZ;

        if (!door.isOpen())
        {
            newX = xAxis + (1 + yLen - radius) * directionVec.x();
            newY = resultHeight;
            newZ = zAxis + (1 + yLen - radius) * directionVec.z();
        }
        else
        {
            if (directionVec.x() == 0)
            {
                newX = xAxis;
                newY = door.getMaximum().y() - (zLen - radius);
                newZ = door.getRotationPoint().z();
            }
            else
            {
                newX = door.getRotationPoint().x();
                newY = door.getMaximum().y() - (xLen - radius);
                newZ = zAxis;
            }
            newY -= 2;
        }
        return locationFactory.create(world, newX, newY, newZ);
    }

    @Override
    protected Vector3Dd getFinalPosition(PBlockData block)
    {
        final IPLocation startLocation = block.getStartLocation();
        final IPLocation finalLoc = getNewLocation(block.getRadius(), startLocation.getX(),
                                                   startLocation.getY(), startLocation.getZ());
        double addX = 0;
        double addZ = 0;
        if (door.isOpen()) // The offset isn't needed when going up.
        {
            addX = northSouth ? 0 : 0.5f;
            addZ = northSouth ? 0.5f : 0;
        }
        return new Vector3Dd(finalLoc.getX() + addX, finalLoc.getY(), finalLoc.getZ() + addZ);
    }

    @Override
    protected void executeAnimationStep(int ticks)
    {
        final double stepSum = step * ticks;
        for (final PBlockData block : savedBlocks)
            block.getFBlock().teleport(getVector.apply(block, stepSum));
    }

    @Override
    protected float getRadius(int xAxis, int yAxis, int zAxis)
    {
        if (!door.isOpen())
        {
            final float height = door.getMaximum().y();
            return height - yAxis;
        }

        final int dX = Math.abs(xAxis - door.getRotationPoint().x());
        final int dZ = Math.abs(zAxis - door.getRotationPoint().z());
        return Math.abs(dX * directionVec.x() + dZ * directionVec.z());
    }
}

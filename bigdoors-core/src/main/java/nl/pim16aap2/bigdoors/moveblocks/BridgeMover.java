package nl.pim16aap2.bigdoors.moveblocks;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PBlockData;
import nl.pim16aap2.bigdoors.api.PSound;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorType;
import nl.pim16aap2.bigdoors.doors.Drawbridge;
import nl.pim16aap2.bigdoors.doors.HorizontalAxisAlignedBase;
import nl.pim16aap2.bigdoors.moveblocks.getnewlocation.GNLVerticalRotEast;
import nl.pim16aap2.bigdoors.moveblocks.getnewlocation.GNLVerticalRotNorth;
import nl.pim16aap2.bigdoors.moveblocks.getnewlocation.GNLVerticalRotSouth;
import nl.pim16aap2.bigdoors.moveblocks.getnewlocation.GNLVerticalRotWest;
import nl.pim16aap2.bigdoors.moveblocks.getnewlocation.IGetNewLocation;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TimerTask;
import java.util.function.BiFunction;

/**
 * Represents a {@link BlockMover} for {@link Drawbridge}s.
 *
 * @author Pim
 */
public class BridgeMover extends BlockMover
{
    private final IGetNewLocation gnl;
    protected final boolean NS;
    protected final BiFunction<PBlockData, Double, Vector3Dd> getVector;
    protected final int tickRate;

    /**
     * Constructs a {@link BlockMover}.
     *
     * @param door            The {@link AbstractDoorBase}.
     * @param time            The amount of time (in seconds) the door will try to toggle itself in.
     * @param skipAnimation   If the door should be opened instantly (i.e. skip animation) or not.
     * @param upDown          Whether the {@link DoorType#DRAWBRIDGE} should go up or down.
     * @param rotateDirection The direction the {@link AbstractDoorBase} will move.
     * @param multiplier      The speed multiplier.
     * @param player          The player who opened this door.
     */
    public BridgeMover(final double time, final @NotNull HorizontalAxisAlignedBase door,
                       final @NotNull PBlockFace upDown, final @NotNull RotateDirection rotateDirection,
                       final boolean skipAnimation, final double multiplier, final @Nullable IPPlayer player,
                       final @NotNull Vector3Di finalMin, final @NotNull Vector3Di finalMax)
    {
        super(door, time, skipAnimation, upDown, rotateDirection, -1, player, finalMin, finalMax);

        NS = door.onNorthSouthAxis();

        int xLen = Math.abs(door.getMaximum().getX() - door.getMinimum().getX());
        int yLen = Math.abs(door.getMaximum().getY() - door.getMinimum().getY());
        int zLen = Math.abs(door.getMaximum().getZ() - door.getMinimum().getZ());
        int doorSize = Math.max(xLen, Math.max(yLen, zLen)) + 1;
        double[] vars = Util.calculateTimeAndTickRate(doorSize, time, multiplier, 5.2);
        this.time = vars[0];
        tickRate = (int) vars[1];

        switch (rotateDirection)
        {
            case NORTH:
                gnl = new GNLVerticalRotNorth(world, xMin, xMax, yMin, yMax, zMin, zMax, upDown, openDirection);
                getVector = this::getVectorNorth;
                break;
            case EAST:
                gnl = new GNLVerticalRotEast(world, xMin, xMax, yMin, yMax, zMin, zMax, upDown, openDirection);
                getVector = this::getVectorEast;
                break;
            case SOUTH:
                gnl = new GNLVerticalRotSouth(world, xMin, xMax, yMin, yMax, zMin, zMax, upDown, openDirection);
                getVector = this::getVectorSouth;
                break;
            case WEST:
                gnl = new GNLVerticalRotWest(world, xMin, xMax, yMin, yMax, zMin, zMax, upDown, openDirection);
                getVector = this::getVectorWest;
                break;
            default:
                gnl = null;
                getVector = null;
                PLogger.get().dumpStackTrace("Failed to open door \"" + getDoorUID()
                                                 + "\". Reason: Invalid rotateDirection \"" +
                                                 rotateDirection.toString() + "\"");
                return; // TODO: MEMORY LEAK!!
        }

        super.constructFBlocks();
    }

    /**
     * Calculates the speed vector of a block when rotating in northern direction.
     *
     * @param block   The block.
     * @param stepSum The angle (in rads) of the block.
     * @return The speed vector of the block.
     */
    @NotNull
    private Vector3Dd getVectorNorth(final @NotNull PBlockData block, final double stepSum)
    {

        double startAngle = block.getStartAngle();
        double posX = block.getFBlock().getPLocation().getX();
        double posY = door.getEngine().getY() - block.getRadius() * Math.cos(startAngle - stepSum);
        double posZ = door.getEngine().getZ() - block.getRadius() * Math.sin(startAngle - stepSum);
        return new Vector3Dd(posX, posY, posZ + 0.5);
    }

    /**
     * Calculates the speed vector of a block when rotating in western direction.
     *
     * @param block   The block.
     * @param stepSum The angle (in rads) of the block.
     * @return The speed vector of the block.
     */
    @NotNull
    private Vector3Dd getVectorWest(final @NotNull PBlockData block, final double stepSum)
    {
        double startAngle = block.getStartAngle();
        double posX = door.getEngine().getX() - block.getRadius() * Math.sin(startAngle - stepSum);
        double posY = door.getEngine().getY() - block.getRadius() * Math.cos(startAngle - stepSum);
        double posZ = block.getFBlock().getPLocation().getZ();
        return new Vector3Dd(posX + 0.5, posY, posZ);
    }

    /**
     * Calculates the speed vector of a block when rotating in southern direction.
     *
     * @param block   The block.
     * @param stepSum The angle (in rads) of the block.
     * @return The speed vector of the block.
     */
    @NotNull
    private Vector3Dd getVectorSouth(final @NotNull PBlockData block, final double stepSum)
    {
        float startAngle = block.getStartAngle();
        double posX = block.getFBlock().getPLocation().getX();
        double posY = door.getEngine().getY() - block.getRadius() * Math.cos(startAngle + stepSum);
        double posZ = door.getEngine().getZ() - block.getRadius() * Math.sin(startAngle + stepSum);
        return new Vector3Dd(posX, posY, posZ + 0.5);
    }

    /**
     * Calculates the speed vector of a block when rotating in eastern direction.
     *
     * @param block   The block.
     * @param stepSum The angle (in rads) of the block.
     * @return The speed vector of the block.
     */
    @NotNull
    private Vector3Dd getVectorEast(final @NotNull PBlockData block, final double stepSum)
    {
        float startAngle = block.getStartAngle();
        double posX = door.getEngine().getX() - block.getRadius() * Math.sin(startAngle + stepSum);
        double posY = door.getEngine().getY() - block.getRadius() * Math.cos(startAngle + stepSum);
        double posZ = block.getFBlock().getPLocation().getZ();
        return new Vector3Dd(posX + 0.5, posY, posZ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void animateEntities()
    {
        super.moverTask = new TimerTask()
        {
            boolean replace = false;
            double counter = 0;
            int endCount = (int) (20 / tickRate * time);
            double step = (Math.PI / 2) / endCount;
            // Add a half a second or the smallest number of ticks closest to it to the timer
            // to make sure the animation doesn't jump at the end.
            int totalTicks = endCount + Math.max(1, 10 / tickRate);
            int replaceCount = endCount / 2;
            long startTime = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();

            @Override
            public void run()
            {
                ++counter;
                if (counter == 0 || (counter < endCount - (45 / tickRate) && counter % (6 * tickRate / 4) == 0))
                    playSound(PSound.DRAWBRIDGE_RATTLING, 0.8f, 0.7f);

                lastTime = currentTime;
                currentTime = System.nanoTime();
                startTime += currentTime - lastTime;
                replace = counter == replaceCount;
                double stepSum = step * Math.min(counter, endCount);

                if (counter > totalTicks)
                {
                    playSound(PSound.THUD, 2f, 0.15f);
                    for (PBlockData block : savedBlocks)
                        block.getFBlock().setVelocity(new Vector3Dd(0D, 0D, 0D));

                    BigDoors.get().getPlatform().newPExecutor().runSync(() -> putBlocks(false));
                    cancel();
                }
                else
                {
                    // It is not possible to edit falling block blockdata (client won't update it),
                    // so delete the current fBlock and replace it by one that's been rotated.
                    // Also, this stuff needs to be done on the main thread.
                    if (replace)
                        BigDoors.get().getPlatform().newPExecutor().runSync(() -> respawnBlocks());
                    for (PBlockData block : savedBlocks)
                    {
                        double radius = block.getRadius();
                        if (radius != 0)
                        {
                            Vector3Dd vec = getVector.apply(block, stepSum)
                                                     .subtract(block.getFBlock().getPosition());
                            vec.multiply(0.101);
                            block.getFBlock().setVelocity(vec);
                        }
                    }
                }
            }
        };
        BigDoors.get().getPlatform().newPExecutor().runAsyncRepeated(moverTask, 14, tickRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float getRadius(final int xAxis, final int yAxis, final int zAxis)
    {
        // Get the current radius of a block between used axis (either x and y, or z and y).
        // When the engine is positioned along the NS axis, the Z values does not change.
        double deltaA = (door.getEngine().getY() - yAxis);
        double deltaB = NS ? (door.getEngine().getX() - xAxis) : (door.getEngine().getZ() - zAxis);
        return (float) Math.sqrt(Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    protected IPLocation getNewLocation(final double radius, final double xAxis, final double yAxis, final double zAxis)
    {
        return gnl.getNewLocation(radius, xAxis, yAxis, zAxis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float getStartAngle(final int xAxis, final int yAxis, final int zAxis)
    {
        // Get the angle between the used axes (either x and y, or z and y).
        // When the engine is positioned along the NS axis, the Z values does not change.
        float deltaA = NS ? door.getEngine().getX() - xAxis : door.getEngine().getZ() - zAxis;
        float deltaB = door.getEngine().getY() - yAxis;
        return (float) Util.clampAngleRad(Math.atan2(deltaA, deltaB));
    }
}

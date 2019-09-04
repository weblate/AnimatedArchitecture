package nl.pim16aap2.bigdoors.doors;

import com.google.common.base.Preconditions;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.compatiblity.ProtectionCompatManager;
import nl.pim16aap2.bigdoors.config.ConfigLoader;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.managers.DoorManager;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.spigotutil.SpigotUtil;
import nl.pim16aap2.bigdoors.util.DoorToggleResult;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.Vector3D;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a utility singleton that is used to open {@link DoorBase}s.
 *
 * @author Pim
 */
public final class DoorOpeningUtility
{
    @Nullable
    private static DoorOpeningUtility instance;

    private final PLogger pLogger;
    private final DoorManager doorManager;
    private final IGlowingBlockSpawner glowingBlockSpawner;
    private final ConfigLoader config;
    private final ProtectionCompatManager protectionManager;

    /**
     * Constructs a new {@link DoorOpeningUtility}.
     *
     * @param pLogger             The logger.
     * @param doorManager         The class that manages the doors.
     * @param glowingBlockSpawner The class that
     * @param config              The configuration of the BigDoors plugin.
     * @param protectionManager   The class used to check with compatibility hooks if it is allowed to be toggled.
     */
    private DoorOpeningUtility(final @NotNull PLogger pLogger, final @NotNull DoorManager doorManager,
                               final @NotNull IGlowingBlockSpawner glowingBlockSpawner,
                               final @NotNull ConfigLoader config,
                               final @NotNull ProtectionCompatManager protectionManager)
    {
        this.pLogger = pLogger;
        this.doorManager = doorManager;
        this.glowingBlockSpawner = glowingBlockSpawner;
        this.config = config;
        this.protectionManager = protectionManager;
    }

    /**
     * Initializes the {@link DoorOpeningUtility}. If it has already been initialized, it'll return that instance
     * instead.
     *
     * @param pLogger             The logger.
     * @param doorManager         The class that manages the doors.
     * @param glowingBlockSpawner The class that
     * @param config              The configuration of the BigDoors plugin.
     * @param protectionManager   The class used to check with compatibility hooks if it is allowed to be toggled.
     * @return The instance of this {@link DoorOpeningUtility}.
     */
    @NotNull
    public static DoorOpeningUtility init(final @NotNull PLogger pLogger, final @NotNull DoorManager doorManager,
                                          final @NotNull IGlowingBlockSpawner glowingBlockSpawner,
                                          final @NotNull ConfigLoader config,
                                          final @NotNull ProtectionCompatManager protectionManager)
    {
        return (instance == null) ?
               instance = new DoorOpeningUtility(pLogger, doorManager, glowingBlockSpawner, config, protectionManager) :
               instance;
    }

    /**
     * Gets the instance of the {@link DoorOpeningUtility} if it exists.
     *
     * @return The instance of the {@link DoorOpeningUtility}.
     */
    @NotNull
    public static DoorOpeningUtility get()
    {
        Preconditions.checkState(instance != null,
                                 "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    /**
     * Aborts an attempt to toggle a {@link DoorBase} and cleans up leftover data from this attempt.
     *
     * @param door   The {@link DoorBase}.
     * @param result The reason
     * @param cause  What caused the toggle in the first place.
     * @return The result.
     */
    @NotNull
    public DoorToggleResult abort(final @NotNull DoorBase door, final @NotNull DoorToggleResult result,
                                  final @NotNull DoorActionCause cause)
    {
        // If the reason the toggle attempt was cancelled was because it was busy, it should obviously
        // not reset the busy status of this door. However, in every other case it should, because the door is
        // registered as busy before all the other checks take place.
        if (!result.equals(DoorToggleResult.BUSY))
            doorManager.setDoorAvailable(door.getDoorUID());

        if (!result.equals(DoorToggleResult.NOPERMISSION))
            if (!cause.equals(DoorActionCause.PLAYER))
                pLogger.warn("Failed to toggle door: " + result.name());
        return result;
    }

    /**
     * Checks if the size of a {@link DoorBase} exceeds the global limit.
     *
     * @param door The {@link DoorBase}.
     * @return True if the size of a {@link DoorBase} exceeds the global limit.
     */
    public boolean isTooBig(final @NotNull DoorBase door)
    {
        // Make sure the doorSize does not exceed the total doorSize.
        // If it does, open the door instantly.
        int maxDoorSize = config.maxDoorSize();
        if (maxDoorSize != -1)
            return door.getBlockCount() > maxDoorSize;
        return false;
    }

    /**
     * Parses the result of {@link ProtectionCompatManager#canBreakBlocksBetweenLocs(UUID, Location, Location)}. If the
     * player is not allowed to break the block(s), they'll receive a message about this.
     *
     * @param door The {@link DoorBase} being opened.
     * @param loc1 The first location of the area to check.
     * @param loc2 The second location of the area to check.
     * @return True if the player is allowed to break the block(s).
     */
    public boolean canBreakBlocksBetweenLocs(final @NotNull DoorBase door, final @NotNull Location loc1,
                                             final @NotNull Location loc2)
    {
        // If the returned value is an empty Optional, the player is allowed to break blocks.
        return protectionManager.canBreakBlocksBetweenLocs(door.getPlayerUUID(), loc1, loc2).map(
            PROT ->
            {
                pLogger
                    .warn("Player \"" + door.getPlayerUUID().toString() + "\" is not allowed to open door " +
                              door.getName() + " (" + door.getDoorUID() + ") here! Reason: " + PROT);
                return false;
            }).orElse(true);
    }

    /**
     * Checks if an area is empty. "Empty" here means that there no blocks that are not air or liquid.
     *
     * @param newMin     The lower bound location of the area.
     * @param newMax     The upper bound location of the area.
     * @param curMin     The current lower bound location of the door (blocks in this area are skipped).
     * @param curMax     The upper lower bound location of the door (blocks in this area are skipped).
     * @param playerUUID The {@link UUID} of the {@link org.bukkit.entity.Player} to notify of violations. May be null.
     * @param world      The world to check the blocks in.
     * @return True if the location is not empty.
     */
    public boolean isLocationEmpty(final @NotNull Location newMin, final @NotNull Location newMax,
                                   final @NotNull Location curMin, final @NotNull Location curMax,
                                   final @Nullable UUID playerUUID, final @NotNull World world)
    {
        boolean isEmpty = true;
        for (int xAxis = newMin.getBlockX(); xAxis <= newMax.getBlockX(); ++xAxis)
        {
            for (int yAxis = newMin.getBlockY(); yAxis <= newMax.getBlockY(); ++yAxis)
            {
                for (int zAxis = newMin.getBlockZ(); zAxis <= newMax.getBlockZ(); ++zAxis)
                {
                    // Ignore blocks that are currently part of the door.
                    // It's expected and accepted for them to be in the way.
                    if (Util.between(xAxis, curMin.getBlockX(), curMax.getBlockX()) &&
                        Util.between(yAxis, curMin.getBlockY(), curMax.getBlockY()) &&
                        Util.between(zAxis, curMin.getBlockZ(), curMax.getBlockZ()))
                        continue;

                    if (!SpigotUtil.isAirOrLiquid(world.getBlockAt(xAxis, yAxis, zAxis)))
                    {
                        if (playerUUID == null)
                            return false;
                        glowingBlockSpawner
                            .spawnGlowinBlock(playerUUID, world.getName(), 10, xAxis, yAxis, zAxis,
                                              ChatColor.RED);
                        isEmpty = false;
                    }
                }
            }
        }
        return isEmpty;
    }

    /**
     * Gets the number of blocks this door can move in the given direction. If set, it won't go further than {@link
     * DoorBase#getBlocksToMove()}
     *
     * @param vec          Which direction to count the number of available blocks in.
     * @param playerUUID   The player for whom to check. May be null.
     * @param world        The world to check the blocks in.
     * @param curMin       The current lower bound location of the door (blocks in this area are skipped).
     * @param curMax       The upper lower bound location of the door (blocks in this area are skipped).
     * @param blocksToMove The number of blocks to try move.
     * @return Gets the number of blocks this door can move in the given direction.
     */
    public int getBlocksInDir(final @NotNull Vector3D vec, final @Nullable UUID playerUUID, final @NotNull World world,
                              final @NotNull Location curMin, final @NotNull Location curMax, final int blocksToMove)
    {
        int startX, startY, startZ, endX, endY, endZ;

        startX =
            vec.getX() == 0 ? curMin.getBlockX() : vec.getX() == 1 ? curMax.getBlockX() + 1 : curMin.getBlockX() - 1;
        startY =
            vec.getY() == 0 ? curMin.getBlockY() : vec.getY() == 1 ? curMax.getBlockY() + 1 : curMin.getBlockY() - 1;
        startZ =
            vec.getZ() == 0 ? curMin.getBlockZ() : vec.getZ() == 1 ? curMax.getBlockZ() + 1 : curMin.getBlockZ() - 1;

        endX = vec.getX() == 0 ? curMax.getBlockX() : startX;
        endY = vec.getY() == 0 ? curMax.getBlockY() : startY;
        endZ = vec.getZ() == 0 ? curMax.getBlockZ() : startZ;

        Location locA = new Location(world, startX, startY, startZ);
        Location locB = new Location(world, endX, endY, endZ);

        // xLen and zLen describe the length of the door in the x and the z direction respectively.
        // If the rotation direction and the blocksToMove variable are defined, use the blocksToMove variable instead.
        int xLen = blocksToMove < 1 ? (curMax.getBlockX() - curMin.getBlockX()) + 1 : blocksToMove;
        int yLen = blocksToMove < 1 ? (curMax.getBlockY() - curMin.getBlockY()) + 1 : blocksToMove;
        int zLen = blocksToMove < 1 ? (curMax.getBlockZ() - curMin.getBlockZ()) + 1 : blocksToMove;

        // The maxDist is the number of blocks to check in a direction. This is either getBlocksToMove if it that has
        // been specified. If it hasn't, it's the length of the door in the provided direction.
        int maxDist = blocksToMove > 0 ? blocksToMove :
                      Math.abs(vec.getX() * xLen + vec.getY() * yLen + vec.getZ() * zLen);

        int ret = 0;
        int steps = 0;
        boolean obstructed = false;
        while (steps < maxDist)
        {
            boolean isEmpty = isLocationEmpty(locA, locB, curMin, curMax, playerUUID, world);
            if (!isEmpty)
            {
                obstructed = true;
                if (playerUUID == null)
                    break;
            }
            if (!obstructed) // There is no point in checking how many blocks are available behind an obstruction.
                ++ret;
            locA.add(vec.getX(), vec.getY(), vec.getZ());
            locB.add(vec.getX(), vec.getY(), vec.getZ());
            ++steps;
        }

        // If the direction was in a negative direction, make sure the output is negative as well.
        return (vec.getX() == -1 || vec.getY() == -1 || vec.getZ() == -1) ? -1 * ret : ret;
    }

    /**
     * Checks if a {@link DoorBase} is busy and set it to busy if that is the case.
     *
     * @param doorUID The UID of the {@link DoorBase} to check.
     * @return True if already busy.
     */
    private boolean isBusySetIfNot(final long doorUID)
    {
        if (doorManager.isDoorBusy(doorUID))
            return true;
        doorManager.setDoorBusy(doorUID);
        return false;
    }

    /**
     * Checks if a {@link DoorBase} can be toggled or not.
     * <p>
     * It checks the following items:
     * <p>
     * - The {@link DoorBase} is not already being animated.
     * <p>
     * - The {@link DoorType} is enabled.
     * <p>
     * - The {@link DoorBase} is not locked.
     * <p>
     * - All chunks this {@link DoorBase} might interact with are loaded.
     *
     * @param door  The {@link DoorBase}.
     * @param cause Who or what initiated this action.
     * @return {@link DoorToggleResult#SUCCESS} if it can be toggled
     */
    @NotNull
    public DoorToggleResult canBeToggled(final @NotNull DoorBase door, final @NotNull DoorActionCause cause)
    {
        if (isBusySetIfNot(door.getDoorUID()))
            return DoorToggleResult.BUSY;

        if (door.isLocked())
            return DoorToggleResult.LOCKED;
        if (!DoorType.isEnabled(door.getType()))
            return DoorToggleResult.TYPEDISABLED;

        if (!chunksLoaded(door))
        {
            pLogger.warn(ChatColor.RED + "Chunks for door " + door.getName() + " could not be not loaded!");
            return DoorToggleResult.ERROR;
        }

        return DoorToggleResult.SUCCESS;
    }

    /**
     * Checks if the {@link org.bukkit.Chunk}s a {@link DoorBase} might interact with are loaded. If they aren't loaded
     * try to load them.
     *
     * @param door The {@link DoorBase}.
     * @return True if all {@link org.bukkit.Chunk}s this {@link DoorBase} might interact with are loaded or have been
     * loaded.
     */
    private boolean chunksLoaded(final @NotNull DoorBase door)
    {
        // Try to load doors and return if successful.
        return door.getWorld().getChunkAt(door.max).load() &&
            door.getWorld().getChunkAt(door.min).isLoaded();
    }

    /**
     * Registers a BlockMover with the {@link DatabaseManager}
     *
     * @param blockMover The {@link BlockMover}.
     */
    public void registerBlockMover(final @NotNull BlockMover blockMover)
    {
        doorManager.addBlockMover(blockMover);
    }

    /**
     * Checks if a {@link BlockMover} of a {@link DoorBase} has been registered with the {@link DatabaseManager}.
     *
     * @param doorUID The UID of the {@link DoorBase}.
     * @return True if a {@link BlockMover} has been registered with the {@link DatabaseManager} for the {@link
     * DoorBase}.
     */
    public boolean isBlockMoverRegistered(final long doorUID)
    {
        return getBlockMover(doorUID).isPresent();
    }

    /**
     * Gets the {@link BlockMover} of a {@link DoorBase} if it has been registered with the {@link DatabaseManager}.
     *
     * @param doorUID The UID of the {@link DoorBase}.
     * @return The {@link BlockMover} of a {@link DoorBase} if it has been registered with the {@link DatabaseManager}.
     */
    @NotNull
    public Optional<BlockMover> getBlockMover(final long doorUID)
    {
        return doorManager.getBlockMover(doorUID);
    }

    /**
     * Gets the speed multiplier of a {@link DoorBase} from the config based on its {@link DoorType}.
     *
     * @param door The {@link DoorBase}.
     * @return The speed multiplier of this {@link DoorBase}.
     */
    double getMultiplier(final @NotNull DoorBase door)
    {
        return config.getMultiplier(door.getType());
    }
}

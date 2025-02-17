package nl.pim16aap2.animatedarchitecture.core.structures;

import com.google.common.flogger.StackSize;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.animatedarchitecture.core.api.Color;
import nl.pim16aap2.animatedarchitecture.core.api.GlowingBlockSpawner;
import nl.pim16aap2.animatedarchitecture.core.api.IBlockAnalyzer;
import nl.pim16aap2.animatedarchitecture.core.api.IChunkLoader;
import nl.pim16aap2.animatedarchitecture.core.api.IConfig;
import nl.pim16aap2.animatedarchitecture.core.api.IExecutor;
import nl.pim16aap2.animatedarchitecture.core.api.IMessageable;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.api.IProtectionCompatManager;
import nl.pim16aap2.animatedarchitecture.core.api.IWorld;
import nl.pim16aap2.animatedarchitecture.core.api.factories.IAnimatedArchitectureEventFactory;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ILocationFactory;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ITextFactory;
import nl.pim16aap2.animatedarchitecture.core.events.IAnimatedArchitectureEventCaller;
import nl.pim16aap2.animatedarchitecture.core.events.IStructureEventTogglePrepare;
import nl.pim16aap2.animatedarchitecture.core.events.IStructureEventToggleStart;
import nl.pim16aap2.animatedarchitecture.core.events.IStructureToggleEvent;
import nl.pim16aap2.animatedarchitecture.core.events.StructureActionCause;
import nl.pim16aap2.animatedarchitecture.core.events.StructureActionType;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.LimitsManager;
import nl.pim16aap2.animatedarchitecture.core.managers.StructureTypeManager;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationBlockManagerFactory;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.AnimationType;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.Animator;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.IAnimationBlockManager;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.IAnimationComponent;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.StructureActivityManager;
import nl.pim16aap2.animatedarchitecture.core.structures.structurearchetypes.IDiscreteMovement;
import nl.pim16aap2.animatedarchitecture.core.util.Cuboid;
import nl.pim16aap2.animatedarchitecture.core.util.Limit;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Represents a utility singleton that is used to open {@link IStructure}s.
 *
 * @author Pim
 */
@Flogger
public final class StructureOpeningHelper
{
    private final ILocalizer localizer;
    private final ITextFactory textFactory;
    private final StructureActivityManager structureActivityManager;
    private final StructureTypeManager structureTypeManager;
    private final IConfig config;
    private final IExecutor executor;
    private final IBlockAnalyzer blockAnalyzer;
    private final ILocationFactory locationFactory;
    private final IProtectionCompatManager protectionCompatManager;
    private final GlowingBlockSpawner glowingBlockSpawner;
    private final IAnimatedArchitectureEventFactory animatedArchitectureEventFactory;
    private final StructureRegistry structureRegistry;
    private final IChunkLoader chunkLoader;
    private final LimitsManager limitsManager;
    private final IAnimatedArchitectureEventCaller animatedArchitectureEventCaller;
    private final AnimationBlockManagerFactory animationBlockManagerFactory;
    private final AnimationRequestData.IFactory movementRequestDataFactory;

    @Inject //
    StructureOpeningHelper(
        ILocalizer localizer,
        ITextFactory textFactory,
        StructureActivityManager structureActivityManager,
        StructureTypeManager structureTypeManager,
        IConfig config,
        IExecutor executor,
        IBlockAnalyzer blockAnalyzer,
        ILocationFactory locationFactory,
        IProtectionCompatManager protectionCompatManager,
        GlowingBlockSpawner glowingBlockSpawner,
        IAnimatedArchitectureEventFactory animatedArchitectureEventFactory,
        StructureRegistry structureRegistry,
        IChunkLoader chunkLoader,
        LimitsManager limitsManager,
        IAnimatedArchitectureEventCaller animatedArchitectureEventCaller,
        AnimationBlockManagerFactory animationBlockManagerFactory,
        AnimationRequestData.IFactory movementRequestDataFactory)
    {
        this.localizer = localizer;
        this.textFactory = textFactory;
        this.structureActivityManager = structureActivityManager;
        this.structureTypeManager = structureTypeManager;
        this.config = config;
        this.executor = executor;
        this.blockAnalyzer = blockAnalyzer;
        this.locationFactory = locationFactory;
        this.protectionCompatManager = protectionCompatManager;
        this.glowingBlockSpawner = glowingBlockSpawner;
        this.animatedArchitectureEventFactory = animatedArchitectureEventFactory;
        this.structureRegistry = structureRegistry;
        this.chunkLoader = chunkLoader;
        this.limitsManager = limitsManager;
        this.animatedArchitectureEventCaller = animatedArchitectureEventCaller;
        this.animationBlockManagerFactory = animationBlockManagerFactory;
        this.movementRequestDataFactory = movementRequestDataFactory;
    }

    /**
     * Aborts an attempt to toggle a {@link IStructure} and cleans up leftover data from this attempt.
     *
     * @param structure
     *     The {@link IStructure}.
     * @param result
     *     The reason the action was aborted.
     * @param cause
     *     What caused the toggle in the first place.
     * @param responsible
     *     Who is responsible for the action.
     * @param stamp
     *     The stamp of the animation. When this is not null, this stamp will be used to unregister the animation. This
     *     will 'release the lock' for the UID.
     * @return The same result that was passed in as argument.
     */
    private StructureToggleResult abort(
        AbstractStructure structure, StructureToggleResult result, StructureActionCause cause, IPlayer responsible,
        IMessageable messageReceiver, @Nullable Long stamp)
    {
        log.atFine().log("Aborted toggle for structure %d because of %s. Toggle Reason: %s, Responsible: %s",
                         structure.getUid(), result.name(), cause.name(), responsible.asString());

        // If the reason the toggle attempt was cancelled was because it was busy, it should obviously
        // not reset the busy status of this structure. However, in every other case it should, because the structure is
        // registered as busy before all the other checks take place.
        if (stamp != null)
            structureActivityManager.unregisterAnimation(structure.getUid(), stamp);

        if (!result.equals(StructureToggleResult.NO_PERMISSION))
        {
            if (messageReceiver instanceof IPlayer)
                messageReceiver.sendError(
                    textFactory,
                    localizer.getMessage(result.getLocalizationKey(),
                                         localizer.getStructureType(structure.getType()),
                                         structure.getName()));
            else
            {
                final Level level = result == StructureToggleResult.BUSY ? Level.FINE : Level.INFO;

                if (result.equals(StructureToggleResult.INSTANCE_UNREGISTERED))
                    log.at(level).withStackTrace(StackSize.FULL)
                       .log("Encountered unregistered structure structure: %d", structure.getUid());
                else
                    log.at(level).log("Failed to toggle structure: %d, reason: %s", structure.getUid(), result.name());
            }
        }
        return result;
    }

    /**
     * See
     * {@link IAnimatedArchitectureEventFactory#createTogglePrepareEvent(StructureSnapshot, StructureActionCause,
     * StructureActionType, IPlayer, double, boolean, Cuboid)}.
     */
    private IStructureEventTogglePrepare callTogglePrepareEvent(
        StructureSnapshot snapshot, StructureActionCause cause, StructureActionType actionType, IPlayer responsible,
        double time, boolean skipAnimation, Cuboid newCuboid)
    {
        final IStructureEventTogglePrepare event =
            animatedArchitectureEventFactory.createTogglePrepareEvent(
                snapshot, cause, actionType, responsible, time, skipAnimation, newCuboid);
        callStructureToggleEvent(event);
        return event;
    }

    /**
     * See
     * {@link IAnimatedArchitectureEventFactory#createTogglePrepareEvent(StructureSnapshot, StructureActionCause,
     * StructureActionType, IPlayer, double, boolean, Cuboid)}.
     */
    private IStructureEventTogglePrepare callTogglePrepareEvent(AnimationRequestData data)
    {
        return callTogglePrepareEvent(
            data.getStructureSnapshot(),
            data.getCause(),
            data.getActionType(),
            data.getResponsible(),
            data.getAnimationTime(),
            data.isAnimationSkipped(),
            data.getNewCuboid());
    }

    /**
     * See
     * {@link IAnimatedArchitectureEventFactory#createToggleStartEvent(AbstractStructure, StructureSnapshot,
     * StructureActionCause, StructureActionType, IPlayer, double, boolean, Cuboid)}.
     */
    private IStructureEventToggleStart callToggleStartEvent(
        AbstractStructure structure, StructureSnapshot snapshot, StructureActionCause cause,
        StructureActionType actionType,
        IPlayer responsible, double time, boolean skipAnimation, Cuboid newCuboid)
    {
        final IStructureEventToggleStart event =
            animatedArchitectureEventFactory.createToggleStartEvent(
                structure, snapshot, cause, actionType, responsible, time, skipAnimation, newCuboid);
        callStructureToggleEvent(event);
        return event;
    }

    /**
     * See
     * {@link IAnimatedArchitectureEventFactory#createToggleStartEvent(AbstractStructure, StructureSnapshot,
     * StructureActionCause, StructureActionType, IPlayer, double, boolean, Cuboid)}.
     */
    private IStructureEventToggleStart callToggleStartEvent(AbstractStructure structure, AnimationRequestData data)
    {
        return callToggleStartEvent(
            structure,
            data.getStructureSnapshot(),
            data.getCause(),
            data.getActionType(),
            data.getResponsible(),
            data.getAnimationTime(),
            data.isAnimationSkipped(),
            data.getNewCuboid());
    }

    private void callStructureToggleEvent(IStructureToggleEvent prepareEvent)
    {
        animatedArchitectureEventCaller.callAnimatedArchitectureEvent(prepareEvent);
    }

    /**
     * Registers a new block mover. Must be called from the main thread.
     */
    private boolean registerBlockMover(
        AbstractStructure structure, AnimationRequestData data, IAnimationComponent component,
        @Nullable IPlayer player,
        AnimationType animationType, long stamp)
    {
        try
        {
            final IAnimationBlockManager animationBlockManager =
                animationBlockManagerFactory.newManager(animationType, player);

            final Animator blockMover =
                new Animator(structure, data, component, animationBlockManager);

            structureActivityManager.addAnimator(stamp, blockMover);
            executor.runOnMainThread(blockMover::startAnimation);
        }
        catch (Exception e)
        {
            log.atSevere().withCause(e).log();
            return false;
        }
        return true;
    }

    private StructureToggleResult toggle(
        StructureSnapshot snapshot, AbstractStructure targetStructure, AnimationRequestData data,
        IAnimationComponent component, IMessageable messageReceiver, @Nullable IPlayer player,
        AnimationType animationType)
    {
        if (snapshot.getOpenDir() == MovementDirection.NONE)
        {
            log.atSevere().withStackTrace(StackSize.FULL).log("OpenDir cannot be 'NONE'!");
            return StructureToggleResult.ERROR;
        }

        if (!structureRegistry.isRegistered(targetStructure))
            return abort(targetStructure, StructureToggleResult.INSTANCE_UNREGISTERED, data.getCause(),
                         data.getResponsible(), messageReceiver, null);

        final OptionalLong registrationResult =
            structureActivityManager.registerAnimation(targetStructure, animationType.requiresWriteAccess());
        if (registrationResult.isEmpty())
            return StructureToggleResult.BUSY;

        final long stamp = registrationResult.getAsLong();

        final StructureToggleResult isOpenable =
            canBeToggled(snapshot, targetStructure.getType(), data.getNewCuboid(), data.getActionType());

        if (isOpenable != StructureToggleResult.SUCCESS)
            return abort(targetStructure, isOpenable, data.getCause(), data.getResponsible(), messageReceiver, stamp);

        final IStructureEventTogglePrepare prepareEvent = callTogglePrepareEvent(data);
        if (prepareEvent.isCancelled())
            return abort(targetStructure, StructureToggleResult.CANCELLED, data.getCause(), data.getResponsible(),
                         messageReceiver, stamp);

        final @Nullable IPlayer responsiblePlayer =
            data.getCause().equals(StructureActionCause.PLAYER) ? data.getResponsible() : null;
        if (!isLocationEmpty(data.getNewCuboid(), snapshot.getCuboid(), responsiblePlayer, snapshot.getWorld()))
            return abort(targetStructure, StructureToggleResult.OBSTRUCTED, data.getCause(), data.getResponsible(),
                         messageReceiver, stamp);

        if (!canBreakBlocksBetweenLocs(snapshot, data.getNewCuboid(), data.getResponsible()))
            return abort(targetStructure, StructureToggleResult.NO_PERMISSION, data.getCause(), data.getResponsible(),
                         messageReceiver, stamp);

        final boolean scheduled = registerBlockMover(targetStructure, data, component, player, animationType, stamp);
        if (!scheduled)
            return StructureToggleResult.ERROR;

        executor.runAsync(() -> callToggleStartEvent(targetStructure, data));

        return StructureToggleResult.SUCCESS;
    }

    StructureToggleResult toggle(AbstractStructure structure, StructureAnimationRequest request, IPlayer responsible)
    {
        final StructureSnapshot snapshot;
        final AnimationRequestData data;
        final IAnimationComponent component;

        structure.getLock().readLock().lock();
        try
        {
            if (request.isSkipAnimation() && !structure.canSkipAnimation())
                return abort(
                    structure, StructureToggleResult.ERROR, request.getCause(), responsible,
                    request.getMessageReceiver(),
                    null);

            if (exceedSizeLimit(structure, responsible))
                return abort(
                    structure, StructureToggleResult.TOO_BIG, request.getCause(), responsible,
                    request.getMessageReceiver(), null);

            final Optional<Cuboid> newCuboid = structure.getPotentialNewCoordinates();
            if (newCuboid.isEmpty())
                return abort(
                    structure, StructureToggleResult.ERROR, request.getCause(), responsible,
                    request.getMessageReceiver(),
                    null);

            final double animationTime = structure.getAnimationTime(request.getTime());
            snapshot = structure.getSnapshot();

            data = movementRequestDataFactory.newToggleRequestData(
                snapshot, request.getCause(), animationTime, request.isSkipAnimation(),
                request.isPreventPerpetualMovement(), newCuboid.get(), responsible,
                request.getAnimationType(), request.getActionType());
            component = structure.constructAnimationComponent(data);
        }
        finally
        {
            structure.getLock().readLock().unlock();
        }
        return toggle(
            snapshot, structure, data, component, request.getMessageReceiver(), responsible,
            request.getAnimationType());
    }

    /**
     * Checks if this structure exceeds the size limit for the given player.
     * <p>
     * See {@link LimitsManager#exceedsLimit(IPlayer, Limit, int)}.
     *
     * @param player
     *     The player whose limit to compare against this structure's size.
     * @return True if {@link AbstractStructure#getBlockCount()} exceeds the {@link Limit#STRUCTURE_SIZE} for this
     * structure.
     */
    private boolean exceedSizeLimit(AbstractStructure structure, IPlayer player)
    {
        return limitsManager.exceedsLimit(player, Limit.STRUCTURE_SIZE, structure.getBlockCount());
    }

    /**
     * Checks if the owner of a structure can break blocks between 2 positions.
     * <p>
     * If the player is not allowed to break the block(s), they'll receive a message about this.
     *
     * @param structure
     *     The {@link IStructure} being opened.
     * @param cuboid
     *     The area of blocks to check.
     * @param responsible
     *     Who is responsible for the action.
     * @return True if the player is allowed to break the block(s).
     */
    public boolean canBreakBlocksBetweenLocs(IStructureConst structure, Cuboid cuboid, IPlayer responsible)
    {
        if (protectionCompatManager.canSkipCheck())
            return true;
        try
        {
            return executor.runOnMainThread(() -> canBreakBlocksBetweenLocs0(structure, cuboid, responsible))
                           .get(500, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            log.atSevere().withCause(e)
               .log("Failed to check if blocks can be broken in cuboid %s for user: '%s' for structure %s",
                    cuboid, responsible, structure);
            return false;
        }
    }

    private boolean canBreakBlocksBetweenLocs0(IStructureConst structure, Cuboid cuboid, IPlayer responsible)
    {
        // If the returned value is an empty Optional, the player is allowed to break blocks.
        return protectionCompatManager.canBreakBlocksBetweenLocs(responsible, cuboid.getMin(), cuboid.getMax(),
                                                                 structure.getWorld()).map(
            protectionCompat ->
            {
                log.atWarning().log("Player '%s' is not allowed to open structure '%s' (%d) here! Reason: %s",
                                    responsible, structure.getName(), structure.getUid(), protectionCompat);
                return false;
            }).orElse(true);
    }

    /**
     * Checks if an area is empty. "Empty" here means that there no blocks that are not air or liquid.
     *
     * @param newCuboid
     *     The {@link Cuboid} representing the area the structure will take up after the toggle.
     * @param currentCuboid
     *     The {@link Cuboid} representing the area the structure currently takes up. Any parts of the new cuboid
     *     overlapping this cuboid will be ignored.
     * @param player
     *     The {@link IPlayer} to notify of violations. May be null.
     * @param world
     *     The world to check the blocks in.
     * @return True if the area is not empty.
     */
    public boolean isLocationEmpty(Cuboid newCuboid, Cuboid currentCuboid, @Nullable IPlayer player, IWorld world)
    {
        final Vector3Di newMin = newCuboid.getMin();
        final Vector3Di newMax = newCuboid.getMax();
        final Vector3Di curMin = currentCuboid.getMin();
        final Vector3Di curMax = currentCuboid.getMax();

        boolean isEmpty = true;
        for (int xAxis = newMin.x(); xAxis <= newMax.x(); ++xAxis)
        {
            for (int yAxis = newMin.y(); yAxis <= newMax.y(); ++yAxis)
            {
                for (int zAxis = newMin.z(); zAxis <= newMax.z(); ++zAxis)
                {
                    // Ignore blocks that are currently part of the structure.
                    // It's expected and accepted for them to be in the way.
                    if (Util.between(xAxis, curMin.x(), curMax.x()) &&
                        Util.between(yAxis, curMin.y(), curMax.y()) &&
                        Util.between(zAxis, curMin.z(), curMax.z()))
                        continue;

                    if (!blockAnalyzer.isAirOrLiquid(locationFactory.create(world, xAxis, yAxis, zAxis)))
                    {
                        if (player == null)
                            return false;

                        glowingBlockSpawner
                            .builder().forPlayer(player).withColor(Color.RED).forDuration(Duration.ofSeconds(4))
                            .atPosition(xAxis + 0.5, yAxis, zAxis + 0.5).inWorld(world).build();
                        isEmpty = false;
                    }
                }
            }
        }
        return isEmpty;
    }

    /**
     * Gets the number of blocks this structure can move in the given direction. If set, it won't go further than
     * {@link IDiscreteMovement#getBlocksToMove()}.
     * <p>
     * TODO: This isn't used anywhere? Perhaps either centralize its usage or remove it.
     *
     * @param vec
     *     Which direction to count the number of available blocks in.
     * @param player
     *     The player for whom to check. May be null.
     * @param world
     *     The world to check the blocks in.
     * @param cuboid
     *     The {@link Cuboid} representing the area the structure currently takes up.
     * @param blocksToMove
     *     The number of blocks to try move.
     * @return Gets the number of blocks this structure can move in the given direction.
     */
    public int getBlocksInDir(Vector3Di vec, @Nullable IPlayer player, IWorld world, Cuboid cuboid, int blocksToMove)
    {
        final Vector3Di curMin = cuboid.getMin();
        final Vector3Di curMax = cuboid.getMax();

        final int startY = vec.y() == 0 ? curMin.y() : vec.y() == 1 ? curMax.y() + 1 : curMin.y() - 1;

        // Structures cannot start outside the world limit.
        if (startY < 0 || startY > 255)
            return 0;

        int startX;
        int startZ;
        int endX;
        int endY;
        int endZ;
        startX = vec.x() == 0 ? curMin.x() : vec.x() == 1 ? curMax.x() + 1 : curMin.x() - 1;
        startZ = vec.z() == 0 ? curMin.z() : vec.z() == 1 ? curMax.z() + 1 : curMin.z() - 1;

        endX = vec.x() == 0 ? curMax.x() : startX;
        endY = vec.y() == 0 ? curMax.y() : startY;
        endZ = vec.z() == 0 ? curMax.z() : startZ;


        Vector3Di locA = new Vector3Di(startX, startY, startZ);
        Vector3Di locB = new Vector3Di(endX, endY, endZ);

        // xLen and zLen describe the length of the structure in the x and the z direction respectively.
        // If the rotation direction and the blocksToMove variable are defined, use the blocksToMove variable instead.
        final int xLen = blocksToMove < 1 ? (curMax.x() - curMin.x()) + 1 : blocksToMove;
        int yLen = blocksToMove < 1 ? (curMax.y() - curMin.y()) + 1 : blocksToMove;
        final int zLen = blocksToMove < 1 ? (curMax.z() - curMin.z()) + 1 : blocksToMove;

        yLen = vec.y() == 1 ? Math.min(255, curMax.y() + yLen) :
               vec.y() == -1 ? Math.max(0, curMin.y() - yLen) : yLen;

        // The maxDist is the number of blocks to check in a direction. This is either getBlocksToMove if it that has
        // been specified. If it hasn't, it's the length of the structure in the provided direction.
        final int maxDist = blocksToMove > 0 ? blocksToMove :
                            Math.abs(vec.x() * xLen + vec.y() * yLen + vec.z() * zLen);

        int ret = 0;
        int steps = 0;
        boolean obstructed = false;
        while (steps < maxDist)
        {
            final boolean isEmpty = isLocationEmpty(new Cuboid(locA, locB), cuboid, player, world);
            if (!isEmpty)
            {
                obstructed = true;
                if (player == null)
                    break;
            }
            if (!obstructed) // There is no point in checking how many blocks are available behind an obstruction.
                ++ret;
            locA = locA.add(vec.x(), vec.y(), vec.z());
            locB = locB.add(vec.x(), vec.y(), vec.z());
            ++steps;
        }

        // If the direction was in a negative direction, make sure the output is negative as well.
        return (vec.x() == -1 || vec.y() == -1 || vec.z() == -1) ? -1 * ret : ret;
    }

    /**
     * Checks if a {@link AbstractStructure} can be toggled or not.
     * <p>
     * It checks the following items:
     * <p>
     * - The {@link AbstractStructure} is not already being animated.
     * <p>
     * - The {@link AbstractStructure} is enabled.
     * <p>
     * - The {@link AbstractStructure} is not locked.
     * <p>
     * - All chunks this {@link AbstractStructure} might interact with are loaded.
     *
     * @param structure
     *     The {@link AbstractStructure}.
     * @param type
     *     The type of structure being toggled.
     * @param newCuboid
     *     The target cuboid of the structure.
     * @param actionType
     *     The type of action.
     * @return {@link StructureToggleResult#SUCCESS} if it can be toggled
     */
    private StructureToggleResult canBeToggled(
        IStructureConst structure, StructureType type, Cuboid newCuboid, StructureActionType actionType)
    {
        if (actionType == StructureActionType.OPEN && !structure.isOpenable())
            return StructureToggleResult.ALREADY_OPEN;
        else if (actionType == StructureActionType.CLOSE && !structure.isCloseable())
            return StructureToggleResult.ALREADY_CLOSED;

        if (structure.isLocked())
            return StructureToggleResult.LOCKED;

        if (!structureTypeManager.isStructureTypeEnabled(type))
            return StructureToggleResult.TYPE_DISABLED;

        if (!chunksLoaded(structure, newCuboid))
        {
            log.atWarning().log("Chunks for structure '%s' could not be not loaded!", structure.getName());
            return StructureToggleResult.ERROR;
        }

        return StructureToggleResult.SUCCESS;
    }

    private boolean chunksLoaded(IStructureConst structure, Cuboid newCuboid)
    {
        final var mode = config.loadChunksForToggle() ?
                         IChunkLoader.ChunkLoadMode.ATTEMPT_LOAD : IChunkLoader.ChunkLoadMode.VERIFY_LOADED;

        final var result = chunkLoader.checkChunks(structure.getWorld(), structure.getCuboid(), mode);
        if (result == IChunkLoader.ChunkLoadResult.FAIL)
            return false;

        return chunkLoader.checkChunks(structure.getWorld(), newCuboid, mode) != IChunkLoader.ChunkLoadResult.FAIL;
    }
}

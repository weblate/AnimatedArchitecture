package nl.pim16aap2.animatedarchitecture.core.structures;

import com.google.common.flogger.StackSize;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Locked;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.animatedarchitecture.core.animation.AnimationRequestData;
import nl.pim16aap2.animatedarchitecture.core.animation.IAnimationComponent;
import nl.pim16aap2.animatedarchitecture.core.animation.StructureActivityManager;
import nl.pim16aap2.animatedarchitecture.core.api.IChunkLoader;
import nl.pim16aap2.animatedarchitecture.core.api.IConfig;
import nl.pim16aap2.animatedarchitecture.core.api.IExecutor;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.api.IRedstoneManager;
import nl.pim16aap2.animatedarchitecture.core.api.IWorld;
import nl.pim16aap2.animatedarchitecture.core.api.factories.IPlayerFactory;
import nl.pim16aap2.animatedarchitecture.core.events.StructureActionCause;
import nl.pim16aap2.animatedarchitecture.core.events.StructureActionType;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.DatabaseManager;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.IPropertyContainerConst;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.IPropertyHolder;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.IPropertyValue;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.IStructureComponent;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.IStructureWithOpenStatus;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.Property;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.PropertyContainer;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.PropertyContainerSnapshot;
import nl.pim16aap2.animatedarchitecture.core.structures.properties.PropertyScope;
import nl.pim16aap2.animatedarchitecture.core.util.Cuboid;
import nl.pim16aap2.animatedarchitecture.core.util.FutureUtil;
import nl.pim16aap2.animatedarchitecture.core.util.LocationUtil;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.Rectangle;
import nl.pim16aap2.animatedarchitecture.core.util.vector.IVector3D;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;
import nl.pim16aap2.util.LazyValue;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents the abstract base all structure types should extend.
 * <p>
 * Please read the documentation of {@link nl.pim16aap2.animatedarchitecture.core.structures} for more information about
 * the structure system.
 * <p>
 * Changes made to this class will not automatically be updated in the database. To update the state in the database,
 * you can use either the {@link #syncData()} method or the {@link #syncDataAsync()} method.
 */
@EqualsAndHashCode
@Flogger
@ThreadSafe
public final class Structure implements IStructureConst, IPropertyHolder
{
    private static final double DEFAULT_ANIMATION_SPEED = 1.5D;

    /**
     * The lock as used by both the {@link StructureBase} and this class.
     */
    @EqualsAndHashCode.Exclude
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Getter
    private final long uid;

    @Getter
    private final IWorld world;

    @GuardedBy("lock")
    private final PropertyContainer propertyContainer;

    @EqualsAndHashCode.Exclude
    private final StructureAnimationRequestBuilder structureToggleRequestBuilder;

    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"))
    private Vector3Di powerBlock;

    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"))
    private String name;

    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"))
    private Cuboid cuboid;

    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"))
    private MovementDirection openDirection;

    /**
     * Represents the locked status of this structure. True = locked, False = unlocked.
     */
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"))
    private boolean isLocked;

    @Getter
    @EqualsAndHashCode.Exclude
    private final StructureOwner primeOwner;

    @EqualsAndHashCode.Exclude
    private final IRedstoneManager redstoneManager;

    @EqualsAndHashCode.Exclude
    private final StructureActivityManager structureActivityManager;

    @EqualsAndHashCode.Exclude
    private final IChunkLoader chunkLoader;

    @EqualsAndHashCode.Exclude
    @GuardedBy("lock")
    private final Map<UUID, StructureOwner> owners;

    @EqualsAndHashCode.Exclude
    @GuardedBy("lock")
    @Getter(onMethod_ = @Locked.Read("lock"), value = AccessLevel.PACKAGE)
    private final Map<UUID, StructureOwner> ownersView;

    @EqualsAndHashCode.Exclude
    private final IConfig config;

    @EqualsAndHashCode.Exclude
    private final ILocalizer localizer;

    @EqualsAndHashCode.Exclude
    private final StructureRegistry structureRegistry;

    @EqualsAndHashCode.Exclude
    private final StructureToggleHelper structureOpeningHelper;

    @EqualsAndHashCode.Exclude
    private final IExecutor executor;

    @EqualsAndHashCode.Exclude
    private final DatabaseManager databaseManager;

    @EqualsAndHashCode.Exclude
    private final IPlayerFactory playerFactory;

    private final StructureType type;

    private final LazyValue<Rectangle> lazyAnimationRange;
    private final LazyValue<Double> lazyAnimationCycleDistance;
    private final LazyValue<StructureSnapshot> lazyStructureSnapshot;
    private final LazyValue<PropertyContainerSnapshot> lazyPropertyContainerSnapshot;

    @EqualsAndHashCode.Include
    @GuardedBy("lock")
    private final IStructureComponent component;

    @Deprecated
    private StructureSerializer<?> serializer;

    @AssistedInject
    Structure(
        @Assisted long uid,
        @Assisted String name,
        @Assisted Cuboid cuboid,
        @Assisted Vector3Di powerBlock,
        @Assisted IWorld world,
        @Assisted boolean isLocked,
        @Assisted MovementDirection openDirection,
        @Assisted StructureOwner primeOwner,
        @Assisted @Nullable Map<UUID, StructureOwner> owners,
        @Assisted PropertyContainer propertyContainer,
        @Assisted StructureType type,
        @Assisted IStructureComponent component,
        ILocalizer localizer,
        DatabaseManager databaseManager,
        StructureRegistry structureRegistry,
        StructureToggleHelper structureOpeningHelper,
        StructureAnimationRequestBuilder structureToggleRequestBuilder,
        IPlayerFactory playerFactory,
        IExecutor executor,
        IRedstoneManager redstoneManager,
        StructureActivityManager structureActivityManager,
        IChunkLoader chunkLoader,
        IConfig config)
    {
        this.component = Objects.requireNonNull(component);

        this.uid = uid;
        this.name = name;
        this.cuboid = cuboid;
        this.powerBlock = powerBlock;
        this.world = world;
        this.isLocked = isLocked;
        this.openDirection = openDirection;
        this.primeOwner = primeOwner;
        this.propertyContainer = propertyContainer;
        this.redstoneManager = redstoneManager;
        this.structureActivityManager = structureActivityManager;
        this.chunkLoader = chunkLoader;

        this.owners = new HashMap<>();
        if (owners == null)
            this.owners.put(primeOwner.playerData().getUUID(), primeOwner);
        else
            this.owners.putAll(owners);
        this.ownersView = Collections.unmodifiableMap(this.owners);

        this.config = config;
        this.localizer = localizer;
        this.databaseManager = databaseManager;
        this.structureRegistry = structureRegistry;
        this.structureOpeningHelper = structureOpeningHelper;
        this.structureToggleRequestBuilder = structureToggleRequestBuilder;
        this.playerFactory = playerFactory;
        this.executor = executor;

        this.type = type;

        lazyAnimationRange = new LazyValue<>(this::calculateAnimationRange);
        lazyAnimationCycleDistance = new LazyValue<>(this::calculateAnimationCycleDistance);

        lazyStructureSnapshot = new LazyValue<>(this::createNewSnapshot);
        lazyPropertyContainerSnapshot = new LazyValue<>(this::newPropertyContainerSnapshot);
    }

    @Override
    public StructureType getType()
    {
        return type;
    }

    @Locked.Read("lock")
    private PropertyContainerSnapshot newPropertyContainerSnapshot()
    {
        return propertyContainer.snapshot();
    }

    @Override
    @Locked.Read("lock")
    public MovementDirection getCycledOpenDirection()
    {
        return component.getCycledOpenDirection(this);
    }

    /**
     * Gets the animation time (in seconds) of this structure.
     * <p>
     * This basically returns max(target, {@link #getMinimumAnimationTime()}), logging a message in case the target time
     * is too low.
     *
     * @param target
     *     The target time.
     * @return The target time if it is bigger than the minimum time, otherwise the minimum.
     */
    @Locked.Read("lock")
    private double calculateAnimationTime(double target)
    {
        return component.calculateAnimationTime(this, target);
    }

    @Locked.Read("lock")
    private Rectangle calculateAnimationRange()
    {
        return component.calculateAnimationRange(this);
    }

    @Locked.Read("lock")
    private double calculateAnimationCycleDistance()
    {
        return component.calculateAnimationCycleDistance(this);
    }

    /**
     * Constructs a new animation component for this structure.
     *
     * @param data
     *     The data to construct the animation component with.
     * @return The constructed animation component.
     */
    @Locked.Read("lock")
    public IAnimationComponent constructAnimationComponent(AnimationRequestData data)
    {
        return this.component.constructAnimationComponent(this, data);
    }

    /**
     * Finds the new minimum and maximum coordinates (represented by a {@link Cuboid}) of this structure that would be
     * the result of toggling it.
     *
     * @return The {@link Cuboid} that would represent the structure if it was toggled right now.
     */
    @Locked.Read("lock")
    public Optional<Cuboid> getPotentialNewCoordinates()
    {
        return component.getPotentialNewCoordinates(this);
    }

    /**
     * Gets the animation time for this structure.
     * <p>
     * The animation time is calculated using this structure's {@link #getBaseAnimationTime()}, its
     * {@link #getMinimumAnimationTime()}, and the multiplier for this type as described by
     * {@link IConfig#getAnimationTimeMultiplier(StructureType)}.
     * <p>
     * If the target is not null, the returned value will simply be the maximum value of the target and
     * {@link #getBaseAnimationTime()}. Note that the time multiplier is ignored in this case.
     *
     * @param target
     *     The target time. When null, {@link #getBaseAnimationTime()} is used.
     * @return The animation time for this structure in seconds.
     */
    @Locked.Read("lock")
    double getAnimationTime(@Nullable Double target)
    {
        final double realTarget = (target != null) ?
            target :
            config.getAnimationTimeMultiplier(getType()) * getBaseAnimationTime();
        return calculateAnimationTime(realTarget);
    }

    /**
     * Gets the distance traveled per animation by the animated block that travels the furthest.
     * <p>
     * For example, for a circular object, this will be a block on the edge, as these blocks travel further per
     * revolution than the blocks closer to the center of the circle.
     * <p>
     * This method looks at the distance per animation cycle. The exact definition of a cycle depends on the
     * implementation of the structure. For a big door, for example, a cycle could be a single toggle, or a quarter
     * circle. To keep things easy, a revolving structure (which may not have a definitive end to its animation) could
     * define a cycle as quarter circle as well.
     * <p>
     * The distance value is used to calculate {@link Structure#getMinimumAnimationTime()} and
     * {@link Structure#getBaseAnimationTime()}.
     *
     * @return The longest distance traveled by an animated block measured in blocks.
     */
    public double getAnimationCycleDistance()
    {
        return lazyAnimationCycleDistance.get();
    }

    /**
     * Gets the rectangle describing the limits within an animation of this door takes place.
     * <p>
     * At no point during an animation will any animated block leave this cuboid, though not guarantees are given
     * regarding how tight the cuboid fits around the animated blocks.
     *
     * @return The animation range.
     */
    @Override
    public Rectangle getAnimationRange()
    {
        return lazyAnimationRange.get();
    }

    /**
     * Invalidates all cached animation data.
     * <p>
     * Certain actions may result in the animation range and animation cycle distance being changed. This method has to
     * be called when that may happen.
     */
    private void invalidateAnimationData()
    {
        lazyAnimationRange.reset();
        lazyAnimationCycleDistance.reset();
        invalidateBasicData();
    }

    /**
     * Has to be called when any "basic data" is changed.
     * <p>
     * "Basic data" here means any data that does not affect the animation in any way that only exists in this class.
     * The name of the structure is one such example. Any type of "basic data" also refers to "All data", but not
     * necessarily the other way round.
     * <p>
     * If the animation may be affected in any way, use {@link #invalidateAnimationData()} instead.
     */
    private void invalidateBasicData()
    {
        lazyStructureSnapshot.reset();
    }

    /**
     * The default speed of the animation in blocks/second, as measured by the fastest-moving block in the structure.
     */
    private double getDefaultAnimationSpeed()
    {
        return DEFAULT_ANIMATION_SPEED;
    }

    /**
     * Gets the lower time limit for an animation.
     * <p>
     * Because animated blocks have a speed limit, as determined by {@link IConfig#maxBlockSpeed()}, there is also a
     * minimum amount of time for its animation.
     * <p>
     * The exact time limit depends on the shape, size, and type of structure.
     *
     * @return The lower animation time limit for this structure in seconds.
     */
    @Override
    public double getMinimumAnimationTime()
    {
        return getAnimationCycleDistance() / config.maxBlockSpeed();
    }

    /**
     * Gets the base animation time for this structure.
     * <p>
     * This is the animation time without any multipliers applied to it.
     *
     * @return The base animation time for this structure in seconds.
     */
    public double getBaseAnimationTime()
    {
        return getAnimationCycleDistance() /
            Math.min(getDefaultAnimationSpeed(), config.maxBlockSpeed());
    }

    /**
     * Checks if this structure can be opened instantly (i.e. skip the animation).
     * <p>
     * This describes whether skipping the animation actually does anything. When a structure can skip its animation, it
     * means that the actual toggle has an effect on the world and as such toggling it without an animation does
     * something.
     * <p>
     * Structures that do not have any effect other than their animation because all their blocks start and stop at
     * exactly the same place (i.e. flags) cannot skip their animation because skipping the animation would mean
     * removing the blocks of the structure from their locations and then placing them back at exactly the same
     * position. This would be a waste of performance for both the server and the client and as such should be
     * prevented.
     *
     * @return True, if this structure can skip its animation.
     */
    @Locked.Read("lock")
    public boolean canSkipAnimation()
    {
        return component.canSkipAnimation(this);
    }

    /**
     * Attempts to toggle a structure. Think twice before using this method. Instead, please look at
     * {@link StructureAnimationRequestBuilder}.
     *
     * @param request
     *     The toggle request to process.
     * @param responsible
     *     Who is responsible for this structure. Either the player who directly toggled it (via a command or the GUI),
     *     or the prime owner when this data is not available.
     * @return The result of the attempt.
     */
    final CompletableFuture<StructureToggleResult> toggle(StructureAnimationRequest request, IPlayer responsible)
    {
        return structureOpeningHelper.toggle(this, request, responsible);
    }

    /**
     * @return True if this structure base should ignore all redstone interaction.
     */
    private boolean shouldIgnoreRedstone()
    {
        return uid < 1 || !config.isRedstoneEnabled();
    }

    private boolean isChunkLoaded(IVector3D position)
    {
        return chunkLoader.checkChunk(world, position, IChunkLoader.ChunkLoadMode.VERIFY_LOADED) ==
            IChunkLoader.ChunkLoadResult.PASS;
    }

    /**
     * Handles the chunk of the rotation point being loaded.
     */
    @Locked.Read("lock")
    public void onChunkLoad()
    {
        if (shouldIgnoreRedstone())
            return;

        if (!isChunkLoaded(powerBlock))
            return;

        // TODO: We should probably make the checks a bit more comprehensive.
        //       Instead of checking if a few chunks with specific points are loaded, we should check if all chunks
        //       that the structure will interact with are loaded.
        final IPropertyValue<Vector3Di> rotationPoint = getPropertyValue(Property.ROTATION_POINT);
        if (rotationPoint.isSet() && !isChunkLoaded(Objects.requireNonNull(rotationPoint.value())))
            return;

        verifyRedstoneState();
    }

    @Locked.Read("lock")
    public void verifyRedstoneState()
    {
        if (shouldIgnoreRedstone())
            return;

        if (!isChunkLoaded(powerBlock))
            return;

        final var result = redstoneManager.isBlockPowered(world, powerBlock);
        if (result == IRedstoneManager.RedstoneStatus.DISABLED)
            return;

        if (result == IRedstoneManager.RedstoneStatus.UNPOWERED && component.canMovePerpetually(this))
        {
            structureActivityManager.stopAnimatorsWithWriteAccess(getUid());
            return;
        }

        onRedstoneChange(result == IRedstoneManager.RedstoneStatus.POWERED);
    }

    @Locked.Read("lock")
    void onRedstoneChange(boolean isPowered)
    {
        if (shouldIgnoreRedstone())
            return;

        final StructureActionType actionType;
        if (component.canMovePerpetually(this))
        {
            if (!isPowered)
            {
                structureActivityManager.stopAnimatorsWithWriteAccess(getUid());
                return;
            }
            actionType = StructureActionType.TOGGLE;
        }
        else if (component instanceof IStructureWithOpenStatus openable)
        {
            if (isPowered && openable.isOpenable())
            {
                actionType = StructureActionType.OPEN;
            }
            else if (!isPowered && openable.isCloseable())
            {
                actionType = StructureActionType.CLOSE;
            }
            else
            {
                log.atFinest().log(
                    "Aborted toggle attempt with %s redstone for openable structure: %s",
                    (isPowered ? "powered" : "unpowered"), this);
                return;
            }
        }
        else
        {
            log.atFinest().log(
                "Aborted toggle attempt with %s redstone for structure: %s",
                (isPowered ? "powered" : "unpowered"), this);
            return;
        }

        structureToggleRequestBuilder
            .builder()
            .structure(this)
            .structureActionCause(StructureActionCause.REDSTONE)
            .structureActionType(actionType)
            .messageReceiverServer()
            .responsible(playerFactory.create(getPrimeOwner().playerData()))
            .build()
            .execute()
            .exceptionally(FutureUtil::exceptionally);
    }

    @Locked.Read("lock")
    private StructureSnapshot createNewSnapshot()
    {
        return new StructureSnapshot(this);
    }

    @Override
    @Locked.Read("lock")
    public StructureSnapshot getSnapshot()
    {
        return lazyStructureSnapshot.get();
    }

    /**
     * Synchronizes this structure and its serialized type-specific data of an {@link Structure} with the database.
     * <p>
     * Unlike {@link #syncData()}, this method will not block the current thread to wait for 1) a read lock to be
     * obtained, 2) the snapshot to be created, and 3) the data to be serialized.
     */
    public CompletableFuture<DatabaseManager.ActionResult> syncDataAsync()
    {
        return CompletableFuture
            .supplyAsync(this::syncData)
            .thenCompose(Function.identity())
            .exceptionally(ex -> FutureUtil.exceptionally(ex, DatabaseManager.ActionResult.FAIL));
    }

    /**
     * Synchronizes this structure and its serialized type-specific data of an {@link Structure} with the database.
     *
     * @return The result of the synchronization.
     */
    @Locked.Read("lock")
    public CompletableFuture<DatabaseManager.ActionResult> syncData()
    {
        try
        {
            return databaseManager
                .syncStructureData(getSnapshot(), serializer.serializeTypeData(this))
                .exceptionally(ex -> FutureUtil.exceptionally(ex, DatabaseManager.ActionResult.FAIL));
        }
        catch (Exception e)
        {
            log.atSevere().withCause(e).log("Failed to sync data for structure: %s", getBasicInfo());
        }
        return CompletableFuture.completedFuture(DatabaseManager.ActionResult.FAIL);
    }

    /**
     * Ensures that the current thread can obtain a write lock.
     * <p>
     * For example, when a thread already holds a read lock, that thread may not (try to) obtain a write lock as well,
     * as this would result in a deadlock.
     *
     * @throws IllegalStateException
     *     When the current thread is not allowed to obtain a write lock.
     */
    private void assertWriteLockable()
    {
        if (lock.getReadHoldCount() > 0)
            throw new IllegalStateException(
                "Caught potential deadlock! Trying to obtain write lock while under read lock!");
    }

    /**
     * Use {@link #withWriteLock(Supplier)} instead.
     */
    @Locked.Write("lock")
    private <T> T withWriteLock0(boolean resetAnimationData, Supplier<T> supplier)
    {
        final T result = supplier.get();
        if (resetAnimationData)
            invalidateAnimationData();
        return result;
    }

    /**
     * Executes a supplier under a write lock.
     *
     * @param supplier
     *     The supplier to execute under the write lock.
     * @param <T>
     *     The return type of the supplier.
     * @return The value returned by the supplier.
     *
     * @throws IllegalStateException
     *     When the current thread may is not allowed to obtain a write lock.
     */
    @SuppressWarnings("unused")
    public <T> T withWriteLock(Supplier<T> supplier)
    {
        assertWriteLockable();
        return withWriteLock0(true, supplier);
    }

    /**
     * Use {@link #withWriteLock(Runnable)} instead.
     */
    @Locked.Write("lock")
    private void withWriteLock0(boolean resetAnimationData, Runnable runnable)
    {
        runnable.run();
        if (resetAnimationData)
            invalidateAnimationData();
    }

    private void withWriteLock(boolean resetAnimationData, Runnable runnable)
    {
        assertWriteLockable();
        withWriteLock0(resetAnimationData, runnable);
    }

    /**
     * Executes a Runnable under a write lock.
     *
     * @throws IllegalStateException
     *     When the current thread may is not allowed to obtain a write lock.
     */
    public void withWriteLock(Runnable runnable)
    {
        withWriteLock(true, runnable);
    }

    /**
     * Executes a supplier under a read lock.
     *
     * @param supplier
     *     The supplier to execute under the read lock.
     * @param <T>
     *     The return type of the supplier.
     * @return The value returned by the supplier.
     */
    // Obtaining a read lock won't cause a deadlock,
    // so no need to check it's possible.
    @Locked.Read("lock")
    @SuppressWarnings("unused")
    public <T> T withReadLock(Supplier<T> supplier)
    {
        return supplier.get();
    }

    /**
     * Executes a Runnable under a read lock.
     */
    // Obtaining a read lock won't cause a deadlock,
    // so no need to check it's possible.
    @Locked.Read("lock")
    @SuppressWarnings("unused")
    public void withReadLock(Runnable runnable)
    {
        runnable.run();
    }

    @Override
    @Locked.Read("lock")
    public String getBasicInfo()
    {
        return IStructureConst.super.getBasicInfo();
    }

    @Override
    @Locked.Read("lock")
    public String toString()
    {
        return uid + ": " + name + "\n"
            + formatLine("Type", getType())
            + formatLine("Component", component.getClass().getName())
            + formatLine("Cuboid", getCuboid())
            + formatLine("PowerBlock Position: ", getPowerBlock())
            + formatLine("PowerBlock Hash: ", LocationUtil.getChunkId(getPowerBlock()))
            + formatLine("World", getWorld())
            + formatLine("This structure is ", (isLocked ? "locked" : "unlocked"))
            + formatLine("OpenDirection", openDirection.name())
            + formatLine("Properties", propertyContainer);
    }

    private String formatLine(String name, @Nullable Object obj)
    {
        final String objString = obj == null ? "NULL" : obj.toString();
        return name + ": " + objString + "\n";
    }

    @Locked.Read("lock")
    public Collection<StructureOwner> getOwners()
    {
        final List<StructureOwner> ret = new ArrayList<>(owners.size());
        ret.addAll(owners.values());
        return ret;
    }

    @Locked.Read("lock")
    public Optional<StructureOwner> getOwner(UUID uuid)
    {
        return Optional.ofNullable(owners.get(uuid));
    }

    @Locked.Read("lock")
    public boolean isOwner(UUID uuid)
    {
        return owners.containsKey(uuid);
    }

    @Locked.Read("lock")
    public boolean isOwner(UUID uuid, PermissionLevel permissionLevel)
    {
        final @Nullable StructureOwner owner = owners.get(uuid);
        return owner != null && owner.permission().isLowerThanOrEquals(permissionLevel);
    }

    /**
     * Changes the position of this {@link Structure}. The min/max order of the positions doesn't matter.
     *
     * @param newCuboid
     *     The {@link Cuboid} representing the area the structure will take up from now on.
     */
    public void setCoordinates(Cuboid newCuboid)
    {
        withWriteLock(
            false,
            () ->
            {
                cuboid = newCuboid;
                invalidateAnimationData();
            });
    }

    @Locked.Write("lock")
    private void setPowerBlock0(Vector3Di pos)
    {
        invalidateBasicData();
        powerBlock = pos;
    }

    /**
     * Updates the position of the powerblock.
     *
     * @param pos
     *     The new position.
     */
    public void setPowerBlock(Vector3Di pos)
    {
        // TODO: Use nicer system than 0().
        assertWriteLockable();
        setPowerBlock0(pos);
        verifyRedstoneState();
    }

    /**
     * Changes the name of the structure.
     *
     * @param name
     *     The new name of this structure.
     */
    public void setName(String name)
    {
        withWriteLock(
            false,
            () ->
            {
                this.name = name;
                invalidateBasicData();
            });
    }

    /**
     * Sets the {@link MovementDirection} this {@link Structure} will open if currently closed.
     * <p>
     * Note that if it's currently in the open status, it is supposed go in the opposite direction, as the closing
     * direction is the opposite of the opening direction.
     *
     * @param openDir
     *     The {@link MovementDirection} this {@link Structure} will open in.
     */
    public void setOpenDirection(MovementDirection openDir)
    {
        withWriteLock(
            false,
            () ->
            {
                openDirection = openDir;
                invalidateAnimationData();
            });
    }

    @Locked.Write("lock")
    private void setLocked0(boolean locked)
    {
        isLocked = locked;
        invalidateBasicData();
    }

    /**
     * Changes the lock status of this structure. Locked structures cannot be opened.
     *
     * @param locked
     *     New lock status.
     */
    public void setLocked(boolean locked)
    {
        assertWriteLockable();
        setLocked0(locked);
        verifyRedstoneState();
    }

    @Locked.Write("lock")
    private @Nullable StructureOwner removeOwner0(UUID ownerUUID)
    {
        if (primeOwner.playerData().getUUID().equals(ownerUUID))
        {
            log.atSevere().withStackTrace(StackSize.FULL).log(
                "Failed to remove owner: '%s' as owner from structure: '%d'" +
                    " because removing an owner with a permission level of 0 is not allowed!",
                primeOwner.playerData(),
                this.getUid()
            );
            return null;
        }
        final @Nullable StructureOwner removed = owners.remove(ownerUUID);
        if (removed != null)
            invalidateBasicData();
        return removed;
    }

    /**
     * Removes an owner from this structure.
     * <p>
     * If the owner to remove is the prime owner, the owner will not be removed and a message will be logged.
     *
     * @param ownerUUID
     *     The UUID of the owner to remove.
     * @return The owner that was removed. If no owner with that UUID exists, or if that specific owner could not be
     * removed, the result will be null.
     *
     * @throws IllegalStateException
     *     When the current thread is not allowed to obtain a write lock.
     */
    @Nullable StructureOwner removeOwner(UUID ownerUUID)
    {
        assertWriteLockable();
        return removeOwner0(ownerUUID);
    }

    @Locked.Write("lock")
    private boolean addOwner0(StructureOwner structureOwner)
    {
        if (structureOwner.permission() == PermissionLevel.CREATOR)
        {
            log.atSevere().withStackTrace(StackSize.FULL).log(
                "Failed to add Owner '%s' as owner to structure: %d because a permission level of 0 is not allowed!",
                structureOwner.playerData(),
                this.getUid()
            );
            return false;
        }
        owners.put(structureOwner.playerData().getUUID(), structureOwner);
        invalidateBasicData();
        return true;
    }

    /**
     * Adds an owner to this structure.
     * <p>
     * If the added owner has a permission level of 0 (i.e. PermissionLevel#CREATOR), the owner will not be added and a
     * message will be logged.
     * <p>
     * If the added owner is already an owner, the existing owner entry will be updated.
     *
     * @param structureOwner
     *     The owner to add.
     * @return True if the owner was added.
     *
     * @throws IllegalStateException
     *     When the current thread is not allowed to obtain a write lock.
     */
    boolean addOwner(StructureOwner structureOwner)
    {
        assertWriteLockable();
        return addOwner0(structureOwner);
    }

    /**
     * Changes the position of this {@link Structure}. The min/max order of the positions does not matter.
     *
     * @param posA
     *     The first new position.
     * @param posB
     *     The second new position.
     */
    public void setCoordinates(Vector3Di posA, Vector3Di posB)
    {
        setCoordinates(new Cuboid(posA, posB));
    }

    @Override
    public IPropertyContainerConst getPropertyContainerSnapshot()
    {
        return lazyPropertyContainerSnapshot.get();
    }

    @Override
    @Locked.Read("lock")
    public <T> IPropertyValue<T> getPropertyValue(Property<T> property)
    {
        return propertyContainer.getPropertyValue(property);
    }

    @Override
    @Locked.Read("lock")
    public boolean hasProperty(Property<?> property)
    {
        return propertyContainer.hasProperty(property);
    }

    @Override
    @Locked.Read("lock")
    public boolean hasProperties(Collection<Property<?>> properties)
    {
        return propertyContainer.hasProperties(properties);
    }

    @Override
    public <T> IPropertyValue<T> setPropertyValue(Property<T> property, @Nullable T value)
    {
        assertWriteLockable();
        return setPropertyValue0(property, value);
    }

    @Locked.Write("lock")
    private <T> IPropertyValue<T> setPropertyValue0(Property<T> property, @Nullable T value)
    {
        final var ret = propertyContainer.setPropertyValue(property, value);
        handlePropertyChange(property);
        return ret;
    }

    /**
     * Handles a change in a property.
     * <p>
     * This will invalidate any cached data in the scope of the property.
     *
     * @param property
     *     The property that changed.
     */
    @GuardedBy("lock")
    private void handlePropertyChange(Property<?> property)
    {
        lazyPropertyContainerSnapshot.reset();
        property.getPropertyScopes().forEach(this::handlePropertyScopeChange);
        invalidateBasicData();
    }

    /**
     * Handles a change in a property scope.
     *
     * @param scope
     *     The scope that changed.
     */
    @GuardedBy("lock")
    private void handlePropertyScopeChange(PropertyScope scope)
    {
        switch (scope)
        {
            case REDSTONE -> verifyRedstoneState();
            case ANIMATION -> invalidateAnimationData();
            default -> throw new IllegalArgumentException("Unknown property scope: '" + scope + "'!");
        }
    }

    @AssistedFactory
    interface IFactory
    {
        Structure create(
            long structureUID,
            String name,
            Cuboid cuboid,
            Vector3Di powerBlock,
            IWorld world,
            boolean isLocked,
            MovementDirection openDirection,
            StructureOwner primeOwner,
            @Nullable Map<UUID, StructureOwner> structureOwners,
            PropertyContainer propertyContainer,
            StructureType type,
            IStructureComponent component
        );
    }
}

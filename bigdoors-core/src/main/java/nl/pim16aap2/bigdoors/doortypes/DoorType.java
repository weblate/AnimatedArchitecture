package nl.pim16aap2.bigdoors.doortypes;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorSerializer;
import nl.pim16aap2.bigdoors.managers.DoorTypeManager;
import nl.pim16aap2.bigdoors.tooluser.creator.Creator;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This class represents a type of Door. "Door" in this case, refers to any kind of animated object, so not necessarily
 * a door.
 *
 * @author Pim
 */
public abstract class DoorType implements IDoorType
{
    @Getter
    protected final @NonNull String pluginName;

    @Getter
    protected final @NonNull String simpleName;

    @Getter
    protected final int typeVersion;

    @Getter
    protected final String translationName;

    @Getter
    private final @NonNull String fullName;

    @Getter
    private final @NonNull List<RotateDirection> validOpenDirections;

    private final @Nullable DoorSerializer<?> doorSerializer;

    /**
     * Constructs a new {@link DoorType}. Don't forget to register it using {@link DoorTypeManager#registerDoorType(DoorType)}.
     *
     * @param pluginName  The name of the plugin that owns this {@link DoorType}.
     * @param simpleName  The 'simple' name of this {@link DoorType}. E.g. "Flag", or "Windmill".
     * @param typeVersion The version of this {@link DoorType}. Note that changing the version results in a completely
     *                    new {@link DoorType}, as far as the database is concerned. This fact can be used if the
     *                    parameters of the constructor for this type need to be changed.
     */
    protected DoorType(final @NonNull String pluginName, final @NonNull String simpleName, final int typeVersion,
                       final @NonNull List<RotateDirection> validOpenDirections)
    {
        this.pluginName = pluginName;
        this.simpleName = simpleName.toLowerCase();
        this.typeVersion = typeVersion;
        this.validOpenDirections = validOpenDirections;
        translationName = "DOORTYPE_" + simpleName.toUpperCase();
        fullName = String.format("%s_%s_%d", getPluginName(), getSimpleName(), getTypeVersion()).toLowerCase();

        DoorSerializer<?> serializer;
        try
        {
            serializer = new DoorSerializer<>(getDoorClass());
        }
        catch (Exception ex)
        {
            serializer = null;
            BigDoors.get().getPLogger().logThrowable(ex, "Failed to intialize serializer for type: " + getFullName());
        }
        doorSerializer = serializer;
    }

    /**
     * Gets the {@link DoorSerializer} for this type.
     *
     * @return The {@link DoorSerializer}.
     */
    public @NonNull Optional<DoorSerializer<?>> getDoorSerializer()
    {
        return Optional.ofNullable(doorSerializer);
    }

    @Override public final boolean isValidOpenDirection(final @NonNull RotateDirection rotateDirection)
    {
        return validOpenDirections.contains(rotateDirection);
    }

    /**
     * Gets the main door class of the type.
     *
     * @return THe class of the door.
     */
    public abstract @NonNull Class<? extends AbstractDoorBase> getDoorClass();

    /**
     * Creates (and registers) a new {@link Creator} for this type.
     *
     * @param player The player who will own the {@link Creator}.
     * @return The newly created {@link Creator}.
     */
    public abstract @NonNull Creator getCreator(@NonNull IPPlayer player);

    /**
     * Creates (and registers) a new {@link Creator} for this type.
     *
     * @param player The player who will own the {@link Creator}.
     * @param name   The name that will be given to the door.
     * @return The newly created {@link Creator}.
     */
    public abstract @NonNull Creator getCreator(@NonNull IPPlayer player, @Nullable String name);

    @Override
    public final @NonNull String toString()
    {
        return getPluginName() + ":" + getSimpleName() + ":" + getTypeVersion();
    }

    @Override
    public final int hashCode()
    {
        // There may only ever exist 1 instance of each DoorType.
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj)
    {
        // There may only ever exist 1 instance of each DoorType.
        return super.equals(obj);
    }
}

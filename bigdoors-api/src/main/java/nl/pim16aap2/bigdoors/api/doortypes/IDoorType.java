package nl.pim16aap2.bigdoors.api.doortypes;

import nl.pim16aap2.bigdoors.api.doors.IDoorBase;
import nl.pim16aap2.bigdoors.api.util.RotateDirection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDoorType
{
    /**
     * Checks if a given {@link RotateDirection} is valid for this type.
     *
     * @param rotateDirection The {@link RotateDirection} to check.
     * @return True if the provided {@link RotateDirection} is valid for this type, otherwise false.
     */
    boolean isValidOpenDirection(@NotNull RotateDirection rotateDirection);

    /**
     * Gets the name of the plugin that owns this {@link IDoorType}.
     *
     * @return The name of the plugin that owns this {@link IDoorType}.
     */
    @NotNull String getPluginName();

    /**
     * Gets the name of this {@link IDoorType}. Note that this is always in lower case!
     *
     * @return The name of this {@link IDoorType}.
     */
    @NotNull String getSimpleName();

    /**
     * Gets the version of this {@link IDoorType}. Note that changing the version creates a whole new {@link IDoorType}
     * and you'll have to take care of the transition.
     *
     * @return The version of this {@link IDoorType}.
     */
    int getTypeVersion();

    /**
     * Obtains the value of this type that represents the key in the translation system.
     *
     * @return The value of this type that represents the key in the translation system.
     */
    @NotNull String getTranslationName();

    /**
     * The fully-qualified name of this {@link IDoorType}.
     */
    @NotNull String getFullName();

    /**
     * Gets a list of all theoretically valid {@link RotateDirection} for this given type. It does NOT take the physical
     * aspects of the {@link IDoorBase} into consideration. Therefore, the actual list of valid {@link RotateDirection}s
     * is most likely going to be a subset of those returned by this method.
     *
     * @return A list of all valid {@link RotateDirection} for this given type.
     */
    @NotNull List<RotateDirection> getValidOpenDirections();
}

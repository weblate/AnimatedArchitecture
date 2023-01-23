package nl.pim16aap2.bigdoors.movable;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Represent the possible attributes of a movable.
 *
 * @author Pim
 */
public enum MovableAttribute
{
    /**
     * (Un)lock the movable.
     */
    LOCK(PermissionLevel.USER),

    /**
     * Toggle the movable. If it is currently opened, it will be closed. If it is currently closed, it will be opened
     * instead.
     */
    TOGGLE(PermissionLevel.USER),

    /**
     * Turns a movable on or off. When on, the movable will rotate until turned off or until the chunks are unloaded.
     */
    SWITCH(PermissionLevel.USER),

    /**
     * Get the info of the movable.
     */
    INFO(PermissionLevel.USER),

    /**
     * Delete the movable.
     */
    DELETE(PermissionLevel.CREATOR),

    /**
     * Relocate the power block.
     */
    RELOCATE_POWERBLOCK(PermissionLevel.ADMIN),

    /**
     * The open status of a movable.
     */
    OPEN_STATUS(PermissionLevel.USER),

    /**
     * The open direction of a movable.
     */
    OPEN_DIRECTION(PermissionLevel.USER),

    /**
     * The number of blocks an animated will try to move.
     */
    BLOCKS_TO_MOVE(PermissionLevel.ADMIN),

    /**
     * Add an owner.
     */
    ADD_OWNER(PermissionLevel.ADMIN),

    /**
     * Remove an owner.
     */
    REMOVE_OWNER(PermissionLevel.ADMIN);

    private static final List<MovableAttribute> VALUES = Arrays.asList(values());

    /**
     * The minimum level of ownership required to access an attribute.
     */
    @Getter
    private final PermissionLevel permissionLevel;

    MovableAttribute(PermissionLevel permissionLevel)
    {
        this.permissionLevel = permissionLevel;
    }

    /**
     * Checks if a given permission level has access to this attribute.
     *
     * @param permissionLevel
     *     The permission level to check.
     * @return True if the given permission level has access to this attribute.
     */
    public boolean canAccessWith(PermissionLevel permissionLevel)
    {
        return permissionLevel.isLowerThanOrEquals(getPermissionLevel());
    }

    /**
     * @return All values in this enum as an unmodifiable list.
     * <p>
     * Unlike {@link #values()}, this does not create a new object.
     */
    public static List<MovableAttribute> getValues()
    {
        return VALUES;
    }
}

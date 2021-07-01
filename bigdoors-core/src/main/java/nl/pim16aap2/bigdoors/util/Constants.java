package nl.pim16aap2.bigdoors.util;


import org.jetbrains.annotations.NotNull;

/**
 * Represents a list of constant variables.
 *
 * @author Pim
 */
public final class Constants
{
    /**
     * The name used by all entities created by BigDoors.
     */
    public static final @NotNull String BIGDOORS_ENTITY_NAME = "BigDoorsEntity";

    /**
     * The amount of time (in ms) a user gets to complete a command waiter.
     */
    public static final int COMMAND_WAITER_TIMEOUT = 60 * 1000;

    /**
     * Whether or not the current build is a dev build. Certain options are enabled, disabled, or overridden depending
     * on this variable.
     */
    public static final boolean DEV_BUILD = true;

    /**
     * Minimum number of ticks a door needs to cool down before it can be toggled again. This should help with some rare
     * cases of overlapping processes and whatnot.
     */
    public static final int MINIMUM_DOOR_DELAY = 10;

    /**
     * The permission node that allows a player to bypass all compatibility hooks.
     */
    public static final @NotNull String COMPAT_BYPASS_PERMISSION = "bigdoors.admin.bypasscompat";

    /**
     * The name of this plugin.
     */
    public static final @NotNull String PLUGIN_NAME = "BigDoors";

    /**
     * The directory where all the extensions are loaded from.
     */
    public static final @NotNull String BIGDOORS_EXTENSIONS_FOLDER = "/Extensions";

    /**
     * The amount of time (in seconds) a user has to complete the door creation process.
     */
    public static final int DOOR_CREATOR_TIME_LIMIT = 120 * 20;
}

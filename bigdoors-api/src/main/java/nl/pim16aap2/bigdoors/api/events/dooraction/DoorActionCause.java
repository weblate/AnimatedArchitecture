package nl.pim16aap2.bigdoors.api.events.dooraction;

import nl.pim16aap2.bigdoors.api.doors.IDoorBase;

/**
 * Represents the different kind of causes that can be the reason of a {@link IDoorBase} action.
 *
 * @author Pim
 */
public enum DoorActionCause
{
    /**
     * The action was initiated by a player.
     */
    PLAYER,

    /**
     * The action was initiated by a redstone signal.
     */
    REDSTONE,

    /**
     * The {@link IDoorBase} was toggled from the console or by a command block.
     */
    SERVER,

    /**
     * The action was initiated by the auto close system.
     */
    AUTOCLOSE,

    /**
     * The action was initiated because this type is always moving (e.g. clocks, flags, windmills).
     */
    PERPETUALMOVEMENT,

}

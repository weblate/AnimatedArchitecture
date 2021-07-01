package nl.pim16aap2.bigdoors.events.dooraction;

import nl.pim16aap2.bigdoors.doors.IDoorBase;

/**
 * Represents the different kinds of actions that are applicable to a door.
 *
 * @author Pim
 */
public enum DoorActionType
{
    /**
     * Open a {@link IDoorBase} if it is currently open, otherwise close it.
     */
    TOGGLE,

    /**
     * Open a {@link IDoorBase}, but only if it is currently closed.
     */
    OPEN,

    /**
     * Close a {@link IDoorBase}, but only if it is currently opened.
     */
    CLOSE
}

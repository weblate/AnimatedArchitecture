package nl.pim16aap2.bigdoors.managers;

public interface IDoorActivityManager
{
    /**
     * Checks if the door with the given UID is 'busy', i.e. currently being animated.
     *
     * @param doorUID The UID of the door.
     * @return True if the door is busy.
     */
    boolean isDoorBusy(long doorUID);
}

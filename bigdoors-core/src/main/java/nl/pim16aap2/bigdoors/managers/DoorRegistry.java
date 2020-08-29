package nl.pim16aap2.bigdoors.managers;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.Restartable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a registry of doors.
 *
 * @author Pim
 * @see <a href="https://en.wikipedia.org/wiki/Multiton_pattern">Wikipedia: Multiton</a>
 */
// TODO: ConcurrentHashMaps are pretty slow at resizing, so try to estimate the size (based on the config options)
//       on init. Also, see the note at #getDoor(long) regarding the use of ConcurrentHashmaps in general.
// TODO: Perhaps this class should be a private member for the DatabaseManager?
public final class DoorRegistry extends Restartable
{
    @NotNull
    private static final DoorRegistry INSTANCE = new DoorRegistry();

    @NotNull
    private final Map<Long, Optional<AbstractDoorBase>> doors = new ConcurrentHashMap<>();

    private DoorRegistry()
    {
        super(BigDoors.get());
        if (INSTANCE != null)
        {
            IllegalAccessError e = new IllegalAccessError("Illegally trying to instantiate DoorManager!");
            PLogger.get().logThrowableSilently(e);
            throw e;
        }
    }

    public static @NotNull DoorRegistry get()
    {
        return INSTANCE;
    }

    /**
     * Attempts to get the {@link AbstractDoorBase} associated the given UID. It will only search
     *
     * @param doorUID The UID of the door.
     * @return The {@link AbstractDoorBase} if it has been retrieved from the database.
     */
    public @NotNull Optional<AbstractDoorBase> getRegisteredDoor(final long doorUID)
    {
        return doors.getOrDefault(doorUID, Optional.empty());
    }

    /**
     * Deletes an {@link AbstractDoorBase} from the registry.
     *
     * @param doorUID The UID of the {@link AbstractDoorBase} to delete.
     */
    void deleteDoor(final long doorUID)
    {
        doors.computeIfPresent(doorUID, (key, val) -> Optional.empty());
    }

    /**
     * Checks if a {@link AbstractDoorBase} associated with a given UID has been registered.
     * <p>
     * Note that this does not mean that this {@link AbstractDoorBase} actually exists. Merely that a mapping to a
     * potentially missing {@link AbstractDoorBase} exists.
     *
     * @param doorUID The UID of the door.
     * @return True if an entry exists for the {@link AbstractDoorBase} with the given UID.
     */
    public boolean isRegistered(final long doorUID)
    {
        return doors.containsKey(doorUID);
    }

    /**
     * Registers an {@link AbstractDoorBase} if it hasn't been registered yet.
     *
     * @param registerable The {@link AbstractDoorBase.Registerable} that belongs to the {@link AbstractDoorBase} that
     *                     is to be registered.
     * @return True if the door was added successfully (and didn't exist yet).
     */
    public boolean registerDoor(final @NotNull AbstractDoorBase.Registerable registerable)
    {
        final @NotNull AbstractDoorBase doorBase = registerable.getAbstractDoorBase();
        return doors.putIfAbsent(doorBase.getDoorUID(), Optional.of(doorBase)) == null;
    }

    @Override
    public void restart()
    {
        doors.clear();
    }

    @Override
    public void shutdown()
    {
        doors.clear();
    }
}

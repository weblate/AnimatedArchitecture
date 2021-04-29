package nl.pim16aap2.bigdoors.events;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.doors.IDoorBase;

/**
 * Represents the event where a door will be created.
 *
 * @author Pim
 */
public interface IDoorPrepareCreateEvent extends IDoorEvent, ICancellableBigDoorsEvent
{
    /**
     * Gets the {@link IDoorBase} that was created.
     * <p>
     * Note that this is NOT the final {@link IDoorBase} that will exist after creation; it is merely a preview!
     *
     * @return The {@link IDoorBase} that will be created.
     */
    @Override
    @NonNull IDoorBase getDoor();
}

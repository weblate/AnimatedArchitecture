package nl.pim16aap2.bigdoors.events;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.IDoorBase;

import java.util.Optional;

/**
 * Represents the event where a door was created.
 *
 * @author Pim
 */
public interface IDoorEvent extends IBigDoorsEvent
{
    /**
     * Gets the {@link IDoorBase} that was created.
     *
     * @return The {@link IDoorBase} that will be created.
     */
    @NonNull IDoorBase getDoor();

    /**
     * Gets the {@link IPPlayer} that was responsible for the creation this door.
     *
     * @return The {@link IPPlayer} that created if the creation was requested by a player. If it was requested by
     * something else (e.g. the server), an empty optional is returned.
     */
    @NonNull Optional<IPPlayer> getResponsible();
}

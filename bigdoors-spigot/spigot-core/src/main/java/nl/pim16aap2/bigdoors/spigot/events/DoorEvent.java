package nl.pim16aap2.bigdoors.spigot.events;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.IDoorBase;
import nl.pim16aap2.bigdoors.events.IDoorEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

abstract class DoorEvent extends BigDoorsSpigotEvent implements IDoorEvent
{
    @Getter
    protected final @NonNull IDoorBase door;

    @Getter
    protected final @NonNull Optional<IPPlayer> responsible;

    protected DoorEvent(final @NonNull IDoorBase door, final @Nullable IPPlayer responsible)
    {
        this.door = door;
        this.responsible = Optional.ofNullable(responsible);
    }
}

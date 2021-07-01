package nl.pim16aap2.bigdoors.spigot.events;

import lombok.Getter;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.doors.IDoorBase;
import nl.pim16aap2.bigdoors.api.events.IDoorEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

abstract class DoorEvent extends BigDoorsSpigotEvent implements IDoorEvent
{
    @Getter
    protected final @NotNull IDoorBase door;

    @Getter
    protected final @NotNull Optional<IPPlayer> responsible;

    protected DoorEvent(final @NotNull IDoorBase door, final @Nullable IPPlayer responsible)
    {
        this.door = door;
        this.responsible = Optional.ofNullable(responsible);
    }
}

package nl.pim16aap2.bigdoors.spigot.util;

import lombok.AllArgsConstructor;
import lombok.val;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.events.BigDoorsSpigotEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorCreatedEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorPrepareAddOwnerEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorPrepareCreateEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorPrepareDeleteEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorPrepareLockChangeEvent;
import nl.pim16aap2.bigdoors.spigot.events.DoorPrepareRemoveOwnerEvent;
import nl.pim16aap2.bigdoors.spigot.events.dooraction.DoorEventToggleEnd;
import nl.pim16aap2.bigdoors.spigot.events.dooraction.DoorEventTogglePrepare;
import nl.pim16aap2.bigdoors.spigot.events.dooraction.DoorEventToggleStart;
import nl.pim16aap2.bigdoors.util.DebugReporter;
import nl.pim16aap2.bigdoors.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class DebugReporterSpigot extends DebugReporter
{
    private final @NotNull BigDoorsSpigot bigDoorsSpigot;

    @Override
    public @NotNull String getDump()
    {
        final StringBuilder sb = new StringBuilder(super.getDump());
        sb.append("BigDoors version: ").append(bigDoorsSpigot.getPlugin().getDescription().getVersion())
          .append("\n");
        sb.append("Server version: ").append(Bukkit.getServer().getVersion()).append("\n");

        sb.append("Registered door types: ")
          .append(Util.toString(BigDoors.get().getDoorTypeManager().getRegisteredDoorTypes()))
          .append("\n");

        sb.append("Enabled door types:    ")
          .append(Util.toString(BigDoors.get().getDoorTypeManager().getEnabledDoorTypes()))
          .append("\n");

        sb.append("Registered addons: ")
          .append(Util.toString(bigDoorsSpigot.getPlugin().getRegisteredAddons()))
          .append("\n");

        val platform = bigDoorsSpigot.getPlatformManagerSpigot().getSpigotPlatform();
        sb.append("SpigotPlatform: ").append(platform == null ? "NULL" : platform.getClass().getName()).append("\n");

        // TODO: Implement this:
//        sb.append("Enabled protection hooks: ")
//          .append(getAllProtectionHooksOrSomething())

        sb.append("EventListeners:\n").append(
            getListeners(DoorPrepareAddOwnerEvent.class, DoorPrepareCreateEvent.class, DoorPrepareDeleteEvent.class,
                         DoorPrepareLockChangeEvent.class, DoorPrepareRemoveOwnerEvent.class, DoorCreatedEvent.class,
                         DoorEventToggleEnd.class, DoorEventTogglePrepare.class, DoorEventToggleStart.class));

        sb.append("Config: ").append(BigDoorsSpigot.get().getConfigLoader()).append("\n");

        return sb.toString();
    }

    private @NotNull String getListeners(final @NotNull Class<?>... classes)
    {
        final StringBuilder sb = new StringBuilder();
        for (Class<?> clz : classes)
        {
            if (!(BigDoorsSpigotEvent.class.isAssignableFrom(clz)))
            {
                sb.append("ERROR: ").append(clz.getName()).append("\n");
                continue;
            }
            try
            {
                val handlerListMethod = clz.getDeclaredField("HANDLERS_LIST");
                handlerListMethod.setAccessible(true);
                val handlers = (HandlerList) handlerListMethod.get(null);
                sb.append("    ").append(clz.getSimpleName()).append(": ")
                  .append(Util.toString(handlers.getRegisteredListeners(),
                                        DebugReporterSpigot::formatRegisteredListener))
                  .append("\n");
            }
            catch (Exception e)
            {
                BigDoors.get().getPLogger()
                        .logThrowable(new RuntimeException("Failed to find MethodHandle for handlers!", e));
                sb.append("ERROR: ").append(clz.getName()).append("\n");
            }
        }

        return sb.toString();
    }

    private static @NotNull String formatRegisteredListener(final @NotNull RegisteredListener listener)
    {
        return String.format("{%s: %s (%s)}",
                             listener.getPlugin(), listener.getListener(), listener.getPriority());
    }
}

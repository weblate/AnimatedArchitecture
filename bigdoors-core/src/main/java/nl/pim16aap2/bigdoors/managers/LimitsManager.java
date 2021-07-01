package nl.pim16aap2.bigdoors.managers;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.managers.ILimitsManager;
import nl.pim16aap2.bigdoors.api.util.Limit;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

public class LimitsManager implements ILimitsManager
{
    @Override
    public @NotNull OptionalInt getLimit(final @NotNull IPPlayer player, final @NotNull Limit limit)
    {
        final boolean hasBypass = BigDoors.get().getPlatform().getPermissionsManager()
                                          .hasPermission(player, limit.getAdminPermission());
        final @NotNull OptionalInt globalLimit = limit.getGlobalLimit(BigDoors.get().getPlatform().getConfigLoader());
        if (hasBypass)
            return globalLimit;

        final @NotNull OptionalInt playerLimit = BigDoors.get().getPlatform().getPermissionsManager()
                                                         .getMaxPermissionSuffix(player, limit.getUserPermission());

        if (globalLimit.isPresent() && playerLimit.isPresent())
            return OptionalInt.of(Math.min(globalLimit.getAsInt(), playerLimit.getAsInt()));

        return globalLimit.isPresent() ? OptionalInt.of(globalLimit.getAsInt()) :
               playerLimit.isPresent() ? OptionalInt.of(playerLimit.getAsInt()) :
               OptionalInt.empty();
    }

    @Override
    public boolean exceedsLimit(final @NotNull IPPlayer player, final @NotNull Limit limit, final int value)
    {
        final @NotNull OptionalInt limitValue = getLimit(player, limit);
        return limitValue.isPresent() && value > limitValue.getAsInt();
    }
}

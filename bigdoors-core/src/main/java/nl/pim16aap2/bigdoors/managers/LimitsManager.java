package nl.pim16aap2.bigdoors.managers;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.util.Limit;

import java.util.OptionalInt;

public class LimitsManager implements ILimitsManager
{
    @Override
    public @NonNull OptionalInt getLimit(final @NonNull IPPlayer player, final @NonNull Limit limit)
    {
        final boolean hasBypass = BigDoors.get().getPlatform().getPermissionsManager()
                                          .hasPermission(player, limit.getAdminPermission());
        final @NonNull OptionalInt globalLimit = limit.getGlobalLimit(BigDoors.get().getPlatform().getConfigLoader());
        if (hasBypass)
            return globalLimit;

        final @NonNull OptionalInt playerLimit = BigDoors.get().getPlatform().getPermissionsManager()
                                                         .getMaxPermissionSuffix(player, limit.getUserPermission());

        if (globalLimit.isPresent() && playerLimit.isPresent())
            return OptionalInt.of(Math.min(globalLimit.getAsInt(), playerLimit.getAsInt()));

        return globalLimit.isPresent() ? OptionalInt.of(globalLimit.getAsInt()) :
               playerLimit.isPresent() ? OptionalInt.of(playerLimit.getAsInt()) :
               OptionalInt.empty();
    }

    @Override
    public boolean exceedsLimit(final @NonNull IPPlayer player, final @NonNull Limit limit, final int value)
    {
        final @NonNull OptionalInt limitValue = getLimit(player, limit);
        return limitValue.isPresent() && value > limitValue.getAsInt();
    }
}

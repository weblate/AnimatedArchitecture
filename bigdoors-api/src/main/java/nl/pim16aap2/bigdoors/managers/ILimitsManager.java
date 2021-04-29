package nl.pim16aap2.bigdoors.managers;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IConfigLoader;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.util.Limit;

import java.util.OptionalInt;

public interface ILimitsManager
{
    /**
     * Gets the value of the {@link Limit} for the given player. It checks the global limit, any admin bypass
     * permission, and the player's personal limit.
     *
     * @param player The player for whom to check the limit.
     * @param limit  The {@link Limit} to check.
     * @return The value of the limit for the given player, or an empty OptionalInt if none was found.
     * <p>
     * If there is a global limit in place (see {@link Limit#getGlobalLimit(IConfigLoader)}), the returned value cannot
     * exceed this. Not even admins and OPs can bypass this limit.
     * <p>
     * If the player has a player limit as well as a global limit, the lowest value of the two will be used.
     */
    @NonNull OptionalInt getLimit(@NonNull IPPlayer player, @NonNull Limit limit);

    /**
     * Checks if a given value exceeds the limit for this player. For more info, see {@link #getLimit(IPPlayer,
     * Limit)}.
     *
     * @param player The player for whom to check the limit.
     * @param limit  The {@link Limit} to check.
     * @param value  The value to compare to the limit.
     * @return True if the given value exceeds the limit for this player. If value <= limit, this will return false.
     */
    boolean exceedsLimit(@NonNull IPPlayer player, @NonNull Limit limit, int value);
}

package nl.pim16aap2.bigdoors.api;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.doortypes.IDoorType;

import java.util.OptionalDouble;

public interface IEconomyManager
{
    /**
     * Buys a door for a player.
     *
     * @param player     The player whose bank account to use.
     * @param world      The world the door is in.
     * @param type       The {@link IDoorType} of the door.
     * @param blockCount The number of blocks in the door.
     * @return True if the player bought the door successfully.
     */
    boolean buyDoor(@NonNull IPPlayer player, @NonNull IPWorld world, @NonNull IDoorType type, int blockCount);

    /**
     * Gets the price of {@link IDoorType} for a specific number of blocks.
     *
     * @param type       The {@link IDoorType}.
     * @param blockCount The number of blocks.
     * @return The price of this {@link IDoorType} with this number of blocks.
     */
    @NonNull OptionalDouble getPrice(@NonNull IDoorType type, int blockCount);

    /**
     * Checks if the economy manager is enabled.
     *
     * @return True if the economy manager is enabled.
     */
    boolean isEconomyEnabled();
}

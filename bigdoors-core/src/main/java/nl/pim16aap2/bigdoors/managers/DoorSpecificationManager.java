package nl.pim16aap2.bigdoors.managers;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.restartable.Restartable;
import nl.pim16aap2.bigdoors.util.delayedinput.DelayedInputRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a manager for handling door specifications requests.
 *
 * @author Pim
 */
public final class DoorSpecificationManager extends Restartable
{
    private static final @NonNull DoorSpecificationManager INSTANCE = new DoorSpecificationManager();

    private static final @NonNull Map<@NonNull IPPlayer, @NonNull DelayedInputRequest<String>> requests = new ConcurrentHashMap<>();

    private DoorSpecificationManager()
    {
        super(BigDoors.get());
    }

    public static @NotNull DoorSpecificationManager get()
    {
        return INSTANCE;
    }

    /**
     * Checks if there is an open door specification request for a player.
     *
     * @param player The player to check.
     * @return True if there is an open door specification request for the player.
     */
    public boolean isActive(final @NonNull IPPlayer player)
    {
        return requests.containsKey(player);
    }

    /**
     * Registers a new delayed input request for the given player.
     * <p>
     * If a request is already active for the given player, the old request will be cancelled and replaced by the new
     * one.
     *
     * @param player  The player to request.
     * @param request The request.
     */
    public void placeRequest(final @NonNull IPPlayer player, final @NonNull DelayedInputRequest<String> request)
    {
        requests.compute(
            player, (key, value) ->
            {
                // Cancel previous requests if any are still active.
                if (value != null)
                    value.cancel();
                return request;
            });
    }

    /**
     * Removes any requests for a player.
     * <p>
     * Note that any active requests are not cancelled. To cancel requests, use {@link #cancelRequest(IPPlayer)}
     * instead.
     *
     * @param player The player whose requests to remove.
     */
    public void removeRequest(final @NonNull IPPlayer player)
    {
        requests.remove(player);
    }

    /**
     * Handles input for a player.
     *
     * @param player The player that provided input.
     * @param input  The input to handle.
     * @return False if no request could be found for the player.
     */
    public boolean handleInput(final @NonNull IPPlayer player, final @NonNull String input)
    {
        final @Nullable DelayedInputRequest<String> request = requests.get(player);
        if (request == null)
            return false;

        request.set(input);
        return true;
    }

    /**
     * Cancels an active request for a player.
     * <p>
     * This does not guarantee that the request will also be removed (depends on {@link DelayedInputRequest}). To fully
     * remove a request, use {@link #removeRequest(IPPlayer)}.
     *
     * @param player The player whose requests to cancel.
     */
    public void cancelRequest(final @NonNull IPPlayer player)
    {
        Optional.ofNullable(requests.get(player)).ifPresent(DelayedInputRequest::cancel);
    }

    @Override
    public void restart()
    {
        shutdown();
    }

    @Override
    public void shutdown()
    {
        requests.values().forEach(DelayedInputRequest::cancel);
        requests.clear();
    }
}

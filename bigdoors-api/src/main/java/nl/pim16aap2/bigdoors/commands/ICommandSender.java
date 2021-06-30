package nl.pim16aap2.bigdoors.commands;

import nl.pim16aap2.bigdoors.api.IMessageable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ICommandSender extends IMessageable
{
    /**
     * Gets the {@link IPPlayer} that issued the command.
     *
     * @return The {@link IPPlayer} that issued the command, if it was a player that issued the command. Otherwise, an
     * empty Optional is returned.
     */
    @NotNull Optional<IPPlayer> getPlayer();

    /**
     * Checks if the command sender is a player.
     *
     * @return True if the command sender is a player.
     */
    boolean isPlayer();

    /**
     * Checks if this sender has a given permission.
     *
     * @param permission The permission node to check.
     * @return True if the player has access to the provided permission, otherwise false.
     */
    default @NotNull CompletableFuture<Boolean> hasPermission(@NotNull String permission)
    {
        return getPlayer().map(player -> player.hasPermission(permission))
                          .orElse(CompletableFuture.completedFuture(true));
    }

    /**
     * Checks if this sender has a given command.
     * <p>
     * Both the user permission (See {@link ICommandDefinition#getUserPermission()}) and the admin permission (See
     * {@link ICommandDefinition#getAdminPermission()} are checked.
     *
     * @param command The {@link ICommandDefinition} of a command to check.
     * @return A {@link BooleanPair} that is true if the player has access to the provided permissions, otherwise false
     * for the user and the admin permission nodes respectively.
     */
    default @NotNull CompletableFuture<BooleanPair> hasPermission(final @NotNull ICommandDefinition command)
    {
        return getPlayer().map(player -> player.hasPermission(command))
                          .orElse(CompletableFuture.completedFuture(new BooleanPair(false, false)));
    }
}

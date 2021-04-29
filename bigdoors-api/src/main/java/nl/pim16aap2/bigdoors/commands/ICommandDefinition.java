package nl.pim16aap2.bigdoors.commands;

import lombok.NonNull;

import java.util.Optional;

public interface ICommandDefinition
{
    /**
     * Gets the user-level permission for a command if it exists.
     *
     * @return The user-level permission for the command.
     */
    @NonNull Optional<String> getUserPermission();

    /**
     * Gets the admin-level permission for a command if it exists.
     *
     * @return The admin-level permission for the command.
     */
    @NonNull Optional<String> getAdminPermission();
}

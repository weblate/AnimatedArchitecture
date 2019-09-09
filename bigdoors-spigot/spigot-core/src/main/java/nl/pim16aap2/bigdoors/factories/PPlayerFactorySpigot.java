package nl.pim16aap2.bigdoors.factories;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.spigotutil.implementations.PPlayerSpigot;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an implementation of {@link IPPlayerFactory} for the Spigot platform.
 *
 * @author Pim
 */
public class PPlayerFactorySpigot implements IPPlayerFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public IPPlayer create(final @NotNull UUID playerUUID, final @NotNull String playerName)
    {
        return new PPlayerSpigot(playerUUID, playerName);
    }
}

package nl.pim16aap2.bigdoors.spigot.util.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Represents the bootstrap loader for Spigot.
 *
 * @author Pim
 */
public abstract class AbstractBigDoorsSpigotLoader extends JavaPlugin
{
    /**
     * Gets the BigDoors API.
     *
     * @param caller The {@link JavaPlugin} that requested the API.
     * @return The BigDoors API.
     */
    @SuppressWarnings("unused")
    public abstract @NotNull Optional<BigDoorsSpigotAbstract> getBigDoorsAPI(@NotNull JavaPlugin caller);

    /**
     * Gets the list of registered addons (i.e. plugins that use the BigDoors API).
     *
     * @return The list of registered addons.
     */
    public abstract @NotNull Set<JavaPlugin> getRegisteredAddons();
}

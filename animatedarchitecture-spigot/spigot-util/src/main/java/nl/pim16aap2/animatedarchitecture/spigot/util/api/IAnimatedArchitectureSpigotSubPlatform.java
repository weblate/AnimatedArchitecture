package nl.pim16aap2.animatedarchitecture.spigot.util.api;

import nl.pim16aap2.animatedarchitecture.core.api.IBlockAnalyzer;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlockFactory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a sub-platform for the AnimatedArchitecture platform running on Spigot.
 * <p>
 * The sub-platforms are used to provide version-specific support.
 *
 * @author Pim
 */
public interface IAnimatedArchitectureSpigotSubPlatform
{
    /**
     * Gets the version of this platform. E.g. "v1_14_R1" for 1.14 to 1.14.4.
     *
     * @return The version.
     */
    String getVersion();

    void init(JavaPlugin plugin);

    IAnimatedBlockFactory getAnimatedBlockFactory();

    IBlockAnalyzer getBlockAnalyzer();

    IGlowingBlockFactory getGlowingBlockFactory();
}

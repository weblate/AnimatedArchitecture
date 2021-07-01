package nl.pim16aap2.bigdoors.spigot.util.api;

import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.PColor;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents the {@link IGlowingBlockSpawner} specialized for the Spigot platform.
 *
 * @author Pim
 */
public interface IGlowingBlockSpawnerSpigot extends IRestartable, IGlowingBlockSpawner, IRestartableHolder
{
    /**
     * Gets the mapping of colors to teams.
     *
     * @return The mapping of colors to teams.
     */
    @NotNull Map<PColor, Team> getTeams();
}

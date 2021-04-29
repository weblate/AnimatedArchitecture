package nl.pim16aap2.bigdoors.spigot.util.api;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.PColor;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import org.bukkit.scoreboard.Team;

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
    @NonNull Map<PColor, Team> getTeams();
}

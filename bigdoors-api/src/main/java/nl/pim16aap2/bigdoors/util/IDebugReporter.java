package nl.pim16aap2.bigdoors.util;

import org.jetbrains.annotations.NotNull;

public interface IDebugReporter
{
    /**
     * Gets the datadump containing useful information for debugging issues.
     */
    @NotNull String getDump();
}

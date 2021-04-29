package nl.pim16aap2.bigdoors.util;

import lombok.NonNull;

public interface IDebugReporter
{
    /**
     * Gets the datadump containing useful information for debugging issues.
     */
    @NonNull String getDump();
}

package nl.pim16aap2.bigdoors.spigot.compatiblity;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum of all ProtectionCompats that can be loaded and some methods to help load them.
 *
 * @author Pim
 */
public enum ProtectionCompat
{
    TOWNY("Towny")
        {
            @Override
            public @Nullable Class<? extends IProtectionCompat> getClass(String version)
            {
                final int[] lastOldVersion = {0, 94, 0, 1};

                final int[] currentVersion = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
                for (int idx = 0; idx < lastOldVersion.length; ++idx)
                {
                    if (currentVersion[idx] == lastOldVersion[idx])
                        continue;

                    return currentVersion[idx] > lastOldVersion[idx] ?
                           TownyNewProtectionCompat.class : TownyOldProtectionCompat.class;
                }
                return null;
            }
        },

    WORLDGUARD("WorldGuard")
        {
            @Override
            public @Nullable Class<? extends IProtectionCompat> getClass(String version)
            {
                if (version.startsWith("7."))
                    return WorldGuard7ProtectionCompat.class;
                else if (version.startsWith("6."))
                    return WorldGuard6ProtectionCompat.class;
                else
                    return null;
            }
        },

    GRIEFPREVENTION("GriefPrevention")
        {
            @Override
            public Class<? extends IProtectionCompat> getClass(String version)
            {
                return GriefPreventionProtectionCompat.class;
            }
        },

    LANDS("Lands")
        {
            @Override
            public Class<? extends IProtectionCompat> getClass(String version)
            {
                return LandsProtectionCompat.class;
            }
        },
    ;

    private static final Map<String, ProtectionCompat> NAME_MAP = new HashMap<>();

    static
    {
        for (final ProtectionCompat compat : ProtectionCompat.values())
            NAME_MAP.put(ProtectionCompat.getName(compat), compat);
    }

    private final String name;

    ProtectionCompat(String name)
    {
        this.name = name;
    }

    /**
     * Get the name of the plugin the given compat hooks into.
     *
     * @param compat
     *     The compat the get the name of the plugin for.
     * @return The name of the plugin the given compat hooks into.
     */
    public static String getName(ProtectionCompat compat)
    {
        return compat.name;
    }

    /**
     * Get the compat for a plugin.
     *
     * @param name
     *     The name of the plugin to get the compat for.
     * @return The compat for a plugin.
     */
    public static @Nullable ProtectionCompat getFromName(String name)
    {
        return NAME_MAP.get(name);
    }

    /**
     * Get the class of the given hook for a specific version of the plugin to load the compat for.
     *
     * @param version
     *     The version of the plugin to load the hook for.
     * @return The {@link IProtectionCompat} class of the compat.
     */
    public abstract @Nullable Class<? extends IProtectionCompat> getClass(String version);
}

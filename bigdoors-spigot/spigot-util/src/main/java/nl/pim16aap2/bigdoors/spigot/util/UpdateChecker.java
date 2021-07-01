package nl.pim16aap2.bigdoors.spigot.util;

import nl.pim16aap2.bigdoors.logging.IPLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A utility class to assist in checking for updates for plugins uploaded to
 * <a href="https://spigotmc.org/resources/">SpigotMC</a>.
 * <p>
 * This class performs asynchronous queries to
 * <a href="https://spiget.org">SpiGet</a>, an REST server which is updated
 * periodically. If the results of {@link #requestUpdateCheck()} are inconsistent with what is published on SpigotMC, it
 * may be due to SpiGet's cache. Results will be updated in due time.
 * <p>
 * Some modifications were made to support downloading of updates and storing the age of an update.
 *
 * @author Parker Hawke - 2008Choco
 * @deprecated This class has been stripped until it can be rewritten.
 */
@Deprecated
public final class UpdateChecker
{
    private @Nullable UpdateResult lastResult = null;

    private final @NotNull JavaPlugin plugin;

    public UpdateChecker(final @NotNull JavaPlugin plugin, final int pluginID,
                         final @Nullable VersionScheme versionScheme, final @NotNull IPLogger logger)
    {
        this.plugin = plugin;
    }

    /**
     * Requests an update check to SpiGet. This request is asynchronous and may not complete immediately as an HTTP GET
     * request is published to the SpiGet API.
     *
     * @return a future update result
     */
    public @NotNull CompletableFuture<UpdateResult> requestUpdateCheck()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the last update result that was queried by {@link #requestUpdateCheck()}. If no update check was performed
     * since this class' initialization, this method will return null.
     *
     * @return the last update check result. null if none.
     */
    public @Nullable UpdateResult getLastResult()
    {
        return lastResult;
    }

    /**
     * Downloads the latest update.
     *
     * @return True if the download was successful.
     */
    public boolean downloadUpdate()
    {
        throw new UnsupportedOperationException();
    }

    public @NotNull String getDownloadUrl()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * A functional interface to compare two version Strings with similar version schemes.
     */
    @FunctionalInterface
    public interface VersionScheme
    {
        /**
         * Compare two versions and return the higher of the two. If null is returned, it is assumed that at least one
         * of the two versions are unsupported by this version scheme parser.
         *
         * @param first  the first version to check
         * @param second the second version to check
         * @return the greater of the two versions. null if unsupported version schemes
         */
        @Nullable String compareVersions(String first, String second);
    }

    /**
     * A constant reason for the result of {@link UpdateResult}.
     */
    public enum UpdateReason
    {

        /**
         * A new update is available for download on SpigotMC.
         */
        NEW_UPDATE, // The only reason that requires an update

        /**
         * A successful connection to the SpiGet API could not be established.
         */
        COULD_NOT_CONNECT,

        /**
         * The JSON retrieved from SpiGet was invalid or malformed.
         */
        INVALID_JSON,

        /**
         * A 401 error was returned by the SpiGet API.
         */
        UNAUTHORIZED_QUERY,

        /**
         * The version of the plugin installed on the server is greater than the one uploaded to SpigotMC's resources
         * section.
         */
        UNRELEASED_VERSION,

        /**
         * An unknown error occurred.
         */
        UNKNOWN_ERROR,

        /**
         * The plugin uses an unsupported version scheme, therefore a proper comparison between versions could not be
         * made.
         */
        UNSUPPORTED_VERSION_SCHEME,

        /**
         * The plugin is up to date with the version released on SpigotMC's resources section.
         */
        UP_TO_DATE

    }

    /**
     * Represents a result for an update query performed by {@link UpdateChecker#requestUpdateCheck()}.
     */
    public final class UpdateResult
    {
        private final @NotNull UpdateReason reason;
        private final @NotNull String newestVersion;
        private final long age;

        { // An actual use for initializer blocks. This is madness!
            lastResult = this;
        }

        private UpdateResult(final @NotNull UpdateReason reason, final @NotNull String newestVersion, final long age)
        {
            this.reason = reason;
            this.newestVersion = newestVersion;
            this.age = age;
        }

        private UpdateResult(final @NotNull UpdateReason reason)
        {
            this.reason = reason;
            newestVersion = plugin.getDescription().getVersion();
            age = -1;
        }

        /**
         * Gets the constant reason of this result.
         *
         * @return the reason
         */
        public @NotNull UpdateReason getReason()
        {
            return reason;
        }

        /**
         * Checks whether or not this result requires the user to update.
         *
         * @return true if requires update, false otherwise
         */
        public boolean requiresUpdate()
        {
            return reason == UpdateReason.NEW_UPDATE;
        }

        /**
         * Gets the latest version of the plugin. This may be the currently installed version, it may not be. This
         * depends entirely on the result of the update.
         *
         * @return the newest version of the plugin
         */
        public @NotNull String getNewestVersion()
        {
            return newestVersion;
        }

        /**
         * Gets the number of seconds since the last update was released.
         *
         * @return The number of seconds since the last update was released or -1 if unavailable.
         */
        public long getAge()
        {
            return age;
        }
    }
}

package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.api.factories.IBigDoorsEventFactory;
import nl.pim16aap2.bigdoors.api.factories.IFallingBlockFactory;
import nl.pim16aap2.bigdoors.api.factories.IPBlockDataFactory;
import nl.pim16aap2.bigdoors.api.factories.IPLocationFactory;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.api.factories.IPWorldFactory;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import nl.pim16aap2.bigdoors.api.commands.IPServer;
import nl.pim16aap2.bigdoors.api.events.IBigDoorsEvent;
import nl.pim16aap2.bigdoors.api.logging.IPLogger;
import nl.pim16aap2.bigdoors.api.managers.IDoorActivityManager;
import nl.pim16aap2.bigdoors.api.managers.ILimitsManager;
import nl.pim16aap2.bigdoors.api.util.IDebugReporter;
import nl.pim16aap2.bigdoors.api.util.messages.IMessages;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public interface IBigDoorsPlatform extends IRestartableHolder, IRestartable
{
    void onEnable();

    void onDisable();

    /**
     * Gets the directory where all data will stored.
     *
     * @return The directory where all data will stored.
     */
    @NotNull File getDataDirectory();

    /**
     * Gets the instance of the {@link IPLocationFactory} for this platform.
     *
     * @return The instance of the {@link IPLocationFactory} for this platform.
     */
    @NotNull IPLocationFactory getPLocationFactory();

    /**
     * Gets the instance of the {@link IEconomyManager} for this platform.
     *
     * @return The instance of the {@link IEconomyManager} for this platform.
     */
    @NotNull IEconomyManager getEconomyManager();

    /**
     * Gets the instance of the {@link IPermissionsManager} for this platform.
     *
     * @return The instance of the {@link IPermissionsManager} for this platform.
     */
    @NotNull IPermissionsManager getPermissionsManager();

    /**
     * Gets the instance of the {@link IProtectionCompatManager} for this platform.
     *
     * @return The instance of the {@link IProtectionCompatManager} for this platform.
     */
    @NotNull IProtectionCompatManager getProtectionCompatManager();

    /**
     * Gets the instance of the {@link IPWorldFactory} for this platform.
     *
     * @return The instance of the {@link IPWorldFactory} for this platform.
     */
    @NotNull IPWorldFactory getPWorldFactory();

    /**
     * Gets the instance of the {@link IPBlockDataFactory} for this platform.
     *
     * @return The instance of the {@link IPBlockDataFactory} for this platform.
     */
    @NotNull IPBlockDataFactory getPBlockDataFactory();

    /**
     * Gets the instance of the {@link IFallingBlockFactory} for this platform.
     *
     * @return The instance of the {@link IFallingBlockFactory} for this platform.
     */
    @NotNull IFallingBlockFactory getFallingBlockFactory();

    /**
     * Gets the instance of the {@link IPPlayerFactory} for this platform.
     *
     * @return The instance of the {@link IPPlayerFactory} for this platform.
     */
    @NotNull IPPlayerFactory getPPlayerFactory();

    /**
     * Gets the instance of the {@link IConfigLoader} for this platform.
     *
     * @return The instance of the {@link IConfigLoader} for this platform.
     */
    @NotNull IConfigLoader getConfigLoader();

    /**
     * Gets the instance of the {@link ISoundEngine} for this platform.
     *
     * @return The instance of the {@link ISoundEngine} for this platform.
     */
    @NotNull ISoundEngine getSoundEngine();

    /**
     * Gets the instance of the {@link IMessagingInterface} for this platform.
     *
     * @return The instance of the {@link IMessagingInterface} for this platform.
     */
    @NotNull IMessagingInterface getMessagingInterface();

    /**
     * Gets the instance of the {@link IMessages} for this platform.
     *
     * @return The instance of the {@link IMessages} for this platform.
     */
    @NotNull IMessages getMessages();

    /**
     * Gets the implementation of a {@link IMessageable} for the server.
     *
     * @return The implementation of a {@link IMessageable} for the server.
     */
    @NotNull IMessageable getMessageableServer();

    /**
     * Gets the instance of the {@link IBlockAnalyzer} for this platform.
     *
     * @return The instance of the {@link IBlockAnalyzer} for this platform.
     */
    @NotNull IBlockAnalyzer getBlockAnalyzer();

    /**
     * Gets the instance of the {@link IPowerBlockRedstoneManager} for this platform.
     *
     * @return The instance of the {@link IPowerBlockRedstoneManager} for this platform.
     */
    @NotNull IPowerBlockRedstoneManager getPowerBlockRedstoneManager();

    /**
     * Gets the instance of the {@link IChunkManager} for this platform.
     *
     * @return The instance of the {@link IChunkManager} for this platform.
     */
    @NotNull IChunkManager getChunkManager();

    /**
     * Gets the instance of the {@link IBigDoorsEventFactory} for this platform.
     *
     * @return The instance of the {@link IBigDoorsEventFactory} for this platform.
     */
    @NotNull IBigDoorsEventFactory getBigDoorsEventFactory();

    /**
     * Calls a {@link IBigDoorsEvent}.
     *
     * @param doorActionEvent The {@link IBigDoorsEvent} to call.
     */
    void callDoorEvent(@NotNull IBigDoorsEvent doorActionEvent);

    /**
     * Checks if a thread is the main thread.
     *
     * @param threadID The ID of the thread to compare.
     * @return True if the thread is the main thread.
     */
    boolean isMainThread(long threadID);

    /**
     * Constructs a new {@link IPExecutor}.
     *
     * @return A new {@link IPExecutor}.
     */
    @NotNull IPExecutor getPExecutor();

    /**
     * Gets the {@link IGlowingBlockSpawner} for the current platform.
     *
     * @return The {@link IGlowingBlockSpawner} for the current platform.
     */
    @NotNull Optional<IGlowingBlockSpawner> getGlowingBlockSpawner();

    /**
     * Gets the {@link IPLogger} for this platform.
     *
     * @return The {@link IPLogger} for this platform.
     */
    @NotNull IPLogger getPLogger();

    /**
     * Gets the {@link IDoorActivityManager} instance.
     *
     * @return The {@link IDoorActivityManager} instance.
     */
    @NotNull IDoorActivityManager getDoorActivityManager();

    /**
     * Gets the {@link IDebugReporter}.
     *
     * @return The {@link IDebugReporter}.
     */
    @NotNull IDebugReporter getDebugReporter();

    /**
     * Gets the build id of BigDoors that is currently running.
     *
     * @return The build id of BigDoors that is currently running.
     */
    @NotNull String getVersion();

    /**
     * Gets the {@link IPServer} instance.
     *
     * @return The {@link IPServer} instance.
     */
    @NotNull IPServer getPServer();

    /**
     * Gets the {@link ILimitsManager}.
     *
     * @return The {@link ILimitsManager}.
     */
    @NotNull ILimitsManager getLimitsManager();
}

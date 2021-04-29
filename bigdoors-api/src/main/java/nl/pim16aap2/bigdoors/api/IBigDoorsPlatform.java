package nl.pim16aap2.bigdoors.api;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.factories.IBigDoorsEventFactory;
import nl.pim16aap2.bigdoors.api.factories.IFallingBlockFactory;
import nl.pim16aap2.bigdoors.api.factories.IPBlockDataFactory;
import nl.pim16aap2.bigdoors.api.factories.IPLocationFactory;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.api.factories.IPWorldFactory;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import nl.pim16aap2.bigdoors.commands.IPServer;
import nl.pim16aap2.bigdoors.events.IBigDoorsEvent;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.managers.IDoorActivityManager;
import nl.pim16aap2.bigdoors.managers.ILimitsManager;
import nl.pim16aap2.bigdoors.util.IDebugReporter;
import nl.pim16aap2.bigdoors.util.messages.IMessages;

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
    @NonNull File getDataDirectory();

    /**
     * Gets the instance of the {@link IPLocationFactory} for this platform.
     *
     * @return The instance of the {@link IPLocationFactory} for this platform.
     */
    @NonNull IPLocationFactory getPLocationFactory();

    /**
     * Gets the instance of the {@link IEconomyManager} for this platform.
     *
     * @return The instance of the {@link IEconomyManager} for this platform.
     */
    @NonNull IEconomyManager getEconomyManager();

    /**
     * Gets the instance of the {@link IPermissionsManager} for this platform.
     *
     * @return The instance of the {@link IPermissionsManager} for this platform.
     */
    @NonNull IPermissionsManager getPermissionsManager();

    /**
     * Gets the instance of the {@link IProtectionCompatManager} for this platform.
     *
     * @return The instance of the {@link IProtectionCompatManager} for this platform.
     */
    @NonNull IProtectionCompatManager getProtectionCompatManager();

    /**
     * Gets the instance of the {@link IPWorldFactory} for this platform.
     *
     * @return The instance of the {@link IPWorldFactory} for this platform.
     */
    @NonNull IPWorldFactory getPWorldFactory();

    /**
     * Gets the instance of the {@link IPBlockDataFactory} for this platform.
     *
     * @return The instance of the {@link IPBlockDataFactory} for this platform.
     */
    @NonNull IPBlockDataFactory getPBlockDataFactory();

    /**
     * Gets the instance of the {@link IFallingBlockFactory} for this platform.
     *
     * @return The instance of the {@link IFallingBlockFactory} for this platform.
     */
    @NonNull IFallingBlockFactory getFallingBlockFactory();

    /**
     * Gets the instance of the {@link IPPlayerFactory} for this platform.
     *
     * @return The instance of the {@link IPPlayerFactory} for this platform.
     */
    @NonNull IPPlayerFactory getPPlayerFactory();

    /**
     * Gets the instance of the {@link IConfigLoader} for this platform.
     *
     * @return The instance of the {@link IConfigLoader} for this platform.
     */
    @NonNull IConfigLoader getConfigLoader();

    /**
     * Gets the instance of the {@link ISoundEngine} for this platform.
     *
     * @return The instance of the {@link ISoundEngine} for this platform.
     */
    @NonNull ISoundEngine getSoundEngine();

    /**
     * Gets the instance of the {@link IMessagingInterface} for this platform.
     *
     * @return The instance of the {@link IMessagingInterface} for this platform.
     */
    @NonNull IMessagingInterface getMessagingInterface();

    /**
     * Gets the instance of the {@link IMessages} for this platform.
     *
     * @return The instance of the {@link IMessages} for this platform.
     */
    @NonNull IMessages getMessages();

    /**
     * Gets the implementation of a {@link IMessageable} for the server.
     *
     * @return The implementation of a {@link IMessageable} for the server.
     */
    @NonNull IMessageable getMessageableServer();

    /**
     * Gets the instance of the {@link IBlockAnalyzer} for this platform.
     *
     * @return The instance of the {@link IBlockAnalyzer} for this platform.
     */
    @NonNull IBlockAnalyzer getBlockAnalyzer();

    /**
     * Gets the instance of the {@link IPowerBlockRedstoneManager} for this platform.
     *
     * @return The instance of the {@link IPowerBlockRedstoneManager} for this platform.
     */
    @NonNull IPowerBlockRedstoneManager getPowerBlockRedstoneManager();

    /**
     * Gets the instance of the {@link IChunkManager} for this platform.
     *
     * @return The instance of the {@link IChunkManager} for this platform.
     */
    @NonNull IChunkManager getChunkManager();

    /**
     * Gets the instance of the {@link IBigDoorsEventFactory} for this platform.
     *
     * @return The instance of the {@link IBigDoorsEventFactory} for this platform.
     */
    @NonNull IBigDoorsEventFactory getBigDoorsEventFactory();

    /**
     * Calls a {@link IBigDoorsEvent}.
     *
     * @param doorActionEvent The {@link IBigDoorsEvent} to call.
     */
    void callDoorEvent(@NonNull IBigDoorsEvent doorActionEvent);

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
    @NonNull IPExecutor getPExecutor();

    /**
     * Gets the {@link IGlowingBlockSpawner} for the current platform.
     *
     * @return The {@link IGlowingBlockSpawner} for the current platform.
     */
    @NonNull Optional<IGlowingBlockSpawner> getGlowingBlockSpawner();

    /**
     * Gets the {@link IPLogger} for this platform.
     *
     * @return The {@link IPLogger} for this platform.
     */
    @NonNull IPLogger getPLogger();

    /**
     * Gets the {@link IDoorActivityManager} instance.
     *
     * @return The {@link IDoorActivityManager} instance.
     */
    @NonNull IDoorActivityManager getDoorActivityManager();

    /**
     * Gets the {@link IDebugReporter}.
     *
     * @return The {@link IDebugReporter}.
     */
    @NonNull IDebugReporter getDebugReporter();

    /**
     * Gets the build id of BigDoors that is currently running.
     *
     * @return The build id of BigDoors that is currently running.
     */
    @NonNull String getVersion();

    /**
     * Gets the {@link IPServer} instance.
     *
     * @return The {@link IPServer} instance.
     */
    @NonNull IPServer getPServer();

    /**
     * Gets the {@link ILimitsManager}.
     *
     * @return The {@link ILimitsManager}.
     */
    @NonNull ILimitsManager getLimitsManager();
}

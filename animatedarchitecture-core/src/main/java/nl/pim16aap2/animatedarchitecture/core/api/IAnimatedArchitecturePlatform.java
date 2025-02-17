package nl.pim16aap2.animatedarchitecture.core.api;

import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlockFactory;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimatedBlockHook;
import nl.pim16aap2.animatedarchitecture.core.api.animatedblock.IAnimationHook;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ILocationFactory;
import nl.pim16aap2.animatedarchitecture.core.api.factories.IPlayerFactory;
import nl.pim16aap2.animatedarchitecture.core.api.factories.IWorldFactory;
import nl.pim16aap2.animatedarchitecture.core.audio.IAudioPlayer;
import nl.pim16aap2.animatedarchitecture.core.commands.CommandFactory;
import nl.pim16aap2.animatedarchitecture.core.commands.DelayedCommandInputRequest;
import nl.pim16aap2.animatedarchitecture.core.commands.IServer;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.AnimatedBlockHookManager;
import nl.pim16aap2.animatedarchitecture.core.managers.AnimationHookManager;
import nl.pim16aap2.animatedarchitecture.core.managers.DatabaseManager;
import nl.pim16aap2.animatedarchitecture.core.managers.DelayedCommandInputManager;
import nl.pim16aap2.animatedarchitecture.core.managers.LimitsManager;
import nl.pim16aap2.animatedarchitecture.core.managers.PowerBlockManager;
import nl.pim16aap2.animatedarchitecture.core.managers.StructureSpecificationManager;
import nl.pim16aap2.animatedarchitecture.core.managers.StructureTypeManager;
import nl.pim16aap2.animatedarchitecture.core.managers.ToolUserManager;
import nl.pim16aap2.animatedarchitecture.core.moveblocks.StructureActivityManager;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureAnimationRequest;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureAnimationRequestBuilder;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureRegistry;
import nl.pim16aap2.animatedarchitecture.core.util.VersionReader;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureFinder;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureRetriever;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureRetrieverFactory;

/**
 * Represents a set of getter methods to get access to the internals of AnimatedArchitecture.
 *
 * @author Pim
 */
@SuppressWarnings("unused")
public interface IAnimatedArchitecturePlatform
{
    /**
     * Restarts the plugin.
     */
    void restartPlugin();

    /**
     * Shuts the plugin down.
     */
    void shutDownPlugin();

    /**
     * Getter for the name of current version.
     * <p>
     * It is not guaranteed that this will return the version in any specific format; only that it is a String.
     *
     * @return The name of the current version.
     */
    String getVersionName();

    /**
     * @return The version info. This provides access to items such as the commit hash, the build id, etc.
     */
    VersionReader.VersionInfo getVersionInfo();

    /**
     * Gets the instance of the {@link IAnimatedArchitectureToolUtil} for this platform.
     *
     * @return The instance of the {@link IAnimatedArchitectureToolUtil} for this platform.
     */
    IAnimatedArchitectureToolUtil getAnimatedArchitectureToolUtil();

    /**
     * Gets the instance of the {@link IWorldFactory} for this platform.
     *
     * @return The instance of the {@link IWorldFactory} for this platform.
     */
    IWorldFactory getWorldFactory();

    /**
     * Gets the instance of the {@link ILocationFactory} for this platform.
     *
     * @return The instance of the {@link ILocationFactory} for this platform.
     */
    ILocationFactory getLocationFactory();

    /**
     * Gets the instance of the {@link IAnimatedBlockFactory} for this platform.
     *
     * @return The instance of the {@link IAnimatedBlockFactory} for this platform.
     */
    IAnimatedBlockFactory getAnimatedBlockFactory();

    /**
     * Gets the instance of the {@link IPlayerFactory} for this platform.
     *
     * @return The instance of the {@link IPlayerFactory} for this platform.
     */
    IPlayerFactory getPlayerFactory();

    /**
     * @return The manager for {@link IAnimatedBlockHook}s.
     */
    AnimatedBlockHookManager getAnimatedBlockHookManager();

    /**
     * @return The manager for {@link IAnimationHook}s.
     */
    AnimationHookManager getAnimationHookManager();

    /**
     * Gets the instance of the {@link IConfig} for this platform.
     *
     * @return The instance of the {@link IConfig} for this platform.
     */
    IConfig getAnimatedArchitectureConfig();

    /**
     * Gets the instance of the {@link IAudioPlayer} for this platform.
     *
     * @return The instance of the {@link IAudioPlayer} for this platform.
     */
    IAudioPlayer getAudioPlayer();

    /**
     * Gets the instance of the {@link IBlockAnalyzer} for this platform.
     *
     * @return The instance of the {@link IBlockAnalyzer} for this platform.
     */
    IBlockAnalyzer getBlockAnalyzer();

    /**
     * Constructs a new {@link IExecutor}.
     *
     * @return A new {@link IExecutor}.
     */
    IExecutor getExecutor();

    /**
     * Gets the {@link GlowingBlockSpawner} for the current platform.
     *
     * @return The {@link GlowingBlockSpawner} for the current platform.
     */
    GlowingBlockSpawner getGlowingBlockSpawner();

    /**
     * Gets the {@link ILocalizer} used to localize strings.
     *
     * @return The {@link ILocalizer} registered for this platform.
     */
    ILocalizer getLocalizer();

    /**
     * Gets the instance of the {@link IMessagingInterface} for this platform.
     *
     * @return The instance of the {@link IMessagingInterface} for this platform.
     */
    IMessagingInterface getMessagingInterface();

    /**
     * Gets the implementation of a {@link IMessageable} for the server.
     *
     * @return The implementation of a {@link IMessageable} for the server.
     */
    IMessageable getMessageableServer();

    /**
     * Gets the {@link IServer} instance.
     *
     * @return The {@link IServer} instance.
     */
    IServer getServer();

    /**
     * Gets the {@link StructureRegistry}.
     *
     * @return The {@link StructureRegistry}.
     */
    StructureRegistry getDoorRegistry();

    /**
     * @return The instance of the {@link IConfig} for this platform.
     */
    IChunkLoader getChunkLoader();

    /**
     * Gets the {@link DatabaseManager}.
     *
     * @return The {@link DatabaseManager}.
     */
    DatabaseManager getDatabaseManager();

    /**
     * Gets the {@link StructureActivityManager} instance.
     *
     * @return The {@link StructureActivityManager} instance.
     */
    StructureActivityManager getDoorActivityManager();

    /**
     * Gets the {@link StructureSpecificationManager} instance.
     *
     * @return The {@link StructureSpecificationManager} instance.
     */
    StructureSpecificationManager getDoorSpecificationManager();

    /**
     * Gets the {@link StructureTypeManager} instance.
     *
     * @return The {@link StructureTypeManager} instance.
     */
    StructureTypeManager getDoorTypeManager();

    /**
     * Gets the {@link ToolUserManager} instance.
     *
     * @return The {@link ToolUserManager} instance.
     */
    ToolUserManager getToolUserManager();

    /**
     * Gets the {@link DelayedCommandInputManager} to manage {@link DelayedCommandInputRequest}s.
     *
     * @return The {@link DelayedCommandInputManager} registered by the platform.
     */
    DelayedCommandInputManager getDelayedCommandInputManager();

    /**
     * Gets the {@link PowerBlockManager} instance.
     *
     * @return The {@link PowerBlockManager} instance.
     */
    PowerBlockManager getPowerBlockManager();

    /**
     * Gets the instance of the {@link IEconomyManager} for this platform.
     *
     * @return The instance of the {@link IEconomyManager} for this platform.
     */
    IEconomyManager getEconomyManager();

    /**
     * Gets the instance of the {@link IPermissionsManager} for this platform.
     *
     * @return The instance of the {@link IPermissionsManager} for this platform.
     */
    IPermissionsManager getPermissionsManager();

    /**
     * @return The command factory.
     */
    CommandFactory getCommandFactory();

    /**
     * @return The factory used to create new {@link StructureRetriever} and {@link StructureFinder} instances.
     */
    StructureRetrieverFactory getStructureRetrieverFactory();

    /**
     * @return A new builder used to create new {@link StructureAnimationRequest} instances.
     */
    StructureAnimationRequestBuilder.IBuilderStructure getStructureAnimationRequestBuilder();

    /**
     * Gets the instance of the {@link IProtectionCompatManager} for this platform.
     *
     * @return The instance of the {@link IProtectionCompatManager} for this platform.
     */
    IProtectionCompatManager getProtectionCompatManager();

    /**
     * Gets the {@link LimitsManager}.
     *
     * @return The {@link LimitsManager}.
     */
    LimitsManager getLimitsManager();
}

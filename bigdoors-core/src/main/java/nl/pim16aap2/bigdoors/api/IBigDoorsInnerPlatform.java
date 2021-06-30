package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.commands.DelayedCommandInputRequest;
import nl.pim16aap2.bigdoors.doors.DoorOpener;
import nl.pim16aap2.bigdoors.managers.AutoCloseScheduler;
import nl.pim16aap2.bigdoors.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.managers.DelayedCommandInputManager;
import nl.pim16aap2.bigdoors.managers.DoorActivityManager;
import nl.pim16aap2.bigdoors.managers.DoorRegistry;
import nl.pim16aap2.bigdoors.managers.DoorSpecificationManager;
import nl.pim16aap2.bigdoors.managers.DoorTypeManager;
import nl.pim16aap2.bigdoors.managers.ILimitsManager;
import nl.pim16aap2.bigdoors.managers.LimitsManager;
import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import org.jetbrains.annotations.NotNull;

public interface IBigDoorsInnerPlatform extends IBigDoorsPlatform
{

    /**
     * Gets the {@link DoorTypeManager} instance.
     *
     * @return The {@link DoorTypeManager} instance.
     */
    @NotNull DoorTypeManager getDoorTypeManager();

    /**
     * Gets the {@link DoorSpecificationManager} instance.
     *
     * @return The {@link DoorSpecificationManager} instance.
     */
    @NotNull DoorSpecificationManager getDoorSpecificationManager();

    /**
     * Gets the {@link ToolUserManager} instance.
     *
     * @return The {@link ToolUserManager} instance.
     */
    @NotNull ToolUserManager getToolUserManager();

    /**
     * Gets the {@link DelayedCommandInputManager} to manage {@link DelayedCommandInputRequest}s.
     *
     * @return The {@link DelayedCommandInputManager} registered by the platform.
     */
    @NotNull DelayedCommandInputManager getDelayedCommandInputManager();

    /**
     * Gets the {@link LimitsManager}.
     *
     * @return The {@link LimitsManager}.
     */
    @Override @NotNull ILimitsManager getLimitsManager();

    /**
     * Gets the {@link DatabaseManager}.
     *
     * @return The {@link DatabaseManager}.
     */
    @NotNull DatabaseManager getDatabaseManager();

    /**
     * Gets the {@link DoorRegistry}.
     *
     * @return The {@link DoorRegistry}.
     */
    @NotNull DoorRegistry getDoorRegistry();

    /**
     * Gets the {@link PowerBlockManager} instance.
     *
     * @return The {@link PowerBlockManager} instance.
     */
    @NotNull PowerBlockManager getPowerBlockManager();

    /**
     * Gets the {@link AutoCloseScheduler} instance.
     *
     * @return The {@link AutoCloseScheduler} instance.
     */
    @NotNull AutoCloseScheduler getAutoCloseScheduler();

    /**
     * Gets the {@link DoorActivityManager} instance.
     *
     * @return The {@link DoorActivityManager} instance.
     */
    @Override @NotNull DoorActivityManager getDoorActivityManager();

    /**
     * Gets the {@link DoorOpener}.
     *
     * @return The {@link DoorOpener}.
     */
    @NotNull DoorOpener getDoorOpener();

    /**
     * Gets the instance of the {@link IBigDoorsToolUtil} for this platform.
     *
     * @return The instance of the {@link IBigDoorsToolUtil} for this platform.
     */
    @NotNull IBigDoorsToolUtil getBigDoorsToolUtil();
}

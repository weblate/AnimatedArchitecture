package nl.pim16aap2.animatedarchitecture.core.commands;

import com.google.common.flogger.StackSize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Locked;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureAttribute;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureRetriever;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.DatabaseManager;
import nl.pim16aap2.animatedarchitecture.core.text.TextType;
import nl.pim16aap2.animatedarchitecture.core.util.Util;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ITextFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Represents a command that relates to an existing structure.
 *
 * @author Pim
 */
@Flogger
public abstract class StructureTargetCommand extends BaseCommand
{
    @Getter
    protected final StructureRetriever structureRetriever;

    private final StructureAttribute structureAttribute;

    /**
     * The result of the {@link #structureRetriever}.
     * <p>
     * This will not be available until after {@link #executeCommand(PermissionsStatus)} has started, but before
     * {@link #performAction(AbstractStructure)} is called.
     * <p>
     * Even after the result has been set, it may still be null in case no doors were found.
     */
    @Getter(onMethod_ = @Locked.Read)
    @Setter(onMethod_ = @Locked.Write)
    private @Nullable AbstractStructure retrieverResult;

    protected StructureTargetCommand(
        ICommandSender commandSender, ILocalizer localizer, ITextFactory textFactory,
        StructureRetriever structureRetriever,
        StructureAttribute structureAttribute)
    {
        super(commandSender, localizer, textFactory);
        this.structureRetriever = structureRetriever;
        this.structureAttribute = structureAttribute;
    }

    @Override
    protected final CompletableFuture<?> executeCommand(PermissionsStatus permissions)
    {
        return getStructure(getStructureRetriever())
            .thenApply(structure ->
                       {
                           setRetrieverResult(structure.orElse(null));
                           return structure;
                       })
            .thenAcceptAsync(structure -> processStructureResult(structure, permissions))
            .exceptionally(Util::exceptionally);
    }

    /**
     * Handles the result of retrieving the structure.
     *
     * @param structure
     *     The result of trying to retrieve the structure.
     * @param permissions
     *     Whether the ICommandSender has user and/or admin permissions.
     */
    private void processStructureResult(Optional<AbstractStructure> structure, PermissionsStatus permissions)
    {
        if (structure.isEmpty())
        {
            log.atFine().log("Failed to find structure %s for command: %s", getStructureRetriever(), this);

            getCommandSender()
                .sendMessage(textFactory, TextType.ERROR,
                             localizer.getMessage("commands.structure_target_command.base.error.structure_not_found"));
            return;
        }

        if (!isAllowed(structure.get(), permissions.hasAdminPermission()))
        {
            log.atFine()
               .log("%s does not have access to structure %s for command %s", getCommandSender(), structure, this);

            getCommandSender()
                .sendMessage(textFactory, TextType.ERROR,
                             localizer.getMessage(
                                 "commands.structure_target_command.base.error.no_permission_for_action",
                                 localizer.getStructureType(structure.get())));
            return;
        }

        try
        {
            performAction(structure.get()).get(30, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to perform command " + this + " for structure " + structure, e);
        }
    }

    /**
     * Checks if execution of this command is allowed for the given {@link AbstractStructure}.
     *
     * @param structure
     *     The {@link AbstractStructure} that is the target for this command.
     * @param bypassPermission
     *     Whether the {@link ICommandSender} has bypass access.
     * @return True if execution of this command is allowed.
     */
    protected boolean isAllowed(AbstractStructure structure, boolean bypassPermission)
    {
        return hasAccessToAttribute(structure, structureAttribute, bypassPermission);
    }

    /**
     * Performs the action of this command on the {@link AbstractStructure}.
     *
     * @param structure
     *     The {@link AbstractStructure} to perform the action on.
     * @return The future of the command execution.
     */
    protected abstract CompletableFuture<?> performAction(AbstractStructure structure);

    /**
     * @return The structure description of the {@link #retrieverResult}.
     */
    protected final StructureDescription getRetrievedStructureDescription()
    {
        return StructureDescription.of(localizer, getRetrieverResult());
    }


    /**
     * Called by {@link #handleDatabaseActionResult(DatabaseManager.ActionResult)} when the database action was
     * cancelled.
     */
    protected void handleDatabaseActionCancelled()
    {
        getCommandSender().sendMessage(textFactory, TextType.ERROR,
                                       localizer.getMessage("commands.base.error.action_cancelled"));
    }

    /**
     * Called by {@link #handleDatabaseActionResult(DatabaseManager.ActionResult)} when the database action was
     * successful.
     */
    protected void handleDatabaseActionSuccess()
    {
    }

    /**
     * Called by {@link #handleDatabaseActionResult(DatabaseManager.ActionResult)} when the database action failed.
     */
    protected void handleDatabaseActionFail()
    {
        getCommandSender().sendMessage(textFactory, TextType.ERROR, localizer.getMessage("constants.error.generic"));
    }

    /**
     * Handles the results of a database action by informing the user of any non-success states.
     * <p>
     * To customize the handling, you can override {@link #handleDatabaseActionFail()},
     * {@link #handleDatabaseActionCancelled()}, or {@link #handleDatabaseActionSuccess()}.
     *
     * @param result
     *     The result obtained from the database.
     */
    protected final void handleDatabaseActionResult(DatabaseManager.ActionResult result)
    {
        log.atFine().log("Handling database action result: %s for command: %s", result.name(), this);
        switch (result)
        {
            case CANCELLED -> handleDatabaseActionCancelled();
            case SUCCESS -> handleDatabaseActionSuccess();
            case FAIL -> handleDatabaseActionFail();
        }
    }

    /**
     * A simple description of a structure.
     *
     * @param typeName
     *     The localized name of the structure's type.
     * @param id
     *     The user-friendly identifier of the structure.
     */
    protected record StructureDescription(String typeName, String id)
    {
        private static final StructureDescription EMPTY_DESCRIPTION = new StructureDescription("Structure", "null");

        private static StructureDescription of(ILocalizer localizer, @Nullable AbstractStructure structure)
        {
            if (structure != null)
                return new StructureDescription(
                    localizer.getStructureType(structure), structure.getName() + " (" + structure.getUid() + ")");

            log.atSevere().withStackTrace(StackSize.FULL).log("Structure not available after database action!");
            return EMPTY_DESCRIPTION;
        }
    }
}

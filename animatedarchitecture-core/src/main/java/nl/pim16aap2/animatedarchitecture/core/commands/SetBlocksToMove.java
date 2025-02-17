package nl.pim16aap2.animatedarchitecture.core.commands;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import lombok.ToString;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureRetriever;
import nl.pim16aap2.animatedarchitecture.core.util.structureretriever.StructureRetrieverFactory;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ITextFactory;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureAttribute;
import nl.pim16aap2.animatedarchitecture.core.structures.structurearchetypes.IDiscreteMovement;
import nl.pim16aap2.animatedarchitecture.core.text.TextType;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the command that is used to change the number of blocks a block will try to move.
 *
 * @author Pim
 */
@ToString
public class SetBlocksToMove extends StructureTargetCommand
{
    public static final CommandDefinition COMMAND_DEFINITION = CommandDefinition.SET_BLOCKS_TO_MOVE;

    private final int blocksToMove;

    @AssistedInject //
    SetBlocksToMove(
        @Assisted ICommandSender commandSender, ILocalizer localizer, ITextFactory textFactory,
        @Assisted StructureRetriever structureRetriever, @Assisted int blocksToMove)
    {
        super(commandSender, localizer, textFactory, structureRetriever, StructureAttribute.BLOCKS_TO_MOVE);
        this.blocksToMove = blocksToMove;
    }

    @Override
    public CommandDefinition getCommand()
    {
        return COMMAND_DEFINITION;
    }

    @Override
    protected void handleDatabaseActionSuccess()
    {
        final var desc = getRetrievedStructureDescription();
        getCommandSender().sendSuccess(textFactory, localizer.getMessage("commands.set_blocks_to_move.success",
                                                                         desc.typeName(), desc.id()));
    }

    @Override
    protected CompletableFuture<?> performAction(AbstractStructure structure)
    {
        if (!(structure instanceof IDiscreteMovement))
        {
            getCommandSender()
                .sendMessage(textFactory, TextType.ERROR,
                             localizer.getMessage("commands.set_blocks_to_move.error.invalid_structure_type",
                                                  localizer.getStructureType(structure), structure.getBasicInfo()));
            return CompletableFuture.completedFuture(null);
        }

        ((IDiscreteMovement) structure).setBlocksToMove(blocksToMove);
        return structure.syncData().thenAccept(this::handleDatabaseActionResult);
    }

    @AssistedFactory
    interface IFactory
    {
        /**
         * Creates (but does not execute!) a new {@link SetBlocksToMove} command.
         *
         * @param commandSender
         *     The {@link ICommandSender} responsible for changing the blocks-to-move distance of the structure.
         * @param structureRetriever
         *     A {@link StructureRetrieverFactory} representing the {@link AbstractStructure} for which the
         *     blocks-to-move distance will be modified.
         * @param blocksToMove
         *     The new blocks-to-move distance.
         * @return See {@link BaseCommand#run()}.
         */
        SetBlocksToMove newSetBlocksToMove(
            ICommandSender commandSender, StructureRetriever structureRetriever, int blocksToMove);
    }
}

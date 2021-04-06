package nl.pim16aap2.bigdoors.commands;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.ICommandSender;

public class Cancel extends BaseCommand
{

    public Cancel(@NonNull ICommandSender commandSender)
    {
        super(commandSender);
    }

    @Override
    public @NonNull CommandDefinition getCommand()
    {
        return CommandDefinition.CANCEL;
    }
}

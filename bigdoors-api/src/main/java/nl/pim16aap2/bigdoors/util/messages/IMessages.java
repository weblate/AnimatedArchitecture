package nl.pim16aap2.bigdoors.util.messages;

import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import org.jetbrains.annotations.NotNull;

public interface IMessages extends IRestartable
{
    /**
     * Tries to get the translated message from the name of a {@link Message}. If no such mapping exists, an empty
     * String will be returned.
     *
     * @param messageName The name of a {@link Message}, see {@link Message#valueOf(String)}.
     * @return The translated String if possible, otherwise an empty String.
     */
    @NotNull String getString(@NotNull String messageName);

    /**
     * Gets the translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     *
     * @param msg    The {@link Message} to translate.
     * @param values The values to substitute for the variables in the message.
     * @return The translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     */
    @NotNull String getString(@NotNull Message msg, @NotNull String... values);
}

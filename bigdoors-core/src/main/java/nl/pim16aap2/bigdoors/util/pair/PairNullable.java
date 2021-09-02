package nl.pim16aap2.bigdoors.util.pair;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a name-value pair.
 *
 * @author Pim
 */
@EqualsAndHashCode
@AllArgsConstructor
@ToString
// SonarLint doesn't like non-final public member variables (S1104).
// However, that's the point of this class and there's no reason to do it in another way.
@SuppressWarnings("squid:S1104")
// CHECKSTYLE:OFF
public final class PairNullable<T, U>
{
    public @Nullable T first;
    public @Nullable U second;
}

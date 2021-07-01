package nl.pim16aap2.bigdoors.util;

import lombok.experimental.UtilityClass;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.logging.IPLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents various small and platform agnostic utility functions.
 *
 * @author Pim
 */
@UtilityClass
public class InnerUtil
{
    /**
     * Logs a throwable using {@link IPLogger#logThrowable(Throwable)} and returns a fallback value.
     * <p>
     * Mostly useful for {@link CompletableFuture#exceptionally(Function)}.
     *
     * @param throwable The throwable to send to the logger.
     * @param fallback  The fallback value to return.
     * @param <T>       The type of the fallback value.
     * @return The fallback value.
     */
    @Contract("_, !null -> !null")
    @SuppressWarnings("NullAway")
    public <T> T exceptionally(final @NotNull Throwable throwable, final T fallback)
    {
        BigDoors.get().getPLogger().logThrowable(throwable);
        return fallback;
    }

    /**
     * See {@link #exceptionally(Throwable, Object)} with a null fallback value.
     *
     * @return Always null
     */
    @SuppressWarnings("NullAway")
    public @Nullable <T> T exceptionally(final @NotNull Throwable throwable)
    {
        return exceptionally(throwable, null);
    }

    /**
     * See {@link #exceptionally(Throwable, Object)} with a fallback value of {@link Optional#empty()}.
     *
     * @return Always {@link Optional#empty()}.
     */
    public <T> Optional<T> exceptionallyOptional(final @NotNull Throwable throwable)
    {
        return exceptionally(throwable, Optional.empty());
    }

    /**
     * Handles exceptional completion of a {@link CompletableFuture}. This ensure that the target is finished
     * exceptionally as well, to propagate the exception.
     *
     * @param throwable The {@link Throwable} to log.
     * @param fallback  The fallback value to return.
     * @param target    The {@link CompletableFuture} to complete.
     * @return The fallback value.
     */
    public <T, U> T exceptionallyCompletion(@NotNull Throwable throwable, T fallback,
                                            @NotNull CompletableFuture<U> target)
    {
        target.completeExceptionally(throwable);
        return fallback;
    }

    /**
     * Handles exceptional completion of a {@link CompletableFuture}. This ensure that the target is finished
     * exceptionally as well, to propagate the exception.
     *
     * @param throwable The {@link Throwable} to log.
     * @param target    The {@link CompletableFuture} to complete.
     * @return Always null;
     */
    public <T> Void exceptionallyCompletion(@NotNull Throwable throwable, @NotNull CompletableFuture<T> target)
    {
        target.completeExceptionally(throwable);
        return null;
    }
}

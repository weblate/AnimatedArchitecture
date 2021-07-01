package nl.pim16aap2.bigdoors.tooluser.stepexecutor;

import lombok.AllArgsConstructor;
import lombok.ToString;
import nl.pim16aap2.bigdoors.api.IPLocationConst;
import nl.pim16aap2.bigdoors.api.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@ToString
@AllArgsConstructor
public class StepExecutorPLocation extends StepExecutor
{
    @ToString.Exclude
    private final @NotNull Function<IPLocationConst, Boolean> fun;

    @Override
    protected boolean protectedAccept(@Nullable Object input)
    {
        Util.requireNonNull(input, "Location input");
        return fun.apply((IPLocationConst) input);
    }

    @Override
    public @NotNull Class<?> getInputClass()
    {
        return IPLocationConst.class;
    }
}

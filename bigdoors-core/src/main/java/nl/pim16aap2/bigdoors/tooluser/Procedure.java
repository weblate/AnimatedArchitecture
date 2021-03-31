package nl.pim16aap2.bigdoors.tooluser;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.tooluser.step.IStep;
import nl.pim16aap2.bigdoors.tooluser.stepexecutor.StepExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public final class Procedure<T extends ToolUser>
{
    @NotNull
    protected IStep currentStep;

    @NotNull
    protected final T toolUser;

    @NotNull
    Iterator<IStep> steps;

    public Procedure(final @NotNull T toolUser, final @NotNull List<IStep> steps)
    {
        this.toolUser = toolUser;
        this.steps = steps.iterator();
        goToNextStep();
    }

    /**
     * Advances to the next step.
     */
    public void goToNextStep()
    {
        if (!steps.hasNext())
        {
            BigDoors.get().getPLogger().logThrowable(new IndexOutOfBoundsException(
                "Trying to advance to the next step while there is none! Step: " + getCurrentStepName()));
            return;
        }
        currentStep = steps.next();

        if (currentStep.skip())
            goToNextStep();
    }

    /**
     * Skips to a specific {@link IStep} in this {@link Procedure}.
     * <p>
     * If the step could not be found, the procedure will skip to the last step.
     *
     * @param goalStep The {@link IStep} to jump to.
     * @return True if the jump was successful, otherwise false.
     */
    public boolean skipToStep(final @NotNull IStep goalStep)
    {
        while (steps.hasNext())
        {
            IStep step = steps.next();
            if (step.equals(goalStep))
            {
                currentStep = step;
                return true;
            }
        }
        return false;
    }

    /**
     * Applies some kind of input to the {@link StepExecutor} for the current step.
     *
     * @param obj The input to apply.
     * @return True if the application was successful.
     */
    public boolean applyStepExecutor(final @Nullable Object obj)
    {
        return currentStep.getStepExecutor().map(stepExecutor -> stepExecutor.apply(obj)).orElse(false);
    }

    /**
     * Gets the message for the current step, with all the variables filled in.
     *
     * @return The message for the current step.
     */
    public @NotNull String getMessage()
    {
        return currentStep.getLocalizedMessage();
    }

    /**
     * Gets the name of the current step.
     *
     * @return The name of the current step.
     */
    public @NotNull String getCurrentStepName()
    {
        return currentStep.getName();
    }

    /**
     * Whether the current step requires waiting for user input.
     *
     * @return True if the current step should wait for user input.
     */
    public boolean waitForUserInput()
    {
        return currentStep.waitForUserInput();
    }
}

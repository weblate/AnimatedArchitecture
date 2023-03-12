package nl.pim16aap2.animatedarchitecture.structures.windmill;

import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureType;
import nl.pim16aap2.animatedarchitecture.core.text.TextType;
import nl.pim16aap2.animatedarchitecture.core.tooluser.Step;
import nl.pim16aap2.animatedarchitecture.core.tooluser.ToolUser;
import nl.pim16aap2.animatedarchitecture.core.tooluser.creator.Creator;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CreatorWindMill extends Creator
{
    private static final StructureType STRUCTURE_TYPE = StructureTypeWindmill.get();

    public CreatorWindMill(ToolUser.Context context, IPlayer player, @Nullable String name)
    {
        super(context, player, name);
    }

    @Override
    protected List<Step> generateSteps()
        throws InstantiationException
    {
        return Arrays.asList(
            factorySetName.construct(),
            factorySetFirstPos
                .textSupplier(text -> text.append(
                    localizer.getMessage("creator.windmill.step_1"), TextType.INFO, getStructureArg()))
                .construct(),
            factorySetSecondPos
                .textSupplier(text -> text.append(
                    localizer.getMessage("creator.windmill.step_2"), TextType.INFO, getStructureArg()))
                .construct(),
            factorySetRotationPointPos
                .textSupplier(text -> text.append(
                    localizer.getMessage("creator.windmill.step_3"), TextType.INFO, getStructureArg()))
                .construct(),
            factorySetPowerBlockPos.construct(),
            factorySetOpenDir.construct(),
            factoryReviewResult.construct(),
            factoryConfirmPrice.construct(),
            factoryCompleteProcess.construct());
    }

    @Override
    protected void giveTool()
    {
        giveTool("tool_user.base.stick_name", "creator.windmill.stick_lore");
    }

    @Override
    protected AbstractStructure constructStructure()
    {
        return new Windmill(constructStructureData());
    }

    @Override
    protected StructureType getStructureType()
    {
        return STRUCTURE_TYPE;
    }
}

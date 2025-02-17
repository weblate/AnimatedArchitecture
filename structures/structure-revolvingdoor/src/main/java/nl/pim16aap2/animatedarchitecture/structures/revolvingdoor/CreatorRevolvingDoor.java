package nl.pim16aap2.animatedarchitecture.structures.revolvingdoor;

import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureType;
import nl.pim16aap2.animatedarchitecture.core.tooluser.Step;
import nl.pim16aap2.animatedarchitecture.core.tooluser.creator.Creator;
import nl.pim16aap2.animatedarchitecture.structures.bigdoor.CreatorBigDoor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CreatorRevolvingDoor extends CreatorBigDoor
{
    private static final StructureType STRUCTURE_TYPE = StructureTypeRevolvingDoor.get();

    public CreatorRevolvingDoor(Creator.Context context, IPlayer player, @Nullable String name)
    {
        super(context, player, name);
    }

    @Override
    protected List<Step> generateSteps()
        throws InstantiationException
    {
        return Arrays.asList(factorySetName.construct(),
                             factorySetFirstPos.messageKey("creator.revolving_door.step_1").construct(),
                             factorySetSecondPos.messageKey("creator.revolving_door.step_2").construct(),
                             factorySetRotationPointPos.messageKey("creator.revolving_door.step_3").construct(),
                             factorySetPowerBlockPos.construct(),
                             factorySetOpenDir.construct(),
                             factoryConfirmPrice.construct(),
                             factoryCompleteProcess.messageKey("creator.revolving_door.success").construct());
    }

    @Override
    protected void giveTool()
    {
        giveTool("tool_user.base.stick_name", "creator.revolving_door.stick_lore", "creator.revolving_door.init");
    }

    @Override
    protected AbstractStructure constructStructure()
    {
        return new RevolvingDoor(constructStructureData());
    }

    @Override
    protected StructureType getStructureType()
    {
        return STRUCTURE_TYPE;
    }
}

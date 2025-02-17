package nl.pim16aap2.animatedarchitecture.core.tooluser;

import nl.pim16aap2.animatedarchitecture.core.api.ILocation;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.api.IProtectionCompatManager;
import nl.pim16aap2.animatedarchitecture.core.api.IWorld;
import nl.pim16aap2.animatedarchitecture.core.structures.AbstractStructure;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureType;
import nl.pim16aap2.animatedarchitecture.core.UnitTestUtil;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ITextFactory;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class PowerBlockRelocatorTest
{
    @Mock
    private AbstractStructure structure;

    @Mock
    private IWorld world;

    private final Vector3Di currentPowerBlockLoc = new Vector3Di(2, 58, 2384);

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPlayer player;

    private IProtectionCompatManager compatManager;

    @Mock
    private ILocation location;

    @Mock
    private PowerBlockRelocator.IFactory factory;

    @BeforeEach
    void init()
    {
        MockitoAnnotations.openMocks(this);

        Mockito.when(structure.getWorld()).thenReturn(world);
        Mockito.when(structure.getPowerBlock()).thenReturn(currentPowerBlockLoc);

        final StructureType structureTypeType = Mockito.mock(StructureType.class);
        Mockito.when(structure.getType()).thenReturn(structureTypeType);
        Mockito.when(structureTypeType.getLocalizationKey()).thenReturn("StructureType");

        compatManager = Mockito.mock(IProtectionCompatManager.class);
        Mockito.when(compatManager.canBreakBlock(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(compatManager.canBreakBlocksBetweenLocs(Mockito.any(), Mockito.any(),
                                                             Mockito.any(), Mockito.any()))
               .thenReturn(Optional.empty());

        final ILocalizer localizer = UnitTestUtil.initLocalizer();

        final ToolUser.Context context = Mockito.mock(ToolUser.Context.class, Answers.RETURNS_MOCKS);
        Mockito.when(context.getProtectionCompatManager()).thenReturn(compatManager);
        Mockito.when(context.getLocalizer()).thenReturn(localizer);
        Mockito.when(context.getTextFactory()).thenReturn(ITextFactory.getSimpleTextFactory());

        final Step.Factory.IFactory assistedStepFactory = Mockito.mock(Step.Factory.IFactory.class);
        //noinspection deprecation
        Mockito.when(assistedStepFactory.stepName(Mockito.anyString()))
               .thenAnswer(invocation -> new Step.Factory(localizer, invocation.getArgument(0, String.class)));
        Mockito.when(context.getStepFactory()).thenReturn(assistedStepFactory);

        Mockito.when(factory.create(Mockito.any(IPlayer.class), Mockito.any(AbstractStructure.class)))
               .thenAnswer(invoc -> new PowerBlockRelocator(context, invoc.getArgument(0, IPlayer.class),
                                                            invoc.getArgument(1, AbstractStructure.class)));
    }

    @Test
    void testMoveToLocWorld()
    {
        final PowerBlockRelocator relocator = factory.create(player, structure);

        Mockito.when(location.getWorld()).thenReturn(Mockito.mock(IWorld.class));

        Assertions.assertFalse(relocator.moveToLoc(location));
        Mockito.verify(player)
               .sendMessage(UnitTestUtil.toText("tool_user.powerblock_relocator.error.world_mismatch StructureType"));

        Mockito.when(location.getWorld()).thenReturn(Mockito.mock(IWorld.class));
    }

    @Test
    void testMoveToLocDuplicated()
    {
        final PowerBlockRelocator relocator = factory.create(player, structure);

        Mockito.when(location.getWorld()).thenReturn(world);

        Mockito.when(location.getPosition()).thenReturn(new Vector3Di(0, 0, 0));
        Assertions.assertTrue(relocator.moveToLoc(location));

        Mockito.when(location.getPosition()).thenReturn(currentPowerBlockLoc);
        Assertions.assertTrue(relocator.moveToLoc(location));
    }

    @Test
    void testMoveToLocNoAccess()
    {
        final PowerBlockRelocator relocator = factory.create(player, structure);

        final String compat = "TestCompat";
        Mockito.when(compatManager.canBreakBlock(Mockito.any(), Mockito.any())).thenReturn(Optional.of(compat));

        Mockito.when(location.getWorld()).thenReturn(world);
        Mockito.when(location.getPosition()).thenReturn(new Vector3Di(0, 0, 0));

        Assertions.assertFalse(relocator.moveToLoc(location));
    }

    @Test
    void testExecution()
    {
        final PowerBlockRelocator relocator = factory.create(player, structure);

        Mockito.when(location.getWorld()).thenReturn(world);
        Mockito.when(location.getPosition()).thenReturn(new Vector3Di(0, 0, 0));

        Assertions.assertTrue(relocator.handleInput(location));

        Mockito.verify(structure).syncData();
    }

    @Test
    void testExecutionUnchanged()
    {
        final PowerBlockRelocator relocator = factory.create(player, structure);

        Mockito.when(location.getWorld()).thenReturn(world);
        Mockito.when(location.getPosition()).thenReturn(currentPowerBlockLoc);

        Assertions.assertTrue(relocator.handleInput(location));

        Mockito.verify(structure, Mockito.never()).syncData();
    }
}

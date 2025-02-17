package nl.pim16aap2.animatedarchitecture.core.tooluser.creator;

import nl.pim16aap2.animatedarchitecture.core.UnitTestUtil;
import nl.pim16aap2.animatedarchitecture.core.api.IAnimatedArchitectureToolUtil;
import nl.pim16aap2.animatedarchitecture.core.api.IEconomyManager;
import nl.pim16aap2.animatedarchitecture.core.api.ILocation;
import nl.pim16aap2.animatedarchitecture.core.api.IPlayer;
import nl.pim16aap2.animatedarchitecture.core.api.IProtectionCompatManager;
import nl.pim16aap2.animatedarchitecture.core.api.IWorld;
import nl.pim16aap2.animatedarchitecture.core.api.factories.ITextFactory;
import nl.pim16aap2.animatedarchitecture.core.commands.CommandFactory;
import nl.pim16aap2.animatedarchitecture.core.commands.SetOpenDirectionDelayed;
import nl.pim16aap2.animatedarchitecture.core.localization.ILocalizer;
import nl.pim16aap2.animatedarchitecture.core.managers.DatabaseManager;
import nl.pim16aap2.animatedarchitecture.core.managers.LimitsManager;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureBaseBuilder;
import nl.pim16aap2.animatedarchitecture.core.structures.StructureType;
import nl.pim16aap2.animatedarchitecture.core.tooluser.Procedure;
import nl.pim16aap2.animatedarchitecture.core.tooluser.Step;
import nl.pim16aap2.animatedarchitecture.core.tooluser.ToolUser;
import nl.pim16aap2.animatedarchitecture.core.util.Cuboid;
import nl.pim16aap2.animatedarchitecture.core.util.MovementDirection;
import nl.pim16aap2.animatedarchitecture.core.util.vector.Vector3Di;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

public class CreatorTest
{
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Creator creator;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPlayer player;

    @Mock
    private IEconomyManager economyManager;

    @Mock
    private LimitsManager limitsManager;

    @Mock
    private CommandFactory commandFactory;

    private AutoCloseable mocks;

    @BeforeEach
    void init()
    {
        mocks = MockitoAnnotations.openMocks(this);

        final StructureType structureType = Mockito.mock(StructureType.class);
        Mockito.when(structureType.getLocalizationKey()).thenReturn("StructureType");

        Mockito.when(creator.getStructureType()).thenReturn(structureType);
        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(true);

        final IProtectionCompatManager protectionCompatManager = Mockito.mock(IProtectionCompatManager.class);
        Mockito.when(protectionCompatManager.canBreakBlock(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(protectionCompatManager.canBreakBlocksBetweenLocs(Mockito.any(), Mockito.any(),
                                                                       Mockito.any(), Mockito.any()))
               .thenReturn(Optional.empty());

        final ILocalizer localizer = UnitTestUtil.initLocalizer();
        final var assistedStepFactory = Mockito.mock(Step.Factory.IFactory.class);
        //noinspection deprecation
        Mockito.when(assistedStepFactory.stepName(Mockito.anyString()))
               .thenAnswer(invocation -> new Step.Factory(localizer, invocation.getArgument(0, String.class)));

        UnitTestUtil.setField(Creator.class, creator, "limitsManager", limitsManager);
        UnitTestUtil.setField(Creator.class, creator, "structureBaseBuilder", Mockito.mock(StructureBaseBuilder.class));
        UnitTestUtil.setField(Creator.class, creator, "databaseManager", Mockito.mock(DatabaseManager.class));
        UnitTestUtil.setField(Creator.class, creator, "economyManager", economyManager);
        UnitTestUtil.setField(Creator.class, creator, "commandFactory", commandFactory);

        UnitTestUtil.setField(ToolUser.class, creator, "player", player);
        UnitTestUtil.setField(ToolUser.class, creator, "localizer", localizer);
        UnitTestUtil.setField(ToolUser.class, creator, "textFactory", ITextFactory.getSimpleTextFactory());
        UnitTestUtil.setField(ToolUser.class, creator, "protectionCompatManager", protectionCompatManager);
        UnitTestUtil.setField(ToolUser.class, creator, "animatedArchitectureToolUtil",
                              Mockito.mock(IAnimatedArchitectureToolUtil.class));
        UnitTestUtil.setField(ToolUser.class, creator, "stepFactory", assistedStepFactory);
    }

    @AfterEach
    void cleanup()
        throws Exception
    {
        mocks.close();
    }

    @Test
    void testNameInput()
    {
        final String input = "1";
        // Numerical names are not allowed.
        Assertions.assertFalse(creator.completeNamingStep(input));
        Mockito.verify(player)
               .sendMessage(UnitTestUtil.toText("creator.base.error.invalid_name " + input + " StructureType"));

        Assertions.assertTrue(creator.completeNamingStep("newDoor"));
        Mockito.verify(creator).giveTool();
    }

    @Test
    void testFirstLocation()
    {
        final ILocation loc = UnitTestUtil.getLocation(12.7, 128, 56.12);

        Mockito.doReturn(false).when(creator).playerHasAccessToLocation(Mockito.any());
        // No access to location
        Assertions.assertFalse(creator.setFirstPos(loc));

        Mockito.doReturn(true).when(creator).playerHasAccessToLocation(Mockito.any());
        Assertions.assertTrue(creator.setFirstPos(loc));
        Assertions.assertEquals(loc.getWorld(), creator.world);
        Assertions.assertEquals(new Vector3Di(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), creator.firstPos);
    }

    @Test
    void testWorldMatch()
    {
        final IWorld world = UnitTestUtil.getWorld();
        final String worldName = world.worldName();
        setField("world", world);

        final IWorld secondWorld = UnitTestUtil.getWorld();
        // Different world, so no match!
        Assertions.assertFalse(creator.verifyWorldMatch(Mockito.mock(IWorld.class)));

        Mockito.when(secondWorld.worldName()).thenReturn(worldName);
        // Same world name, so match!
        Assertions.assertTrue(creator.verifyWorldMatch(secondWorld));
    }

    @Test
    void testInit()
    {
        Assertions.assertDoesNotThrow(() -> creator.init());
    }

    @Test
    void testSecondLocation()
    {
        Mockito.doReturn(false).when(creator).playerHasAccessToLocation(Mockito.any());

        final IWorld world = UnitTestUtil.getWorld();

        final Vector3Di vec1 = new Vector3Di(12, 128, 56);
        final Vector3Di vec2 = vec1.add(10, 10, 10);
        final Cuboid cuboid = new Cuboid(vec1, vec2);

        setField("firstPos", vec1);
        setField("world", world);

        final ILocation loc = UnitTestUtil.getLocation(vec2, world);

        // Not allowed, because no access to location
        Assertions.assertFalse(creator.setSecondPos(loc));

        Mockito.doReturn(true).when(creator).playerHasAccessToLocation(Mockito.any());
        Mockito.when(limitsManager.getLimit(Mockito.any(), Mockito.any()))
               .thenReturn(OptionalInt.of(cuboid.getVolume() - 1));
        // Not allowed, because the selected area is too big.
        Assertions.assertFalse(creator.setSecondPos(loc));
        Mockito.verify(player)
               .sendMessage(UnitTestUtil.toText(String.format("creator.base.error.area_too_big StructureType %d %d",
                                                              cuboid.getVolume(), cuboid.getVolume() - 1)));

        Mockito.when(limitsManager.getLimit(Mockito.any(), Mockito.any()))
               .thenReturn(OptionalInt.of(cuboid.getVolume() + 1));
        Mockito.doReturn(false).when(creator).playerHasAccessToCuboid(Mockito.any(), Mockito.any());
        // Not allowed, because no access to one or more blocks in the cuboid area.
        Assertions.assertFalse(creator.setSecondPos(loc));

        Mockito.doReturn(true).when(creator).playerHasAccessToCuboid(Mockito.any(), Mockito.any());
        Assertions.assertTrue(creator.setSecondPos(loc));
        Assertions.assertEquals(cuboid, creator.cuboid);
    }

    @Test
    void testConfirmPrice()
    {
        Mockito.doNothing().when(creator).abort();

        final Procedure procedure = Mockito.mock(Procedure.class);
        Mockito.doReturn(procedure).when(creator).getProcedure();

        Assertions.assertTrue(creator.confirmPrice(false));
        Mockito.verify(player).sendMessage(UnitTestUtil.toText("creator.base.error.creation_cancelled"));

        Mockito.doReturn(OptionalDouble.empty()).when(creator).getPrice();
        Mockito.doReturn(false).when(creator).buyStructure();

        Assertions.assertTrue(creator.confirmPrice(true));
        Mockito.verify(player)
               .sendMessage(UnitTestUtil.toText("creator.base.error.insufficient_funds StructureType 0"));

        double price = 123.41;
        Mockito.doReturn(OptionalDouble.of(price)).when(creator).getPrice();
        Mockito.doReturn(false).when(creator).buyStructure();
        Assertions.assertTrue(creator.confirmPrice(true));
        Mockito.verify(player).sendMessage(
            UnitTestUtil.toText(String.format("creator.base.error.insufficient_funds StructureType %.2f", price)));

        Mockito.doReturn(true).when(creator).buyStructure();
        Assertions.assertTrue(creator.confirmPrice(true));
        Mockito.verify(procedure).goToNextStep();
    }

    @Test
    void testSkipPrice()
    {
        Mockito.doReturn(OptionalDouble.empty()).when(creator).getPrice();
        Assertions.assertTrue(creator.skipConfirmPrice());

        Mockito.doReturn(OptionalDouble.of(1)).when(creator).getPrice();
        Assertions.assertFalse(creator.skipConfirmPrice());
    }

    @Test
    void testOpenDirectionStep()
    {
        Mockito.when(commandFactory.getSetOpenDirectionDelayed())
               .thenReturn(Mockito.mock(SetOpenDirectionDelayed.class));

        final StructureType structureType = Mockito.mock(StructureType.class);
        final Set<MovementDirection> validOpenDirections = EnumSet.of(MovementDirection.EAST, MovementDirection.WEST);
        Mockito.when(structureType.getValidOpenDirections()).thenReturn(validOpenDirections);

        Mockito.when(creator.getStructureType()).thenReturn(structureType);

        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.NONE));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.NORTH));
        Assertions.assertTrue(creator.completeSetOpenDirStep(MovementDirection.EAST));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.SOUTH));
        Assertions.assertTrue(creator.completeSetOpenDirStep(MovementDirection.WEST));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.CLOCKWISE));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.COUNTERCLOCKWISE));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.UP));
        Assertions.assertFalse(creator.completeSetOpenDirStep(MovementDirection.DOWN));
    }

    @Test
    void testGetPrice()
    {
        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(false);
        final Cuboid cuboid = new Cuboid(new Vector3Di(1, 2, 3), new Vector3Di(4, 5, 6));
        setField("cuboid", cuboid);
        Assertions.assertTrue(creator.getPrice().isEmpty());

        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(true);
        Mockito.when(economyManager.getPrice(Mockito.any(), Mockito.anyInt()))
               .thenAnswer(invocation -> OptionalDouble.of(invocation.getArgument(1, Integer.class).doubleValue()));

        final OptionalDouble price = creator.getPrice();
        Assertions.assertTrue(price.isPresent());
        Assertions.assertEquals(cuboid.getVolume(), price.getAsDouble());
    }

    @Test
    void testBuyStructure()
    {
        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(false);

        final Cuboid cuboid = new Cuboid(new Vector3Di(1, 2, 3), new Vector3Di(4, 5, 6));
        setField("cuboid", cuboid);
        Assertions.assertTrue(creator.buyStructure());

        final IWorld world = Mockito.mock(IWorld.class);
        setField("world", world);

        final StructureType StructureType = Mockito.mock(StructureType.class);
        Mockito.when(creator.getStructureType()).thenReturn(StructureType);

        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(true);
        creator.buyStructure();
        Mockito.verify(economyManager).buyStructure(player, world, StructureType, cuboid.getVolume());
    }

    @Test
    void testCompleteSetPowerBlockStep()
    {
        Mockito.doNothing().when(creator).abort();

        final IWorld world = UnitTestUtil.getWorld();

        final Vector3Di cuboidMin = new Vector3Di(10, 20, 30);
        final Vector3Di cuboidMax = new Vector3Di(40, 50, 60);
        final Cuboid cuboid = new Cuboid(cuboidMin, cuboidMax);

        final ILocation outsideCuboid = UnitTestUtil.getLocation(70, 80, 90, world);
        final ILocation insideCuboid = UnitTestUtil.getLocation(25, 35, 45, world);

        setField("cuboid", cuboid);
        setField("world", world);

        Assertions.assertFalse(creator.completeSetPowerBlockStep(UnitTestUtil.getLocation(0, 1, 2)));

        Mockito.doReturn(false).when(creator).playerHasAccessToLocation(Mockito.any());
        Assertions.assertFalse(creator.completeSetPowerBlockStep(outsideCuboid));

        Mockito.doReturn(true).when(creator).playerHasAccessToLocation(Mockito.any());
        Assertions.assertFalse(creator.completeSetPowerBlockStep(insideCuboid));

        Mockito.verify(player)
               .sendMessage(UnitTestUtil.toText("creator.base.error.powerblock_inside_structure StructureType"));

        final double distance = cuboid.getCenter().getDistance(outsideCuboid.getPosition());
        final int lowLimit = (int) (distance - 1);
        Mockito.when(limitsManager.getLimit(Mockito.any(), Mockito.any())).thenReturn(OptionalInt.of(lowLimit));

        Assertions.assertFalse(creator.completeSetPowerBlockStep(outsideCuboid));
        Mockito.verify(player).sendMessage(
            UnitTestUtil.toText(String.format("creator.base.error.powerblock_too_far StructureType %.2f %d",
                                              distance, lowLimit)));

        Mockito.when(limitsManager.getLimit(Mockito.any(), Mockito.any())).thenReturn(OptionalInt.of(lowLimit + 10));
        Assertions.assertTrue(creator.completeSetPowerBlockStep(outsideCuboid));
    }

    @Test
    void testCompleteSetRotationPointStep()
    {
        final IWorld world = UnitTestUtil.getWorld();

        final Vector3Di cuboidMin = new Vector3Di(10, 20, 30);
        final Vector3Di cuboidMax = new Vector3Di(40, 50, 60);
        final Cuboid cuboid = new Cuboid(cuboidMin, cuboidMax);

        setField("world", world);
        setField("cuboid", cuboid);

        // World mismatch, so not allowed
        Assertions.assertFalse(creator.completeSetRotationPointStep(UnitTestUtil.getLocation(1, 1, 1)));

        Mockito.doReturn(false).when(creator).playerHasAccessToLocation(Mockito.any());
        // Location not allowed
        Assertions.assertFalse(creator.completeSetRotationPointStep(UnitTestUtil.getLocation(1, 1, 1, world)));

        Mockito.doReturn(true).when(creator).playerHasAccessToLocation(Mockito.any());
        // Point too far away
        Assertions.assertFalse(creator.completeSetRotationPointStep(UnitTestUtil.getLocation(1, 1, 1, world)));
        Mockito.verify(player).sendMessage(UnitTestUtil.toText("creator.base.error.invalid_rotation_point"));

        Assertions.assertTrue(creator.completeSetRotationPointStep(UnitTestUtil.getLocation(11, 21, 31, world)));
    }

    private void setField(String fieldName, @Nullable Object obj)
    {
        try
        {
            final Field f = Creator.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(creator, obj);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}

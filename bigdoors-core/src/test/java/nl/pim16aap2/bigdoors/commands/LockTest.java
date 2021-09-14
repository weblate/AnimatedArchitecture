package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.factories.IBigDoorsEventFactory;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.events.IDoorPrepareLockChangeEvent;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.BasicPLogger;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.util.CompletableFutureHandler;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static nl.pim16aap2.bigdoors.commands.CommandTestingUtil.initCommandSenderPermissions;

class LockTest
{
    private DoorRetriever.AbstractRetriever doorRetriever;

    @Mock
    private AbstractDoor door;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPPlayer commandSender;

    @Mock
    private IDoorPrepareLockChangeEvent event;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Lock.IFactory factory;

    @Mock
    private IBigDoorsPlatform platform;

    @BeforeEach
    void init()
    {
        MockitoAnnotations.openMocks(this);

        initCommandSenderPermissions(commandSender, true, true);
        Mockito.when(door.isDoorOwner(Mockito.any(UUID.class))).thenReturn(true);
        Mockito.when(door.isDoorOwner(Mockito.any(IPPlayer.class))).thenReturn(true);
        doorRetriever = DoorRetriever.ofDoor(door);

        Mockito.when(door.syncData()).thenReturn(CompletableFuture.completedFuture(true));

        final IBigDoorsEventFactory eventFactory = Mockito.mock(IBigDoorsEventFactory.class);
        Mockito.when(eventFactory.createDoorPrepareLockChangeEvent(Mockito.any(), Mockito.anyBoolean(), Mockito.any()))
               .thenReturn(event);

        final IPLogger logger = new BasicPLogger();
        final CompletableFutureHandler handler = new CompletableFutureHandler(logger);
        final ILocalizer localizer = UnitTestUtil.initLocalizer();

        Mockito.when(factory.newLock(Mockito.any(ICommandSender.class),
                                     Mockito.any(DoorRetriever.AbstractRetriever.class),
                                     Mockito.anyBoolean()))
               .thenAnswer(invoc -> new Lock(invoc.getArgument(0, ICommandSender.class), logger, localizer,
                                             invoc.getArgument(1, DoorRetriever.AbstractRetriever.class),
                                             invoc.getArgument(2, Boolean.class), platform, handler, eventFactory));
    }

    @Test
    @SneakyThrows
    void test()
    {
        final boolean lock = true;
        Mockito.when(event.isCancelled()).thenReturn(true);

        Assertions.assertTrue(factory.newLock(commandSender, doorRetriever, lock).run().get(1, TimeUnit.SECONDS));
        Mockito.verify(door, Mockito.never()).setLocked(lock);

        Mockito.when(event.isCancelled()).thenReturn(false);
        Assertions.assertTrue(factory.newLock(commandSender, doorRetriever, lock).run().get(1, TimeUnit.SECONDS));
        Mockito.verify(door).setLocked(lock);
    }
}

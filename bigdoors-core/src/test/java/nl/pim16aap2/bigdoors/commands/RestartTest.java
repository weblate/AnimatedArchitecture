package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.BasicPLogger;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.util.CompletableFutureHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;


class RestartTest
{
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPServer commandSender;

    @Mock
    private IBigDoorsPlatform bigDoorsPlatform;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Restart.IFactory factory;

    @BeforeEach
    void beforeEach()
    {
        MockitoAnnotations.openMocks(this);

        final IPLogger logger = new BasicPLogger();
        final CompletableFutureHandler handler = new CompletableFutureHandler(logger);
        final ILocalizer localizer = UnitTestUtil.initLocalizer();

        Mockito.when(factory.newRestart(Mockito.any(ICommandSender.class)))
               .thenAnswer(invoc -> new Restart(invoc.getArgument(0, ICommandSender.class),
                                                logger, localizer, bigDoorsPlatform, handler));
    }

    @Test
    @SneakyThrows
    void test()
    {
        Assertions.assertTrue(factory.newRestart(commandSender).run().get(1, TimeUnit.SECONDS));
        Mockito.verify(bigDoorsPlatform).restart();
    }
}

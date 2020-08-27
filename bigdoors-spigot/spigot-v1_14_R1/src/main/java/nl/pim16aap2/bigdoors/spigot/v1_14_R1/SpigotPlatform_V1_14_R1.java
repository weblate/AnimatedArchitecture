package nl.pim16aap2.bigdoors.spigot.v1_14_R1;

import nl.pim16aap2.bigdoors.api.IBlockAnalyzer;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.factories.IFallingBlockFactory;
import nl.pim16aap2.bigdoors.api.factories.IPBlockDataFactory;
import nl.pim16aap2.bigdoors.spigot.util.api.BigDoorsSpigotAbstract;
import nl.pim16aap2.bigdoors.spigot.util.api.ISpigotPlatform;
import nl.pim16aap2.bigdoors.util.PLogger;
import org.jetbrains.annotations.NotNull;

public final class SpigotPlatform_V1_14_R1 implements ISpigotPlatform
{
    private static final String VERSION = "v1_14_R1";
    private static final SpigotPlatform_V1_14_R1 instance = new SpigotPlatform_V1_14_R1();

    private IFallingBlockFactory fallingBlockFactory;
    private IPBlockDataFactory pBlockDataFactory;
    private IBlockAnalyzer blockAnalyzer;
    private IGlowingBlockSpawner glowingBlockSpawner;

    private SpigotPlatform_V1_14_R1()
    {
    }

    @NotNull
    @Override
    public String getVersion()
    {
        return VERSION;
    }

    /**
     * Obtains the instance of this class.
     *
     * @return The instance of this class.
     */
    @NotNull
    public static SpigotPlatform_V1_14_R1 get()
    {
        return instance;
    }

    @Override
    public void init(final @NotNull BigDoorsSpigotAbstract plugin)
    {
        glowingBlockSpawner = new GlowingBlockSpawner_V1_14_R1(plugin, PLogger.get());
        fallingBlockFactory = new FallingBlockFactory_V1_14_R1();
        pBlockDataFactory = new PBlockDataFactorySpigot_V1_14_R1();
        blockAnalyzer = new BlockAnalyzer_V1_14_R1();
    }

    @NotNull
    @Override
    public IFallingBlockFactory getFallingBlockFactory()
    {
        return fallingBlockFactory;
    }

    @NotNull
    @Override
    public IPBlockDataFactory getPBlockDataFactory()
    {
        return pBlockDataFactory;
    }

    @NotNull
    @Override
    public IBlockAnalyzer getBlockAnalyzer()
    {
        return blockAnalyzer;
    }

    @NotNull
    @Override
    public IGlowingBlockSpawner getGlowingBlockSpawner()
    {
        return glowingBlockSpawner;
    }
}

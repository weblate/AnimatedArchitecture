package nl.pim16aap2.bigdoors.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.vector.Vector3DdConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PLocation implements IPLocation
{
    @Getter
    private final @NotNull IPWorld world;

    @Getter
    @Setter
    private double x;

    @Getter
    @Setter
    private double y;

    @Getter
    @Setter
    private double z;

    public PLocation(final @NotNull IPWorld world, final @NotNull Vector3DiConst position)
    {
        this(world, position.getX(), position.getY(), position.getZ());
    }

    public PLocation(final @NotNull IPWorld world, final @NotNull Vector3DdConst position)
    {
        this(world, position.getX(), position.getY(), position.getZ());
    }

    @Override
    public int getBlockX()
    {
        return (int) x;
    }

    @Override
    public int getBlockY()
    {
        return (int) y;
    }

    @Override
    public int getBlockZ()
    {
        return (int) z;
    }

    @Override
    public @NotNull IPLocation add(double x, double y, double z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public @NotNull IPLocation clone()
    {
        return new PLocation(getWorld(), getX(), getY(), getZ());
    }
}

package nl.pim16aap2.bigdoors.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.vector.Vector3DdConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;

@AllArgsConstructor
public class PLocation implements IPLocation
{
    @Getter
    private final @NonNull IPWorld world;

    @Getter
    @Setter
    private double x;

    @Getter
    @Setter
    private double y;

    @Getter
    @Setter
    private double z;

    public PLocation(final @NonNull IPWorld world, final @NonNull Vector3DiConst position)
    {
        this(world, position.getX(), position.getY(), position.getZ());
    }

    public PLocation(final @NonNull IPWorld world, final @NonNull Vector3DdConst position)
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
    public @NonNull IPLocation add(double x, double y, double z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public @NonNull IPLocation clone()
    {
        return new PLocation(getWorld(), getX(), getY(), getZ());
    }
}

package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.util.vector.Vector3DdConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a mutable position in a world.
 *
 * @author Pim
 */
public interface IPLocation extends IPLocationConst, Cloneable
{
    /**
     * Changes the x coordinate.
     *
     * @param newVal The new coordinate.
     */
    void setX(double newVal);

    /**
     * Changes the y coordinate.
     *
     * @param newVal The new coordinate.
     */
    void setY(double newVal);

    /**
     * Changes the z coordinate.
     *
     * @param newVal The new coordinate.
     */
    void setZ(double newVal);

    /**
     * Adds values to the coordinates of this location.
     *
     * @param x The value to add to the x coordinate.
     * @param y The value to add to the y coordinate.
     * @param z The value to add to the z coordinate.
     * @return This current IPLocation.
     */
    @NotNull IPLocation add(double x, double y, double z);

    /**
     * Adds values to the coordinates of this location.
     *
     * @param vector The vector to add to the coordinates.
     * @return This current IPLocation.
     */
    default @NotNull IPLocation add(@NotNull Vector3DiConst vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Adds values to the coordinates of this location.
     *
     * @param vector The vector to add to the coordinates.
     * @return This current IPLocation.
     */
    default @NotNull IPLocation add(@NotNull Vector3DdConst vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    @NotNull IPLocation clone();
}

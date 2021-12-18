package nl.pim16aap2.bigdoors.doors;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.util.FastFieldSetter;
import nl.pim16aap2.bigdoors.util.UnsafeGetter;
import nl.pim16aap2.util.SafeStringBuilder;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages the serialization aspects of the doors.
 *
 * @param <T>
 *     The type of door.
 * @author Pim
 */
@Flogger
public final class DoorSerializer<T extends AbstractDoor>
{
    private static final @Nullable Unsafe UNSAFE = UnsafeGetter.getUnsafe();

    private final @Nullable FastFieldSetter<AbstractDoor, DoorBase> fieldCopierDoorBase =
        getFieldCopierDoorBase(UNSAFE);

    @SuppressWarnings("rawtypes")
    private static final JsonAdapter<List> LIST_JSON_ADAPTER = new Moshi.Builder().build().adapter(List.class);

    /**
     * The list of serializable fields in the target class {@link #doorClass}.
     */
    private final List<Field> fields = new ArrayList<>();

    /**
     * The target class.
     */
    private final Class<T> doorClass;

    /**
     * The constructor in the {@link #doorClass} that takes exactly 1 argument of the type {@link DoorBase} if such a
     * constructor exists.
     */
    private final @Nullable Constructor<T> ctor;

    public DoorSerializer(Class<T> doorClass)
    {
        this.doorClass = doorClass;

        if (Modifier.isAbstract(doorClass.getModifiers()))
            throw new IllegalArgumentException("THe DoorSerializer only works for concrete classes!");

        @Nullable Constructor<T> ctorTmp = null;
        try
        {
            ctorTmp = doorClass.getDeclaredConstructor(DoorBase.class);
            ctorTmp.setAccessible(true);
        }
        catch (Exception e)
        {
            log.at(Level.FINER).withCause(e).log("Class %s does not have a DoorData ctor! Using Unsafe instead!",
                                                 getDoorTypeName());
        }
        ctor = ctorTmp;
        if (ctor == null && UNSAFE == null)
            throw new RuntimeException("Could not find CTOR for class " + getDoorTypeName() +
                                           " and Unsafe is unavailable! This type cannot be enabled!");

        log.at(Level.FINE).log("Using %s construction method for class %s.",
                               (ctor == null ? "Unsafe" : "Reflection"), getDoorTypeName());

        findAnnotatedFields();
    }

    private void findAnnotatedFields()
        throws UnsupportedOperationException
    {
        final List<Field> fieldList = new ArrayList<>();
        Class<?> clazz = doorClass;
        while (!clazz.equals(AbstractDoor.class))
        {
            fieldList.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        for (final Field field : fieldList)
            if (field.isAnnotationPresent(PersistentVariable.class))
            {
                field.setAccessible(true);
                fields.add(field);
            }
    }

    /**
     * Serializes the type-specific data of a door.
     *
     * @param door
     *     The door.
     * @return The serialized type-specific data.
     */
    public String serialize(AbstractDoor door)
        throws Exception
    {
        final ArrayList<Object> values = new ArrayList<>(fields.size());
        for (final Field field : fields)
            try
            {
                values.add(field.get(door));
            }
            catch (IllegalAccessException e)
            {
                throw new Exception(String.format("Failed to get value of field %s (type %s) for door type %s!",
                                                  field.getName(), field.getType().getName(), getDoorTypeName()), e);
            }
        return serialize(values);
    }

    private String serialize(ArrayList<Object> fieldValues)
    {
        return LIST_JSON_ADAPTER.toJson(fieldValues);
    }

    /**
     * Deserializes the serialized type-specific data of a door.
     * <p>
     * The doorBase and the deserialized data are then used to create an instance of the door type.
     *
     * @param doorBase
     *     The base door data.
     * @param data
     *     The serialized type-specific data.
     * @return The newly created instance.
     */
    public T deserialize(DoorBase doorBase, String data)
        throws Exception
    {
        @SuppressWarnings("unchecked") //
        final @Nullable List<Object> values = LIST_JSON_ADAPTER.fromJson(data);
        if (values == null)
            throw new IllegalArgumentException("Received null when trying to deserialize input: '" + data + "'");

        // All numerical values are returned as doubles, so use the fields list
        // to figure out which specific type to use.
        for (int idx = 0; idx < values.size(); ++idx)
        {
            final @Nullable Object value = values.get(idx);
            if (value instanceof Double doubleVal)
                values.set(idx, downCastDouble(idx, doubleVal));
        }

        return instantiate(doorBase, values);
    }

    private Object downCastDouble(int idx, Double doubleVal)
    {
        final @Nullable Field field = fields.size() >= idx ? fields.get(idx) : null;
        if (field == null ||
            (!Number.class.isAssignableFrom(field.getType()) && !field.getType().isPrimitive()))
        {
            log.at(Level.FINE).log("Could not store double val %s in field %s!", doubleVal, field);
            return doubleVal;
        }
        final Class<?> type = field.getType();
        if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type))
            return doubleVal.intValue();
        else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type))
            return doubleVal;
        else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type))
            return doubleVal.floatValue();
        else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type))
            return doubleVal.longValue();
        else if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type))
            return doubleVal.byteValue();
        else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type))
            return doubleVal.shortValue();
        return doubleVal;
    }

    T instantiate(DoorBase doorBase, List<Object> values)
        throws Exception
    {
        if (values.size() != fields.size())
            throw new IllegalStateException(String.format("Expected %d arguments but received %d for type %s",
                                                          fields.size(), values.size(), getDoorTypeName()));
        try
        {
            final @Nullable T door = instantiate(doorBase);
            if (door == null)
                throw new IllegalStateException("Failed to initialize door!");
            for (int idx = 0; idx < fields.size(); ++idx)
                fields.get(idx).set(door, values.get(idx));
            return door;
        }
        catch (Exception e)
        {
            throw new Exception("Failed to create new instance of type: " + getDoorTypeName(), e);
        }
    }

    /**
     * Attempts to create a new instance of {@link #doorClass} using the provided base data.
     * <p>
     * When {@link #ctor} is available, {@link #instantiateReflection(DoorBase, Constructor)} is used. If that is not
     * the case, {@link #instantiateUnsafe(DoorBase)} is used instead.
     *
     * @param doorBase
     *     The {@link DoorBase} to use for basic {@link AbstractDoor} initialization.
     * @return A new instance of {@link #doorClass} if one could be constructed.
     */
    private @Nullable T instantiate(DoorBase doorBase)
        throws IllegalAccessException, InstantiationException, InvocationTargetException
    {
        return ctor == null ? instantiateUnsafe(doorBase) : instantiateReflection(doorBase, ctor);
    }

    private T instantiateReflection(DoorBase doorBase, Constructor<T> ctor)
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        return ctor.newInstance(doorBase);
    }

    private @Nullable T instantiateUnsafe(DoorBase doorBase)
        throws InstantiationException
    {
        if (UNSAFE == null || fieldCopierDoorBase == null)
            return null;

        @SuppressWarnings("unchecked") //
        final T door = (T) UNSAFE.allocateInstance(doorClass);
        fieldCopierDoorBase.copy(door, doorBase);
        return door;
    }

    public String getDoorTypeName()
    {
        return doorClass.getName();
    }

    /**
     * Prints the persistent field names and values of a door.
     * <p>
     * 1 field per line.
     *
     * @param door
     *     The {@link AbstractDoor} whose {@link PersistentVariable}s to print.
     * @return A String containing the names and values of the persistent parameters of the provided door.
     */
    public String toString(AbstractDoor door)
    {
        if (!doorClass.isAssignableFrom(door.getClass()))
        {
            log.at(Level.SEVERE).withCause(new IllegalArgumentException(
                "Expected type " + getDoorTypeName() + " but received type " + door.getClass().getName())).log();
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (final Field field : fields)
        {
            String value;
            try
            {
                value = field.get(door).toString();
            }
            catch (IllegalAccessException e)
            {
                log.at(Level.SEVERE).withCause(e).log();
                value = "ERROR";
            }
            sb.append(field.getName()).append(": ").append(value).append('\n');
        }
        return sb.toString();
    }

    private String getConstructionModeName()
    {
        if (this.ctor == null && UNSAFE == null)
            return "No method available!";
        return this.ctor == null ? "Unsafe" : "Constructor";
    }

    @Override
    public String toString()
    {
        final SafeStringBuilder sb = new SafeStringBuilder("DoorSerializer: ")
            .append(getDoorTypeName()).append(", Construction Mode: ").append(getConstructionModeName())
            .append(", fields:\n");

        for (final Field field : fields)
            sb.append("* Type: ").append(field.getType().getName())
              .append(", name: \"").append(field.getName())
              .append("\"\n");
        return sb.toString();
    }

    private static @Nullable FastFieldSetter<AbstractDoor, DoorBase> getFieldCopierDoorBase(@Nullable Unsafe unsafe)
    {
        if (unsafe == null)
            return null;
        try
        {
            return FastFieldSetter.of(unsafe, DoorBase.class, AbstractDoor.class, "doorBase");
        }
        catch (Exception e)
        {
            log.at(Level.FINE).withCause(e).log("Failed to get FastFieldSetter for DoorBase of class: ");
            return null;
        }
    }
}

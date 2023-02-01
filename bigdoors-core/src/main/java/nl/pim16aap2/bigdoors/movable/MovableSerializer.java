package nl.pim16aap2.bigdoors.movable;

import com.google.common.flogger.StackSize;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.movable.serialization.DeserializationConstructor;
import nl.pim16aap2.bigdoors.movable.serialization.PersistentVariable;
import nl.pim16aap2.reflection.ReflectionBuilder;
import nl.pim16aap2.util.SafeStringBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the serialization aspects of the movables.
 * <p>
 * The {@link PersistentVariable} annotation is used on fields to determine which fields are serialized. If a name is
 * provided to the annotation, the object will be serialized using that name. If more than one unnamed field of the same
 * type is defined, the serializer will throw an exception on startup. Similarly, there can be no two fields with the
 * same name.
 * <p>
 * In the constructor, the {@link PersistentVariable} annotation can be used to specify the name of the object to
 * deserialize. If no name is provided, the object is matched using its type instead. Like with the variables, no
 * ambiguity in parameter types or names is allowed.
 * <p>
 * The {@link AbstractMovable.MovableBaseHolder} object is always provided and does not need to be handled in any
 * specific way.
 * <p>
 * When a value is missing during deserialization, null will be substituted in its place if it is not a primitive. If
 * the type is a primitive, an exception will be thrown.
 * <p>
 * For example:
 * <pre> {@code
 * public class MyMovable extends AbstractMovable
 * {
 *     @PersistentVariable("ambiguousInteger0")
 *     private int myInt0;
 *
 *     @PersistentVariable("ambiguousInteger1")
 *     private int myInt1;
 *
 *     @PersistentVariable
 *     private String nonAmbiguous
 *
 *     @DeserializationConstructor
 *     public MyMovable(
 *         AbstractMovable.Holder base,
 *         @PersistentVariable("ambiguousInteger0") int0,
 *         @PersistentVariable("ambiguousInteger1") int1,
 *         String str)
 *     {
 *         super(base);
 *         this.myInt0 = int0;
 *         this.myInt1 = int1;
 *         this.nonAmbiguous = str;
 *     }
 *     ...
 * }}</pre>
 *
 * @param <T>
 *     The type of movable.
 * @author Pim
 */
@Flogger
public final class MovableSerializer<T extends AbstractMovable>
{
    /**
     * The target class.
     */
    private final Class<T> movableClass;
    /**
     * The list of serializable fields in the target class {@link #movableClass} that are annotated with
     * {@link PersistentVariable}.
     */
    private final List<AnnotatedField> fields;

    /**
     * The constructor in the {@link #movableClass} that takes exactly 1 argument of the type {@link MovableBase}.
     */
    private final Constructor<T> ctor;

    /**
     * The parameters of the {@link #ctor}.
     */
    private final List<ConstructorParameter> parameters;

    public MovableSerializer(Class<T> movableClass)
    {
        this.movableClass = movableClass;

        if (Modifier.isAbstract(movableClass.getModifiers()))
            throw new IllegalArgumentException("THe MovableSerializer only works for concrete classes!");

        fields = findAnnotatedFields(movableClass);
        ctor = getConstructor(movableClass);
        parameters = getConstructorParameters(ctor);
    }

    private static <T> Constructor<T> getConstructor(Class<T> movableClass)
    {
        @SuppressWarnings("unchecked") //
        final Constructor<T> ctor = (Constructor<T>) ReflectionBuilder
            .findConstructor(movableClass)
            .withAnnotations(DeserializationConstructor.class)
            .setAccessible().get();
        return ctor;
    }

    private static List<ConstructorParameter> getConstructorParameters(Constructor<?> ctor)
    {
        final List<ConstructorParameter> ret = new ArrayList<>(ctor.getParameterCount());
        boolean foundBase = false;
        final Set<Class<?>> unnamedParameters = new HashSet<>();
        final Set<String> namedParameters = new HashSet<>();

        for (final Parameter parameter : ctor.getParameters())
        {
            if (parameter.getType() == AbstractMovable.MovableBaseHolder.class && !foundBase)
            {
                foundBase = true;
                ret.add(new ConstructorParameter("", AbstractMovable.MovableBaseHolder.class));
                continue;
            }

            final ConstructorParameter constructorParameter = ConstructorParameter.of(parameter);
            if (constructorParameter.name == null && !unnamedParameters.add(parameter.getType()))
                throw new IllegalArgumentException(
                    "Found ambiguous parameter type " + parameter + " in constructor: " + ctor);
            if (constructorParameter.name != null && !namedParameters.add(constructorParameter.name))
                throw new IllegalArgumentException(
                    "Found ambiguous parameter name " + parameter + " in constructor: " + ctor);

            ret.add(constructorParameter);
        }

        if (!foundBase)
            throw new IllegalArgumentException(
                "Could not found parameter MovableBaseHolder in deserialization constructor: " + ctor);

        return ret;
    }

    private static List<AnnotatedField> findAnnotatedFields(Class<? extends AbstractMovable> movableClass)
        throws UnsupportedOperationException
    {
        final List<AnnotatedField> fields = ReflectionBuilder
            .findField().inClass(movableClass)
            .withAnnotations(PersistentVariable.class)
            .checkSuperClasses()
            .setAccessible()
            .get().stream()
            .map(AnnotatedField::of)
            .toList();

        final Set<Class<?>> unnamedFields = new HashSet<>();
        final Set<String> namedFields = new HashSet<>();
        for (final AnnotatedField field : fields)
        {
            if (field.annotatedName == null && !unnamedFields.add(field.field.getType()))
                throw new IllegalArgumentException(
                    "Found ambiguous field type " + field + " in class: " + movableClass.getName());
            if (field.annotatedName != null && !namedFields.add(field.annotatedName))
                throw new IllegalArgumentException(
                    "Found ambiguous field name " + field + " in class: " + movableClass.getName());
        }

        return fields;
    }

    /**
     * Serializes the type-specific data of a movable.
     *
     * @param movable
     *     The movable.
     * @return The serialized type-specific data.
     */
    public byte[] serialize(AbstractMovable movable)
        throws Exception
    {
        final LinkedHashMap<String, Object> values = new LinkedHashMap<>(fields.size());
        for (final AnnotatedField field : fields)
            try
            {
                values.put(field.finalName, field.field.get(movable));
            }
            catch (IllegalAccessException e)
            {
                throw new Exception(String.format("Failed to get value of field %s (type %s) for movable type %s!",
                                                  field.fieldName(), field.typeName(), getMovableTypeName()), e);
            }
        return toByteArray(values);
    }

    /**
     * Deserializes the serialized type-specific data of a movable.
     * <p>
     * The movable and the deserialized data are then used to create an instance of the movable type.
     *
     * @param registry
     *     The registry to use for any potential registration.
     * @param movable
     *     The base movable data.
     * @param data
     *     The serialized type-specific data.
     * @return The newly created instance.
     */
    public T deserialize(MovableRegistry registry, AbstractMovable.MovableBaseHolder movable, byte[] data)
    {
        //noinspection unchecked
        return (T) registry.computeIfAbsent(movable.get().getUid(), () -> deserialize(movable, data));
    }

    @VisibleForTesting
    T deserialize(AbstractMovable.MovableBaseHolder movable, byte[] data)
    {
        @Nullable Map<String, Object> dataAsMap = null;
        try
        {
            dataAsMap = fromByteArray(data);
            return instantiate(movable, dataAsMap);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to deserialize movable " + movable + "\nWith Data: " + dataAsMap, e);
        }
    }

    private static byte[] toByteArray(Serializable serializable)
        throws Exception
    {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream))
        {
            objectOutputStream.writeObject(serializable);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static Map<String, Object> fromByteArray(byte[] arr)
        throws Exception
    {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(arr)))
        {
            final Object obj = objectInputStream.readObject();
            if (!(obj instanceof LinkedHashMap))
                throw new IllegalStateException(
                    "Unexpected deserialization type! Expected ArrayList, but got " + obj.getClass().getName());

            //noinspection unchecked
            return (Map<String, Object>) obj;
        }
    }

    @VisibleForTesting
    T instantiate(AbstractMovable.MovableBaseHolder movableBase, Map<String, Object> values)
        throws Exception
    {
        if (values.size() != fields.size())
            log.atWarning().log("Expected %d arguments but received %d for type %s",
                                fields.size(), values.size(), getMovableTypeName());
        @Nullable Object @Nullable [] deserializedParameters = null;
        try
        {
            deserializedParameters = deserializeParameters(movableBase, values);
            return ctor.newInstance(deserializedParameters);
        }
        catch (Exception t)
        {
            throw new Exception(
                "Failed to create new instance of type: " + getMovableTypeName() + ", with parameters: " +
                    Arrays.toString(deserializedParameters), t);
        }
    }

    private Object[] deserializeParameters(AbstractMovable.MovableBaseHolder base, Map<String, Object> values)
    {
        final Map<Class<?>, Object> classes = new HashMap<>(values.size());
        for (final var entry : values.entrySet())
            classes.put(entry.getValue().getClass(), entry.getValue());

        final Object[] ret = new Object[this.parameters.size()];
        int idx = -1;
        for (final ConstructorParameter param : this.parameters)
        {
            ++idx;

            try
            {
                final @Nullable Object data;
                if (param.type == AbstractMovable.MovableBaseHolder.class)
                    data = base;
                else if (param.name != null)
                    data = getDeserializedObject(base, values, param.name);
                else
                    data = getDeserializedObject(base, classes, param.type);

                if (param.isRemappedFromPrimitive && data == null)
                    throw new IllegalArgumentException(
                        "Received null parameter that cannot accept null values: " + param);

                //noinspection DataFlowIssue
                ret[idx] = data;
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(
                    String.format("Could not set index %d in constructor from key %s from values %s.",
                                  idx, (param.name == null ? param.type : param.name),
                                  (param.name == null ? classes : values)), e);
            }
        }
        return ret;
    }

    private static @Nullable <T> Object getDeserializedObject(
        AbstractMovable.MovableBaseHolder base, Map<T, Object> map, T key)
    {
        final @Nullable Object ret = map.get(key);
        if (ret != null)
            return ret;

        if (!map.containsKey(key))
            log.atSevere().log("No value found for key '%s' for movable: %s", key, base);

        return null;
    }

    public String getMovableTypeName()
    {
        return movableClass.getName();
    }

    /**
     * Prints the persistent field names and values of a movable.
     * <p>
     * 1 field per line.
     *
     * @param movable
     *     The {@link AbstractMovable} whose {@link PersistentVariable}s to print.
     * @return A String containing the names and values of the persistent parameters of the provided movable.
     */
    public String toString(AbstractMovable movable)
    {
        if (!movableClass.isAssignableFrom(movable.getClass()))
        {
            log.atSevere().withStackTrace(StackSize.FULL)
               .log("Expected type '%s' but received type '%s'!", getMovableTypeName(), movable.getClass().getName());
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (final AnnotatedField field : fields)
        {
            String value;
            try
            {
                value = field.field.get(movable).toString();
            }
            catch (IllegalAccessException e)
            {
                log.atSevere().withCause(e).log();
                value = "ERROR";
            }
            sb.append(field.field.getName()).append(": ").append(value).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        final SafeStringBuilder sb = new SafeStringBuilder("MovableSerializer: ")
            .append(getMovableTypeName())
            .append(", fields:\n");

        for (final AnnotatedField field : fields)
            sb.append("* Type: ").append(field.typeName())
              .append(", name: \"").append(field.finalName)
              .append("\" (\"").append(field.annotatedName == null ? "unspecified" : field.annotatedName)
              .append("\")\n");
        return sb.toString();
    }

    private record AnnotatedField(Field field, @Nullable String annotatedName, String fieldName, String finalName)
    {
        public static AnnotatedField of(Field field)
        {
            verifyFieldType(field);
            final String annotatedName = field.getAnnotation(PersistentVariable.class).value();
            final String fieldName = field.getName();
            final String finalName = annotatedName.isBlank() ? fieldName : annotatedName;
            final @Nullable String finalAnnotatedName = annotatedName.isBlank() ? null : annotatedName;
            return new AnnotatedField(field, finalAnnotatedName, fieldName, finalName);
        }

        private static void verifyFieldType(Field field)
        {
            if (!field.getType().isPrimitive() && !Serializable.class.isAssignableFrom(field.getType()))
                throw new UnsupportedOperationException(
                    String.format("Type %s of field %s is not serializable!",
                                  field.getType().getName(), field.getName()));
        }

        public String typeName()
        {
            return field.getType().getName();
        }
    }

    private record ConstructorParameter(@Nullable String name, Class<?> type, boolean isRemappedFromPrimitive)
    {
        public ConstructorParameter(@Nullable String name, Class<?> type)
        {
            this(name, remapPrimitives(type), type.isPrimitive());
        }

        public static ConstructorParameter of(Parameter parameter)
        {
            return new ConstructorParameter(getName(parameter), parameter.getType());
        }

        private static @Nullable String getName(Parameter parameter)
        {
            final @Nullable var annotation = parameter.getAnnotation(PersistentVariable.class);
            //noinspection ConstantValue
            if (annotation == null)
                return null;
            return annotation.value().isBlank() ? null : annotation.value();
        }

        private static Class<?> remapPrimitives(Class<?> clz)
        {
            if (!clz.isPrimitive())
                return clz;
            if (clz == boolean.class)
                return Boolean.class;
            if (clz == char.class)
                return Character.class;
            if (clz == byte.class)
                return Byte.class;
            if (clz == short.class)
                return Short.class;
            if (clz == int.class)
                return Integer.class;
            if (clz == long.class)
                return Long.class;
            if (clz == float.class)
                return Float.class;
            if (clz == double.class)
                return Double.class;
            throw new IllegalStateException("Processing unexpected class type: " + clz);
        }
    }
}

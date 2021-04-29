package nl.pim16aap2.bigdoors.spigot.loader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.StandardCopyOption;

/**
 * Represents the bootstrap loader for Spigot.
 *
 * @author Pim
 */
@SuppressWarnings("unused")
public class BigDoorsSpigotPlugin extends JavaPlugin
{
    private boolean isEnabled = false;
    private static final @NonNull String JAR_JAR_NAME = "bigdoors-spigot-core.jar";
    private final @NonNull File jarPath;

    private final @NonNull PClassLoader classLoader;
    private @Nullable IBigDoorsPlatform bigDoorsSpigot;

    public BigDoorsSpigotPlugin()
    {
        jarPath = new File(getDataFolder(), JAR_JAR_NAME);
        classLoader = new PClassLoader(super.getClassLoader());

        try
        {
            if (!extractJar())
                throw new IllegalStateException("Jar could not be extracted!");

            classLoader.addURL(jarPath.toURI().toURL());

            Class<?> bigDoorsSpigotClass = classLoader.loadClass("nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot");
            bigDoorsSpigot = (IBigDoorsPlatform) bigDoorsSpigotClass.getDeclaredConstructor(JavaPlugin.class)
                                                                    .newInstance(this);

            isEnabled = true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        if (!isEnabled)
            getLogger().severe("Failed to initialize the plugin!");
    }

    private boolean extractJar()
    {
        try (val inputStream = getClass().getClassLoader().getResourceAsStream(JAR_JAR_NAME))
        {
            if (inputStream == null)
            {
                getLogger().severe("Failed to extract the BigDoors Spigot jar!");
                return false;
            }
            java.nio.file.Files.copy(inputStream, jarPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    @SneakyThrows
    public void onEnable()
    {
        if (!isEnabled || bigDoorsSpigot == null)
        {
            getLogger().severe("Plugin failed to initialize!");
            return;
        }
        bigDoorsSpigot.onEnable();
    }

    @Override
    @SneakyThrows
    public void onDisable()
    {
        if (!isEnabled || bigDoorsSpigot == null)
            return;
        bigDoorsSpigot.onDisable();
    }
}

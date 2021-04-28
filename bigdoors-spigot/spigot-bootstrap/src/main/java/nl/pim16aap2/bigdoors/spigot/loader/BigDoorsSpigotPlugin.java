package nl.pim16aap2.bigdoors.spigot.loader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.StandardCopyOption;

/**
 * Represents the bootstrap loader for Spigot.
 *
 * @author Pim
 */
public class BigDoorsSpigotPlugin extends JavaPlugin
{
    private boolean isEnabled = false;
    private final @NonNull String jarJarName = "bigdoors-spigot-core.jar";
    private final @NonNull File jarPath;

    private final @NonNull PClassLoader classLoader;
    private @Nullable Object bigDoorsSpigot;

    private @Nullable Method onEnable;
    private @Nullable Method onDisable;
    private @Nullable Method shutdown;
    private @Nullable Method restart;

    public BigDoorsSpigotPlugin()
    {
        jarPath = new File(getDataFolder(), jarJarName);
        classLoader = new PClassLoader(super.getClassLoader());

        try
        {
            if (!extractJar())
                throw new IllegalStateException("Jar could not be extracted!");

            classLoader.addURL(jarPath.toURI().toURL());

            Class<?> bigDoorsSpigotClass = classLoader.loadClass("nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot");
            bigDoorsSpigot = bigDoorsSpigotClass.getDeclaredConstructor(JavaPlugin.class).newInstance(this);

            onEnable = bigDoorsSpigotClass.getDeclaredMethod("onEnable");
            onDisable = bigDoorsSpigotClass.getDeclaredMethod("onDisable");
            shutdown = bigDoorsSpigotClass.getDeclaredMethod("shutdown");
            restart = bigDoorsSpigotClass.getDeclaredMethod("restart");
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
        try (val inputStream = getClass().getClassLoader().getResourceAsStream(jarJarName))
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
        if (!isEnabled || bigDoorsSpigot == null || onEnable == null)
        {
            getLogger().severe("Plugin failed to initialize!");
            return;
        }
        onEnable.invoke(bigDoorsSpigot);
    }

    @Override
    @SneakyThrows
    public void onDisable()
    {
        if (!isEnabled || bigDoorsSpigot == null || onDisable == null)
            return;
        onDisable.invoke(bigDoorsSpigot);
    }
}

package nl.pim16aap2.bigdoors.spigot.loader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import nl.pim16aap2.bigdoors.spigot.util.api.AbstractBigDoorsSpigotLoader;
import nl.pim16aap2.bigdoors.spigot.util.api.BigDoorsSpigotAbstract;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO: Keep track of the plugin's state while initializing/enabling it.
//       If something went wrong (e.g. incorrect MC version, database version mismatch),
//       just close the ClassLoader to fully unload the plugin and maybe register a backup
//       command handler to intercept everything and report some error messages.

@SuppressWarnings("unused")
public class BigDoorsSpigotLoader extends AbstractBigDoorsSpigotLoader
{
    private boolean isEnabled = false;
    private static final @NonNull String JAR_JAR_NAME = "bigdoors-spigot-core.jar";
    private final @NonNull File jarPath;

    private @Nullable PClassLoader classLoader;

    private @Nullable BigDoorsSpigotAbstract plugin;

    private final @NonNull Set<JavaPlugin> addons = new CopyOnWriteArraySet<>();

    public BigDoorsSpigotLoader()
    {
        jarPath = new File(getDataFolder(), JAR_JAR_NAME);
        init();
    }

    private void init()
    {
        classLoader = new PClassLoader(super.getClassLoader());

        try
        {
            if (!extractJar())
                throw new IllegalStateException("Jar could not be extracted!");

            classLoader.addURL(jarPath.toURI().toURL());

            Class<?> bigDoorsSpigotClass = classLoader.loadClass("nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot");
            plugin = (BigDoorsSpigotAbstract) bigDoorsSpigotClass
                .getDeclaredConstructor(AbstractBigDoorsSpigotLoader.class)
                .newInstance(this);

            isEnabled = true;
        }
        catch (Exception exception)
        {
            closeClassLoader();
            exception.printStackTrace();
        }
        if (!isEnabled)
            getLogger().severe("Failed to initialize the plugin!");
    }

    private void closeClassLoader()
    {
        if (classLoader == null)
            return;

        try
        {
            classLoader.close();
            classLoader = null;
            plugin = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public @NonNull Set<JavaPlugin> getRegisteredAddons()
    {
        return Collections.unmodifiableSet(addons);
    }

    @Override
    public @NonNull Optional<BigDoorsSpigotAbstract> getBigDoorsAPI(final @NonNull JavaPlugin caller)
    {
        if (caller == this)
            throw new IllegalArgumentException(
                "Caller plugin is the same instance as the requested plugin! Please provide the instance of the plugin that actually requested the BigDoors API instead!");

        addons.add(caller);
        return Optional.ofNullable(plugin);
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
        if (!isEnabled || plugin == null)
        {
            getLogger().severe("Plugin failed to initialize!");
            return;
        }
        plugin.onEnable();
    }

    @Override
    @SneakyThrows
    public void onDisable()
    {
        if (!isEnabled || plugin == null)
            return;
        plugin.onDisable();
    }
}

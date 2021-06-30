package nl.pim16aap2.bigdoors.spigot.loader;


import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Represents an {@link URLClassLoader} with its {@link URLClassLoader#addURL(URL)} method exposed.
 *
 * @author Pim
 */
class PClassLoader extends URLClassLoader
{
    public PClassLoader(final @NotNull URL[] urls, final @NotNull ClassLoader parent)
    {
        super(urls, parent);
    }

    public PClassLoader(final @NotNull ClassLoader parent)
    {
        super(new URL[]{}, parent);
    }

    @Override
    protected void addURL(final @NotNull URL url)
    {
        super.addURL(url);
    }
}

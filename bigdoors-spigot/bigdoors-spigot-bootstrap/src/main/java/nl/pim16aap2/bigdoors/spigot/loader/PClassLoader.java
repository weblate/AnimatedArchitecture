package nl.pim16aap2.bigdoors.spigot.loader;


import lombok.NonNull;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Represents an {@link URLClassLoader} with its {@link URLClassLoader#addURL(URL)} method exposed.
 *
 * @author Pim
 */
class PClassLoader extends URLClassLoader
{
    public PClassLoader(final @NonNull URL[] urls, final @NonNull ClassLoader parent)
    {
        super(urls, parent);
    }

    public PClassLoader(final @NonNull ClassLoader parent)
    {
        super(new URL[]{}, parent);
    }

    @Override
    protected void addURL(final @NonNull URL url)
    {
        super.addURL(url);
    }
}

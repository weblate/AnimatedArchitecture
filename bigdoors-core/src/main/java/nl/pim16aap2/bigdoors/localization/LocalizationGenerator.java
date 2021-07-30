package nl.pim16aap2.bigdoors.localization;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import lombok.val;
import nl.pim16aap2.bigdoors.BigDoors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a class that can generate a localization file from multiple sources.
 * <p>
 * All sources are merged and written to a new set of files in the specified output directory with the specified base
 * name.
 *
 * @author Pim
 */
public class LocalizationGenerator
{
    private final @NotNull Object lck = new Object();

    private final @NotNull Path outputDirectory;
    private final @NotNull String outputBaseName;

    /**
     * @param outputDirectory The output directory to write all the combined localizations into.
     * @param outputBaseName  The base name of the properties files in the output directory.
     */
    public LocalizationGenerator(@NotNull Path outputDirectory, @NotNull String outputBaseName)
    {
        this.outputDirectory = outputDirectory;
        this.outputBaseName = outputBaseName;
    }

    /**
     * See {@link #addResources(String, Path)}.
     *
     * @param localizer The {@link Localizer} to shut down before (re)generating the localization file(s). This ensures
     *                  the file isn't locked when running on *shudders* Windows.
     *                  <p>
     *                  After (re)generation, the localizer will be re-initialized.
     * @return The current {@link LocalizationGenerator} instance.
     */
    public @NotNull LocalizationGenerator addResources(@NotNull Localizer localizer, @NotNull String baseName,
                                                       @NotNull Path directory)
    {
        synchronized (lck)
        {
            localizer.shutdown();
            addResources(baseName, directory);
            localizer.restart();
            return this;
        }
    }

    /**
     * Adds a new set of resources to the current localization set.
     * <p>
     * Only files of the format "directory/basename[_locale].properties" are included. "[_locale]" is optional here.
     *
     * @param baseName  The base name of the properties files.
     * @param directory The directory of the properties files.
     * @return The current {@link LocalizationGenerator} instance.
     */
    public @NotNull LocalizationGenerator addResources(@NotNull String baseName, @NotNull Path directory)
    {
        synchronized (lck)
        {
            try
            {
                val localeFiles = getLocaleFilesInDirectory(baseName, directory);
                for (val localeFile : localeFiles)
                    mergeWithExistingLocaleFile(localeFile);
            }
            catch (Exception e)
            {
                BigDoors.get().getPLogger().logThrowable(e, "Failed to add resources from directory \"" +
                    directory + "\" with base name: \"" + baseName + "\"");
            }
            return this;
        }
    }

    /**
     * Appends new keys from a locale file into the existing locale file.
     * <p>
     * The existing locale file is derived from {@link LocaleFile#path()} of the input locale file, {@link
     * #outputDirectory}, and {@link #outputBaseName}.
     * <p>
     * If the output file does not exist yet, a new file will be created.
     *
     * @param localeFile The locale file whose files to copy into the existing file.
     * @throws IOException When an I/O error occurred.
     */
    @GuardedBy("lck")
    void mergeWithExistingLocaleFile(@NotNull LocaleFile localeFile)
        throws IOException
    {
        val existingLocaleFile = getOutputLocaleFile(localeFile.locale());
        ensureFileExists(existingLocaleFile);
        val existing = readFile(existingLocaleFile);
        val newlines = readFile(localeFile.path());
        val appendable = getAppendable(existing, newlines);
        appendToFile(existingLocaleFile, appendable);
    }

    /**
     * Ensures that a file exists.
     * <p>
     * If the file does not already exists, it will be created.
     *
     * @param file The file whose existence to ensure.
     * @throws IOException When the file could not be created.
     */
    static void ensureFileExists(@NotNull Path file)
        throws IOException
    {
        if (Files.isRegularFile(file))
            return;
        Files.createDirectories(file.getParent());
        Files.createFile(file);
    }

    /**
     * Appends a list of strings to a file.
     * <p>
     * Every entry in the list will be printed on its own line.
     *
     * @param path   The path of the file.
     * @param append The list of Strings (lines) to append to the file.
     */
    static void appendToFile(@NotNull Path path, @NotNull List<String> append)
    {
        if (append.isEmpty())
            return;

        val sb = new StringBuilder();
        append.forEach(line -> sb.append(line).append("\n"));
        try
        {
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to write localization file: " + path, e);
        }
    }

    /**
     * Retrieves the list of Strings from a list of lines that can be appended to the list of existing Strings.
     * <p>
     * A line can be appended when its key does not appear in the list of existing Strings.
     *
     * @param existing The base list of lines.
     * @param newLines The set of new lines to try to append to
     * @return A list with all lines from the existing set with any lines from the newLines set added to it where the
     * key did not exist in the existing list yet.
     */
    static @NotNull List<String> getAppendable(@NotNull List<String> existing, @NotNull List<String> newLines)
    {
        if (existing.isEmpty())
            return newLines;

        val keys = getKeySet(existing);
        val merged = new ArrayList<String>(newLines.size());

        for (val line : newLines)
        {
            val key = getKeyFromLine(line);
            if (key == null || keys.contains(key))
                continue;
            merged.add(line);
        }

        merged.trimToSize();
        return merged;
    }

    /**
     * Gets a set containing all the keys in a list of string.
     *
     * @param lines The lines containing "key=value" mappings.
     * @return A set with all the keys used in the lines.
     */
    static @NotNull Set<String> getKeySet(@NotNull List<String> lines)
    {
        final Set<String> ret = new HashSet<>(lines.size());
        for (val line : lines)
        {
            val key = getKeyFromLine(line);
            if (key != null)
                ret.add(key);
        }
        return ret;
    }

    /**
     * Retrieves the key from a String with the format "key=value".
     *
     * @param line A string containing a key/value pair.
     * @return The key as used in the line.
     */
    static @Nullable String getKeyFromLine(@NotNull String line)
    {
        val parts = line.split("=", 2);
        return parts.length == 2 ? parts[0] : null;
    }

    /**
     * Retrieves the path of the output locale file.
     * <p>
     * The path is derived from {@link LocaleFile#path()} of the input locale file, {@link #outputDirectory}, and {@link
     * #outputBaseName}.
     *
     * @param locale The locale used to derive the path of the output file.
     * @return The path of the output file.
     */
    @NotNull Path getOutputLocaleFile(@NotNull String locale)
    {
        val fileName = String.format("%s%s.properties", outputBaseName, locale.length() == 0 ? "" : ("_" + locale));
        return outputDirectory.resolve(fileName);
    }

    /**
     * Reads all the lines from a file.
     *
     * @param file A path to a file.
     * @return A list of Strings where every string represents a single line in the provided file. If the input file is
     * not a (valid) file, an empty list is returned.
     */
    static @NotNull List<String> readFile(@NotNull Path file)
    {
        if (!Files.isRegularFile(file))
            return Collections.emptyList();
        final ArrayList<String> tmp = new ArrayList<>();

        try (val bufferedReader = Files.newBufferedReader(file))
        {
            for (String line; (line = bufferedReader.readLine()) != null; )
            {
                if (line.length() == 0)
                    continue;
                if (line.charAt(0) == '#')
                    continue;
                tmp.add(line);
            }
        }
        catch (IOException e)
        {
            BigDoors.get().getPLogger().logThrowable(e, "Failed to read localization file: " + file);
            return Collections.emptyList();
        }
        return tmp;
    }

    /**
     * Retrieves all the {@link LocaleFile}s in a directory.
     *
     * @param baseName  The base name of the locale files.
     * @param directory The directory to search in.
     * @return A list of {@link LocaleFile}s found in the directory.
     */
    static @NotNull List<LocaleFile> getLocaleFilesInDirectory(@NotNull String baseName, @NotNull Path directory)
    {
        try (val stream = Files.list(directory))
        {
            return getLocaleFiles(baseName,
                                  stream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toList()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of {@link LocaleFile}s from a base name and a list of files.
     *
     * @param baseName The base name of the translation files.
     * @param files    The list of files to look at.
     * @return A list of {@link LocaleFile}s that includes all files that met the correct naming format of
     * "directory/basename[_locale].properties".
     */
    static @NotNull List<LocaleFile> getLocaleFiles(@NotNull String baseName, @NotNull List<Path> files)
    {
        final @NotNull ArrayList<LocaleFile> ret = new ArrayList<>(files.size());
        for (val file : files)
        {
            @Nullable val localeFile = parseLocaleFile(baseName, file.getFileName().toString());
            if (localeFile != null)
                ret.add(new LocaleFile(file, localeFile));
        }
        ret.trimToSize();
        return ret;
    }

    /**
     * Retrieves the locale from a locale file with the format "basename[_locale].properties".
     *
     * @param baseName The base name of the translation files.
     * @param fileName The name of the location file. This should only include the name of the file itself and not its
     *                 parent directories.
     * @return The locale as used in the filename.
     */
    static @Nullable String parseLocaleFile(@NotNull String baseName, @NotNull String fileName)
    {
        if (!fileName.startsWith(baseName) || !fileName.endsWith(".properties"))
            return null;

        String locale = fileName.replace(baseName, "").replace(".properties", "");
        if (locale.length() > 0)
        {
            if (locale.charAt(0) != '_')
                return null;
            locale = locale.substring(1);
        }
        return locale;
    }

    /**
     * Represents a translation file for a specific {@link Locale}.
     *
     * @param path   The path of the file.
     * @param locale The {@link Locale} this file represents.
     */
    record LocaleFile(@NotNull Path path, @NotNull String locale) {}
}

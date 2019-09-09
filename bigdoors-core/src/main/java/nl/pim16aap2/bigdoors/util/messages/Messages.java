package nl.pim16aap2.bigdoors.util.messages;

import nl.pim16aap2.bigdoors.api.IRestartableHolder;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.Restartable;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class that loads key/value pairs used for translations.
 *
 * @author Pim
 */
public final class Messages extends Restartable
{
    /**
     * The name of the default language file.
     */
    private static final String DEFAULTFILENAME = "en_US.txt";
    private final PLogger plogger;

    private static final Pattern matchDots = Pattern.compile("\\.");
    private static final Pattern matchNewLines = Pattern.compile("\\\\n");
    private static final Pattern matchColorCodes = Pattern.compile("&((?i)[0-9a-fk-or])");

    /**
     * The directory of the language file.
     */
    private final File fileDir;

    /**
     * The map of all messages.
     * <p>
     * Key: The {@link Message} enum entry.
     * <p>
     * Value: The translated message.
     */
    private Map<Message, String> messageMap = new EnumMap<>(Message.class);

    /**
     * The selected language file.
     */
    private File textFile;

    /**
     * Constructs for Messages object.
     *
     * @param holder   The {@link IRestartableHolder} that manages this object.
     * @param fileDir  The directory the messages file(s) will be in.
     * @param fileName The name of the file that will be loaded, if it exists. Extension excluded.
     * @param plogger  The {@link PLogger} object that will be used for logging.
     */
    public Messages(final @NotNull IRestartableHolder holder, final @NotNull File fileDir,
                    final @NotNull String fileName, final @NotNull PLogger plogger)
    {
        super(holder);
        this.plogger = plogger;
        this.fileDir = fileDir;

        if (!fileDir.exists())
            if (!fileDir.mkdirs())
            {
                plogger.logException(new IOException("Failed to create folder: \"" + fileDir.toString() + "\""));
                return;
            }

        textFile = new File(fileDir, fileName + ".txt");
        if (!textFile.exists())
        {
            plogger.warn("Failed to load language file: \"" + textFile
                             + "\": File not found! Using default file instead!");
            textFile = new File(fileDir, DEFAULTFILENAME);
        }
        writeDefaultFile();
        readFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restart()
    {
        shutdown();
        readFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown()
    {
        messageMap.clear();
    }

    /**
     * Copies the default language file to the default location. The default location is the directory of the language
     * specified in the config + the {@link #DEFAULTFILENAME}.
     */
    private void writeDefaultFile()
    {
        File defaultFile = new File(fileDir, DEFAULTFILENAME);
        if (defaultFile.exists() && !defaultFile.setWritable(true))
            plogger.severe("Failed to make file \"" + defaultFile + "\" writable!");

        // Load the DEFAULTFILENAME from the resources folder.
        InputStream in = null;
        try
        {
            URL url = getClass().getClassLoader().getResource(DEFAULTFILENAME);
            if (url == null)
                plogger.logMessage("Failed to read resources file from the jar! "
                                       +
                                       "The default translation file cannot be generated! Please contact pim16aap2");
            else
            {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                in = connection.getInputStream();
                java.nio.file.Files.copy(in, defaultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e)
        {
            plogger.logException(e, "Failed to write default file to \"" + textFile + "\".");
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException e)
            {
                plogger.logException(e);
            }
        }
        if (!defaultFile.setWritable(false))
        {
            plogger.logException(new IOException("Failed to make default translation file writable! " +
                                                     "This is not a big problem as long as you remember not to " +
                                                     "edit it manually!"));
        }
    }

    /**
     * Reads the translations from the provided translations file.
     */
    private void readFile()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(textFile)))
        {
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null)
            {
                // Ignore comments.
                if (sCurrentLine.startsWith("#") || sCurrentLine.isEmpty())
                    continue;

                String[] parts = sCurrentLine.split("=", 2);
                try
                {
                    Message msg = Message.valueOf(matchDots.matcher(parts[0]).replaceAll("_"));
                    String value = matchNewLines.matcher(matchColorCodes.matcher(parts[1]).replaceAll("\u00A7$1"))
                                                .replaceAll("\n");
                    messageMap.put(msg, value);
                }
                catch (IllegalArgumentException e)
                {
                    plogger.logMessage("Failed to identify Message corresponding to key: \"" + parts[0] +
                                           "\". Its value will be ignored!");
                }
            }
        }
        catch (FileNotFoundException e)
        {
            plogger.logException(e, "Locale file \"" + textFile + "\" does not exist!");
        }
        catch (IOException e)
        {
            plogger.logException(e, "Could not read locale file! \"" + textFile + "\"");
        }

        for (Message msg : Message.values())
            if (!msg.equals(Message.EMPTY) && !messageMap.containsKey(msg))
            {
                plogger.warn("Could not find translation of key: " + msg.name());
                messageMap.put(msg, getFailureString(msg.name()));
            }
    }

    /**
     * Gets the default String to return in case a value could not be found for a given String.
     *
     * @param key The key that could not be resolved.
     * @return The default String to return in case a value could not be found for a given String.
     */
    @NotNull
    private String getFailureString(final @NotNull String key)
    {
        return "Translation for key \"" + key + "\" not found! Contact server admin!";
    }

    /**
     * Gets the translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     *
     * @param msg    The {@link Message} to translate.
     * @param values The values to substitute for the variables in the message.
     * @return The translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     */
    @NotNull
    public String getString(final @NotNull Message msg, final @NotNull String... values)
    {
        if (msg.equals(Message.EMPTY))
            return "";

        if (values.length != Message.getVariableCount(msg))
        {
            plogger.logException(new IllegalArgumentException("Expected " + Message.getVariableCount(msg)
                                                                  + " variables for key " + msg.name() +
                                                                  " but only got " + values.length
                                                                  + ". This is a bug. Please contact pim16aap2!"));
            return getFailureString(msg.name());
        }

        String value = messageMap.get(msg);
        if (value != null)
        {
            for (int idx = 0; idx != values.length; ++idx)
                value = value.replaceAll(Message.getVariableName(msg, idx), values[idx]);
            return value;
        }

        plogger.warn("Failed to get the translation for key " + msg.name());
        return getFailureString(msg.name());
    }
}

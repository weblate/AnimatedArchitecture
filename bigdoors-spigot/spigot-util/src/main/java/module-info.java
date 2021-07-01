module bigdoors.spigot
{
    requires bigdoors.core;

    requires lombok;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires jdk.unsupported;
    requires java.sql;
    requires com.google.errorprone.annotations;
    requires org.bukkit;

    exports nl.pim16aap2.bigdoors.spigot.util;
    exports nl.pim16aap2.bigdoors.spigot.util.api;
    exports nl.pim16aap2.bigdoors.spigot.util.implementations;
}

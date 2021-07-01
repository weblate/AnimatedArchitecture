module bigdoors
{
    requires lombok;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires jdk.unsupported;
    requires java.sql;
    requires sqlite.jdbc;
    requires com.google.errorprone.annotations;

    exports nl.pim16aap2.bigdoors.annotations;
    exports nl.pim16aap2.bigdoors.api;
    exports nl.pim16aap2.bigdoors.api.factories;
    exports nl.pim16aap2.bigdoors.api.restartable;
    exports nl.pim16aap2.bigdoors.commands;
    exports nl.pim16aap2.bigdoors.doors;
}

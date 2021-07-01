module bigdoors.core
{
    requires lombok;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires jdk.unsupported;
    requires java.sql;
    requires sqlite.jdbc;
    requires com.google.errorprone.annotations;

    exports nl.pim16aap2.bigdoors;
    exports nl.pim16aap2.bigdoors.annotations;
    exports nl.pim16aap2.bigdoors.api;
    exports nl.pim16aap2.bigdoors.api.factories;
    exports nl.pim16aap2.bigdoors.api.restartable;
    exports nl.pim16aap2.bigdoors.commands;
    exports nl.pim16aap2.bigdoors.doors;
    exports nl.pim16aap2.bigdoors.doors.doorArchetypes;
    exports nl.pim16aap2.bigdoors.doortypes;
    exports nl.pim16aap2.bigdoors.events;
    exports nl.pim16aap2.bigdoors.events.dooraction;
    exports nl.pim16aap2.bigdoors.logging;
    exports nl.pim16aap2.bigdoors.moveblocks;
    exports nl.pim16aap2.bigdoors.tooluser;
    exports nl.pim16aap2.bigdoors.tooluser.creator;
    exports nl.pim16aap2.bigdoors.tooluser.step;
    exports nl.pim16aap2.bigdoors.tooluser.stepexecutor;
    exports nl.pim16aap2.bigdoors.util;
    exports nl.pim16aap2.bigdoors.util.cache;
    exports nl.pim16aap2.bigdoors.util.delayedinput;
    exports nl.pim16aap2.bigdoors.util.functional;
    exports nl.pim16aap2.bigdoors.util.messages;
    exports nl.pim16aap2.bigdoors.util.pair;
    exports nl.pim16aap2.bigdoors.util.vector;
}

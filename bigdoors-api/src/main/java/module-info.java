module bigdoors.api
{
    requires lombok;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires com.google.errorprone.annotations;

    exports nl.pim16aap2.bigdoors.api;
    exports nl.pim16aap2.bigdoors.api.managers;
    exports nl.pim16aap2.bigdoors.api.doortypes;
    exports nl.pim16aap2.bigdoors.api.commands;
    exports nl.pim16aap2.bigdoors.api.logging;
    exports nl.pim16aap2.bigdoors.api.annotations;
    exports nl.pim16aap2.bigdoors.api.util;
    exports nl.pim16aap2.bigdoors.api.util.functional;
    exports nl.pim16aap2.bigdoors.api.util.pair;
    exports nl.pim16aap2.bigdoors.api.util.messages;
    exports nl.pim16aap2.bigdoors.api.util.vector;
    exports nl.pim16aap2.bigdoors.api.doors;
    exports nl.pim16aap2.bigdoors.api.events;
    exports nl.pim16aap2.bigdoors.api.events.dooraction;
    exports nl.pim16aap2.bigdoors.api.restartable;
    exports nl.pim16aap2.bigdoors.api.factories;
}

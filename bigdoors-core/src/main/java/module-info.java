module bigdoors.core
{
    requires lombok;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires jdk.unsupported;
    requires java.sql;
    requires sqlite.jdbc;
    requires com.google.errorprone.annotations;

    requires bigdoors.api;
}

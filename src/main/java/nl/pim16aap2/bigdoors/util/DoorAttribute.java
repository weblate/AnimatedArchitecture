package nl.pim16aap2.bigdoors.util;

public enum DoorAttribute
{
    LOCK                (2),
    TOGGLE              (2),
    INFO                (2),
    DELETE              (0),
    RELOCATEPOWERBLOCK  (1),
    CHANGETIMER         (1),
    DIRECTION_STRAIGHT  (1),
    DIRECTION_ROTATE    (DoorAttribute.DIRECTION_STRAIGHT.permissionLevel),
    BLOCKSTOMOVE        (1),
    ADDOWNER            (0),
    REMOVEOWNER         (0);

    private int permissionLevel;

    private DoorAttribute(int permissionLevel)
    {
        this.permissionLevel = permissionLevel;
    }

    public static int getPermissionLevel(DoorAttribute atr)
    {
        return atr.permissionLevel;
    }
}

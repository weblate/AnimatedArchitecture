package nl.pim16aap2.bigdoors.storage;

import nl.pim16aap2.bigdoors.config.ConfigLoader;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.DoorType;
import nl.pim16aap2.bigdoors.exceptions.TooManyDoorsException;
import nl.pim16aap2.bigdoors.spigotutil.PlayerRetriever;
import nl.pim16aap2.bigdoors.spigotutil.WorldRetriever;
import nl.pim16aap2.bigdoors.storage.sqlite.SQLiteJDBCDriverConnection;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import nl.pim16aap2.bigdoors.util.MessagingInterfaceStdout;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
//@RunWith(MockitoJUnitRunner.Silent.class)
public class SQLiteJDBCDriverConnectionTest
{
    @Mock
    private ConfigLoader config;
    @Mock
    private World world;

    private static final String DELETEDOORNAME = "deletemeh";

    private static final UUID player1UUID = UUID.fromString("27e6c556-4f30-32bf-a005-c80a46ddd935");
    private static final UUID player2UUID = UUID.fromString("af5c6f36-445d-3786-803d-c2e3ba0dc3ed");
    private static final UUID player3UUID = UUID.fromString("b50ad385-829d-3141-a216-7e7d7539ba7f");
    private static final String player1Name = "pim16aap2";
    private static final String player2Name = "TestBoy";
    private static final String player2NameALT = "TestMan";
    private static final String player3Name = "thirdwheel";
    private static final UUID worldUUID = UUID.fromString("ea163ae7-de27-4b3e-b642-d459d56bb360");

    @Mock
    private Player player1, player2, player3;

    @Mock
    private WorldRetriever worldRetriever;
    @Mock
    private PlayerRetriever playerRetriever;

    private DoorBase door1;
    private DoorBase door2;
    private DoorBase door3;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private static File dbFile;
    private static File dbFileBackup;
    private static File dbFileV0;
    private static String testDir;
    private IStorage storage;

    // Initialize files.
    static
    {
        try
        {
            testDir = new File(".").getCanonicalPath() + "/tests";
            dbFile = new File(testDir + "/test.db");
            dbFileBackup = new File(dbFile.toString() + ".BACKUP");
            dbFileV0 = new File(dbFile.toString() + ".v0");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static final File logFile = new File(testDir, "log.txt");
    private static final PLogger plogger = PLogger.init(logFile, new MessagingInterfaceStdout("BigDoorsTest"));

    static
    {
        plogger.setConsoleLogging(true);
        plogger.setOnlyLogExceptions(true); // Only log errors etc.
    }

    // Initialize mocking.
    @Before
    public void setupMocking()
    {
        DoorOpeningUtility.init(null, null, null, null, null);
        when(world.getUID()).thenReturn(worldUUID);
        when(worldRetriever.worldFromString(worldUUID)).thenReturn(world);
        when(player1.getName()).thenReturn(player1Name);
        when(player2.getName()).thenReturn(player2Name);
        when(player3.getName()).thenReturn(player3Name);
//        when(player1.getUniqueId()).thenReturn(player1UUID);
//        when(player2.getUniqueId()).thenReturn(player2UUID);
//        when(player3.getUniqueId()).thenReturn(player3UUID);
        when(playerRetriever.getOfflinePlayer(player1UUID)).thenReturn(player1);
        when(playerRetriever.getOfflinePlayer(player2UUID)).thenReturn(player2);
        when(playerRetriever.getOfflinePlayer(player3UUID)).thenReturn(player3);
//        when(playerRetriever.nameFromUUID(player1UUID)).thenReturn(Optional.of(player1Name));
//        when(playerRetriever.nameFromUUID(player2UUID)).thenReturn(Optional.of(player2Name));
//        when(playerRetriever.nameFromUUID(player3UUID)).thenReturn(Optional.of(player3Name));
        when(config.dbBackup()).thenReturn(true);

        try
        {
            DoorBase.DoorData doorData;
            {
                Location min = new Location(world, 144, 75, 153);
                Location max = new Location(world, 144, 131, 167);
                Location engine = new Location(world, 144, 75, 153);
                Location powerBlock = new Location(world, 101, 101, 101);
                boolean isOpen = false;
                doorData = new DoorBase.DoorData(min, max, engine, powerBlock, world, isOpen,
                                                 RotateDirection.valueOf(0));
            }
            door1 = DoorType.BIGDOOR.getNewDoor(plogger, 1, doorData);

            door1.setName("massive1");
            door1.setLock(false);
            door1.setAutoClose(0);
            door1.setBlocksToMove(0);
            door1.setDoorOwner(new DoorOwner(door1.getDoorUID(), player1UUID, player1Name, 0));


            {
                Location min = new Location(world, 144, 75, 168);
                Location max = new Location(world, 144, 131, 182);
                Location engine = new Location(world, 144, 75, 153);
                Location powerBlock = new Location(world, 102, 102, 102);
                boolean isOpen = false;
                doorData = new DoorBase.DoorData(min, max, engine, powerBlock, world, isOpen,
                                                 RotateDirection.valueOf(0));
            }
            door2 = DoorType.DRAWBRIDGE.getNewDoor(plogger, 2, doorData);
            door2.setName("massive2");
            door2.setLock(false);
            door2.setAutoClose(0);
            door2.setBlocksToMove(0);
            door2.setDoorOwner(new DoorOwner(door2.getDoorUID(), player1UUID, player1Name, 0));


            {
                Location min = new Location(world, 144, 70, 168);
                Location max = new Location(world, 144, 151, 112);
                Location engine = new Location(world, 144, 75, 153);
                Location powerBlock = new Location(world, 103, 103, 103);
                boolean isOpen = false;
                doorData = new DoorBase.DoorData(min, max, engine, powerBlock, world, isOpen, RotateDirection.NORTH);
            }
            door3 = DoorType.BIGDOOR.getNewDoor(plogger, 3, doorData);
            door3.setName("massive2");
            door3.setLock(false);
            door3.setAutoClose(0);
            door3.setBlocksToMove(0);
            door3.setDoorOwner(new DoorOwner(door3.getDoorUID(), player2UUID, player2Name, 0));
        }
        catch (Exception e)
        {
            plogger.logException(e);
        }
    }

    /**
     * Initializes the storage object.
     */
    private void initStorage()
    {
        storage = new SQLiteJDBCDriverConnection(dbFile, plogger, config, worldRetriever, playerRetriever);
    }

    /**
     * Prepares files for a test run.
     */
    @BeforeClass
    public static void prepare()
    {
        if (dbFile.exists())
        {
            System.out.println("WARNING! FILE \"dbFile\" STILL EXISTS! Attempting deletion now!");
            dbFile.delete();
        }
        if (dbFileV0.exists())
        {
            System.out.println("WARNING! FILE \"dbFileV0\" STILL EXISTS! Attempting deletion now!");
            dbFileV0.delete();
        }
        if (dbFileBackup.exists())
        {
            System.out.println("WARNING! FILE \"dbFileBackup\" STILL EXISTS! Attempting deletion now!");
            dbFileBackup.delete();
        }
    }

    /**
     * Runs cleanup after the tests. Remove leftovers from previous runs and store the finished databases of this run
     * (for debugging purposes).
     */
    @AfterClass
    public static void cleanup()
    {
        // Remove any old database files and append ".FINISHED" to the name of the current one, so it
        // won't interfere with the next run, but can still be used for manual inspection.
        File oldDB = new File(dbFile.toString() + ".FINISHED");
        File oldLog = new File(logFile.toString() + ".FINISHED");
        File oldV0db = new File(dbFileV0.toString() + ".FINISHED");

        plogger.setConsoleLogging(true);
        if (oldDB.exists())
            oldDB.delete();
        if (oldV0db.exists())
            oldV0db.delete();
        if (dbFileBackup.exists())
            dbFileBackup.delete();

        try
        {
            Files.move(dbFile.toPath(), oldDB.toPath());
        }
        catch (IOException e)
        {
            plogger.logException(e);
        }
        try
        {
            Files.move(dbFileV0.toPath(), oldV0db.toPath());
        }
        catch (IOException e)
        {
            plogger.logException(e);
        }
        try
        {
            if (oldLog.exists())
                oldLog.delete();
            while (!plogger.isEmpty())
                Thread.sleep(100L);
            Files.move(logFile.toPath(), oldLog.toPath());
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs all tests.
     */
    @Test
    public void runTests()
        throws TooManyDoorsException, InvocationTargetException, NoSuchMethodException, IllegalAccessException,
               NoSuchFieldException, IOException
    {
        initStorage();
        insertDoors();
        verifyDoors();
        auxiliaryMethods();
        modifyDoors();
        testUpgrade();
        insertDoors(); // Insert the doors again to make sure the upgrade went smoothly.
        // Make sure no errors were logged.
        waitForLogger();
        assertEquals(logFile.length(), 0);

        plogger.setOnlyLogExceptions(false);

        plogger.logMessage("================================\nStarting failure testing now:");
        testFailures();
    }

    /**
     * Tests inserting doors in the database.
     */
    public void insertDoors()
    {
        storage.insert(door1);
        storage.insert(door2);
        storage.insert(door3);
    }

    /**
     * Checks if a door was successfully added to the database and that all data in intact.
     *
     * @param door The door to verify.
     */
    private void testRetrieval(DoorBase door) throws TooManyDoorsException
    {
        assertNotNull(storage);
        assertNotNull(door);
        assertNotNull(door.getPlayerUUID().toString());
        assertNotNull(door.getName());

        Optional<List<DoorBase>> test = storage.getDoors(door.getPlayerUUID(), door.getName());
        if (!test.isPresent())
            fail("COULD NOT RETRIEVE DOOR WITH ID \"" + door.getDoorUID() + "\"!");

        if (test.get().size() != 1)
            fail("TOO MANY DOORS FOUND FOR DOOR WITH ID \"" + door.getDoorUID() + "\"!");

        if (!door.equals(test.get().get(0)))
            fail("Data of retrieved door is not the same!");
    }

    /**
     * Verifies that the data of all doors that have been added to the database so far is correct.
     */
    public void verifyDoors() throws TooManyDoorsException
    {
        testRetrieval(door1);
        testRetrieval(door2);
        testRetrieval(door3);
    }

    /**
     * Tests the basic SQL methods.
     */
    public void auxiliaryMethods()
    {
        // Check simple methods.
        assertEquals(0, storage.getPermission(player1UUID.toString(), 1));
        assertEquals(1, storage.getDoorCountForPlayer(player1UUID, "massive1"));
        assertEquals(2, storage.getDoorCountForPlayer(player1UUID));
        assertEquals(1, storage.getDoorCountForPlayer(player2UUID));
        assertEquals(1, storage.getDoorCountByName("massive1"));
        assertTrue(storage.getDoor(player1UUID, 1).isPresent());
        assertEquals(door1, storage.getDoor(player1UUID, 1).get());
        assertFalse(storage.getDoor(player1UUID, 3).isPresent());
        assertTrue(storage.getDoor(1).isPresent());
        assertEquals(door1, storage.getDoor(1).get());
        assertFalse(storage.getDoor(9999999).isPresent());
        assertTrue(storage.getOwnerOfDoor(1L).isPresent());
        assertEquals(door1.getDoorOwner(), storage.getOwnerOfDoor(1L).get());
        assertTrue(storage.isBigDoorsWorld(worldUUID));
        assertFalse(storage.isBigDoorsWorld(UUID.randomUUID()));

        assertEquals(1, storage.getOwnerCountOfDoor(1L));

        // Check if adding owners works correctly.
        assertEquals(1, storage.getOwnersOfDoor(1L).size());
        // Try adding player2 as owner of door 2.
        storage.addOwner(2L, player2UUID, 1);
        // Try adding player 1 as owner of door 2, while player 1 is already the creator! This is not allowed.
        storage.addOwner(2L, player1UUID, 0);
        // Try adding player 2 as owner of door 2, while player 1 is already the creator! This is not allowed.
        storage.addOwner(2L, player2UUID, 0);

        // Try adding a player that is not in the database yet as owner.
        assertEquals(1, storage.getOwnersOfDoor(1L).size());
        storage.addOwner(1L, player3UUID, 1);
        assertEquals(2, storage.getOwnersOfDoor(1L).size());

        // Verify the permission level of player 2 over door 2.
        assertEquals(1, storage.getPermission(player2UUID.toString(), 2L));
        // Verify there are only 2 owners of door 2 (player 1 didn't get copied).
        assertEquals(2, storage.getOwnersOfDoor(2L).size());

        // Verify that player 2 is the creator of exactly 1 door.
        assertTrue(storage.getDoors(player2UUID.toString(), 0).isPresent());
        assertEquals(1, storage.getDoors(player2UUID.toString(), 0).get().size());

        // Verify that player 2 is owner with permission level <= 1 of exactly 2 doors (door 3 (0) and door 2 (1)).
        assertTrue(storage.getDoors(player2UUID.toString(), 1).isPresent());
        assertEquals(2, storage.getDoors(player2UUID.toString(), 1).get().size());

        // Verify that player 2 is owner with permission level <= 1 of exactly 2 doors, both named "massive2".
        assertTrue(storage.getDoors(player2UUID.toString(), "massive2", 1).isPresent());
        assertEquals(2, storage.getDoors(player2UUID.toString(), "massive2", 1).get().size());

        // Verify that player 2 is owner with permission level <= 1 of exactly 1 door, named "massive2".
        assertTrue(storage.getDoors(player2UUID.toString(), "massive2", 0).isPresent());
        assertEquals(1, storage.getDoors(player2UUID.toString(), "massive2", 0).get().size());

        // Verify that adding an existing owner overrides the permission level.
        storage.addOwner(2L, player2UUID, 2);
        assertEquals(2, storage.getPermission(player2UUID.toString(), 2L));

        // Remove player 2 as owner of door 2.
        storage.removeOwner(2L, player2UUID.toString());
        assertEquals(1, storage.getOwnersOfDoor(2L).size());

        // Try to remove player 1 (creator) of door 2. This is not allowed.
        storage.removeOwner(2L, player1UUID.toString());
        assertEquals(1, storage.getOwnersOfDoor(2L).size());

        // Verify that after deletion of player 2 as owner, player 2 is now owner with permission level <= 1
        // of exactly 1 door, named "massive2" (door 3).
        assertTrue(storage.getDoors(player2UUID.toString(), "massive2", 1).isPresent());
        assertEquals(1, storage.getDoors(player2UUID.toString(), "massive2", 1).get().size());

        // Verify that player 1 is owner of exactly 1 door named "massive2".
        assertTrue(storage.getDoors(player1UUID, "massive2").isPresent());
        assertEquals(1, storage.getDoors(player1UUID, "massive2").get().size());

        // Verify that player 1 owns exactly 2 doors.
        assertTrue(storage.getDoors(player1UUID).isPresent());
        assertEquals(2, storage.getDoors(player1UUID).get().size());

        // Verify that there are exactly 2 doors named "massive2" in the database.
        assertTrue(storage.getDoors("massive2").isPresent());
        assertEquals(2, storage.getDoors("massive2").get().size());

        // Insert a copy of door 1 in the database (will have doorUID = 4).
        storage.insert(door1);

        // Verify there are now exactly 2 doors named "massive1" in the database.
        assertTrue(storage.getDoors("massive1").isPresent());
        assertEquals(2, storage.getDoors("massive1").get().size());

        // Remove the just-added copy of door 1 (doorUID = 4) from the database.
        storage.removeDoor(4L);

        // Verify that after removal of the copy of door 1 (doorUID = 4), there is now exactly 1 door named
        // "massive1" in the database again.
        assertTrue(storage.getDoors("massive1").isPresent());
        assertEquals(1, storage.getDoors("massive1").get().size());

        // Verify that player 2 cannot delete doors they do not own (door 1 belongs to player 1).
        storage.removeOwner(1L, player2UUID.toString());
        assertTrue(storage.getDoors("massive1").isPresent());
        assertEquals(1, storage.getDoors("massive1").get().size());

        // Add 10 copies of door3 with a different name to the database.
        door3.setName(DELETEDOORNAME);
        // Verify there are currently exactly 0 doors with this different name in the database.
        assertFalse(storage.getDoors(DELETEDOORNAME).isPresent());

        for (int idx = 0; idx < 10; ++idx)
            storage.insert(door3);

        // Verify there are now exactly 10 doors with this different name in the database.
        assertTrue(storage.getDoors(DELETEDOORNAME).isPresent());
        assertEquals(10, storage.getDoors(DELETEDOORNAME).get().size());

        // Remove all 10 doors we just added (owned by player 2) and verify there are exactly 0 entries of the door with
        // the new name after batch removal. Also revert the name change of door 3.
        storage.removeDoors(player2UUID.toString(), DELETEDOORNAME);
        assertFalse(storage.getDoors(DELETEDOORNAME).isPresent());
        assertTrue(storage.getDoor(3L).isPresent());
        door3.setName(storage.getDoor(3L).get().getName());

        // Make sure the player name corresponds to the correct UUID.
        assertTrue(storage.getPlayerName(player2UUID.toString()).isPresent());
        assertEquals(player2Name, storage.getPlayerName(player2UUID.toString()).get());
        assertFalse(storage.getPlayerUUID(player2NameALT).isPresent());
        assertTrue(storage.getPlayerUUID(player2Name).isPresent());
        assertEquals(player2UUID, storage.getPlayerUUID(player2Name).get());
        assertNotSame(player1UUID, storage.getPlayerUUID(player2Name).get());

        // Update player 2's name to their alt name and make sure the old name is gone and the new one is reachable.
        storage.updatePlayerName(player2UUID.toString(), player2NameALT);
        assertFalse(storage.getPlayerUUID(player2Name).isPresent());
        assertTrue(storage.getPlayerUUID(player2NameALT).isPresent());
        assertEquals(player2UUID, storage.getPlayerUUID(player2NameALT).get());

        // Revert name change of player 2.
        storage.updatePlayerName(player2UUID.toString(), player2Name);

        long chunkHash = Util.simpleChunkHashFromLocation(door1.getPowerBlockLoc().getBlockX(),
                                                          door1.getPowerBlockLoc().getBlockZ());
        assertNotNull(storage.getPowerBlockData(chunkHash));
        assertEquals(3, storage.getPowerBlockData(chunkHash).size());
    }

    /**
     * Verifies that door 3 exists in the database, and that the database entry of door 3 does equals the object of door
     * 3.
     */
    private void assertDoor3Parity()
    {
        // Check if door 3 exists in the database.
        assertTrue(storage.getDoor(player2UUID, 3L).isPresent());
        // Check if the object of door 3 and the database entry of door 3 are the same.
        assertEquals(door3, storage.getDoor(player2UUID, 3L).get());
    }

    /**
     * Verifies that door 3 exists in the database, and that the database entry of door 3 does not equal the object of
     * door 3.
     */
    private void assertDoor3NotParity()
    {
        // Check if door 3 exists in the database.
        assertTrue(storage.getDoor(player2UUID, 3L).isPresent());
        // Check if the object of door 3 and the database entry of door 3 are NOT the same.
        assertNotSame(door3, storage.getDoor(player2UUID, 3L).get());
    }

    /**
     * Runs tests of the methods that modify doors in the database.
     */
    public void modifyDoors()
    {
        // Test changing autoCloseTime value.
        {
            final int testAutoCloseTime = 20;
            // Change the autoCloseTimer of the object of door 3.
            storage.updateDoorAutoClose(3, testAutoCloseTime);
            // Verify that door 3 in the database is no longer the same as the door 3 object.
            // This should be the case, because the auto close timer is 0 for the door 3 object.
            assertDoor3NotParity();
            door3.setAutoClose(testAutoCloseTime);
            assertEquals(door3, storage.getDoor(player2UUID, 3L).get());

            // Reset the autoclose timer of both the object of door 3 and the database entry of door 3 and
            // verify data parity.
            door3.setAutoClose(0);
            storage.updateDoorAutoClose(3, 0);
            assertDoor3Parity();
        }

        // Test changing blocksToMove value.
        {
            final int testBlocksToMove = 20;
            // Change blocksToMove of the object of door 3.
            storage.updateDoorBlocksToMove(3, testBlocksToMove);
            // Verify that door 3 in the database is no longer the same as the door 3 object.
            // This should be the case, because the blocksToMove value is 0 for the door 3 object.
            assertDoor3NotParity();
            // Update the door 3 object to have the same blocksToMove value as the door 3 in the database
            // And verify that the door 3 in the database and the door 3 object are the same again.
            door3.setBlocksToMove(testBlocksToMove);
            assertDoor3Parity();

            // Reset the blocksToMove value of both the object of door 3 and the database entry of door 3 and
            // verify data parity.
            door3.setBlocksToMove(0);
            storage.updateDoorBlocksToMove(3, 0);
            assertDoor3Parity();
        }

        // Test (un)locking.
        {
            // Set the lock status of the database entry of door 3 to true.
            storage.setLock(3L, true);
            // Verify that the database entry of door 3 and the object of door 3 are no longer the same.
            // This should be the case because the database entry of door 3 is now locked,
            // while the object of door 3 is not.
            assertDoor3NotParity();
            // Set the object of door 3 to locked so it matches the database entry of door 3. Then make sure
            // Both the object and the database entry of door 3 match.
            door3.setLock(true);
            assertDoor3Parity();

            // Reset the lock status of both the database entry and the object of door 3 and verify they are
            // the same again.
            storage.setLock(3L, false);
            door3.setLock(false);
            assertDoor3Parity();
        }

        // Test rotate direction change
        {
            RotateDirection oldDir = door3.getOpenDir();
            RotateDirection newDir = RotateDirection.getOpposite(oldDir);

            // Set the rotation direction of the database entry of door 3 to true.
            storage.updateDoorOpenDirection(3L, newDir);
            // Verify that the database entry of door 3 and the object of door 3 are no longer the same.
            // This should be the case because the rotate directions should differ.
            assertDoor3NotParity();
            // Change the rotation direction of the object of door 3 so that it matches the rotation direction
            // of the database entry of door 3.
            door3.setOpenDir(newDir);
            assertDoor3Parity();

            // Reset the rotation direction of both the database entry and the object of door 3 and verify they are
            // the same again.
            storage.updateDoorOpenDirection(3L, oldDir);
            door3.setOpenDir(oldDir);
            assertDoor3Parity();
        }

        // Test power block relocation.
        {
            // Create a new location that is not the same as the current power block location of door 3.
            Location oldLoc = door3.getPowerBlockLoc();
            Location newLoc = oldLoc.clone();
            newLoc.setY((newLoc.getBlockX() + 30) % 256);
            assertNotSame(newLoc, oldLoc);

            // Set the power block location of the database entry of door 3 to the new location.
            storage.updateDoorPowerBlockLoc(3L, newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ(),
                                            newLoc.getWorld().getUID());
            // Verify that the database entry of door 3 and the object of door 3 are no longer the same.
            // This should be the case because the power block locations should differ between them.
            assertDoor3NotParity();
            // Move the powerBlock location of the object of door 3 so that it matches the database entry of door 3.
            // Then make sure both the object and the database entry of door 3 match.
            door3.setPowerBlockLocation(newLoc);
            assertDoor3Parity();

            // Reset the powerBlock location of both the database entry and the object of door 3 and verify they are the
            // same again.
            storage.updateDoorPowerBlockLoc(3L, oldLoc.getBlockX(), oldLoc.getBlockY(), oldLoc.getBlockZ(),
                                            oldLoc.getWorld().getUID());
            door3.setPowerBlockLocation(oldLoc);
            assertDoor3Parity();
        }

        // Test updating doors.
        {
            // Create some new locations and verify they're different from the old min/max values.
            Location oldMin = door3.getMinimum();
            Location oldMax = door3.getMaximum();
            Location newMin = oldMin.clone().add(0, 20, 10);
            Location newMax = oldMax.clone().add(40, 0, 20);
            assertNotSame(oldMin, newMin);
            assertNotSame(oldMax, newMax);

            // Set the coordinates of the database entry of door 3 to the new location.
            storage.updateDoorCoords(3L, false, newMin.getBlockX(), newMin.getBlockY(), newMin.getBlockZ(),
                                     newMax.getBlockX(), newMax.getBlockY(), newMax.getBlockZ());
            // Verify that the database entry of door 3 and the object of door 3 are no longer the same.
            // This should be the case because the coordinates should differ between them.
            assertDoor3NotParity();
            // Move the coordinates of the object of door 3 so that it matches the database entry of door 3.
            // Then make sure both the object and the database entry of door 3 match.
            door3.setMinimum(newMin);
            door3.setMaximum(newMax);
            assertDoor3Parity();

            // Reset the coordinates of both the database entry and the object of door 3 and verify they are the
            // same again.
            storage.updateDoorCoords(3L, false, oldMin.getBlockX(), oldMin.getBlockY(), oldMin.getBlockZ(),
                                     oldMax.getBlockX(), oldMax.getBlockY(), oldMax.getBlockZ());
            door3.setMinimum(oldMin);
            door3.setMaximum(oldMax);
            assertDoor3Parity();
        }
    }

    /**
     * Makes this thread wait for the logger to finish writing everything to the log file.
     */
    private void waitForLogger()
    {
        while (!plogger.isEmpty())
        {
            try
            {
                Thread.sleep(10L);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        // Wait a bit longer to make sure it's finished writing the file as well.
        try
        {
            Thread.sleep(20L);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Runs tests to verify that exceptions are caught when the should be and properly handled.
     */
    public void testFailures()
        throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        // Disable console logging of errors as it's the point of this test. This way I won't get scared by errors in the console.
        plogger.setConsoleLogging(false);

        waitForLogger();
        long logSize = logFile.length();
        // Try adding a player whose name cannot be found. Perhaps because of an invalid UUID or something.
        // What should happen: Error saying no player name could be retrieved.
        UUID testUUID = UUID.randomUUID();
        while (testUUID.equals(player1UUID) || testUUID.equals(player2UUID) || testUUID.equals(player3UUID))
            testUUID = UUID.randomUUID();
        storage.addOwner(2L, testUUID, 1);

        // Make sure new errors were added to the log file.
        waitForLogger();
        long newLogSize = logFile.length();
        assertTrue(newLogSize > logSize);
        logSize = newLogSize;


        // Verify database disabling works as intended.
        {
            // Set the enabled status of the database to false.
            final Field databaseLock = SQLiteJDBCDriverConnection.class.getDeclaredField("enabled");
            databaseLock.setAccessible(true);
            databaseLock.setBoolean(storage, false);

            // Verify that an IllegalStateException is thrown whenever retrieval is attempted while the database is
            // disabled.
            assertThrows(IllegalStateException.class, () -> storage.getDoor(1L));

            // Set the database state to enabled again and verify that it's now possible to retrieve doors again.
            databaseLock.setBoolean(storage, true);
            assertTrue(storage.getDoor(player1UUID, 1L).isPresent());
        }

        // Make sure new errors were added to the log file.
        waitForLogger();
        newLogSize = logFile.length();
        assertTrue(newLogSize > logSize);
        logSize = newLogSize;

        // Verify database locking works as intended.
        {
            // Set the enabled status of the database to false.
            final Method databaseLock = SQLiteJDBCDriverConnection.class
                .getDeclaredMethod("setDatabaseLock", boolean.class);
            databaseLock.setAccessible(true);
            databaseLock.invoke(storage, true);

            // Verify that an IllegalStateException is thrown whenever retrieval is attempted while the database is
            // locked.
            assertThrows(IllegalStateException.class, () -> storage.getDoor(1L));

            // Unlock the database again and verify that it's now possible to retrieve doors again.
            databaseLock.invoke(storage, false);
            assertTrue(storage.getDoor(player1UUID, 1L).isPresent());
        }

        // Make sure new errors were added to the log file.
        waitForLogger();
        newLogSize = logFile.length();
        assertTrue(newLogSize > logSize);
        logSize = newLogSize;

        plogger.setConsoleLogging(true); // Enable console logging again after the test.
    }

    /**
     * Runs tests to verify that upgrading the database from version 0 works as intended.
     */
    public void testUpgrade()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException
    {
        final Method stripToV0 = SQLiteJDBCDriverConnection.class.getDeclaredMethod("stripToV0");
        stripToV0.setAccessible(true);
        final Method upgrade = SQLiteJDBCDriverConnection.class.getDeclaredMethod("upgrade");
        upgrade.setAccessible(true);
        final Method replaceTempPlayerNames = SQLiteJDBCDriverConnection.class
            .getDeclaredMethod("replaceTempPlayerNames");
        replaceTempPlayerNames.setAccessible(true);

        Files.copy(dbFile.toPath(), dbFileV0.toPath());
        SQLiteJDBCDriverConnection storageV0 = new SQLiteJDBCDriverConnection(dbFileV0, plogger, config, worldRetriever,
                                                                              playerRetriever);
        stripToV0.invoke(storageV0);
        upgrade.invoke(storageV0);
        replaceTempPlayerNames.invoke(storageV0);

        // Verify that all data part of V0 is left unchanged during the conversion process.
        // Also make sure that data that should be added properly (player names) worked out fine.
        assertTrue(storageV0.getDoor(player1UUID, 1L).isPresent());
        DoorBase test1 = storageV0.getDoor(player1UUID, 1L).get();
        assertEquals(door1.getDoorOwner(), test1.getDoorOwner());
        assertEquals(door1.getDoorUID(), test1.getDoorUID());
        assertEquals(door1.getName(), test1.getName());
        assertEquals(door1.getWorld(), test1.getWorld());
        assertEquals(door1.isOpen(), test1.isOpen());
        assertEquals(door1.getMinimum(), test1.getMinimum());
        assertEquals(door1.getMaximum(), test1.getMaximum());
        assertEquals(door1.getEngine(), test1.getEngine());
        assertEquals(door1.isLocked(), test1.isLocked());
    }
}

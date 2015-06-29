package fr.ekinci.evictorpattern;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;

/**
 * Test for Evictor class's methods
 *
 * @author Gokan EKINCI
 */
public class EvictorTest {
    private final static long REFRESH_PERIOD            = 1_000L;
    private final static long EVICTOR_BEGIN_TIMESTAMP   = 5_000L;
    private final static long EVICTABLE_TIMESTAMP       = 2_000L; // EVICTOR_BEGIN_TIMESTAMP - EVICTABLE_TIMESTAMP     = 3000 is superior to REFRESH_PERIOD
    private final static long NOT_EVICTABLE_TIMESTAMP   = 4_500L; // EVICTOR_BEGIN_TIMESTAMP - NOT_EVICTABLE_TIMESTAMP = 500  is inferior to REFRESH_PERIOD
    private Field startTimestampField;

    @Before
    public void init() throws NoSuchFieldException, SecurityException{
        startTimestampField = ConnectionHolder.class.getDeclaredField("startTimestamp");
        startTimestampField.setAccessible(true);
    }
    
    @Test
    public void testFindAndEvictOldValues() throws IllegalArgumentException, IllegalAccessException {     
        final String[] ids = {"tata", "toto", "tutu", "titi"};
  
        final Connection[] connections = {
            Mockito.mock(Connection.class), 
            Mockito.mock(Connection.class),
            Mockito.mock(Connection.class),
            Mockito.mock(Connection.class)
        };
        
        // startTimestamp is 0
        final ConnectionHolder[] connectionHolders = {
            new ConnectionHolder(connections[0]),
            new ConnectionHolder(connections[1]),
            new ConnectionHolder(connections[2]),
            new ConnectionHolder(connections[3])
        };
                
        final Map<String, ConnectionHolder> connectionRegistryMapMock = new HashMap<String, ConnectionHolder>(){{
            put(ids[0], connectionHolders[0]);
            put(ids[1], connectionHolders[1]);
            put(ids[2], connectionHolders[2]);
            put(ids[2], connectionHolders[3]);
        }};
        
        
        /* *** Initialize parameters *** */
        
        // TIMESTAMP
        startTimestampField.set(connectionHolders[0], EVICTABLE_TIMESTAMP);       // Evictable
        startTimestampField.set(connectionHolders[1], EVICTABLE_TIMESTAMP);       // Evictable
        startTimestampField.set(connectionHolders[2], NOT_EVICTABLE_TIMESTAMP);   // Not Evictable
        startTimestampField.set(connectionHolders[3], NOT_EVICTABLE_TIMESTAMP);   // Not Evictable
        
        // IN_USE
        connectionHolders[0].setInUse(false);                                     // Evictable
        connectionHolders[1].setInUse(true);                                      // Not Evictable
        connectionHolders[2].setInUse(false);                                     // Evictable
        connectionHolders[3].setInUse(true);                                      // Not Evictable
        
        /* *** RULES *** */
        //                        TIMESTAMP        IN_USE           RESULT
        // connectionHolders[0] : Evictable     && Evictable     => Evictable
        // connectionHolders[1] : Evictable     && Not Evictable => NOT Evictable
        // connectionHolders[2] : Not Evictable && Evictable     => NOT Evictable
        // connectionHolders[3] : Not Evictable && Not Evictable => NOT Evictable
        
        // Start evictor process
        final Evictor<String, ConnectionHolder> gc = 
            new Evictor<String, ConnectionHolder>(connectionRegistryMapMock, REFRESH_PERIOD); 
        gc.findAndEvictOldValues(connectionRegistryMapMock, EVICTOR_BEGIN_TIMESTAMP, REFRESH_PERIOD);
        
        Assert.assertNull(connectionRegistryMapMock.get(ids[0]));    // => Evictable
        Assert.assertNotNull(connectionRegistryMapMock.get(ids[1])); // => NOT Evictable
        Assert.assertNotNull(connectionRegistryMapMock.get(ids[2])); // => NOT Evictable
        Assert.assertNotNull(connectionRegistryMapMock.get(ids[2])); // => NOT Evictable
    }
}

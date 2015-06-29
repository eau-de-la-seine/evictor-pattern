package fr.ekinci.evictorpattern;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;


/**
 * Non-serializable POJO for holding Connection
 * @author Gokan EKINCI
 */
public class ConnectionHolder implements Evictable {
    private final static Logger LOG = Logger.getLogger(ConnectionHolder.class);
    private final long startTimestamp;
    private final Connection connection;
    private volatile boolean inUse;
    
    public ConnectionHolder(Connection connection){
        this.connection = connection;
        this.startTimestamp = System.currentTimeMillis();
        inUse = false;
    }
    
    /* GETTERS */
    public long getStartTimestamp() {
        return startTimestamp;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    /* SETTER */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * If connection is not used and refreshPeriod is superior 
     * to the difference between gc timestamp and current object timestamp
     * return true, false otherwise
     */
    @Override
    public boolean isEvictable(long gcTimestamp, long refreshPeriod) {
        LOG.debug("isEvictable calculation : ");
        LOG.debug("((gcTimestamp - startTimestamp) > refreshPeriod) : " + "((" + gcTimestamp + "-" + startTimestamp + ") > " + refreshPeriod + ")");
        LOG.debug("Result : " + (gcTimestamp - startTimestamp));
        boolean isEvictable = (!inUse) && ((gcTimestamp - startTimestamp) > refreshPeriod);
        LOG.debug("isEvictable : " + isEvictable);
        return isEvictable;
    }

    /**
     * Close connection (clean resource)
     */
    @Override
    public void evict() {
        try {
            if(connection != null){
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Error happened in ConnectionHolder.collectGarbage() "
                    + "when connection.close() has been called.", e);
        }        
    }
}

package fr.ekinci.evictorpattern;


/**
 * If an object is evictable (cleanable)
 * 
 * @author Gokan EKINCI
 */
public interface Evictable {
    
    /**
     * Indicate if object is evictable
     * 
     * @param evictorBeginTimestamp     Evictor start timestamp
     * @param refreshPeriod             refreshPeriod before cleaning old value
     * @return                          true if object is evictable (cleanable), false otherwise
     */
    public boolean isEvictable(long evictorBeginTimestamp, long refreshPeriod);
    
    /**
     * Clean resources
     */
    public void evict();
}

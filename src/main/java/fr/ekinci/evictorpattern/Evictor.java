package fr.ekinci.evictorpattern;

import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Class which periodically suppress old values of a map 
 * 
 * @author Gokan EKINCI
 */
public class Evictor<ID, VALUE_TYPE extends Evictable> {
    private final static Logger LOG = Logger.getLogger(Evictor.class);
    private final Map<ID, VALUE_TYPE> evictableDataMap;
    private final Thread thread;
    private final long refreshPeriod;
    private volatile boolean runEvictor;
    
    public Evictor(
        Map<ID, VALUE_TYPE> connectionRegistryMapParameter, 
        long refreshPeriodParameter
    ){
        evictableDataMap = connectionRegistryMapParameter;
        refreshPeriod = refreshPeriodParameter;
        
        runEvictor = true;
        
        thread = new Thread(new Runnable(){
            @Override
            public void run() {
                while(runEvictor){
                    try {
                        LOG.debug("Evictor is launching a new cleaning step");
                        findAndEvictOldValues(evictableDataMap, System.currentTimeMillis(), refreshPeriod); 
                        LOG.debug("Evictor has finished its step");
                        Thread.sleep(refreshPeriod);
                    } catch (InterruptedException e) {
                        LOG.error("Error happened in Evictor.findAndEvictOldValues() "
                                + "when sleep(refreshPeriod) has been closed.", e);
                    }
                }
            }           
        });
    }
    
    
    /**
     * Start a new Thread which evict "evictable" elements
     */
    public void startEvictor(){
        thread.start();
    }
    
    
    /**
     * Process of removing non-used and old values 
     *
     * @param evictableDataMap      The map we are trying to clean
     * @param evictorBeginTimestamp Timestamp of evictor process has begun
     * @param refreshPeriod         The period 
     */
    public void findAndEvictOldValues(Map<ID, VALUE_TYPE> evictableDataMap, long evictorBeginTimestamp, long refreshPeriod){
        Iterator<Map.Entry<ID, VALUE_TYPE>> iter = evictableDataMap.entrySet().iterator();
        
        while (iter.hasNext()) {
            Map.Entry<ID, VALUE_TYPE> entry = iter.next();
            VALUE_TYPE value = entry.getValue();           

            if(value.isEvictable(evictorBeginTimestamp, refreshPeriod)){
                value.evict();  
                iter.remove(); // Remove element from map
            }           
        }
    }
    
    
    /**
     * Stop the evictor's thread
     */
    public void stopEvictor(){
        runEvictor = false;
    }
}

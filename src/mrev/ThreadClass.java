package mrev;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ThreadClass class contains the fundamental parts of a thread used in the
 * program. A class will extend the ThreadClass for the fundamental parts of
 * a thread.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-29, 20:13:49
 * @version 0.0.1
 */
public class ThreadClass {
    
    // -------------------------------------------------------------------------

    private final AtomicBoolean stopped = new AtomicBoolean(true);
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    
    private long readTime = 0;
    
    // -------------------------------------------------------------------------
    
    /**
     * This method verifies if the thread is stopped.
     * @return boolean If the thread is stopped.
     */
    public boolean isStopped() {
        return stopped.get();
    }
    
    /**
     * This method verifies if the thread is stopping, running it's last
     * loop before beeing stopped.
     * @return boolean If the thread is stopping.
     */
    public boolean isStopping() {
        return stopping.get();
    }
    
    /**
     * This method start the thread.
     */
    public void start() {
        
        stopped.set(false);
        stopping.set(false);
        
        executeBefore();
        
        while (!stopped.get() && !stopping.get()) {
            
            readTime = System.currentTimeMillis();
         
            executeWhile();
            
            final long diff = System.currentTimeMillis() - readTime;
            if (diff < 200) {
                try {
                    Thread.sleep(200 - diff);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadClass.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        stopped.set(true);
        stopping.set(false);
        
        executeAfter();
    }
    
    /**
     * This method stop the thread.
     */
    public void stop() {
        stopping.set(true);
    }
    
    /**
     * This method is called before the thread starts and may be
     * overriden.
     */
    public void executeBefore() {
        
    }
    
    /**
     * This method is called while the thread is executed, once each loop and
     * may be overridden.
     */
    public void executeWhile() {
        
    }
    
    /**
     * This method is called while the thread is executed, once each loop and
     * may be overridden.
     */
    public void executeAfter() {
        
    }
}

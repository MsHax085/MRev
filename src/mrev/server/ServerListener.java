package mrev.server;

import mrev.ThreadClass;
import mrev.server.database.DatabaseHandler;

/**
 * The ServerListener class handle the gameservers connection to the database by
 * executing commands and saving data/log from the gameservers.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-29, 18:27:50
 * @version 0.0.1
 */
public class ServerListener extends ThreadClass implements Runnable {
    
    // -------------------------------------------------------------------------
    
    private final DatabaseHandler db = new DatabaseHandler();
    
    // -------------------------------------------------------------------------
    
    /**
     * This method create a new thread for the ThreadClass to be executed within,
     * because TheadClass will only run in the thread it was called from.
     */
    public void startListening() {
        final Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * This method is called when the new thread is started from the startListening
     * function. The ThreadClass is then started within this new thread.
     */
    @Override
    public void run() {
        super.start();
    }
    
    /**
     * The ServerListener class extends ThreadClass and override the executeBefore
     * function, which is executed before the thread starts.
     * 
     * In this thread the executeBefore method start waiting servers.
     */
    @Override
    public void executeBefore() {
        
        db.open();
        
        if (db.verifyConnection()) {
            System.out.println("MySQL connection verified!");
        } else {
            System.out.println("Starting without an established MySQL connection!");
        }
    }
    
    /**
     * The ServerListener class extends ThreadClass and override the executeWhile
     * function, which is executed while the thread is running.
     * 
     * In this thread the executeWhile method handle the command input from
     * the database and save gameserver data/log to the database log.
     */
    @Override
    public void executeWhile() {
        
    }
    
    /**
     * The ServerListener class extends ThreadClass and override the executeAfter
     * function, which is executed before the thread stops.
     * 
     * In this thread the executeAfter method save the remaining data of the temporary
     * log storage to the database. This method also stop alive servers and set those to start on restart.
     */
    @Override
    public void executeAfter() {
        System.out.println("The server listener has stopped!");
    }
}

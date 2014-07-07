package mrev.server;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mrev.Notifier;
import mrev.ThreadClass;
import mrev.server.components.Server_Executor;
import mrev.server.components.Server_Processes;
import mrev.server.components.Server_Start;
import mrev.server.database.DatabaseHandler;
import mrev.server.gameserver.Gameserver;
import mrev.server.gameserver.components.Gameserver_IoStream;
import mrev.server.gameserver.components.Gameserver_Logger;

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
    
    public static final Server_Processes server_processes = new Server_Processes();
    
    private final Server_Start server_start = new Server_Start();
    private final Server_Executor server_executor = new Server_Executor();
    
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
        
        // Verify connection
        if (db.verifyConnection()) {
            Notifier.print("MySQL connection verified!");
        } else {
            Notifier.print("Starting without an established MySQL connection!");
            return;
        }
        
        // Clear waiting commands
        if (server_executor.clearCommands(db)) {
            Notifier.print("Cleared waiting commands!");
        } else {
            Notifier.print("Failed to clear waiting commands!");
        }
        
        // Start waiting servers
        if (server_start.startServers(db)) {
            Notifier.print("Sucessfully started waiting servers (" + server_processes.getNumberOfGameservers() + ")!");
        } else {
            Notifier.print("Failed to start waiting servers!");
        }
        
        Notifier.print("Successfully started the server listener!");
    }
    
    /**
     * The ServerListener class extends ThreadClass and override the executeWhile
     * function, which is executed while the thread is running.
     * 
     * In this thread the executeWhile method handle the command input from
     * the database and save gameserver data/log to the database log.
     * 
     * NOTE: Verify database connection!
     */
    @Override
    public void executeWhile() {
        
        boolean useDb = true;
        
        if (!db.verifyConnection()) {// Verify MySQL connection
            Notifier.print("Performing an update without MySQL ...");
            useDb = false;
        }
        
        // Execute commands
        if (useDb) {
            server_executor.execute();
        }
        
        // Update logs and flag finished servers
        for (Map.Entry<Integer, Gameserver> entry : server_processes.getSetOfGameservers()) {
            
            updateGameserver(entry, false);
            
        }
        
        clearStoppedGameservers(false);
    }
    
    /**
     * The ServerListener class extends ThreadClass and override the executeAfter
     * function, which is executed before the thread stops.
     * 
     * In this thread the executeAfter method save the remaining data of the temporary
     * log storage to the database. This method also stop alive servers, set those to start on restart and close the MySQL connection.
     */
    @Override
    public void executeAfter() {
        
        if (!db.verifyConnection()) {
            
            Notifier.print("Closing without an established MySQL connection!");
            
        } else {
            
            Notifier.print("MySQL connection verified!");
            
            /*
                One server may only save 20 log-rows to database at once, thus 5 tries should be enough but
                10 tries may be safer.
            */
            long timestamp = 0;
            for (int tries = 0; tries < 10; tries++) {
                
                timestamp = System.currentTimeMillis();
                
                if (!server_processes.isExistingGameservers()) {
                    break;
                }
                
                // Update logs, stop servers and flag finished servers
                for (Map.Entry<Integer, Gameserver> entry : server_processes.getSetOfGameservers()) {

                    updateGameserver(entry, true);

                }

                clearStoppedGameservers(true);
            
                long timedifference = System.currentTimeMillis() - timestamp;
                if (timedifference < 2000) {
                    try {
                        Thread.sleep(2000 - timedifference);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
            
            // Notify servers which failed to stop and/or log all remaining data
            if (server_processes.isExistingGameservers()) {

                for (Map.Entry<Integer, Gameserver> entry : server_processes.getSetOfGameservers()) {

                    final int port = entry.getKey();
                    Notifier.print("Failed to stop and/or log remaining data from server on port: " + port);

                }
            }
            
            db.close();
        }
        
        Notifier.print("Successfully stopped the server listener!");
    }
    
    /**
     * This method update the Gameservers by reading and updating logs, also removing stopped Gameservers
     * @param entry The Entry containing Gameserver port and class reference
     * @param stopAll If all servers shall be stopped
     */
    private void updateGameserver(Map.Entry<Integer, Gameserver> entry, boolean stopAll) {
        
        final int port = entry.getKey();
        final Gameserver gameserver = (Gameserver) entry.getValue();
        final Gameserver_IoStream gamserver_iostream = gameserver.getIoStream();
        final Gameserver_Logger gameserver_logger = gameserver.getLogger();

        final boolean isOutStreamReady = gamserver_iostream.isOutStreamReady();
        final boolean isTemporaryLogEmpty = gameserver_logger.isTemporaryLogEmpty();
        final boolean isAlive = gameserver.getProcess().isAlive();

        // Update temporary logger
        if (isOutStreamReady) {
            gameserver_logger.writeOutStreamToTemporaryLog(gamserver_iostream);
        }

        // Update primary logger
        if (!isTemporaryLogEmpty) {
            gameserver_logger.writeTemporaryLogToDb(port, db);
        }
        
        // Stop all Gameservers
        if (stopAll &&
            isAlive) {
            gameserver.sendStop(port);
        }

        // Remove stopped servers
        if (!isOutStreamReady &&
            isTemporaryLogEmpty &&
            !isAlive) {

            Notifier.print("Server on port " + port + " was flagged as stopped!");
            server_processes.addStoppedGameserver(port);
        }
    }
    
    /**
     * This method clear the stopped Gameservers
     * @param onlineOnRestart If the server shall start on (core) restart
     */
    private void clearStoppedGameservers(boolean onlineOnRestart) {
        
        if (server_processes.isExistingStoppedGameservers()) {
            
            for (Integer port : server_processes.getStoppedGameservers()) {
                
                final Gameserver gameserver = server_processes.getGameserver(port);
                
                if (gameserver.getIoStream().close()) {
                    Notifier.print("Closed readers for server on port: " + port);
                } else {
                    Notifier.print("Failed to close readers for server on port: " + port);
                }
                
                if (server_start.updateServerStatusAndOnCoreRestart(db, false, onlineOnRestart, port)) {
                    Notifier.print("Updated status for server on port: " + port);
                } else {
                    Notifier.print("Failed to update status for server on port: " + port);
                }
                
                server_processes.removeGameserver(port);
                Notifier.print("Server cleared on port: " + port);
                
            }
            
            server_processes.removeStoppedGameservers();
        }
    }
}

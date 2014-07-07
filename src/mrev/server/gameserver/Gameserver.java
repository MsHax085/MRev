package mrev.server.gameserver;

import mrev.Notifier;
import mrev.server.gameserver.components.Gameserver_IoStream;
import mrev.server.gameserver.components.Gameserver_Logger;
import mrev.server.gameserver.components.Gameserver_Process;

/**
 * The Gameserver class ties together the gameserver component classes.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-02, 23:01:23
 * @version 0.0.1
 */
public class Gameserver {

    // -------------------------------------------------------------------------
    
    private final Gameserver_Process process;
    private final Gameserver_Logger logger;
    private final Gameserver_IoStream stream;
    
    private long stopped_timestamp = 0;
    
    // -------------------------------------------------------------------------
    
    /**
     * This is the construction which initialize a new gameserver object.
     * @param port The server port.
     * @param jar The server jar to be used, filename only.
     * @param memory The amount of max memory to be reserved for the server.
     */
    public Gameserver(int port, String jar, int memory) {
        
        process = new Gameserver_Process();
        logger = new Gameserver_Logger();
        
        final Process p = this.process.start(port, jar, memory);
        
        stream = new Gameserver_IoStream(p);
        
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * This method returns a reference to the gameserver process handler.
     * @return Gameserver_Process The gameserver process handler.
     */
    public Gameserver_Process getProcess() {
        return process;
    }
    
    /**
     * This method returns a reference to the gameserver logger.
     * @return Gameserver_Logger The gameserver logger.
     */
    public Gameserver_Logger getLogger() {
        return logger;
    }
    
    /**
     * This method returns a reference to the gamesever I/O-stream.
     * @return Gameserver_IoStream The gameserver I/O-stream.
     */
    public Gameserver_IoStream getIoStream() {
        return stream;
    }
    
    /**
     * This method stops the server. When the server is first requested to stop
     * it's stopped by command. If the command fails to stop the server within 20 seconds the process
     * is then forced to be destroyed.
     * @param port The Gameserver port
     */
    public void sendStop(int port) {
        
        if (stopped_timestamp == 0) {
            stream.send("stop");
            stopped_timestamp = System.currentTimeMillis();
            
            Notifier.print("Server on port " + port + " was asked to stop!");
            
        } else if (System.currentTimeMillis() - stopped_timestamp > (20 * 1000)) {// 20 Seconds
            process.destroy();
            
            Notifier.print("Server on port " + port + " was forced to stop!");
        }
    }
}

package mrev.server.gameserver.components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * The Gameserver_IoStream class handle the I/O-streams for a specific gameserver.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-03, 20:35:32
 * @version 0.0.1
 */
public class Gameserver_IoStream {
    
    // -------------------------------------------------------------------------
    
    private final BufferedWriter writer;
    private final BufferedReader reader;
    
    // -------------------------------------------------------------------------

    /**
     * This is the construtor which initialize the I/O-streams for a specific gameserver.
     * @param process The specific gameserver.
     */
    public Gameserver_IoStream(Process process) {
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * This method verifies if the output stream is ready to be read. In this case,
     * if the gameserver console output is ready to be read.
     * @return boolean If the output stream is ready to be read.
     */
    public boolean isOutStreamReady() {
        
        try {
            return reader.ready();
        } catch (IOException ex) {
            return false;
        }
    }
    
    /**
     * This method reads the next line from the output stream. In this case the
     * gameserver console output is read.
     * @return String The next line from the output stream.
     */
    public String readOutStreamLine() {
        
        try {
            return reader.readLine();
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * This method send a message to the gameserver console.
     * @param row The message to be sent.
     * @return boolean If the message was successfully sent.
     */
    public boolean send(String row) {
        
        try {
                
            writer.write(row);
            writer.newLine();
            writer.flush();

        } catch (IOException ex) {
            return false;
        }
        
        return true;
    }
}

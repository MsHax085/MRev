package mrev.server.gameserver.components;

import java.util.LinkedList;

/**
 * The Gameserver_Logger class handle the temporary logging of each gameserver and
 * it's transfer to the primary database.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-03, 20:26:31
 * @version 0.0.1
 */
public class Gameserver_Logger {
    
    // -------------------------------------------------------------------------
    
    private final LinkedList<String> temporaryLog = new LinkedList<>();

    // -------------------------------------------------------------------------
    
    /**
     * This method verifies if the temporary gameserver log is empty.
     * @return boolean If the gameserver log is empty.
     */
    public boolean isTemporaryLogEmpty() {
        return temporaryLog.isEmpty();
    }
    
    /**
     * This method returns a reference to the temporary gameserver log.
     * @return LinkedList<> The gameserver log.
     */
    public LinkedList<String> getTemporaryLog() {
        return temporaryLog;
    }
    
    /**
     * This method writes data from the gameserver outputstream to the temporary log.
     * @param stream The IoStream of the gameserver.
     */
    public void writeOutStreamToTemporaryLog(Gameserver_IoStream stream) {
        
        int lines = 0;
        while (stream.isOutStreamReady()) {
            
            final String line = stream.readOutStreamLine();
            if (line != null) {
                addToTemporary(line);
            }
            
            if (lines > 19) {
                break;
            }
            
            lines++;
        }
    }
    
    /**
     * This method writes data from the temporary gameserver log to the primary database.
     * @param port The server port.
     * @param dbf A DatabaseFunctions reference.
     */
    /*public void writeTemporaryLogToDb(int port, DatabaseFunctions dbf) {
        
        if (log.isEmpty()) {
            return;
        }
        
        final LinkedList<String> list = (LinkedList) log.clone();
        int lines = 0;

        for (String line : list) {
            
            dbf.insertToServerLog(port, line);
            log.remove(line);
            
            if (lines > 19) {
                break;
            }
            
            lines++;
        }
    }*/
    
    /**
     * This method adds a row to the temporary gameserver log.
     * @param row The row to be added.
     */
    private void addToTemporary(String row) {
        
        temporaryLog.add(row);
        
        if (temporaryLog.size() > 100) {// In case of database failure, no need to save over 100 rows
            temporaryLog.poll();
        }
    }
    
    /**
     * This method removes a row from the temporary gameserver log.
     * @param row The row to be removed.
     */
    public void removeFromTemporary(String row) {
        temporaryLog.remove(row);
    }
}

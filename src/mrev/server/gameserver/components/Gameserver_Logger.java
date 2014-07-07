package mrev.server.gameserver.components;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import mrev.Notifier;
import mrev.server.database.DatabaseHandler;

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
     * @param db The database reference
     */
    public void writeTemporaryLogToDb(int port, DatabaseHandler db) {
        
        if (temporaryLog.isEmpty()) {
            return;
        }
        
        final LinkedList<String> list = (LinkedList) temporaryLog.clone();
        int lines = 0;

        for (String line : list) {
            
            insertToServerLog(port, line, db);
            temporaryLog.remove(line);
            
            if (lines > 19) {
                break;
            }
            
            lines++;
        }
    }
    
    /**
     * This method writes log data to the primary database and remove rows if the number of rows exceed 100.
     * @param port The server port.
     * @param text The data to be written.
     * @param db The database reference.
     * @return boolean If the operation was successfull.
     */
    private boolean insertToServerLog(int port, String text, DatabaseHandler db) {
        
        try {
            
            final PreparedStatement ps1 = db.getLogConnection().prepareStatement("INSERT INTO server_" + port + " (log_text) VALUES (?)");
            final PreparedStatement ps2 = db.getLogConnection().prepareStatement("SELECT id FROM server_" + port + " WHERE id = (SELECT MAX(id) - 100 FROM server_" + port + ")");
            final PreparedStatement ps3 = db.getLogConnection().prepareStatement("DELETE FROM server_" + port + " WHERE id < ?");
            
            ps1.setString(1, text);
            ps1.executeUpdate();
            ps1.close();
            
            final ResultSet rs = ps2.executeQuery();
            
            if (rs.next()) {
                
                final int row_id = rs.getInt("id");
                
                if (row_id > 0) {
                    ps3.setInt(1, row_id);
                    ps3.executeUpdate();
                    ps3.close();
                }
                
            }
            
            rs.close();
            ps2.close();
            
        } catch (SQLException ex) {
            Notifier.print("Failed to insert  \"" + text + "\" to server log on port " + port + ": " + ex.getMessage());
            return false;
        }
        return true;
    }
    
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
}

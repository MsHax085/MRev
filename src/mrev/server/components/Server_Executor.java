package mrev.server.components;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mrev.server.database.DatabaseHandler;

/**
 *
 * @author Richard Dahlgren
 * @since 2014-jul-06, 20:15:18
 * @version 0.0.1
 */
public class Server_Executor {

    public void execute() {
        
    }
    
    /**
     * This method clear the server commands
     * @param db The database reference.
     * @return If statement was successfully executed
     */
    public boolean clearCommands(DatabaseHandler db) {
        
        try {
            
            final PreparedStatement ps = db.getMainConnection().prepareStatement("TRUNCATE TABLE gameservers_exec_commands");
            
            ps.executeUpdate();
            ps.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
}
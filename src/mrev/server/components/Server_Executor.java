package mrev.server.components;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mrev.Notifier;
import mrev.server.ServerListener;
import mrev.server.database.DatabaseHandler;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Richard Dahlgren
 * @since 2014-jul-06, 20:15:18
 * @version 0.0.1
 */
public class Server_Executor {

    /**
     * This method execute all server commands.
     * @param db The database reference.
     */
    public void executeCommands(DatabaseHandler db) {
        
        try {
            
            final PreparedStatement ps = db.getMainConnection().prepareStatement("SELECT server_port, command, status FROM gameservers_exec_commands");
            final ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final int port = rs.getInt("server_port");
                final String command = rs.getString("command");
                final int status = rs.getInt("status");
                
                Notifier.print("Executing command \"" + command + "\" with status \"" + status + "\" on server with port: " + port);
                
                if (executeCommand(db, port, command, status)) {
                    removeCommand(db, port, command);
                }
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method execute a server command.
     * @param db The database reference.
     * @param port The server port.
     * @param command The server command to be executed.
     * @param status The server command status.
     * @return If command shall be removed from database, when the whole execution finished.
     */
    public boolean executeCommand(DatabaseHandler db, int port, String command, int status) {
        
        switch (command) {
            
            case "start":
            {
                return executeCommand_Start(db, port);
            }
            case "stop":
            {
                return executeCommand_Stop(db, port);
            }
            case "restart":
            {
                return executeCommand_Restart(db, port, status);
            }
            case "wipe:total":
            {
                return executeCommand_Wipe(db, port, "TOTAL");
            }
            case "wipe:world":
            {
                return executeCommand_Wipe(db, port, "WORLD");
            }
            case "wipe:plugins":
            {
                return executeCommand_Wipe(db, port, "PLUGINS");
            }
            case "wipe:logs":
            {
                return executeCommand_Wipe(db, port, "LOGS");
            }
            default:
            {
                if (ServerListener.server_processes.isExistingGameserver(port)) {
                    ServerListener.server_processes.getGameserver(port).getIoStream().send(command);
                }
                return true;// Remove command; command sent to server
            }
        }
    }
    
    /**
     * This method execute the start command.
     * @param port The server port.
     * @return If command shall be removed from database, when the whole execution finished.
     */
    private boolean executeCommand_Start(DatabaseHandler db, int port) {
        
        if (ServerListener.server_processes.isExistingGameserver(port)) {
            return true;// Remove command, server already started
        }
        
        if (ServerListener.server_processes.isExistingStoppedGameserver(port)) {
            return false;// Wait to execute command, server stopping
        }
        
        if (ServerListener.server_start.startServer(db, port)) {
            Notifier.print("Started server on port: " + port);
        } else {
            Notifier.print("Failed to start server on port: " + port);
        }
        
        return true;
    }
    
    /**
     * This method execute the stop command.
     * @param port The server port.
     * @return If command shall be removed from database, when the whole execution finished.
     */
    private boolean executeCommand_Stop(DatabaseHandler db, int port) {
        
        if (ServerListener.server_processes.isExistingGameserver(port)) {
            ServerListener.server_processes.getGameserver(port).sendStop(port);
        }
        
        return true;// Remove command, server already stopped
    }
    
    /**
     * This method execute the restart command.
     * @param port The server port.
     * @param status The server command status.
     * @return If command shall be removed from database, when the whole execution finished.
     */
    private boolean executeCommand_Restart(DatabaseHandler db, int port, int status) {
        
        final Server_Processes serverlistener = ServerListener.server_processes;
        final boolean isExistingGameserver = serverlistener.isExistingGameserver(port);
        
        if (isExistingGameserver && status == 0) {
            serverlistener.getGameserver(port).sendStop(port);
            updateCommand(db, 1, port, "restart");
            
        } else if (!isExistingGameserver &&
                   !serverlistener.isExistingStoppedGameserver(port)) {
            
            if (ServerListener.server_start.startServer(db, port)) {
                Notifier.print("Started server on port: " + port);
            } else {
                Notifier.print("Failed to start server on port: " + port);
            }
            
            return true;// Remove command, server restarted
        }
        
        return false;
    }
    
    /**
     * This method execute the wipe command.
     * @param port The server port.
     * @param type The type of wipe.
     * @return If command shall be removed from database, when the whole execution finished.
     */
    private boolean executeCommand_Wipe(DatabaseHandler db, int port, String type) {
        
        if (ServerListener.server_processes.isExistingGameserver(port)) {
            return true;// Remove command, server online -> can't wipe
        }
        
        if (ServerListener.server_processes.isExistingStoppedGameserver(port)) {
            return false;// Wait to execute command, server stopping
        }
        
        switch (type) {
            
            case "TOTAL":
            {
                wipe("servers/server_" + port);
                break;
            }
            case "WORLD":
            {
                wipe("servers/server_" + port + "/" + getServerLevelName(db, port));
                break;
            }
            case "PLUGINS":
            {
                wipe("servers/server_" + port + "/plugins");
                break;
            }
            case "LOGS":
            {
                wipe("servers/server_" + port + "/logs");
                break;
            }
            default:
            {
                break;
            }
        }
        
        return true;// Remove command, server wiped
    }
    
    /**
     * This method updates a server command.
     * @param db The database reference.
     * @param status The server command status.
     * @param port The server port.
     * @param command The server command to be removed.
     * @return If statement was successfully executed.
     */
    private boolean updateCommand(DatabaseHandler db, int status, int port, String command) {
        
        try {
            
            final PreparedStatement ps = db.getMainConnection().prepareStatement("UPDATE gameservers_exec_commands SET status = ? WHERE server_port = ? AND command = ?");
            ps.setInt(1, status);
            ps.setInt(2, port);
            ps.setString(3, command);
            
            ps.executeUpdate();
            ps.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    /**
     * This method removes a server command.
     * @param db The database reference.
     * @param port The server port.
     * @param command The server command to be removed.
     * @return If statement was successfully executed.
     */
    private boolean removeCommand(DatabaseHandler db, int port, String command) {
        
        try {
            
            final PreparedStatement ps = db.getMainConnection().prepareStatement("DELETE FROM gameservers_exec_commands WHERE server_port = ? AND command = ?");
            ps.setInt(1, port);
            ps.setString(2, command);
            
            ps.executeUpdate();
            ps.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
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
    
    /**
     * This method handle wipe of each directory
     * @param directory The directory
     */
    private void wipe(String directory) {
        
        final File dir = new File(directory);
        
        try {
            
            FileUtils.deleteDirectory(dir);
            
        } catch (IOException ex) {
            
            try {
                
                FileUtils.forceDelete(dir);
                
            } catch (IOException ex1) {
                Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex1);
            }
            
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method returns the server level name
     * @param id The server id
     * @return The server level name
     */
    private String getServerLevelName(DatabaseHandler db, int port) {
        
        try {
            
            final PreparedStatement ps = db.getMainConnection().prepareStatement("SELECT level_name FROM gameservers_settings WHERE server_port = ?");
            ps.setInt(1, port);
            
            final ResultSet rs = ps.executeQuery();
            String level_name = null;
            
            if (rs.next()) {
                level_name = rs.getString("level_name");
            }
            
            rs.close();
            ps.close();
            
            return level_name;
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Executor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "world";
    }
}
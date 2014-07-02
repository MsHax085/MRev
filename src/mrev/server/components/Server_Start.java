package mrev.server.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import mrev.server.database.DatabaseHandler;
import mrev.server.gameserver.Gameserver;
import org.apache.commons.io.FileUtils;

/**
 * The Server_Start class handle the startup of gameservers.
 *
 * @author Richard Dahlgren
 * @since 2014-jul-02, 17:03:15
 * @version 0.0.1
 */
public class Server_Start {
    
    /**
     * This method start all valid gameserver on application restart.
     * @param db The database reference.
     * @return boolean If the servers was started.
     */
    public boolean startServers(DatabaseHandler db) {
        
        try {
            
            final PreparedStatement ps1 = db.getMainConnection().prepareStatement("SELECT server_port FROM gameservers_status WHERE online_on_restart = ?");
            final PreparedStatement ps2 = db.getMainConnection().prepareStatement("SELECT * FROM gameservers_settings WHERE date_suspended IS NULL AND server_port = ? OR date_suspended >= CURDATE( ) AND server_port = ?");
            
            ps1.setBoolean(1, true);
            
            final ResultSet rs1 = ps1.executeQuery();
            
            while (rs1.next()) {
                
                final int port = rs1.getInt("server_port");
                ps2.setInt(1, port);
                ps2.setInt(2, port);
                
                final ResultSet rs2 = ps2.executeQuery();
                
                if (rs2.next()) {
                    startServer(db, rs2);
                }
                
                rs2.close();
                
            }
            
            ps2.close();
            rs1.close();
            ps1.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Server_Start.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

    /**
     * This method start one single gameserver with the function below.
     * @param db The database reference.
     * @param port The server port.
     * @return boolean If the server was started.
     * @throws SQLException 
     */
    public boolean startServer(DatabaseHandler db, int port) throws SQLException {
        
        final PreparedStatement ps = db.getMainConnection().prepareStatement("SELECT * FROM gameservers_settings WHERE server_port = ?");
        
        ps.setInt(1, port);
        
        boolean started = false;
        final ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            started = startServer(db, rs);
        }
        
        rs.close();
        ps.close();
        
        return started;
    }
    
    /**
     * This method start one single gameserver.
     * @param db The database reference.
     * @param rs The ResultSet containing the server data.
     * @return boolean If the server was started.
     * @throws SQLException 
     */
    private boolean startServer(DatabaseHandler db, ResultSet rs) throws SQLException {
       
        final int port = rs.getInt("server_port");
        final String jar = rs.getString("jar");
                
        try {
            
            setServerSettings(rs, jar, port);
            
        } catch (IOException ex) {
            Logger.getLogger(Server_Start.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        final int memory = rs.getInt("memory");
        
        createLogTable(db, port);
        
        final Gameserver sm = new Gameserver(port, jar, memory);
        
        updateServerStatusAndOnCoreRestart(db, true, false, port);
        
        return true;
    }
    
    /**
     * This method create a log-table for a specific gameserver if it doesn't exist
     * @param db The database reference
     * @param port The server port
     * @throws SQLException 
     */
    public void createLogTable(DatabaseHandler db, int port) throws SQLException {
        
        final PreparedStatement ps = db.getLogConnection().prepareStatement("CREATE TABLE IF NOT EXISTS server_" + port + " (id int(11) NOT NULL AUTO_INCREMENT, log_text text NOT NULL, PRIMARY KEY (id))");
        ps.executeUpdate();
        
    }
    
    /**
     * This method update the gameserver status and set if server shall be restarted on (this application) restart
     * @param db The database reference
     * @param online If the server is online
     * @param online_on_restart If the server shall start on restart
     * @param port The server port
     * @throws SQLException 
     */
    public void updateServerStatusAndOnCoreRestart(DatabaseHandler db, boolean online, boolean online_on_restart, int port) throws SQLException {
        
        final PreparedStatement ps = db.getMainConnection().prepareStatement("UPDATE gameservers_status SET online = ?, online_on_restart = ? WHERE server_port = ?");
        ps.setBoolean(1, online);
        ps.setBoolean(2, online_on_restart);
        ps.setInt(3, port);
        
        ps.executeUpdate();
        ps.close();
        
    }
    
    /**
     * This method set the gameserver settings upon startup.
     * @param rs The ResultSet containing the gameserver data.
     * @param jar The gameserver JAR to be used, name and file extension only.
     * @param port The gameserver port.
     * @throws IOException
     * @throws SQLException 
     */
    private void setServerSettings(ResultSet rs, String jar, int port) throws IOException, SQLException {
        
        final String dir = "servers/" + port + "/";
        
        // Create files
        createProperyFile(dir);
        createJarFile(dir, jar);
        
        // Set properties
        final Properties p = new Properties();
        
        p.setProperty("allow-flight", "" + rs.getBoolean("flight"));
        p.setProperty("allow-nether", "" + rs.getBoolean("nether"));
        p.setProperty("announce-player-achievements", "" + rs.getBoolean("announce_achievements"));
        p.setProperty("difficulty", "" + rs.getInt("difficulty"));
        p.setProperty("enable-query", "" + rs.getBoolean("query"));
        p.setProperty("enable-rcon", "false");
        p.setProperty("enable-command-block", "" + rs.getBoolean("cmd_block"));
        p.setProperty("force-gamemode", "" + rs.getBoolean("force_gmode"));
        p.setProperty("gamemode", "" + rs.getInt("gmode"));
        p.setProperty("generate-structures", "" + rs.getBoolean("gen_structures"));
        p.setProperty("generator-settings", "" + rs.getString("gen_settings"));
        p.setProperty("hardcore", "" + rs.getBoolean("hardcore"));
        p.setProperty("level-name", "" + rs.getString("level_name"));
        p.setProperty("level-seed", "" + rs.getString("level_seed"));
        p.setProperty("level-type", "" + rs.getString("level_type"));
        p.setProperty("max-build-height", "" + rs.getInt("build_height"));
        p.setProperty("max-players", "" + rs.getInt("max_players"));
        p.setProperty("motd", "" + rs.getString("motd"));
        p.setProperty("online-mode", "" + rs.getBoolean("online_mode"));
        p.setProperty("op-permission-level", "" + rs.getInt("op_perm_level"));
        p.setProperty("player-idle-timeout", "" + rs.getInt("idle_timeout"));
        p.setProperty("pvp", "" + rs.getBoolean("pvp"));
        p.setProperty("query.port", "" + port);
        p.setProperty("rcon.password", "");
        p.setProperty("rcon.port", "");
        p.setProperty("resource-pack", "" + rs.getString("res_pack"));
        p.setProperty("server-ip", "127.0.0.1");
        p.setProperty("server-name", "" + rs.getString("srv_name"));
        p.setProperty("server-port", "" + port);
        p.setProperty("snooper-enabled", "false");
        p.setProperty("spawn-animals", "" + rs.getBoolean("animals"));
        p.setProperty("spawn-monsters", "" + rs.getBoolean("monsters"));
        p.setProperty("spawn-npcs", "" + rs.getBoolean("npcs"));
        p.setProperty("spawn-protection", "" + rs.getInt("spawn_protection"));
        p.setProperty("view-distance", "" + rs.getInt("view_dist"));
        p.setProperty("white-list", "" + rs.getBoolean("whitelist"));
        
        p.store(new FileOutputStream(dir + "server.properties"), null);
    }
    
    /**
     * This method create a new properies file for the gameserver.
     * @param dir The gameserver directory.
     * @throws IOException 
     */
    private void createProperyFile(String dir) throws IOException {
        
        final File directory = new File(dir);
        final File properties = new File(dir + "server.properties");
        
        directory.mkdirs();// Create directory
        
        properties.createNewFile();// Create file if not existing
        
    }
    
    /**
     * This method copy a new JAR file to the gameserver directory.
     * @param dir The gameserver directory
     * @param jar The gameserver JAR
     * @throws IOException 
     */
    private void createJarFile(String dir, String jar) throws IOException {
        
        File source;
        
        switch (jar) {
            case "vanilla.jar":
            {
                source = new File("servers/jars/vanilla.jar");
                break;
            }
            case "bukkit.jar":
            {
                source = new File("servers/jars/bukkit.jar");
                break;
            }
            default:
            {
                return;
            }
        }
        
        final File destination = new File(dir + "mc.jar");
        FileUtils.copyFile(source, destination);
    }
}

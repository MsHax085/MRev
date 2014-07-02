package mrev.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DatabaseHandler class handle the MySQL database connections. Access to each
 * database connection is given through references and the connection is automatically
 * retried upon verification.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-02, 21:08:44
 * @version 0.0.1
 */
public class DatabaseHandler {

    // -------------------------------------------------------------------------
    
    private final String HOST = "localhost";
    private final String PORT = "3306";
    private final String MAIN_DATABASE = "revision";
    private final String LOG_DATABASE = "revision_logs";
    private final String USER = "root";
    private final String PASS = "1";
    
    private Connection main_conn = null;
    private Connection log_conn = null;
    
    // -------------------------------------------------------------------------
    
    /**
     * This method opens the MySQL connection.
     */
    public void open() {
        
        try {
            
            Class.forName("com.mysql.jdbc.Driver");
            
            System.out.println("MySQL JDBC Driver Registred!");
            
            DriverManager.setLoginTimeout(5);
            main_conn = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + MAIN_DATABASE, USER, PASS);
            log_conn = DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + LOG_DATABASE, USER, PASS);
            
            if (main_conn != null && log_conn != null) {
                System.out.println("MySQL Connection Established!");
                
            } else {
                System.out.println("Failed to Establish MySQL Connection!");
            }
            
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Failed to Establish MySQL Connection: " + ex.getMessage());
        }
    }
    
    /**
     * This method closes the MySQL connection.
     */
    public void close() {
        
        System.out.println("MySQL Connection Closing!");
        
        try {
            if (isMainConnectionOpen()) {
                main_conn.close();
            }
            
            if (isLogConnectionOpen()) {
                log_conn.close();
            }
        } catch (SQLException ex) {
            System.out.println("Failed to Close MySQL Connection: " + ex.getMessage());
        }
        
        System.out.println("MySQL Connection Closed!");
    }
    
    /**
     * This method retries to establish a MySQL connection if the previous fails.
     * @return boolean If a new connection was established.
     */
    private boolean retryConnection() {
        
        int retries = 0;
        while (!isConnectionOpen()) {
            
            System.out.println("Retrying Connection ...");
            
            close();
            open();
            
            if (retries > 4) {
                return false;
            }
            retries++;
        }
        
        return true;
    }
    
    /**
     * This method verifies the MySQL connection to the main database.
     * @return boolean If the MySQL connection is open to the main database.
     * @throws SQLException
     */
    private boolean isMainConnectionOpen() throws SQLException {
        return main_conn != null && !main_conn.isClosed();
    }
    
    /**
     * This method verifies the MySQL connection to the log (logging) database.
     * @return boolean If the MySQL connection is open to the log database.
     * @throws SQLException 
     */
    private boolean isLogConnectionOpen() throws SQLException {
        return log_conn != null && !log_conn.isClosed();
    }
    
    /**
     * This method verifies if the MySQL connection is open, tying both isMainConnectionOpen
     * and isLogConnectionOpen together in one method.
     * @return boolean If the MySQL connection is open.
     */
    private synchronized boolean isConnectionOpen() {
        try {
            return isMainConnectionOpen() && isLogConnectionOpen();
        } catch (SQLException ex) {
            System.out.println("Failed to check connection: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * This method verifies the connection. The connection is retried if not open.
     * @return boolean If the MySQL connection is open.
     */
    public boolean verifyConnection() {
        
        if (isConnectionOpen()) {
            return true;
        }
        
        return retryConnection();
    }
    
    /**
     * This method returns a reference to the MySQL connection for the main database.
     * @return Connection A reference to the MySQL connection for the main database.
     */
    public Connection getMainConnection() {
        return main_conn;
    }
    
    /**
     * This method returns a reference to the MySQL connection for the log (logging) database.
     * @return Connection A reference to the MySQL connection for the log database.
     */
    public Connection getLogConnection() {
        return log_conn;
    }
}

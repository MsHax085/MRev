package mrev.server.gameserver.components;

import java.io.File;
import java.io.IOException;

/**
 * The Gameserver_Process class handle the gameserver process.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-03, 20:47:52
 * @version 0.0.1
 */
public class Gameserver_Process {
    
    // -------------------------------------------------------------------------
    
    private Process process = null;
    private boolean destroyed = false;
    
    // -------------------------------------------------------------------------
    
    /**
     * This method verifies if the process is alive.
     * @return boolean If the process is alive.
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * This method start the gameserver process.
     * @param port The server port.
     * @param jar The server jar to be used, filename only.
     * @param memory The amount of max memory to be reserved for the server.
     * @return Process The created process of the started gameserver.
     * 
     * NOTE: All arguments to ProcessBuilder may be sent in one call?
     */
    public Process start(int port, String jar, int memory) {
        
        final String dir = "servers/server_" + port + "/";
        final File path = new File(dir);
        
        ProcessBuilder pbuilder;

        if (jar.equals("bukkit.jar")) {
            pbuilder = new ProcessBuilder("java", "-Xmx" + memory + "M", "-jar", path.getAbsolutePath() + File.separator + "mc.jar", "-o", "true", "-nojline");

        } else {
            pbuilder = new ProcessBuilder("java", "-Xmx" + memory + "M", "-jar", path.getAbsolutePath() + File.separator + "mc.jar", "nogui");
        }

        pbuilder.redirectErrorStream(true);
        pbuilder.directory(path);

        try {
            
            process = pbuilder.start();
            
        } catch (IOException ex) {
            System.out.println("Failed to start server on port " + port + ": " + ex.getMessage());
        }
        
        return process;
    }
    
    /**
     * This method destroy the gameserver process. If the process already has been
     * destroyed once it will be forced to be destroyed.
     */
    public void destroy() {
        
        if (destroyed) {
            process.destroyForcibly();
        } else {
            process.destroy();
            destroyed = true;
        }
    }
}

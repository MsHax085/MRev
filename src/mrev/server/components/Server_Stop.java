package mrev.server.components;

import java.util.Map;
import mrev.server.ServerListener;
import mrev.server.gameserver.Gameserver;
import mrev.server.gameserver.components.Gameserver_IoStream;
import mrev.server.gameserver.components.Gameserver_Logger;

/**
 * The Server_Stop class handle the stopping of gameservers.
 *
 * @author Richard Dahlgren
 * @since 2014-jul-06, 19:54:58
 * @version 0.0.1
 */
public class Server_Stop {

    public void stopAll() {
        
        int tries = 0;
        /*
        while (tries < 5 && ServerListener.server_processes.isExistingGameservers()) {
            
            for (Map.Entry<Integer, Gameserver> e : ServerListener.server_processes.getSetOfGameservers()) {
                
                final int port = e.getKey();
                final Gameserver gameserver = (Gameserver) e.getValue();
                final Gameserver_IoStream gamserver_iostream = gameserver.getIoStream();
                final Gameserver_Logger logger = gameserver.getLogger();
                
                if (gameserver.getProcess().isAlive()) {
                    gameserver.sendStop();
                } else {
                    ServerListener.server_processes.removeGameserver(port);
                }
                
                
                } else if (logger.isTemporaryLogEmpty()) {
                    
                    GameserverHandler.processes.addFinishedGameserver(port);
                }
                
                logger.writeIoStreamToLog(stream);
                
                if (GameserverHandler.dbh.verifyConnection()) {
                    logger.writeLogToDb(port, GameserverHandler.dbf);
                }
                
            }
            
            GameserverHandler.processes.removeFinishedGameservers();
            tries++;
        }*/
    }
}

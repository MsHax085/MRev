package mrev.server.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import mrev.server.gameserver.Gameserver;

/**
 * The Server_Processes class keep track of both the running and stopped gameservers. When a gameserver
 * is removed it's added to the stopped_gameservers list, waiting for it's final logs to be saved.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-11, 21:03:01
 * @version 0.0.1
 */
public class Server_Processes {
    
    // -------------------------------------------------------------------------
    
    private final HashMap<Integer, Gameserver> gameservers = new HashMap<>();
    private final ArrayList<Integer> stopped_gameservers = new ArrayList<>();
    
    // -------------------------------------------------------------------------

    /**
     * This method verifies if there are any existing/running gameservers.
     * @return boolean If there are any existing/running gameservers.
     */
    public boolean isExistingGameservers() {
        return !gameservers.isEmpty();
    }
    
    /**
     * This method verifies if there is an existing/running gameservers by a
     * given port.
     * @param port The server port to be verified.
     * @return boolean If there are any existing/running gameservers by the given port.
     */
    public boolean isExistingGameserver(int port) {
        return gameservers.containsKey(port);
    }
    
    /**
     * This method verifies if there is an existing/running gameserver by a
     * given port that has been stopped, waiting to save final logs.
     * @param port The server port to be verified.
     * @return boolean If there is an existing/running gameserver by a given port that has been stopped, waiting to save final logs.
     */
    public boolean isExistingStoppedGameserver(int port) {
        return stopped_gameservers.contains(port);
    }
    
    /**
     * This method returns a reference to the set of existing/running gameservers.
     * @return Set<Entry<Integer, Gameserver>> The set of existing/running gameservers.
     */
    public Set<Entry<Integer, Gameserver>> getSetOfGameservers() {
        return gameservers.entrySet();
    }
    
    /**
     * This method adds a new existing/running gameserver to the list.
     * @param port The server port.
     * @param gameserver The gameserver reference.
     */
    public void addGameserver(int port, Gameserver gameserver) {
        gameservers.put(port, gameserver);
    }
    
    /**
     * This method adds a stopped gameserver, which is waiting to save final logs, to the list.
     * @param port The server port.
     */
    public void addStoppedGameserver(int port) {
        stopped_gameservers.add(port);
    }
    
    /**
     * This method removes an existing/running gameserver from the list adds the gameserver to the
     * stopped_gameservers list, waiting to save it's final logs.
     * @param port The server port.
     */
    public void removeGameserver(int port) {
        gameservers.remove(port);
        stopped_gameservers.add(port);
    }
    
    /**
     * This method removes a stopped gameserver, which is waiting to save final logs, from the list.
     * @param port The server port.
     */
    public void removeStoppedGameserver(int port) {
        stopped_gameservers.remove(port);
    }
    
    /**
     * This method remove all stopped gameservers, which is waiting to save final logs, from the list.
     */
    public void removeStoppedGameservers() {
        
        if (!stopped_gameservers.isEmpty()) {
            stopped_gameservers.clear();
        }
    }
}

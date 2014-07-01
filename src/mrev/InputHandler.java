package mrev;

import java.util.Scanner;
import mrev.server.ServerListener;

/**
 * The InputHandler class is the main class which handle the fundamentals
 * of the application. Such fundamentals are command handling, startup and stopping
 * of the server listener.
 *
 * @author Richard Dahlgren
 * @since 2014-jun-29, 14:41:28
 * @version 0.0.1
 */
public class InputHandler extends ThreadClass {
    
    // -------------------------------------------------------------------------
    
    private final Scanner io;
    
    private final String NAME = "Revision";
    private final String VERSION = "0.1";
    
    private final ServerListener serverListener;
    
    // -------------------------------------------------------------------------
    
    /**
     * This is the main method which start the application.
     * @param args Unused
     */
    public static void main(String[] args) {
        final InputHandler inputHandler = new InputHandler();
    }

    /**
     * This is the constructor which initialize and start the application on startup.
     */
    public InputHandler() {
        this.io = new Scanner(System.in);
        this.serverListener = new ServerListener();
        
        super.start();
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * The InputHandler class extends ThreadClass and override the executeBefore
     * function, which is executed before the thread starts.
     * 
     * In this thread the executeBefore method post the first notifications.
     */
    @Override
    public void executeBefore() {
        
        System.out.println(NAME + " [Version: " + VERSION + "]");
        System.out.println("Waiting for commands ...");
        
    }
    
    /**
     * The InputHandler class extends ThreadClass and override the executeWhile
     * function, which is executed while the thread is running.
     * 
     * In this thread the executeWhile method handle the command input from
     * the console.
     * 
     * NOTE: Future versions may let the server listener handle several commands
     */
    @Override
    public void executeWhile() {
        
        if (!io.hasNext()) {
            return;
        }
        
        final String input = io.next().toUpperCase();
        
        switch (input) {
            
            case "START":
            {
                System.out.println("Attempting to start server listener ...");
                {
                    serverListener.startListening();
                }
                break;
            }
            
            case "STOP":
            {
                System.out.println("Attempting to stop server listener ...");
                {
                    if (serverListener.isStopped()) {
                        System.out.println("The server listener is already stopped!");
                        break;
                    }
                    
                    serverListener.stop();
                }
                break;
            }
            
            case "EXIT":
            {
                System.out.println("Attempting to exit application ...");
                {
                    if (!serverListener.isStopped()) {
                        System.out.println("The server listener must be stopped before exiting!");
                        break;
                    }
                    
                    super.stop();
                }
                break;
            }
            
            case "HELP":
            {
                System.out.println("Available commands:");
                System.out.println("START - Start servers");
                System.out.println("STOP  - Stop servers");
                System.out.println("EXIT  - Exit application");
                break;
            }
            default:
            {
                System.out.println("Could not recognize command: " + input);
                break;
            }
        }
    }
    
    /**
     * The InputHandler class extends ThreadClass and override the executeAfter
     * function, which is executed before the thread stops.
     * 
     * In this thread the executeAfter method verifies that the server listener
     * is stopping and stopping it if it's running.
     */
    @Override
    public void executeAfter() {
        
        if (!serverListener.isStopped()) {
            System.out.println("Stopping running server listener ...");
            serverListener.stop();
        }
        
        System.out.println("Application has stopped!");
    }
}

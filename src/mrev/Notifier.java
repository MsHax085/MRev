package mrev;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Richard Dahlgren
 * @since 2014-jul-07, 19:16:58
 * @version 0.0.1
 */
public class Notifier {

    // -------------------------------------------------------------------------
    
    private static final String pattern = "HH:mm:ss";
    private static final SimpleDateFormat format = new SimpleDateFormat(pattern);
    
    // -------------------------------------------------------------------------
    
    /**
     * This method print a message with the timetag
     * @param message The message to be printed
     */
    public static void print(String message) {
        
        String tag = "[" + format.format(new Date()) + "] ";
        System.out.println(tag + message);
        
    }
}

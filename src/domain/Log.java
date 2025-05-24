package domain;    

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Level;

/**
 * The Log class provides a utility for recording exception messages to a log file.
 * It uses Java's logging framework to log exceptions at the SEVERE level to a file
 * named after the static field `nombre`. The log file is appended with each new log entry.
 */
public class Log{
    public static String nombre="POOBkemon";

    /**
     * Records the provided exception information to a log file.
     * The log file is named using the static field `nombre` and
     * is appended with log entries. The method logs the exception
     * at the SEVERE level using Java's logging framework.
     *
     * @param e the exception to be recorded in the log file
     */
    public static void record(Exception e){
        try{
            Logger logger=Logger.getLogger(nombre);
            logger.setUseParentHandlers(false);
            FileHandler file=new FileHandler(nombre+".log",true);
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);
            logger.log(Level.SEVERE,e.toString(),e);
            file.close();
        }catch (Exception oe){
            oe.printStackTrace();
            System.exit(0);
        }
    }
}
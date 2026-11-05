package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Registro centralizado de excepciones en el archivo {@code <nombre>.log} (modo append).
 * El manejador de archivo se crea una sola vez; registrar un error nunca termina la aplicación.
 */
public final class Log {

    public static String nombre = "POOBkemon";

    private static Logger logger;
    private static String loggerFile;

    private Log() {
    }

    /**
     * Registra la excepción con nivel SEVERE.
     *
     * @param e excepción a registrar
     */
    public static synchronized void record(Exception e) {
        try {
            currentLogger().log(Level.SEVERE, e.toString(), e);
        } catch (IOException | SecurityException ioe) {
            // Último recurso: el registro no puede ser causa de que la aplicación falle.
            System.err.println("No fue posible escribir en el log: " + ioe.getMessage());
            e.printStackTrace();
        }
    }

    private static Logger currentLogger() throws IOException {
        if (logger == null || !nombre.equals(loggerFile)) {
            Logger created = Logger.getLogger(nombre);
            created.setUseParentHandlers(false);
            for (var handler : created.getHandlers()) {
                handler.close();
                created.removeHandler(handler);
            }
            FileHandler file = new FileHandler(nombre + ".log", true);
            file.setFormatter(new SimpleFormatter());
            created.addHandler(file);
            logger = created;
            loggerFile = nombre;
        }
        return logger;
    }
}

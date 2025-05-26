package domain;

/**
 * POOBkemonException is a custom exception class designed to handle specific errors
 * that may occur in the context of the POOBkemon application. This class extends the
 * standard Exception class and provides predefined error messages to categorize
 * common issues that might arise during the application's execution.
 *
 * The predefined error messages cover scenarios such as incomplete data inputs,
 * invalid data formats, missing trainer or Pokémon data, issues related to items,
 * attempts to perform invalid actions (e.g., using a fainted Pokémon), and errors
 * involving unrecognized Pokémon identifiers.
 */
public class POOBkemonException extends Exception {
    public static final String INCOMPLETE_DATA = "Información incompleta para inicializar el juego.";
    public static final String INVALID_FORMAT = "Formato inválido en los datos del juego.";
    public static final String MISSING_TRAINER_DATA = "Datos del entrenador faltantes.";
    public static final String MISSING_POKEMON_DATA = "Datos del Pokémon faltantes.";
    public static final String MISSING_ITEMS_DATA = "Datos de ítems faltantes.";
    public static final String LESS_INFORMACION_POKEMON  = "No se encuentra la información completa del pokemon.";
    public static final String NULL_BAGPACK = "La mochila (BagPack) no puede ser nula.";
    public static final String POKEMON_WEAK_CHANGE = "No se puede cambiar a un pokémon debilitado";
    public static final String POKEMON_ID_NOT_FOUND = "No se encontró un pokémon con ID: ";
    public POOBkemonException(String mensaje) {
        super(mensaje);
    }

}

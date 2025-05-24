package domain;

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

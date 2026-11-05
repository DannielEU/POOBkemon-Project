package domain;

/**
 * Mensajes de decisión que viajan entre la interfaz, las máquinas y {@link POOBkemon}.
 * Cada decisión es un arreglo cuyo primer elemento es la acción:
 * <ul>
 *   <li>{@code Attack, idAtaque, idPokemon, idEntrenador}</li>
 *   <li>{@code UseItem, idEntrenador, idPokemon, nombreItem}</li>
 *   <li>{@code ChangePokemon, idEntrenador, idPokemon}</li>
 *   <li>{@code Run, idEntrenador}</li>
 *   <li>{@code timeOver, idEntrenador, idPokemon}</li>
 * </ul>
 * Esta clase es el único lugar donde se conoce ese formato.
 */
public final class Decision {

    public static final String ATTACK = "Attack";
    public static final String USE_ITEM = "UseItem";
    public static final String CHANGE_POKEMON = "ChangePokemon";
    public static final String RUN = "Run";
    public static final String TIME_OVER = "timeOver";

    private Decision() {
    }

    public static String[] attack(int attackId, int pokemonId, int trainerId) {
        return new String[]{ATTACK, String.valueOf(attackId), String.valueOf(pokemonId), String.valueOf(trainerId)};
    }

    public static String[] useItem(int trainerId, int pokemonId, String itemName) {
        return new String[]{USE_ITEM, String.valueOf(trainerId), String.valueOf(pokemonId), itemName};
    }

    public static String[] changePokemon(int trainerId, int pokemonId) {
        return new String[]{CHANGE_POKEMON, String.valueOf(trainerId), String.valueOf(pokemonId)};
    }

    public static String[] run(int trainerId) {
        return new String[]{RUN, String.valueOf(trainerId)};
    }

    public static String[] timeOver(int trainerId, int pokemonId) {
        return new String[]{TIME_OVER, String.valueOf(trainerId), String.valueOf(pokemonId)};
    }

    /**
     * Verifica que la decisión tenga el número mínimo de parámetros para su acción.
     *
     * @throws POOBkemonException si la decisión es nula, vacía, desconocida o incompleta
     */
    public static void validate(String[] decision) throws POOBkemonException {
        if (decision == null || decision.length == 0) {
            throw new POOBkemonException("Se necesita introducir un movimiento valido");
        }
        int required;
        switch (decision[0]) {
            case ATTACK:
                required = 4;
                break;
            case USE_ITEM:
                required = 4;
                break;
            case CHANGE_POKEMON:
            case TIME_OVER:
                required = 3;
                break;
            case RUN:
                required = 2;
                break;
            default:
                throw new POOBkemonException("Acción no reconocida: " + decision[0]);
        }
        if (decision.length < required) {
            throw new POOBkemonException("Faltan parámetros para " + decision[0]);
        }
    }
}
